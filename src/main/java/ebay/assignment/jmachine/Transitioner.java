package ebay.assignment.jmachine;

public interface Transitioner<EventT extends JEvent, StateT extends JState<EventT, StateT>> {
    public enum JPedalAction {
        KeepMoving, Skip
    }

    JPedalAction onTransition(StateT from, EventT event, StateT to);
}
