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

/**
 * Listens for broadcast changes in the mobile app's program state, which are sent via the
 * Wearable Messaging api.
 */
public class MobileAppStateListenerService extends WearableListenerService implements MessageApi.MessageListener {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        try {
            String message = new String(messageEvent.getData(), "UTF-8");
            Log.i(Logger.TAG, String.format("Received message from mobile %s", message));

            Intent intent = new Intent(Actions.ACTION_STATE_UPDATE);
            intent.putExtra(Actions.EXTRA_PROGRAM_STATE, message);
            sendBroadcast(intent);
        }
        catch (Exception e) {
            Log.w(Logger.TAG, "Failed to encode mobile message", e);
        }
    }

}
