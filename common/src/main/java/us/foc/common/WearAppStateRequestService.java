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

package us.foc.common;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;

/**
 * Sends a message to the mobile app requesting that it change the program state, using the
 * Wearable Messaging Api
 */
public class WearAppStateRequestService extends BasePlayServicesIntentService {

    @Override protected byte[] sendMessageToNodes(Intent intent) {
        String action = intent.getAction();
        byte[] message;

        Log.i(Logger.TAG, String.format("Requesting state change on mobile app to %s", action));

        if (MessageActions.REQUEST_START_PROGRAM.equals(action)) {
            message = MessageActions.REQUEST_START_PROGRAM.getBytes();
        }
        else if (MessageActions.REQUEST_STOP_PROGRAM.equals(action)) {
            message = MessageActions.REQUEST_STOP_PROGRAM.getBytes();
        }
        else if (MessageActions.REQUEST_PROGRAM_STATE.equals(action)) {
            message = MessageActions.REQUEST_PROGRAM_STATE.getBytes();
        }
        else {
            throw new RuntimeException("Attempted to sent a program change request with null action");
        }

        return message;
    }

    @Override public void onResult(MessageApi.SendMessageResult sendMessageResult) {
        Log.i(Logger.TAG, "Sent message, success=" + sendMessageResult.getStatus().isSuccess());
    }

}
