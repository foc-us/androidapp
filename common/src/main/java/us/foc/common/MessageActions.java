package us.foc.common;

public interface MessageActions {

    /**
     * Requests that the current program should start.
     */
    String REQUEST_START_PROGRAM = "REQUEST_START_PROGRAM";

    /**
     * Requests that the current program should stop.
     */
    String REQUEST_STOP_PROGRAM = "REQUEST_STOP_PROGRAM";

    /**
     * Requests that the receiving application broadcast its program state.
     */
    String REQUEST_PROGRAM_STATE = "REQUEST_PROGRAM_STATE";

    /**
     * Broadcasts that the program mode is started.
     */
    String RESPONSE_STARTED = "RESPONSE_STARTED";

    /**
     * Broadcasts that the program mode is stopped.
     */
    String RESPONSE_STOPPED = "RESPONSE_STOPPED";

}
