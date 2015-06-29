package us.foc.transcranial.dcs.ui.dialogs;

import us.foc.transcranial.dcs.model.ProgramEntity;

public interface SaveEventListener {

    void onModeSaved(ProgramEntity.ProgramMode mode);

    void onDurationSaved(int seconds);

    void onFrequencySaved(long frequency);

    void onMinFreqSaved(long minFreq);

    void onMaxFreqSaved(long maxFreq);

    void onCurrentSaved(int current);

    void onVoltageSaved(int voltage);

    void onCurrentOffsetSaved(int currentOffset);

    void onDutyCycleSaved(long dutyCycle);

    void onShamDurationSaved(int seconds);
}
