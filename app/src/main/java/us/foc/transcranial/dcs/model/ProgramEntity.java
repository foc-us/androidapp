package us.foc.transcranial.dcs.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import us.foc.transcranial.dcs.R;

@DatabaseTable(tableName = "Program")
public class ProgramEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<String, Integer> programIdToBackgroundResourceId = new HashMap<>();

    public static final String PROGRAM_ID_GAMER = "gamer";
    public static final String PROGRAM_ID_ENDURO = "enduro";
    public static final String PROGRAM_ID_WAVE = "wave";
    public static final String PROGRAM_ID_PULSE = "pulse";
    public static final String PROGRAM_ID_NOISE = "noise";

    static {
        programIdToBackgroundResourceId.put(PROGRAM_ID_GAMER, R.drawable.program_bg);
        programIdToBackgroundResourceId.put(PROGRAM_ID_ENDURO, R.drawable.program_enduro);
        programIdToBackgroundResourceId.put(PROGRAM_ID_WAVE, R.drawable.program_wave);
        programIdToBackgroundResourceId.put(PROGRAM_ID_PULSE, R.drawable.program_pulse);
        programIdToBackgroundResourceId.put(PROGRAM_ID_NOISE, R.drawable.program_noise);
    }

    public String getProgramId() {
        return programId;
    }

    public enum ProgramMode {
        DCS(R.string.mode_dcs),
        ACS(R.string.mode_acs),
        RNS(R.string.mode_rns),
        PCS(R.string.mode_pcs);

        private final int labelResId;

        ProgramMode(int labelResId) {
            this.labelResId = labelResId;
        }

        public static ProgramMode getFromByte(byte ordinal) {
            return values()[ordinal];
        }

        public int getLabelResId() {
            return labelResId;
        }
    }

    public static class MetaData implements Serializable {
        private final String programId;
        private final String programName;
        private final String creatorName;
        private final String programDesc;

        public MetaData(String programId, String programName, String creatorName, String programDesc) {
            this.programId = programId;
            this.programName = programName;
            this.creatorName = creatorName;
            this.programDesc = programDesc;
        }
    }

//
//    private final MetaData metaData;

    @DatabaseField(id = true)
    private String programId;

    @DatabaseField
    private byte apiId;

    @DatabaseField
    private String programName;

    @DatabaseField
    private String creatorName;

    @DatabaseField
    private String programDesc;

    @DatabaseField
    private boolean valid;

    @DatabaseField
    private ProgramMode programMode;

    @DatabaseField
    private Integer duration;   // 5 - 40 mins

    @DatabaseField
    private Integer current;     // 0.1 - 2.0 mA

    @DatabaseField
    private Integer voltage;    // 10 - 60 V

    @DatabaseField
    private Boolean sham;

    @DatabaseField
    private Integer shamDuration;    // 0 - 50 mins

    @DatabaseField
    private Boolean bipolar;

    @DatabaseField
    private Integer currentOffset;   // 0.1 - 1.8 mA

    @DatabaseField
    private Long frequency;       // 0.1 - 300 Hz

    @DatabaseField
    private Long dutyCycle;       // 20 - 80 %

    @DatabaseField
    private Boolean randomCurrent;

    @DatabaseField
    private Boolean randomFrequency;

    @DatabaseField
    private Long minFrequency;   // 0.1 - 300 Hz

    @DatabaseField
    private Long maxFrequency;   // 0.1 - 300 Hz

    public static String parseProgramId(byte[] descriptor0) {

        return getString(descriptor0, 1, 9);
    }

    public void parseDescriptors(byte apiId,
                                 byte[] descriptor0,
                                 byte[] descriptor1) {

        this.apiId = apiId;
        // Decode the first Descriptor:
        // Btye 0: Valid
        // Byte 1-9 : Name
        // Byte 10 : Mode
        // Byte 11-12 : Duration
        // Byte 13 : Sham
        // Byte 14-15 : Duration (Sham)
        // Byte 16-17 : Current
        // Byte 18-19 : Current offset
        if (descriptor0.length >= 20) {
            valid = getBoolean(descriptor0, 0);
//            programId = getString(descriptor0, 1, 9);
            programMode = ProgramMode.getFromByte(descriptor0[10]);
            if (duration != null) {
                duration = getInteger(descriptor0, 11);
            }
            sham = getBoolean(descriptor0, 13);
            shamDuration = getInteger(descriptor0, 14);
            if (current != null) {
                current = getInteger(descriptor0, 16);
            }
            if (currentOffset != null) {
                currentOffset = getInteger(descriptor0, 18);
            }
        }

        // Decode the second Descriptor:
        // Btye 0: Voltage
        // Byte 1 : Bipolar
        // Byte 2-5 : Frequency / Min Frequency
        // Byte 6-9 : Max Frequency / Duty Cycle
        // Byte 11 : Random Current
        // Byte 12 : Random Current
        if (descriptor1.length >= 20) {
            voltage = getSmallInteger(descriptor1, 0);
            if (bipolar != null) {
                bipolar = getBoolean(descriptor1, 1);
            }
            if (frequency != null) {
                frequency = getLong(descriptor1, 2);
            }
            if (minFrequency != null) {
                minFrequency = getLong(descriptor1, 2);
            }
            if (maxFrequency != null) {
                maxFrequency = getLong(descriptor1, 6);
            }
            if (dutyCycle != null) {
                dutyCycle = getLong(descriptor1, 6);
            }
        }
    }

    public byte[] getData(int descriptorId) {

        byte[] data = new byte[20];
        Arrays.fill(data, (byte) 0x0);

        if (descriptorId == 0) {

            putBoolean(data, valid, 0);
            putString(data, programId, 1, 9);
            putSmallInteger(data, programMode.ordinal(), 10);
            if (duration != null) {
                putInteger(data, duration, 11);
            }
            putBoolean(data, sham, 13);
            putInteger(data, shamDuration, 14);
            if (current != null) {
                putInteger(data, current, 16);
            }
            if (currentOffset != null) {
                putInteger(data, currentOffset, 18);
            }
        }
        else {
            putSmallInteger(data, voltage, 0);
            if (bipolar != null) {
                putBoolean(data, bipolar, 1);
            }
            if (frequency != null) {
                putLong(data, frequency, 2);
            }
            if (minFrequency != null) {
                putLong(data, minFrequency, 2);
            }
            if (maxFrequency != null) {
                putLong(data, maxFrequency, 6);
            }
            if (dutyCycle != null) {
                putLong(data, dutyCycle, 6);
            }
        }
        return data;
    }


    private ProgramEntity() {
    }

    private ProgramEntity(Builder builder) {

        this.programId = builder.metaData.programId;
        this.programName = builder.metaData.programName;
        this.creatorName = builder.metaData.creatorName;
        this.programDesc = builder.metaData.programDesc;

        this.programMode = builder.programMode;
        this.current = builder.current;
        this.duration = builder.durationSeconds;
        this.voltage = builder.voltage;

        this.sham = builder.sham;
        this.shamDuration = builder.shamDuration;
        this.bipolar = builder.bipolar;
        this.randomCurrent = builder.randomCurrent;
        this.randomFrequency = builder.randomFrequency;

        this.currentOffset = builder.currentOffset;
        this.dutyCycle = builder.dutyCycle;
        this.frequency = builder.frequency;

        this.minFrequency = builder.minFrequency;
        this.maxFrequency = builder.maxFrequency;
    }

    public byte getApiId() {
        return apiId;
    }

    public String getProgramName() {
        return programName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getProgramDesc() {
        return programDesc;
    }

    public ProgramMode getProgramMode() {
        return programMode;
    }

    public Integer getCurrent() {
        return current;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getVoltage() {
        return voltage;
    }

    public Integer getBgResId() {
        return programIdToBackgroundResourceId.get(programId);
    }

    public Boolean isSham() {
        return sham;
    }

    public Integer getShamDuration() {
        return shamDuration;
    }

    public Boolean isBipolar() {
        return bipolar;
    }

    public Boolean isRandomCurrent() {
        return randomCurrent;
    }

    public Boolean isRandomFrequency() {
        return randomFrequency;
    }

    public Integer getCurrentOffset() {
        return currentOffset;
    }

    public Long getDutyCycle() {
        return dutyCycle;
    }

    public Long getFrequency() {
        return frequency;
    }

    public Long getMinFrequency() {
        return minFrequency;
    }

    public Long getMaxFrequency() {
        return maxFrequency;
    }

    public Integer getDurationSeconds() {
        return duration;
    }

    /**
     * Resets the attributes to default state if required
     *
     * @param programMode the program mode
     */
    public void updateProgramMode(ProgramMode programMode) {
        this.programMode = programMode;

        switch (programMode) {
            case DCS:
                resetToDirect();
                break;
            case ACS:
                resetToAlternating();
                break;
            case RNS:
                resetToRandom();
                break;
            case PCS:
                resetToPulse();
                break;
        }
    }

    private void resetToDirect() {
        bipolar = null;
        currentOffset = null;
        frequency = null;
        dutyCycle = null;
        randomCurrent = null;
        randomFrequency = null;
        minFrequency = null;
        maxFrequency = null;
    }

    private void resetToAlternating() {
        if (bipolar == null) {
            bipolar = false;
        }
        if (currentOffset == null) {
            currentOffset = 0;
        }
        if (frequency == null) {
            frequency = 1000L;
        }

        dutyCycle = null;
        randomCurrent = null;
        randomFrequency = null;
        minFrequency = null;
        maxFrequency = null;
    }

    private void resetToRandom() {
        if (bipolar == null) {
            bipolar = false;
        }
        if (currentOffset == null) {
            currentOffset = 0;
        }
        if (frequency == null) {
            frequency = 1000L;
        }
        if (dutyCycle == null) {
            dutyCycle = 20L;
        }
        if (randomCurrent == null) {
            randomCurrent = true;
        }
        if (randomFrequency == null) {
            randomFrequency = false;
        }

        minFrequency = null;
        maxFrequency = null;
    }

    private void resetToPulse() {
        if (bipolar == null) {
            bipolar = false;
        }
        if (currentOffset == null) {
            currentOffset = 0;
        }
        if (frequency == null) {
            frequency = 1000L;
        }
        if (dutyCycle == null) {
            dutyCycle = 20L;
        }
        randomCurrent = null;
        randomFrequency = null;
        minFrequency = null;
        maxFrequency = null;
    }


    public void setCurrent(Integer current) {
        this.current = (int) ProgramSetting.CURRENT.getValidatedValue(current);
    }

    public void setDuration(Integer duration) {
        this.duration = (int) ProgramSetting.DURATION.getValidatedValue(duration);
    }

    public void setVoltage(Integer voltage) {
        this.voltage = (int) ProgramSetting.VOLTAGE.getValidatedValue(voltage);
    }

    public void setSham(Boolean sham) {
        this.sham = sham;
    }

    public void setShamDuration(Integer duration) {
        this.shamDuration = (int) ProgramSetting.SHAM_DURATION.getValidatedValue(duration);
    }

    public void setBipolar(Boolean bipolar) {
        this.bipolar = bipolar;
    }

    public void setRandomCurrent(Boolean randomCurrent) {
        this.randomCurrent = randomCurrent;
    }

    public void setRandomFrequency(Boolean randomFrequency) {
        this.randomFrequency = randomFrequency;
    }

    public void setCurrentOffset(Integer currentOffset) {
        this.currentOffset = (int) ProgramSetting.CURRENT_OFFSET.getValidatedValue(currentOffset);
    }

    public void setDutyCycle(Long dutyCycle) {
        this.dutyCycle = ProgramSetting.DUTY_CYCLE.getValidatedValue(dutyCycle);
    }

    public void setFrequency(Long frequency) {
        this.frequency = ProgramSetting.FREQUENCY.getValidatedValue(frequency);
    }

    public void setMinFrequency(Long minFrequency) {
        this.minFrequency = ProgramSetting.MIN_FREQ.getValidatedValue(minFrequency);
    }

    public void setMaxFrequency(Long maxFrequency) {
        this.maxFrequency = ProgramSetting.MAX_FREQ.getValidatedValue(maxFrequency);
    }

    public static class Builder {

        private final MetaData metaData;
        private final ProgramMode programMode;
        private final Integer current;
        private final Integer durationSeconds;
        private final Integer voltage;

        private Boolean sham;
        private Integer shamDuration;
        private Boolean bipolar;
        private Boolean randomCurrent;
        private Boolean randomFrequency;

        private Integer currentOffset;

        private Long dutyCycle;
        private Long frequency;
        private Long minFrequency;
        private Long maxFrequency;

        public Builder(MetaData metaData, ProgramMode programMode, int current, int durationSeconds, int voltage) {
            this.metaData = metaData;
            this.programMode = programMode;
            this.current = current;
            this.durationSeconds = durationSeconds;
            this.voltage = voltage;

            this.sham = false; // default value
        }

        public Builder withSham(boolean enabled) {
            this.sham = enabled;
            return this;
        }

        public Builder withShamDuration(int value) {
            this.shamDuration = value;
            return this;
        }

        public Builder withBipolar(boolean enabled) {
            this.bipolar = enabled;
            return this;
        }

        public Builder withCurrentOffset(Integer currentOffset) {
            this.currentOffset = currentOffset;
            return this;
        }

        public Builder withFrequency(Long frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder withDutyCycle(Long cycle) {
            this.dutyCycle = cycle;
            return this;
        }

        public Builder withRandomCurrent(boolean randomCurrent) {
            this.randomCurrent = randomCurrent;
            return this;
        }

        public Builder withRandomFrequency(boolean randomFrequency) {
            this.randomFrequency = randomFrequency;
            return this;
        }

        public Builder withMinFrequency(Long minFrequency) {
            this.minFrequency = minFrequency;
            return this;
        }

        public Builder withMaxFrequency(Long maxFrequency) {
            this.maxFrequency = maxFrequency;
            return this;
        }

        public ProgramEntity build() {
            return new ProgramEntity(this);
        }
    }

    // FIXME - there must be a more concise way to do this!!!
    private static String getString(byte[] bytes, int start, int end) {

        Integer zeroIndex = null;
        byte[] id = new byte[end - start + 1];
        for (int i = start; i <= end; i++) {
            id[i - start] = bytes[i];

            if (bytes[i] == 0 && zeroIndex == null) {
                zeroIndex = i - start;
                break;
            }
        }

        if (zeroIndex == null) {
            zeroIndex = end - start + 1;
        }

        String programId = new String(id).substring(0, zeroIndex);

        return programId;
    }

    private static boolean getBoolean(byte[] bytes, int position) {
        return bytes[position] == 0x1;
    }

    private static int getSmallInteger(byte[] bytes, int position) {
        return bytes[position] & 0xff;
    }

    public static int getInteger(byte[] bytes, int start) {
        return ((bytes[start + 1] & 0xff) << 8) + (bytes[start] & 0xff);
    }

    private static long getLong(byte[] bytes, int start) {

        return ((long) (bytes[start + 3] & 0xff) << 24) +
                ((long) (bytes[start + 2] & 0xff) << 16) +
                ((long) (bytes[start + 1] & 0xff) << 8) +
                (long) (bytes[start] & 0xff);
    }

    private static void putBoolean(byte[] data, boolean value, int index) {

        data[index] = (byte) (value ? 0x01 : 0x00);
    }

    private static void putString(byte[] data, String value, int start, int end) {

        for (int i = start; i <= end; i++) {

            if (i < value.length() + start) {
                data[i] = (byte) (value.charAt(i - start));
            }
            else {
                data[i] = 0x00;
            }
        }
    }

    private static void putLong(byte[] data, long value, int start) {

        data[start + 3] = (byte) ((value >> 24) & 0xff);
        data[start + 2] = (byte) ((value >> 16) & 0xff);
        data[start + 1] = (byte) ((value >> 8) & 0xff);
        data[start] = (byte) (value & 0xff);
    }

    private static void putInteger(byte[] data, int value, int start) {

        data[start + 1] = (byte) ((value >> 8) & 0xff);
        data[start] = (byte) (value & 0xff);
    }

    private static void putSmallInteger(byte[] data, int value, int start) {

        data[start] = (byte) (value & 0xff);
    }
}
