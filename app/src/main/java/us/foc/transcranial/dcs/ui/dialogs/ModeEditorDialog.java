package us.foc.transcranial.dcs.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import butterknife.InjectView;
import butterknife.OnClick;
import us.foc.transcranial.dcs.R;
import us.foc.transcranial.dcs.model.ProgramEntity;

/**
 * Allows the user to select the program mode from a dialog
 */
public class ModeEditorDialog extends BaseDialogFragment implements CompoundButton.OnCheckedChangeListener {

    private static final String CURRENT_MODE = "CURRENT_MODE";

    public static ModeEditorDialog newInstance(ProgramEntity.ProgramMode currentMode) {
        Bundle args = new Bundle();
        args.putSerializable(CURRENT_MODE, currentMode);

        ModeEditorDialog fragment = new ModeEditorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private ProgramEntity.ProgramMode currentMode;

    @InjectView(R.id.dialog_title) TextView dialogTitle;
    @InjectView(R.id.mode_value) TextView modeValue;

    @InjectView(R.id.direct_mode) RadioButton directMode;
    @InjectView(R.id.alternating_mode) RadioButton alternatingMode;
    @InjectView(R.id.random_mode) RadioButton randomMode;
    @InjectView(R.id.pulse_mode) RadioButton pulseMode;

    @OnClick(R.id.btn_save) void onSaveClicked() {
        if (listener != null) {
            listener.onModeSaved(currentMode);
        }
        dismiss();
    }

    @OnClick(R.id.btn_discard) void onDiscardClicked() {
        dismiss();
    }

    @Override protected int getLayoutResId() {
        return R.layout.dialog_mode_editor;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentMode = (ProgramEntity.ProgramMode) getArguments().getSerializable(CURRENT_MODE);

        switch (currentMode) {
            case DCS:
                directMode.setChecked(true);
                break;
            case ACS:
                alternatingMode.setChecked(true);
                break;
            case RNS:
                randomMode.setChecked(true);
                break;
            case PCS:
                pulseMode.setChecked(true);
                break;
        }
        updateModeText();

        directMode.setOnCheckedChangeListener(this);
        alternatingMode.setOnCheckedChangeListener(this);
        randomMode.setOnCheckedChangeListener(this);
        pulseMode.setOnCheckedChangeListener(this);
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            uncheckOtherWidgets(buttonView);
        }

        if (buttonView.equals(directMode)) {
            currentMode = ProgramEntity.ProgramMode.DCS;
        }
        if (buttonView.equals(alternatingMode)) {
            currentMode = ProgramEntity.ProgramMode.ACS;
        }
        if (buttonView.equals(randomMode)) {
            currentMode = ProgramEntity.ProgramMode.RNS;
        }
        if (buttonView.equals(pulseMode)) {
            currentMode = ProgramEntity.ProgramMode.PCS;
        }
        updateModeText();
    }

    private void updateModeText() {

        String modeLabel = getString(currentMode.getLabelResId());
        modeValue.setText(modeLabel);
    }

    private void uncheckOtherWidgets(CompoundButton buttonView) {

        if (directMode.isChecked() && !buttonView.equals(directMode)) {
            directMode.setChecked(false);
        }
        if (alternatingMode.isChecked() && !buttonView.equals(alternatingMode)) {
            alternatingMode.setChecked(false);
        }
        if (randomMode.isChecked() && !buttonView.equals(randomMode)) {
            randomMode.setChecked(false);
        }
        if (pulseMode.isChecked() && !buttonView.equals(pulseMode)) {
            pulseMode.setChecked(false);
        }
    }

}
