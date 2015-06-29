package us.foc.transcranial.dcs.model;

import android.util.Log;

import java.io.Serializable;

import us.foc.transcranial.dcs.common.Logger;
import us.foc.transcranial.dcs.ui.dialogs.SeekLabelGenerator;

public enum ProgramSetting implements Serializable {

    MODE(0L, 0L, null, 0, null),
    CURRENT(100L, 2000L, 100, 1000, "%.1fmA"),
    DURATION(300L, 2400L, 1, 1, "%02.0f"),
    VOLTAGE(10L, 60L, 1, 1, "%.0fV"),
    SHAM(0L, 0L, null, 0, null),
    BIPOLAR(0L, 0L, null, 0, null),
    CURRENT_OFFSET(100L, 1800L, 100, 1000, "%.1fmA"),
    FREQUENCY(100L, 300000L, null, 1000, null),
    DUTY_CYCLE(20L, 80L, 1, 1, "%.0f%%"),
    RANDOM_CURRENT(0L, 0L, null, 0, null),
    RANDOM_FREQ(0L, 0L, null, 0, null),
    MIN_FREQ(100L, 300000L, null, 1000, null),
    MAX_FREQ(100L, 300000L, null, 1000, null),
    SHAM_DURATION(0L, 50L, 1, 1, "%.0f");

    private final long min;
    private final long max;
    private final Integer increment;
    private final int apiScalar;
    private final String displayFormat;
    private SeekLabelGenerator seekLabelGenerator;

    /**
     * @param min           the minimum allowed value
     * @param max           the maximum allowed value
     * @param increment     the increment
     * @param apiScalar     the api scaling value
     * @param displayFormat the display format
     */
    ProgramSetting(long min, long max, Integer increment, int apiScalar, String displayFormat) {
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.apiScalar = apiScalar;
        this.displayFormat = displayFormat;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public Integer getIncrement() {
        return increment;
    }

    public String getFormattedValue(long currentValue) {

        if (displayFormat != null) {
            float value = (float) currentValue / apiScalar;
            return String.format(displayFormat, value);
        }
        else {
            return getSeekLabelGenerator().labelForValue(currentValue);
        }
    }

    public long getValidatedValue(long value) {

        if (value < getMin()) {
            Log.e(Logger.TAG, "Invalid " + toString() + " value: " + String.valueOf(value));
            return getMin();
        }
        else if (value > getMax()) {
            Log.e(Logger.TAG, "Invalid " + toString() + " value: " + String.valueOf(value));
            return getMax();
        }
        else {
            return value;
        }
    }

    public Object getDisplayFormat() {
        return displayFormat;
    }

    public SeekLabelGenerator getSeekLabelGenerator() {
        if (seekLabelGenerator == null) {
            seekLabelGenerator = new SeekLabelGenerator(this);
        }
        return seekLabelGenerator;
    }
}
