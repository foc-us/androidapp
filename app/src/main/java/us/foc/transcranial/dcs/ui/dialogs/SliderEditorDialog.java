package us.foc.transcranial.dcs.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.InjectView;
import butterknife.OnClick;
import us.foc.transcranial.dcs.R;
import us.foc.transcranial.dcs.common.Logger;
import us.foc.transcranial.dcs.model.ProgramSetting;

/**
 * Provides a dialog that allows selection of a defined range of labels using a slider.
 */
public class SliderEditorDialog extends BaseDialogFragment {

    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String CURRENT_VALUE_KEY = "CURRENT_VALUE_KEY";
    private static final String SETTING_KEY = "SETTING_KEY";

    public static SliderEditorDialog newInstance(long currentValue, ProgramSetting setting, String title) {
        Bundle args = new Bundle();
        args.putLong(CURRENT_VALUE_KEY, currentValue);
        args.putSerializable(SETTING_KEY, setting);
        args.putString(TITLE_KEY, title);

        SliderEditorDialog fragment = new SliderEditorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private long currentValue;
    private ProgramSetting setting;

    @InjectView(R.id.dialog_title) TextView dialogTitle;
    @InjectView(R.id.seek_value) TextView seekValue;
    @InjectView(R.id.seek_bar_minutes) SeekBar seekBar;

    @OnClick(R.id.btn_save) void onSaveClicked() {
        if (listener != null) {

            switch (setting) {
                case FREQUENCY:
                    listener.onFrequencySaved(currentValue);
                    break;
                case MIN_FREQ:
                    listener.onMinFreqSaved(currentValue);
                    break;
                case MAX_FREQ:
                    listener.onMaxFreqSaved(currentValue);
                    break;
                case CURRENT:
                    listener.onCurrentSaved((int) currentValue);
                    break;
                case VOLTAGE:
                    listener.onVoltageSaved((int) currentValue);
                    break;
                case DUTY_CYCLE:
                    listener.onDutyCycleSaved(currentValue);
                    break;
                case CURRENT_OFFSET:
                    listener.onCurrentOffsetSaved((int) currentValue);
                    break;
                case SHAM_DURATION:
                    listener.onShamDurationSaved((int) currentValue);
                    break;
                default:
                    Log.e(Logger.TAG, "Unknown slider result type: " + setting.toString());
                    break;
            }
        }
        dismiss();
    }

    @OnClick(R.id.btn_discard) void onDiscardClicked() {
        dismiss();
    }

    @Override protected int getLayoutResId() {
        return R.layout.dialog_slider_editor;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentValue = getArguments().getLong(CURRENT_VALUE_KEY);
        setting = (ProgramSetting) getArguments().getSerializable(SETTING_KEY);
        String title = getArguments().getString(TITLE_KEY);
        dialogTitle.setText(title);

        final SeekLabelGenerator seekLabelGenerator = setting.getSeekLabelGenerator();
        seekBar.setMax(seekLabelGenerator.getSeekbarMax());

        String persistedValue = setting.getFormattedValue(currentValue);
        seekValue.setText(persistedValue);
        seekBar.setProgress(seekLabelGenerator.positionForValue(currentValue));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekValue.setText(seekLabelGenerator.getLabelForPosition(progress));
                currentValue = seekLabelGenerator.getValueForPosition(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

}
