package us.foc.transcranial.dcs;

interface Actions {

    /**
     * Requests an update on the current state of the mobile app.
     */
    String ACTION_STATE_UPDATE = "ACTION_STATE_UPDATE";

    /**
     * Key for the program state the watch is requesting that the mobile app switches to.
     */
    String EXTRA_PROGRAM_STATE = "EXTRA_PROGRAM_STATE";

}
