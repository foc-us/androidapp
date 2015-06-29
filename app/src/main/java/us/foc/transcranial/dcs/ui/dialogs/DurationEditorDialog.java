package us.foc.transcranial.dcs.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.InjectView;
import butterknife.OnClick;
import us.foc.transcranial.dcs.R;
import us.foc.transcranial.dcs.model.ProgramSetting;

/**
 * Allows the user to select a duration value using a dialog
 */
public class DurationEditorDialog extends BaseDialogFragment {

    private static final String DURATION_KEY = "DURATION_KEY";

    public static DurationEditorDialog newInstance(int seconds) {
        Bundle args = new Bundle();
        args.putInt(DURATION_KEY, seconds);

        DurationEditorDialog fragment = new DurationEditorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @InjectView(R.id.dialog_title) TextView dialogTitle;
    @InjectView(R.id.seek_value) TextView seekValue;
    @InjectView(R.id.seek_bar_minutes) SeekBar seekBarMinutes;
    @InjectView(R.id.seek_bar_seconds) SeekBar seekBarSeconds;

    @OnClick(R.id.btn_save) void onSaveClicked() {
        if (listener != null) {
            listener.onDurationSaved(currentDuration);
        }
        dismiss();
    }

    @OnClick(R.id.btn_discard) void onDiscardClicked() {
        dismiss();
    }

    private int currentDuration;

    private String minutesText = "00";
    private String secondsText = "00";

    @Override protected int getLayoutResId() {
        return R.layout.dialog_duration_editor;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentDuration = getArguments().getInt(DURATION_KEY);

        seekBarSeconds.setMax(59);

        // Need to take away the min since the slider always starts at zero.
        long minuteRange = ProgramSetting.DURATION.getMax() - ProgramSetting.DURATION.getMin();
        seekBarMinutes.setMax((int) minuteRange / 60);

        seekBarSeconds.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                secondsText = String.format("%02d", progress);
                refreshSeekValue();

                currentDuration -= (currentDuration % 60);
                currentDuration += progress;
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarMinutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int mins = progress + (int) (ProgramSetting.DURATION.getMin() / 60);
                minutesText = String.format("%02d", mins);
                refreshSeekValue();

                int seconds = mins * 60;

                if (progress == seekBarMinutes.getMax()) {
                    seekBarSeconds.setProgress(0);
                    seekBarSeconds.setEnabled(false);
                    currentDuration = seconds;
                }
                else {
                    currentDuration = seconds + (currentDuration % 60);
                    seekBarSeconds.setEnabled(true);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int mins = (int) ((currentDuration - ProgramSetting.DURATION.getMin()) / 60);
        int secs = currentDuration % 60;
        seekBarMinutes.setProgress(mins);
        seekBarSeconds.setProgress(secs);
        seekBarSeconds.setEnabled(mins < seekBarMinutes.getMax());
    }

    private void refreshSeekValue() {
        seekValue.setText(minutesText + ":" + secondsText);
    }

    public void setDialogTitle(CharSequence title) {
        dialogTitle.setText(title);
    }

}
