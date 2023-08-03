package ebay.assignment.machine;

import ebay.assignment.jmachine.JState;

sealed interface JNeoState extends JState<JTestEvent, JNeoState> {
    default boolean canHandle(JTestEvent evt) {
        return switch(evt) {
            case BotsShot -> false;
            default       -> true;
        };
    }

    @Override
    default JNeoState handle(JTestEvent evt) throws Exception {
        return switch(evt) {
            case BotsShot -> throw new Exception("I'm killed'!!!");
            case RedPill  -> new RealWorld();
            case BluePill -> new MatrixWorld();
        };
    }

    public static record RealWorld() implements JNeoState {}
    public static record MatrixWorld() implements JNeoState {}
}


