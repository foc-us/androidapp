package us.foc.transcranial.dcs.ui.dialogs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import us.foc.transcranial.dcs.model.ProgramSetting;

/**
 * Generates labels for a given range of values.
 */
public class SeekLabelGenerator implements Serializable {

    private static final LabelledRange[] freguencyRanges = new LabelledRange[]{

            new LabelledRange(100L, 900L, 100L, "%.1f"),
            new LabelledRange(1000L, 300000L, 1000L, "%.0f")
//            new LabelledRange(100L,    1000L,   100L,   "%.1f"),
//            new LabelledRange(1000L,   20000L,  1000L,  "%.0f"),
//            new LabelledRange(20000L,  50000L,  5000L,  "%.0f"),
//            new LabelledRange(50000L,  160000L, 10000L, "%.0f"),
//            new LabelledRange(160000L, 300000L, 20000L, "%.0f")
    };

    private int seekbarMax;

    private final Map<Integer, String> labelMap;
    private final Map<Integer, Long> valueMap;

    public SeekLabelGenerator(ProgramSetting setting) {
        seekbarMax = 0;
        labelMap = new HashMap<>();
        valueMap = new HashMap<>();

        if (setting.getIncrement() == null &&
                setting.getDisplayFormat() == null) {

            for (LabelledRange range : freguencyRanges) {

                for (long value = range.min;
                     value <= range.max;
                     value += range.increment, seekbarMax++) {

                    String label = String.format(range.displayFormat, (float) value / 1000);
                    labelMap.put(seekbarMax, label);
                    valueMap.put(seekbarMax, value);
                }
            }
        }
        else {

            for (long value = setting.getMin();
                 value <= setting.getMax();
                 value += setting.getIncrement(), seekbarMax++) {

                String label = setting.getFormattedValue(value);
                labelMap.put(seekbarMax, label);
                valueMap.put(seekbarMax, value);
            }
        }
    }

    /**
     * Returns the label for the seekbar position.
     *
     * @param position an integer ranging from 1 to the max seekbar value
     */
    public String getLabelForPosition(int position) {
        return labelMap.get(position);
    }

    /**
     * Returns the value for the seekbar position.
     *
     * @param position an integer ranging from 1 to the max seekbar value
     */
    public long getValueForPosition(int position) {
        return valueMap.get(position);
    }

    public String labelForValue(long value) {
        return labelMap.get(positionForValue(value));
    }

    public int positionForValue(long value) {
        for (Map.Entry<Integer, Long> obj : valueMap.entrySet()) {
            long mapValue = obj.getValue();

            if (mapValue == value) {
                return obj.getKey();
            }
        }
        return -1;
    }

    public int getSeekbarMax() {
        return seekbarMax - 1;
    }

    private static class LabelledRange {

        private final long min;
        private final long max;
        private final long increment;
        private final String displayFormat;

        public LabelledRange(long min, long max, long increment, String displayFormat) {
            this.min = min;
            this.max = max;
            this.increment = increment;
            this.displayFormat = displayFormat;
        }
    }
}
