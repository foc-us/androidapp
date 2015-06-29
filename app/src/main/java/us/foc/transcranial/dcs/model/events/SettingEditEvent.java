package us.foc.transcranial.dcs.model.events;

import us.foc.transcranial.dcs.model.ProgramSetting;

public class SettingEditEvent {

    private final ProgramSetting setting;

    public SettingEditEvent(ProgramSetting setting) {
        this.setting = setting;
    }

    public ProgramSetting getSetting() {
        return setting;
    }

}
