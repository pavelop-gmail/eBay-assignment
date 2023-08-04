package ebay.assignment.machine;

import ebay.assignment.jmachine.JMachine;
import ebay.assignment.jmachine.Transitioner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static ebay.assignment.machine.JTestEvent.BotsShot;
import static ebay.assignment.machine.JTestEvent.RedPill;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JMachineTest {

    static Transitioner<JTestEvent, JNeoState> stateChecker = (currentState, evt, nextState) -> switch (evt) {
        case BotsShot -> Transitioner.JPedalAction.Skip;
        default       -> Transitioner.JPedalAction.KeepMoving;
    };

    @org.junit.jupiter.api.Test
    @org.junit.Test
    public void checkInitState() {
        JMachine<JTestEvent, JNeoState> machine = new JMachine<>(new JNeoState.MatrixWorld(), stateChecker);
        assertEquals(machine.state(), new JNeoState.MatrixWorld());
    }

    @org.junit.jupiter.api.Test
    @org.junit.Test
    public void moveStateWithEvent() throws InterruptedException {
        JMachine<JTestEvent, JNeoState> machine = new JMachine<>(new JNeoState.MatrixWorld(), stateChecker);
        machine.put(RedPill);
        assertEquals(machine.state(), new JNeoState.RealWorld());
    }

    @org.junit.jupiter.api.Test
    @org.junit.Test
    public void skipEvent() throws InterruptedException {
        JMachine<JTestEvent, JNeoState> machine = new JMachine<>(new JNeoState.MatrixWorld(), stateChecker);
        JNeoState prevState = machine.state();
        machine.put(BotsShot);
        assertEquals(machine.state(), prevState);
    }

    @org.junit.jupiter.api.Test
    @org.junit.Test
    public void reset() throws InterruptedException, IOException {
        JMachine<JTestEvent, JNeoState> machine = new JMachine<>(new JNeoState.MatrixWorld(), stateChecker);
        machine.reset(new JNeoState.RealWorld());
        assertEquals(machine.state(), new JNeoState.RealWorld());
    }

    @org.junit.jupiter.api.Test
    @org.junit.Test
    public void persistState() throws Exception {
        File tempFile = Files.createTempFile(UUID.randomUUID().toString(), ".state").toFile();
        tempFile.deleteOnExit();
        JMachine<JTestEvent, JNeoState> persistedMachine = new JMachine<>(new JNeoState.MatrixWorld(), stateChecker, tempFile);
        persistedMachine.put(RedPill);
        assertEquals(persistedMachine.state(), new JNeoState.RealWorld());

        JMachine<JTestEvent, JNeoState> machine = new JMachine<>(new JNeoState.MatrixWorld(), stateChecker);
        machine.recoverStateFrom(tempFile);
        assertEquals(machine.state(), new JNeoState.RealWorld());
    }

}
