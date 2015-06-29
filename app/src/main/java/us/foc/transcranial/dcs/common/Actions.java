package us.foc.transcranial.dcs.common;

public interface Actions {

    String ACTION_CONNECTION_STATE_CHANGED = "us.foc.transcranial.dcs.common.bt.le.ACTION_CONNECTION_STATE_CHANGED";
    String EXTRA_CONNECTION_STATUS = "us.foc.transcranial.dcs.common.bt.le.EXTRA_CONNECTION_STATUS";
    String EXTRA_PLAY_FAILED = "us.foc.transcranial.dcs.common.bt.le.EXTRA_PLAY_FAILED";
    String EXTRA_STATUS_CODE = "us.foc.transcranial.dcs.common.bt.le.EXTRA_STATUS_CODE";

    String ACTION_BATTERY_LEVEL = "us.foc.transcranial.dcs.common.bt.le.ACTION_BATTERY_LEVEL";
    String EXTRA_BATTERY_LEVEL = "us.foc.transcranial.dcs.common.bt.le.EXTRA_BATTERY_LEVEL";

    String ACTION_ACTUAL_CURRENT_NOTIFICATION = "us.foc.transcranial.dcs.common.bt.le.ACTION_ACTUAL_CURRENT_NOTIFICATION";
    String ACTION_ACTIVE_MODE_DURATION_NOTIFICATION = "us.foc.transcranial.dcs.common.bt.le.ACTION_ACTIVE_MODE_DURATION_NOTIFICATION";
    String EXTRA_NOTIFICATION_VALUE = "us.foc.transcranial.dcs.common.bt.le.EXTRA_NOTIFICATION_VALUE";

    String ACTION_INVALID_VERSION_LEVEL = "us.foc.transcranial.dcs.common.bt.le.ACTION_INVALID_VERSION_LEVEL";
    String EXTRA_VERSION_NUMBER = "us.foc.transcranial.dcs.common.bt.le.EXTRA_VERSION_NUMBER";

    String ACTION_PROGRAM_STATE_CHANGE_REQUEST = "us.foc.transcranial.dcs.common.bt.le.ACTION_PROGRAM_STATE_CHANGE_REQUEST";
    String EXTRA_STATE_CHANGE = "us.foc.transcranial.dcs.common.bt.le.EXTRA_STATE_CHANGE";

    String ACTION_PROGRAM_ATTRIBUTES_READ = "us.foc.transcranial.dcs.common.bt.le.ACTION_PROGRAM_ATTRIBUTES_READ";
    String EXTRA_PROGRAM_NAME = "us.foc.transcranial.dcs.common.bt.le.EXTRA_PROGRAM_NAME";

    String PROGRAM_ENTITY = "PROGRAM_ENTITY";
}
