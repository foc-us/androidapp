package us.foc.transcranial.dcs.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import us.foc.transcranial.dcs.R;

public class ProgramSettingsBridge {

    private final ProgramEntity settings;

    public ProgramSettingsBridge(ProgramEntity settings) {
        this.settings = settings;
    }

    public List<SettingsEntity> constructSettingsList(Context context) {

        List<SettingsEntity> list = new ArrayList<>();

        list.add(new SettingsEntity(context.getString(R.string.settings_mode),
                                    context.getString(settings.getProgramMode().getLabelResId()),
                                    ProgramSetting.MODE));

        list.add(new SettingsEntity(context.getString(R.string.settings_current),
                                    settings.getCurrent(),
                                    ProgramSetting.CURRENT));

        int min = settings.getDurationSeconds() / 60;
        int seconds = settings.getDurationSeconds() % 60;
        String label = String.format("%02d:%02d", min, seconds);

        list.add(new SettingsEntity(context.getString(R.string.settings_time),
                                    label, ProgramSetting.DURATION));

        // conditional (only show if notnull)

        if (settings.getFrequency() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_frequency),
                                        settings.getFrequency(),
                                        ProgramSetting.FREQUENCY));
        }

        if (settings.getCurrentOffset() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_offset),
                                        settings.getCurrentOffset(),
                                        ProgramSetting.CURRENT_OFFSET));
        }

        if (settings.getDutyCycle() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_duty_cycle),
                                        settings.getDutyCycle().toString(),
                                        ProgramSetting.DUTY_CYCLE));
        }

        if (settings.isRandomFrequency() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_random_freq),
                                        stringForBool(context, settings.isRandomFrequency()),
                                        ProgramSetting.RANDOM_FREQ));
        }

        if (settings.getMinFrequency() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_min_freq),
                                        settings.getMinFrequency(), ProgramSetting.MIN_FREQ));
        }

        if (settings.isRandomCurrent() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_random_current),
                                        stringForBool(context, settings.isRandomCurrent()), ProgramSetting.RANDOM_CURRENT));
        }

        if (settings.getMaxFrequency() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_max_freq),
                                        settings.getMaxFrequency(), ProgramSetting.MAX_FREQ));
        }

        if (settings.isBipolar() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_bipolar),
                                        stringForBool(context, settings.isBipolar()), ProgramSetting.BIPOLAR));
        }

        if (settings.isSham() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_sham),
                                        stringForBool(context, settings.isSham()), ProgramSetting.SHAM));
        }

        if (settings.getShamDuration() != null) {

            list.add(new SettingsEntity(context.getString(R.string.settings_sham_period),
                                        settings.getShamDuration(), ProgramSetting.SHAM_DURATION));
        }

        if (settings.getVoltage() != null) {
            list.add(new SettingsEntity(context.getString(R.string.settings_voltage),
                                        settings.getVoltage(), ProgramSetting.VOLTAGE));
        }

        return list;
    }

    private String stringForBool(Context context, boolean polar) {
        return polar ? context.getString(R.string.boolean_on) : context.getString(R.string.boolean_off);
    }

    public static class SettingsEntity {

        private final String title;
        private final String value;
        private final ProgramSetting programSetting;

        public SettingsEntity(String title, long value, ProgramSetting programSetting) {
            this.title = title;
            this.value = programSetting.getFormattedValue(value);
            this.programSetting = programSetting;
        }

        public SettingsEntity(String title, String value, ProgramSetting programSetting) {
            this.title = title;
            this.value = value;
            this.programSetting = programSetting;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }

        public ProgramSetting getProgramSetting() {
            return programSetting;
        }
    }
}
