package ebay.assignment.jmachine;

import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class JMachine<EventT extends JEvent, StateT extends JState<EventT, StateT>> {
    private final StateT init;
    private final Transitioner<EventT, StateT> onTransition;

    private Logger logger = LoggerFactory.getLogger(JMachine.class);
    private ReentrantLock putLock = new ReentrantLock();
    private AtomicReference<StateT> current;

    private Option<File> saveTo = Option.none();

    public JMachine(StateT pInit, Transitioner<EventT, StateT> pOnTransition) {
        if (null == pInit) throw new IllegalArgumentException("Init state must be not null.");
        if (null == pOnTransition) throw new IllegalArgumentException("onTransition must be not null.");

        init = pInit;
        current = new AtomicReference<>(init);
        onTransition = pOnTransition;
    }

    public JMachine(StateT pInit, Transitioner<EventT, StateT> pOnTransition, File pSaveTo) throws IOException {
        this(pInit, pOnTransition);
        if (null == pSaveTo) throw new IllegalArgumentException("SaveTo must be not null.");
        saveTo = Option.some(pSaveTo);
        saveState(current.get());
    }

    public StateT state() {
        return current.get();
    }

    public void reset(StateT toState) throws InterruptedException {
        putLock.lockInterruptibly();
        current.set(toState);
        saveState(toState);
        putLock.unlock();
    }

    public void recoverStateFrom(File file) throws IOException, ClassNotFoundException, InterruptedException {
        if (null == file) throw new IllegalArgumentException("file must be not null.");

        ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
        StateT state = (StateT)is.readObject();
        reset(state);
    }

    public boolean put(EventT evt) throws InterruptedException {
        putLock.lockInterruptibly();

        StateT curr = current.get();

        Try<Boolean> resT = Try.of ( () -> {
                StateT next = curr.canHandle(evt) ? curr.handle(evt) : curr;
                Transitioner.JPedalAction transitionRes = onTransition.onTransition(curr, evt, next);
                return new Tuple2<>(next, transitionRes);
            }
        ).map(t -> switch(t._2) {
            case KeepMoving :
                    StateT next = t._1;
                    try {
                        saveState(next);
                        current.set(next);
                        logger.debug("going to state " + next);
                        yield true;
                    } catch (Exception e) {
                        logger.error("Failed to save state", e);
                        yield false;
                    }
            case Skip:
                    logger.debug("skipping event " + evt);
                    yield false;

            }
        ).recover ( t -> {
            logger.error("Error happened", t);
            return false;
        }) ;

        putLock.unlock();
        return resT.get();
    }

    private void saveState(StateT state) throws IOException {
        if (saveTo.isDefined()) {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(saveTo.get()));
            try {
                os.writeObject(state);
            } finally {
                os.close();
            }
        }
    }
}
