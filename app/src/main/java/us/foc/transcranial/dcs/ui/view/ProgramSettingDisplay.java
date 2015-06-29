package us.foc.transcranial.dcs.ui.view;

public interface ProgramSettingDisplay {

    /**
     * Sets the displayed title for a program setting
     *
     * @param title - string value
     */
    void setPsTitle(CharSequence title);

    /**
     * Sets the displayed value for a program setting
     *
     * @param value - string representation of a program setting
     */
    void setPsValue(CharSequence value);

    void setValueOn(boolean on);

}
