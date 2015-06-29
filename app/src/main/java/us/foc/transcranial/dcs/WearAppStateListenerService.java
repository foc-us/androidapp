/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.foc.transcranial.dcs;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import us.foc.transcranial.dcs.common.Actions;
import us.foc.transcranial.dcs.common.Logger;

/**
 * Listens for Wearable Messages which are broadcast by the wear app, interprets them, and sends a
 * broadcast which is received by the ApiService class.
 */
public class WearAppStateListenerService extends WearableListenerService implements MessageApi.MessageListener {

    @Override public void onMessageReceived(MessageEvent messageEvent) {
        try {
            String message = new String(messageEvent.getData(), "UTF-8");
            handleMessage(message);
        }
        catch (Exception e) {
            Log.w(Logger.TAG, "Failed to encode wear message", e);
        }
    }

    private void handleMessage(String message) { // TODO handle invalid case
        if (message != null) {
            Log.i(Logger.TAG, String.format("Received message from wear %s", message));

            // Send request to ApiService...
            Intent intent = new Intent(Actions.ACTION_PROGRAM_STATE_CHANGE_REQUEST);
            intent.putExtra(Actions.EXTRA_STATE_CHANGE, message);
            sendBroadcast(intent);
        }
        else {
            Log.w(Logger.TAG, "Received null wear message");
        }
    }
}
