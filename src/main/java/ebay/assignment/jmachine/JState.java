package ebay.assignment.jmachine;

import java.io.Serializable;

public interface JState<EventT extends JEvent, StateT extends JState<EventT, StateT>> extends Serializable {
    boolean canHandle(EventT evt);
    StateT handle(EventT evt) throws Exception;
}
