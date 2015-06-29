package us.foc.transcranial.dcs.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import us.foc.transcranial.dcs.R;
import us.foc.transcranial.dcs.bluetooth.ConnectionStatus;
import us.foc.transcranial.dcs.common.Actions;
import us.foc.transcranial.dcs.common.Logger;
import us.foc.transcranial.dcs.model.ProgramEntity;
import us.foc.transcranial.dcs.model.ProgramSetting;
import us.foc.transcranial.dcs.model.events.SettingEditEvent;
import us.foc.transcranial.dcs.ui.dialogs.BaseDialogFragment;
import us.foc.transcranial.dcs.ui.dialogs.DurationEditorDialog;
import us.foc.transcranial.dcs.ui.dialogs.ModeEditorDialog;
import us.foc.transcranial.dcs.ui.dialogs.SaveEventListener;
import us.foc.transcranial.dcs.ui.dialogs.SliderEditorDialog;
import us.foc.transcranial.dcs.ui.view.SettingEditorView;

/**
 * A fragment which displays all the details for a program and allows the user to edit settings.
 * Also provides play options for a program.
 */
public class ProgramFragment extends Fragment implements SettingsEditEventListener, SaveEventListener {

    private static final String STATUS_FORMAT = "%02d:%02d - %s";
    private static final String PLAY_ICON = "fa-play";
    private static final String STOP_ICON = "fa-stop";

    public static ProgramFragment newInstance(Bundle args) {
        ProgramFragment fragment = new ProgramFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ProgramEntity entity;
    private int actualCurrent;
    private int activeModeDuration;
    private int activeModeRemaining;
    private boolean playing = false;

    private UserCommandListener userCommandListener;

    @InjectView(R.id.program_name) TextView programName;
    @InjectView(R.id.program_description) TextView programDesc;
    @InjectView(R.id.program_creator) TextView programCreator;
    @InjectView(R.id.program_bg) ImageView programBg;

    @InjectView(R.id.play_button) FontAwesomeText btnPlay;
    @InjectView(R.id.repeat_button) FontAwesomeText btnRepeat;
    @InjectView(R.id.btn_bluetooth) ImageView btnBluetooth;
    @InjectView(R.id.current_duration_view) TextView currentStatus;

    @InjectView(R.id.settings_editor) SettingEditorView settingEditorView;

    @OnClick(R.id.play_button) void onPlayClicked() {

        if (!playing) {
            userCommandListener.onPlayProgram(entity);
        }
        else {
            userCommandListener.onStopProgram();
        }
        refreshPlayState();
    }

    @OnClick(R.id.repeat_button) void onRepeatClicked() {
        userCommandListener.onReplayProgram();
        currentStatus.setText(getActivity().getString(R.string.status_restarting));
    }

    @OnClick(R.id.btn_bluetooth) void onConnectClicked() {
        userCommandListener.onScanForDevices();
    }

    @OnClick(R.id.settings_button) void onSettingsClicked() {
        boolean isVisible = (settingEditorView.getVisibility() == View.VISIBLE);

        if (isVisible) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            settingEditorView.startAnimation(fadeOut);
            settingEditorView.setVisibility(View.GONE);
        }
        else {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
            settingEditorView.startAnimation(fadeIn);
            settingEditorView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshPlayState() {
        playing = !playing;
        btnPlay.setIcon(playing ? STOP_ICON : PLAY_ICON);
        btnRepeat.setEnabled(playing);
        currentStatus.setText("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entity = (ProgramEntity) getArguments().getSerializable(Actions.PROGRAM_ENTITY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_program, container, false);
        ButterKnife.inject(this, view);
        btnBluetooth.setActivated(true);

        if (entity != null) {
            programName.setText(entity.getProgramName());
            programCreator.setText(entity.getCreatorName());
            programDesc.setText(entity.getProgramDesc());

            Integer backgroundResId = entity.getBgResId();
            if (backgroundResId != null) {
                programBg.setImageResource(backgroundResId);
            }

            settingEditorView.setProgramEntity(entity);
            settingEditorView.setSettingEditEventListener(this);
        }

        Activity activity = getActivity();
        if (activity instanceof UserCommandListener) {
            userCommandListener = (UserCommandListener) activity;
        }

        if (userCommandListener == null) {
            Log.e(Logger.TAG, "Activity is not a UserCommandListerner!");
            return null;
        }

        updateConnectionState(userCommandListener.getConnectionStatus(), false, null);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateConnectionState(userCommandListener.getConnectionStatus(), false, null);

        registerReceiverForAction(gattConnectionStateChangedReceiver, Actions.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiverForAction(actualCurrentNotificationReceiver, Actions.ACTION_ACTUAL_CURRENT_NOTIFICATION);
        registerReceiverForAction(activeModeDurationNotificationReceiver, Actions.ACTION_ACTIVE_MODE_DURATION_NOTIFICATION);
        registerReceiverForAction(programAttributesReadReceiver, Actions.ACTION_PROGRAM_ATTRIBUTES_READ);
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(gattConnectionStateChangedReceiver);
        unregisterReceiver(actualCurrentNotificationReceiver);
        unregisterReceiver(activeModeDurationNotificationReceiver);
        unregisterReceiver(programAttributesReadReceiver);
    }

    private void registerReceiverForAction(BroadcastReceiver receiver, String action) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        getActivity().registerReceiver(receiver, filter);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        getActivity().unregisterReceiver(receiver);
    }

    @Override public void onSettingEditEvent(SettingEditEvent event) {
        BaseDialogFragment dialog = null;

        switch (event.getSetting()) {
            case MODE:
                dialog = ModeEditorDialog.newInstance(entity.getProgramMode());
                break;
            case CURRENT:
                dialog = SliderEditorDialog.newInstance(
                        entity.getCurrent(), ProgramSetting.CURRENT, "Current (mA)");
                break;
            case DURATION:
                dialog = DurationEditorDialog.newInstance(entity.getDurationSeconds());
                break;
            case VOLTAGE:
                dialog = SliderEditorDialog.newInstance(
                        entity.getVoltage(), ProgramSetting.VOLTAGE, "Voltage (V)");
                break;
            case SHAM:
                entity.setSham(flipBoolean(entity.isSham()));
                settingEditorView.setProgramEntity(entity);
                break;
            case SHAM_DURATION:
                dialog = SliderEditorDialog.newInstance(
                        entity.getShamDuration(), ProgramSetting.SHAM_DURATION, "Sham Duration (secs)");
                break;
            case BIPOLAR:
                entity.setBipolar(flipBoolean(entity.isBipolar()));
                settingEditorView.setProgramEntity(entity);
                break;
            case RANDOM_CURRENT:
                entity.setRandomCurrent(flipBoolean(entity.isRandomCurrent()));
                settingEditorView.setProgramEntity(entity);
                break;
            case RANDOM_FREQ:
                entity.setRandomFrequency(flipBoolean(entity.isRandomFrequency()));
                settingEditorView.setProgramEntity(entity);
                break;
            case CURRENT_OFFSET:
                dialog = SliderEditorDialog.newInstance(
                        entity.getCurrentOffset(),
                        ProgramSetting.CURRENT_OFFSET, "Offset (mA)");
                break;
            case DUTY_CYCLE:
                dialog = SliderEditorDialog.newInstance(
                        entity.getDutyCycle(),
                        ProgramSetting.DUTY_CYCLE, "Duty Cycle (%)");
                break;
            case FREQUENCY:
                dialog = SliderEditorDialog.newInstance(
                        entity.getFrequency(), ProgramSetting.FREQUENCY, "Frequency (Hz)");
                break;
            case MIN_FREQ:
                dialog = SliderEditorDialog.newInstance(
                        entity.getMinFrequency(), ProgramSetting.MIN_FREQ, "Min. Freq. (Hz)");
                break;
            case MAX_FREQ:
                dialog = SliderEditorDialog.newInstance(
                        entity.getMaxFrequency(), ProgramSetting.MAX_FREQ, "Max. Freq. (Hz)");
                break;
        }

        if (dialog != null) {
            dialog.setSaveEventListener(this);
            dialog.show(getChildFragmentManager(), null);
        }
    }

    /**
     * @param value a boolean value to be flipped
     * @return false if null, or the inverse of the current value
     */
    private Boolean flipBoolean(@Nullable Boolean value) {
        return value != null && !value;
    }

    @Override
    public void onModeSaved(ProgramEntity.ProgramMode mode) { // TODO change displayed attributes
        entity.updateProgramMode(mode);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onDurationSaved(int seconds) {
        entity.setDuration(seconds);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onFrequencySaved(long frequency) {
        entity.setFrequency(frequency);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onMinFreqSaved(long minFreq) {
        entity.setMinFrequency(minFreq);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onMaxFreqSaved(long maxFreq) {
        entity.setMaxFrequency(maxFreq);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onCurrentSaved(int current) {
        entity.setCurrent(current);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onVoltageSaved(int voltage) {
        entity.setVoltage(voltage);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onCurrentOffsetSaved(int offset) {
        entity.setCurrentOffset(offset);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onDutyCycleSaved(long dutyCycle) {
        entity.setDutyCycle(dutyCycle);
        settingEditorView.setProgramEntity(entity);
    }

    @Override public void onShamDurationSaved(int seconds) {
        entity.setShamDuration(seconds);
        settingEditorView.setProgramEntity(entity);
    }

    private final BroadcastReceiver gattConnectionStateChangedReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {

            Integer statusCode = null;
            boolean playFailed = false;
            if (intent.getExtras().containsKey(Actions.EXTRA_PLAY_FAILED)) {
                playFailed = intent.getExtras().getBoolean(Actions.EXTRA_PLAY_FAILED);
            }
            if (intent.getExtras().containsKey(Actions.EXTRA_STATUS_CODE)) {
                statusCode = intent.getExtras().getInt(Actions.EXTRA_STATUS_CODE);
            }

            ConnectionStatus status = userCommandListener.getConnectionStatus();
            updateConnectionState(status, playFailed, statusCode);
        }
    };

    private void updateConnectionState(ConnectionStatus status,
                                       boolean playFailed,
                                       Integer statusCode) {

        playing = status == ConnectionStatus.PLAYING;
        boolean connected = status == ConnectionStatus.CONNECTED;
        btnPlay.setEnabled(connected || playing);
        btnBluetooth.setEnabled(status == ConnectionStatus.DISCONNECTED);

        if (status == ConnectionStatus.CONNECTING) {
            currentStatus.setText(R.string.connecting);

            btnBluetooth.setImageResource(R.drawable.bluetooth_flasher);
            AnimationDrawable frameAnimation = (AnimationDrawable) btnBluetooth.getDrawable();
            frameAnimation.start();
        }
        else {

            Drawable btIcon = getActivity().getResources().getDrawable(connected || playing ?
                                                                               R.drawable.bluetooth_icon_blue : R.drawable.bluetooth_disconntected_icon);
            btnBluetooth.setImageDrawable(btIcon);

            if (playFailed || statusCode != null) {
                if (playFailed) {
                    currentStatus.setText(getString(R.string.play_failed));
                }
                else {
                    currentStatus.setText(getString(R.string.status_error, statusCode));
                }
                currentStatus.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentStatus.setText("");
                    }
                }, 2000);
            }
            else {
                currentStatus.setText("");
            }
        }

        if (playing) {
            btnPlay.setIcon(STOP_ICON);
        }
        else {
            btnPlay.setIcon(PLAY_ICON);
        }
    }


    private final BroadcastReceiver actualCurrentNotificationReceiver = new BroadcastReceiver() {

        @Override public void onReceive(Context context, Intent intent) {

            actualCurrent = intent.getExtras().getInt(Actions.EXTRA_NOTIFICATION_VALUE);
            updateStatus();
        }
    };

    private final BroadcastReceiver activeModeDurationNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            activeModeDuration = intent.getExtras().getInt(Actions.EXTRA_NOTIFICATION_VALUE);
            updateStatus();
        }
    };

    private final BroadcastReceiver programAttributesReadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String programName = intent.getExtras().getString(Actions.EXTRA_PROGRAM_NAME);
            String status = getString(R.string.read_program_attributes, programName);
            currentStatus.setText(status);
        }
    };

//    private final BroadcastReceiver activeModeRemainingNotificationReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            activeModeRemaining = intent.getExtras().getInt(Actions.EXTRA_NOTIFICATION_VALUE);
//            updateStatus();
//        }
//    };

    private void updateStatus() {

        // Only update the display if we are currently playing (we may
        // receive notifications just after telling the device to stop).
        if (playing) {
            String status = String.format(STATUS_FORMAT,
                                          activeModeDuration / 60,
                                          activeModeDuration % 60,
                                          ProgramSetting.CURRENT.getFormattedValue(actualCurrent));
            currentStatus.setText(status);
        }
    }
}
