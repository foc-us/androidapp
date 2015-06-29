package us.foc.transcranial.dcs;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import us.foc.common.MessageActions;
import us.foc.common.WearAppStateRequestService;

/**
 * Displays the state of the Focus device on an Android Wear device, and provides options for
 * starting or stopping the current program.
 */
public class MainWearActivity extends Activity {

    /**
     * The current state of the program on
     */
    enum ProgramState {
        STARTED,
        STOPPED,
        REQUESTING,
        NOT_CONNECTED,
        CONNECTING
    }

    private static final int VOICE_REQUEST_CODE = 9001;
    private static final int NOTIFICATION_ID = 9002;
    private static final long REQUEST_TIMEOUT_MS = 3000;

    private ProgramState programState;

    private TextView actionText;
    private TextView voiceText;

    private View.OnClickListener actionClickListener;
    private View.OnClickListener voiceClickListener;

    private Handler connectionHandler;
    private Handler requestHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionClickListener = new View.OnClickListener() { // change program state or request it if unknown
            @Override public void onClick(View view) {

                switch (programState) {
                    case STARTED:
                        stopProgram();
                        break;
                    case STOPPED:
                        startProgram();
                        break;
                    case NOT_CONNECTED:
                        setProgramState(ProgramState.CONNECTING);
                        startRequestServiceWithAction(MessageActions.REQUEST_PROGRAM_STATE);
                        break;
                    default: // ignore clicks for requesting/unknown as mobile app state is unknown
                        break;
                }
            }
        };

        voiceClickListener = new View.OnClickListener() { // launch voice recognition activity
            @Override public void onClick(View view) {
                try {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));
                    startActivityForResult(intent, VOICE_REQUEST_CODE);
                }
                catch (ActivityNotFoundException e) {
                    Log.e(Logger.TAG, "Failed to find activity for speech recognition");
                }
            }
        };

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                actionText = (TextView) stub.findViewById(R.id.action_button);
                voiceText = (TextView) stub.findViewById(R.id.voice_button);

                actionText.setOnClickListener(actionClickListener);
                voiceText.setOnClickListener(voiceClickListener);

                setProgramState(ProgramState.CONNECTING);
                startRequestServiceWithAction(MessageActions.REQUEST_PROGRAM_STATE);
            }
        });
    }

    private void setProgramState(ProgramState state) {

        if (connectionHandler != null) {
            connectionHandler.removeCallbacks(failedConnection);
            connectionHandler = null;
        }

        if (requestHandler != null) {
            requestHandler.removeCallbacks(failedRequestRunnable);
            requestHandler = null;
        }

        if (programState != state) {
            Log.d(Logger.TAG, String.format("Changing wear device state from %s to %s", programState, state));
            this.programState = state;

            switch (this.programState) {
                case STARTED:
                    actionText.setText(R.string.stop_program);
                    break;
                case STOPPED:
                    actionText.setText(R.string.start_program);
                    break;
                case REQUESTING:
                    actionText.setText(R.string.request_state);
                    break;
                case CONNECTING:
                    actionText.setText(R.string.connecting_state);
                    connectionHandler = new Handler();
                    connectionHandler.postDelayed(failedConnection, REQUEST_TIMEOUT_MS);
                    break;
                case NOT_CONNECTED:
                    actionText.setText(getString(R.string.connect_to_phone));
                    break;
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (!results.isEmpty()) {
                String spokenText = results.get(0);
                handleVoiceCommand(spokenText);
            }
        }
        else {
            Log.e(Logger.TAG, "Error launching voice recognition activity, got result code " + resultCode);
        }
    }

    private void startRequestServiceWithAction(String action) {
        Intent intent = new Intent(this, WearAppStateRequestService.class);
        intent.setAction(action);
        startService(intent);
    }

    private void startProgram() {
        if (programState == ProgramState.STOPPED) {
            startRequestServiceWithAction(MessageActions.REQUEST_START_PROGRAM);
            setProgramState(ProgramState.REQUESTING);

            requestHandler = new Handler();
            failedRequestRunnable = new FailedRequestRunnable(ProgramState.STOPPED);
            requestHandler.postDelayed(failedRequestRunnable, REQUEST_TIMEOUT_MS);
        }
    }

    private void stopProgram() {
        if (programState == ProgramState.STARTED) {
            startRequestServiceWithAction(MessageActions.REQUEST_STOP_PROGRAM);
            setProgramState(ProgramState.REQUESTING);

            requestHandler = new Handler();
            failedRequestRunnable = new FailedRequestRunnable(ProgramState.STARTED);
            requestHandler.postDelayed(failedRequestRunnable, REQUEST_TIMEOUT_MS);
        }
    }

    private void handleVoiceCommand(String spokenText) {
        if (spokenText != null) {

            if (spokenText.toLowerCase().equals(getString(R.string.start_program).toLowerCase())) {
                Log.d(Logger.TAG, "Voice command triggered program start request");
                startProgram();
            }
            else if (spokenText.toLowerCase().equals(getString(R.string.stop_program).toLowerCase())) {
                Log.d(Logger.TAG, "Voice command triggered program stop request");
                stopProgram();
            }
            else {
                Log.d(Logger.TAG, String.format("Voice command '%s' not recognised", spokenText));
            }
        }
    }

    private void postNotification() {
        Intent viewIntent = new Intent(this, MainWearActivity.class);
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.watch_bg))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.take_charge))
                        .setContentIntent(viewPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACTION_STATE_UPDATE);
        registerReceiver(programStateChangeReceiver, filter);
    }

    @Override protected void onStop() {
        super.onStop();

        if (programStateChangeReceiver != null) {
            unregisterReceiver(programStateChangeReceiver);
        }

        postNotification();
    }

    private final BroadcastReceiver programStateChangeReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, final Intent intent) {

            String programState = intent.getStringExtra(Actions.EXTRA_PROGRAM_STATE);

            if (MessageActions.RESPONSE_STARTED.equals(programState)) {
                setProgramState(ProgramState.STARTED);
            }
            else if (MessageActions.RESPONSE_STOPPED.equals(programState)) {
                setProgramState(ProgramState.STOPPED);
            }
            else { // unknown
                setProgramState(ProgramState.CONNECTING);
            }
        }
    };

    private final Runnable failedConnection = new Runnable() {
        @Override public void run() {
            Toast.makeText(MainWearActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
            setProgramState(ProgramState.NOT_CONNECTED);
        }
    };

    private FailedRequestRunnable failedRequestRunnable;

    /**
     * Cancels the request after X ms if a message has not been received from the mobile app.
     */
    private class FailedRequestRunnable implements Runnable {

        private final ProgramState fallbackState;

        public FailedRequestRunnable(ProgramState fallbackState) {
            this.fallbackState = fallbackState;
        }

        @Override public void run() {
            Toast.makeText(MainWearActivity.this, R.string.request_failed, Toast.LENGTH_LONG).show();
            setProgramState(fallbackState);
        }
    }

}
