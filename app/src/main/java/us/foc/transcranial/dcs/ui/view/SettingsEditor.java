package us.foc.transcranial.dcs.ui.view;

import us.foc.transcranial.dcs.model.ProgramEntity;
import us.foc.transcranial.dcs.ui.fragments.SettingsEditEventListener;

public interface SettingsEditor {

    /**
     * Displays the settings for a Focus program
     *
     * @param program - a valid program entity
     */
    void setProgramEntity(ProgramEntity program);

    void setSettingEditEventListener(SettingsEditEventListener listener);

}
