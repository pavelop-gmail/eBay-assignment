package ebay.assignment.jcandy;

import ebay.assignment.jmachine.JEvent;
import ebay.assignment.jmachine.JMachine;
import ebay.assignment.jmachine.JState;
import ebay.assignment.jmachine.Transitioner;

import java.io.IOException;
import java.util.List;

public class JCandyMachine {
    enum CandyEvent implements JEvent {
        RedCandy, YellowCandy
    }

    sealed interface CandyState extends JState<CandyEvent, CandyState> {
        default boolean canHandle(CandyEvent evt) {
            return true;
        }
    }
    sealed interface ErrorCandyState {}

    record InitState() implements CandyState {
        public CandyState handle(CandyEvent evt) {
            return switch(evt) {
                case RedCandy:    yield new RedState(1);
                case YellowCandy: yield new YellowState(1);
            };
        }
    }

    record RedState(int count) implements CandyState {
        public CandyState handle(CandyEvent evt) {
            return switch (evt) {
                case RedCandy:
                    if (count == 2) yield new RedError();
                    else yield new RedState(count + 1);
                case YellowCandy: yield new YellowState(1);
            };
        }
    }

    record YellowState(int count) implements CandyState {
        public CandyState handle(CandyEvent evt) {
            return switch (evt) {
                case YellowCandy:
                    if (count == 2) yield new YellowError();
                    else yield new YellowState(count + 1);
                case RedCandy: yield new RedState(1);
            };
        }
    }

    record RedError() implements CandyState, ErrorCandyState {
        public CandyState handle(CandyEvent evt) {
            return switch (evt) {
                case RedCandy: yield new RedError();
                case YellowCandy: yield new YellowState(1);
            };
        }
    }

    record YellowError() implements CandyState, ErrorCandyState {
        public CandyState handle(CandyEvent evt) {
            return switch (evt) {
                case YellowCandy: yield new YellowError();
                case RedCandy: yield new RedState(1);
            };
        }
    }

    static Transitioner<CandyEvent, CandyState> stateChecker = (currentState, evt, nextState) -> {
        if (currentState instanceof RedState && nextState instanceof RedError) {
            System.out.println(evt.toString() + " => Alert!!!. Red Error");
        } else
        if (currentState instanceof YellowState && nextState instanceof YellowError) {
            System.out.println(evt.toString() + " => Alert!!!. Yellow Error");
        } else {
            System.out.println(evt.toString() + " => " + nextState.toString());
        }
        return Transitioner.JPedalAction.KeepMoving;
    };

    public static void main(String[] args) throws InterruptedException, IOException {
        JMachine<CandyEvent, CandyState> machine = new JMachine<>(new InitState(), stateChecker);

        System.out.println("Good run:");
        List<CandyEvent> goodEvents = List.of(CandyEvent.RedCandy, CandyEvent.YellowCandy,
                CandyEvent.RedCandy, CandyEvent.YellowCandy,
                CandyEvent.RedCandy, CandyEvent.YellowCandy);

        for (CandyEvent evt: goodEvents) {
            machine.put(evt);
        }
        System.out.println("Completed with state: " + machine.state());
        System.out.println();

        machine.reset(new InitState());

        System.out.println("Almost good run:");
        List<CandyEvent> almostGoodEvents = List.of(CandyEvent.RedCandy, CandyEvent.YellowCandy, CandyEvent.YellowCandy,
                                          CandyEvent.RedCandy, CandyEvent.YellowCandy,
                                          CandyEvent.RedCandy, CandyEvent.RedCandy, CandyEvent.YellowCandy);

        for (CandyEvent evt: almostGoodEvents) {
            machine.put(evt);
        }
        System.out.println("Completed with state: " + machine.state());
        System.out.println();

        machine.reset(new InitState());

        System.out.println("Bad run:");
        List<CandyEvent> badEvents = List.of(CandyEvent.RedCandy,
                                             CandyEvent.YellowCandy, CandyEvent.YellowCandy, CandyEvent.YellowCandy, CandyEvent.YellowCandy,
                                             CandyEvent.RedCandy, CandyEvent.RedCandy, CandyEvent.RedCandy, CandyEvent.RedCandy);

        for (CandyEvent evt: badEvents) {
            machine.put(evt);
        }
        System.out.println("Completed with state: " + machine.state());
        System.out.println();
    }


}
