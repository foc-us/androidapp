package us.foc.common;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/**
 * Abstract class which handles the boilerplate of connecting to Google Play services. Messages are
 * sent as byte[] using the Wearable Messaging Api, and transmitted to all connected nodes.
 */
public abstract class BasePlayServicesIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<MessageApi.SendMessageResult> {

    private static final long CONNECTION_TIME_OUT_MS = 500;
    private GoogleApiClient googleApiClient;

    private static final String MESSAGE_PATH = "/program_request";

    public BasePlayServicesIntentService() {
        super(BasePlayServicesIntentService.class.getSimpleName());
    }

    @Override public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        if (googleApiClient.isConnected()) {
            transmitMessage(intent);
        }
        else {
            Log.e(Logger.TAG, "Wear disconnected from Google Play Services!");
        }
        googleApiClient.disconnect();
    }

    protected abstract byte[] sendMessageToNodes(Intent intent);

    private void transmitMessage(Intent intent) {
        byte[] message = sendMessageToNodes(intent);
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        for (Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), MESSAGE_PATH,
                    message).setResultCallback(this);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(Logger.TAG, "Wear connected to Google Play Services!");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(Logger.TAG, "Wear connected to Google Play Services suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(Logger.TAG, "Wear connection to Google Play Services failed!");
    }

}