package us.foc.transcranial.dcs.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

import us.foc.common.MessageActions;
import us.foc.common.MobileAppStateResponseService;
import us.foc.transcranial.dcs.common.Actions;
import us.foc.transcranial.dcs.common.AppPreferences;
import us.foc.transcranial.dcs.common.Logger;
import us.foc.transcranial.dcs.db.ProgramEntityDao;
import us.foc.transcranial.dcs.model.ProgramEntity;

public class ApiService extends Service {

    // Minimum allowed device version number:
    private static final float MINIMUM_DEVICE_VERSION_NUMBER = 1.8f;
    private static final float BLE_CHECK_ALL_CLEAR_VERSION_NUMBER = 1.9f;
    private static final int CONNECTION_TIMEOUT = 3000;  // 3 seconds

    // Standard BT Battery UUIDs:
//    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
//    private static final UUID BATTERY_LEVEL_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    // Standard Device Info UUIDs:
    private static final UUID DEVICE_INFO_SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    private static final UUID DEVICE_INFO_FIRMWARE_VERSION_UUID = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");

    // tDCS UUIDs:
    public static final UUID TDCS_SERVICE_UUID = UUID.fromString("0000AAB0-F845-40FA-995D-658A43FEEA4C");
    private static final UUID TDCS_CONTROL_UUID = UUID.fromString("0000AAB1-F845-40FA-995D-658A43FEEA4C");
    private static final UUID TDCS_CONTROL_RESPONSE_UUID = UUID.fromString("0000AAB2-F845-40FA-995D-658A43FEEA4C");
    private static final UUID TDCS_DATA_BUFFER_UUID = UUID.fromString("0000AAB3-F845-40FA-995D-658A43FEEA4C");
    private static final UUID TDCS_ACTUAL_CURRENT_UUID = UUID.fromString("0000AAB4-F845-40FA-995D-658A43FEEA4C");
    private static final UUID TDCS_ACTIVE_MODE_DURATION_UUID = UUID.fromString("0000AAB5-F845-40FA-995D-658A43FEEA4C");
    private static final UUID TDCS_ACTIVE_MODE_REMAINING_TIME_UUID = UUID.fromString("0000AAB6-F845-40FA-995D-658A43FEEA4C");

    private static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Command and Sub-Command Ids:s
    private static final byte COMMAND_ID_MANAGE_PROGRAMS = 0x02;
    //    private static final byte SUB_COMMAND_ID_NOT_SET = -1;
    private static final byte SUB_COMMAND_ID_MAX_PROGS = 0x00;
    //    private static final byte SUB_COMMAND_ID_VALID_PROGS = 0x01;
    private static final byte SUB_COMMAND_ID_PROG_STATUS = 0x02;
    private static final byte SUB_COMMAND_ID_READ_PROG = 0x03;
    private static final byte SUB_COMMAND_ID_WRITE_PROG = 0x04;
    private static final byte SUB_COMMAND_ID_ENABLE_PROG = 0x05;
    //    private static final byte SUB_COMMAND_ID_DISABLE_PROG = 0x06;
    private static final byte SUB_COMMAND_ID_START_PROG = 0x07;
    private static final byte SUB_COMMAND_ID_STOP_PROG = 0x08;

    //    Status:
//    0 ­ success;
//    1 ­ failure;
//    2 ­ command ID is not supported.
    private static final byte COMMAND_RESPONSE_SUCCESS = 0;
//    private static final byte COMMAND_RESPONSE_FAILURE = 1;
//    private static final byte COMMAND_RESPONSE_COMMAND_ID_NOT_SUPPORTTED = 2;

    private BluetoothGatt mBluetoothGatt;

    private ConnectionStatus connectionState = ConnectionStatus.DISCONNECTED;

    private Byte currentSubCommandId;
    private Byte currentProgramId;
    private Byte currentDescriptorId;
    private Byte maxPrograms;
    private boolean restarting;
    private boolean stopped;
    private boolean started;

    private int lastActualCurrentReading = 0;

    private byte[] descriptor0;

    private ProgramEntityDao programEntityDao;

    private final IBinder binder = new ApiServiceBinder();
    private ProgramEntity currentProgramEntity;

    private Handler handler;

    // Declare a binder which provides access to the service class for its clients
    public class ApiServiceBinder extends Binder {

        public ApiService getService() {
            return ApiService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        disconnect();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiverForAction(programStateChangeRequestReceiver, Actions.ACTION_PROGRAM_STATE_CHANGE_REQUEST);

        handler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(programStateChangeRequestReceiver);
    }

    private void registerReceiverForAction(BroadcastReceiver receiver, String action) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        registerReceiver(receiver, filter);
    }

    public ConnectionStatus getConnectionState() {
        return connectionState;
    }

    /**
     * BT Connection functions:
     */

    /**
     * On connection we shall:
     * Connect Bluetooth
     * -> Discover Services.
     * -> Get firmware version (must be >= "1.8")
     * -> Get Max number of programs (not all are valid)
     * -> For each program:
     * -> Get its status...
     * -> If valid:
     * -> Read attribute descriptor 1
     * -> Read attribute descriptor 2
     * -> When the attributes for the last program have been read, tell the UI we are connected...
     *
     * @param device a valid bluetooth device
     */

//    @Override
    public void connect(BluetoothDevice device) {

        connectionState = ConnectionStatus.CONNECTING;
        broadcastConnectionNotification();

        programEntityDao = new ProgramEntityDao(this);

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothGatt.connect();
    }

    /**
     * Reconnect to the given device....
     *
     * @return - connected
     */
    public boolean reconnection() {

        String connectedDeviceAddress = AppPreferences.getPairedDeviceAddress(this);

        if (TextUtils.isEmpty(connectedDeviceAddress)) {
            return false;
        }

        BluetoothAPIManager manager = new BluetoothAPIManager(this);

        if (!BluetoothAdapter.checkBluetoothAddress(connectedDeviceAddress)) {
            return false;
        }

        BluetoothAdapter adapter = manager.getBluetoothAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(connectedDeviceAddress);

        if (device == null) {
            return false;
        }
        connect(device);

        return true;
    }

//    @Override
    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * Service API functions:
     */
//    @Override
//    public void readBatteryLevel() throws InvalidCharacteristic {
//        readCharacteristic(BATTERY_SERVICE_UUID, BATTERY_LEVEL_UUID);
//    }

    /**
     * Read the data for all the programs.  This is initiated by reading the maximum
     * number of programs.
     *
     * @throws InvalidCharacteristic
     */
    public void readAllPrograms() throws InvalidCharacteristic {

        currentSubCommandId = SUB_COMMAND_ID_MAX_PROGS;
        writeCommand(currentSubCommandId, null, null);
    }

    /**
     * Read the device's Firmware Version number.
     *
     * @throws InvalidCharacteristic
     */
    private void readDeviceVersion() throws InvalidCharacteristic {
        readCharacteristic(DEVICE_INFO_SERVICE_UUID, DEVICE_INFO_FIRMWARE_VERSION_UUID);
    }

    public void playProgram(String programId) throws InvalidCharacteristic, SQLException {

        currentProgramEntity = programEntityDao.get(programId);

        playCurrentProgram();
    }

    public void playCurrentProgram() throws InvalidCharacteristic, SQLException {

        if (currentProgramEntity != null) {

            stopped = false;

            currentProgramId = currentProgramEntity.getApiId();

            // Initiate the writing of the two descriptors by writing the
            // first into the devices data buffer...
            writeDataBuffer((byte) 0);    // descriptor zero
        }
        else {
            Log.e(Logger.TAG, "Current program entity not set.");
        }
    }

    public void stopProgram() throws SQLException, InvalidCharacteristic {
        restarting = false;
        sendStopProgramCommand();
    }

    public void replayProgram() throws InvalidCharacteristic, SQLException {

        restarting = true;
        sendStopProgramCommand();
    }

    /**
     * Device API commands:
     */
    private void sendStopProgramCommand() throws SQLException, InvalidCharacteristic {

        if (currentProgramEntity != null) {
            currentProgramId = currentProgramEntity.getApiId();

            stopped = true;

            // Issue the stop command!
            writeCommand(SUB_COMMAND_ID_STOP_PROG, currentProgramId, null);
        }
        else {
            Log.e(Logger.TAG, "Stopped called when no current program entry set");
        }
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt,
                                                    int status,
                                                    int newState) {

                    if (newState == BluetoothProfile.STATE_CONNECTED) {

                        mBluetoothGatt.discoverServices();

                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        connectionState = ConnectionStatus.DISCONNECTED;
                        Log.i(Logger.TAG, "Disconnected from GATT server.");
                        broadcastConnectionNotification();
                    }
                    else {
                        Log.e(Logger.TAG, "Unknown connection state.");
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    try {

                        // Check if the device has a compatible version of firmware...
                        readDeviceVersion();

                    }
                    catch (InvalidCharacteristic invalidCharacteristic) {


                        abortConnection(invalidCharacteristic);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {

//                        if (BATTERY_LEVEL_UUID.equals(characteristic.getUuid())) {
//
//                            int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//                            broadcastBatteryLevel(batteryLevel);
//
//                        }
//                        else
                        if (TDCS_CONTROL_RESPONSE_UUID.equals(characteristic.getUuid())) {

                            byte[] commandData = characteristic.getValue();

                            boolean valid = false;
                            Byte programId = null;
                            Byte descriptor = null;

                            if (commandData.length > 1) {
                                if (commandData[1] == COMMAND_RESPONSE_SUCCESS) {

                                    // Deal with Manage Program read responses.
                                    if (commandData[0] == COMMAND_ID_MANAGE_PROGRAMS) {

                                        if (commandData.length > 2) {

                                            valid = true;
                                            switch (currentSubCommandId) {

                                                case SUB_COMMAND_ID_MAX_PROGS:

                                                    // Store the number of programs we need to read
                                                    // attributes for...
                                                    maxPrograms = commandData[2];

                                                    // Now read the status of the first program...
                                                    currentSubCommandId = SUB_COMMAND_ID_PROG_STATUS;
                                                    currentProgramId = 0;
                                                    programId = currentProgramId;
                                                    break;
//                                            case SUB_COMMAND_ID_VALID_PROGS:
//                                                byte validPrograms = commandData[2];
//                                                currentSubCommandId = null;
//                                                break;

                                                case SUB_COMMAND_ID_PROG_STATUS:
                                                    byte programStatus = commandData[2];

                                                    // If a valid program, read it's first descriptor...
                                                    if (programStatus == 0x01) {

                                                        currentSubCommandId = SUB_COMMAND_ID_READ_PROG;
                                                        currentDescriptorId = 0;
                                                        descriptor = 0;

                                                    }
                                                    else { // Otherwise ignore it and move on...

                                                        // Move to the next program...
                                                        currentProgramId++;

                                                        // if that was that last one, end the read process...
                                                        if (currentProgramId >= maxPrograms) {
                                                            currentSubCommandId = null;

                                                            setState(ConnectionStatus.CONNECTED);
                                                        }
                                                    }
                                                    programId = currentProgramId;
                                                    break;

                                                default:
                                                    valid = false;
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }

                            // If we have a follow-on command to send, send it...
                            if (valid && currentSubCommandId != null) {
                                try {
                                    writeCommand(currentSubCommandId, programId, descriptor);
                                }
                                catch (InvalidCharacteristic invalidCharacteristic) {
                                    abortConnection(invalidCharacteristic);
                                    valid = false;
                                }
                            }

                            if (!valid) {
                                currentSubCommandId = null;
                                Log.e(Logger.TAG, "Invalid command response: "
                                        + characteristic.getUuid().toString() + Arrays.toString(commandData));
                            }
                        }
                        // We received a data buffer for one of the two descriptors?
                        else if (TDCS_DATA_BUFFER_UUID.equals(characteristic.getUuid())) {

                            // If the already have the first descriptor and this is the
                            // second, we can process the attributes for the program...
                            if (currentDescriptorId == 1) {

                                if (descriptor0 != null) {

                                    String programId = ProgramEntity.parseProgramId(descriptor0);

                                    if (!TextUtils.isEmpty(programId)) {

                                        try {
                                            ProgramEntity programEntity = programEntityDao.get(programId);

                                            if (programEntity != null) {
                                                programEntity.parseDescriptors(
                                                        currentProgramId,
                                                        descriptor0,
                                                        characteristic.getValue());

                                                programEntityDao.update(programEntity);

                                                // Tell the UI we've read the attributes for ths program...
                                                broadcastProgramAttributesRead(programEntity.getProgramName());

                                            }
                                            else {
                                                Log.e(Logger.TAG, "Unable to find program: " + programId);
                                            }
                                        }
                                        catch (SQLException e) {
                                            Log.e(Logger.TAG, "Unable to parse descriptors");
                                        }
                                    }
                                }

                                currentProgramId++;
                                if (currentProgramId < maxPrograms) {
                                    currentSubCommandId = SUB_COMMAND_ID_PROG_STATUS;
                                }
                                else {
                                    currentSubCommandId = null;

                                    connectionState = ConnectionStatus.CONNECTED;
                                    broadcastConnectionNotification();
                                }
                            }
                            else {
                                descriptor0 = characteristic.getValue();
                                currentDescriptorId++;
                            }

                            if (currentSubCommandId != null) {
                                try {
                                    writeCommand(currentSubCommandId, currentProgramId, currentDescriptorId);
                                }
                                catch (InvalidCharacteristic invalidCharacteristic) {
                                    currentSubCommandId = null;
                                    abortConnection(invalidCharacteristic);
                                }
                            }
                        }
                        // We received a data buffer for one of the two descriptors?
                        else if (DEVICE_INFO_FIRMWARE_VERSION_UUID.equals(characteristic.getUuid())) {

                            String version = characteristic.getStringValue(0);

                            if (version.contains("/")) {
                                version = version.split(Pattern.quote("/"))[0];
                            }

                            float versionNumeric = Float.valueOf(version);

                            // Firmware needs to be at least version 1.8, however if we are running
                            // Android 5.0 there is a BLE issue which means we need firmware 1.9.
                            if (versionNumeric < MINIMUM_DEVICE_VERSION_NUMBER ||
                                    (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP &&
                                            versionNumeric < BLE_CHECK_ALL_CLEAR_VERSION_NUMBER)) {
                                broadcastInvalidVersionNumber(versionNumeric);
                                setState(ConnectionStatus.DISCONNECTED);
                            }
                            else {
                                try {
                                    readAllPrograms();
                                }
                                catch (InvalidCharacteristic invalidCharacteristic) {
                                    abortConnection(invalidCharacteristic);
                                }
                            }
                            Log.e(Logger.TAG, "Version: " + version);
                        }
                    }
                    else {
                        Log.e(Logger.TAG, "Invalid read characteristic response:" + String.valueOf(status));
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        if (TDCS_CONTROL_UUID.equals(characteristic.getUuid())) {

                            byte[] commandData = characteristic.getValue();

                            boolean valid = false;
                            if (commandData.length > 1) {

                                if (commandData[0] == COMMAND_ID_MANAGE_PROGRAMS &&
                                        commandData[1] == currentSubCommandId) {
                                    try {
                                        if (currentSubCommandId == SUB_COMMAND_ID_READ_PROG) {

                                            readCharacteristic(TDCS_SERVICE_UUID, TDCS_DATA_BUFFER_UUID);

                                        }
                                        else if (currentSubCommandId == SUB_COMMAND_ID_WRITE_PROG) {

                                            if (currentDescriptorId == 0) {
                                                writeDataBuffer((byte) 1);
                                            }
                                            else {
                                                // Finally, enable the program with the new attributes..
                                                writeCommand(SUB_COMMAND_ID_ENABLE_PROG, currentProgramId, null);
                                            }
                                        }
                                        else if (currentSubCommandId == SUB_COMMAND_ID_ENABLE_PROG) {

//                                            broadcastUpdate(Actions.ACTION_GATT_UPDATED);

                                            setPlayModeNotifications(true);

                                        }
                                        else if (currentSubCommandId == SUB_COMMAND_ID_START_PROG) {

                                            Log.e(Logger.TAG, "Program started?");

                                        }
                                        else if (currentSubCommandId == SUB_COMMAND_ID_STOP_PROG) {

                                            // If retarting, re-issue the start command...
                                            if (restarting) {
                                                setPlayModeNotifications(true);
                                            }
                                            else {
                                                // Otherwise, we should stop the notifications and end.
                                                setPlayModeNotifications(false);
                                                connectionState = ConnectionStatus.CONNECTED;
                                                broadcastConnectionNotification();
                                            }

                                        }
                                        else {
                                            // Command set on the device so now read the command response.
                                            readCharacteristic(TDCS_SERVICE_UUID, TDCS_CONTROL_RESPONSE_UUID);
                                        }
                                        valid = true;
                                    }
                                    catch (Exception invalidCharacteristic) {
                                        abortConnection(invalidCharacteristic);
                                    }
                                }
                            }

                            if (!valid) {
                                Log.e(Logger.TAG, "Invalid command response: "
                                        + characteristic.getUuid().toString() + Arrays.toString(commandData));
                            }
                        }
                        else if (TDCS_DATA_BUFFER_UUID.equals(characteristic.getUuid())) {

                            // Data buffer written for current descriptor, now issue the command
                            // to write it to memory...
                            try {
                                writeCommand(SUB_COMMAND_ID_WRITE_PROG, currentProgramId, currentDescriptorId);
                            }
                            catch (InvalidCharacteristic invalidCharacteristic) {
                                abortConnection(invalidCharacteristic);
                            }
                        }
                    }
                    else {
                        // Invalid status - abort the connection...
                        connectionState = ConnectionStatus.DISCONNECTED;
                        broadcastConnectionNotification(status);
                    }
                }

                /**
                 * Callback triggered as a result of a remote characteristic notification.
                 *
                 * @param gatt GATT client the characteristic is associated with
                 * @param characteristic Characteristic that has been updated as a result
                 *                       of a remote notification event.
                 */
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {

                    byte[] valueData = characteristic.getValue();
                    int value;

                    if (!stopped) {

                        // is this a Actual Current notification?
                        if (connectionState == ConnectionStatus.PLAYING &&
                                TDCS_ACTUAL_CURRENT_UUID.equals(characteristic.getUuid())) {

                            value = ProgramEntity.getInteger(valueData, 0);
                            value = Math.round((float) value / 100f) * 100;

                            // We need to detect when the actual current has been zero for more than
                            // three seconds and signal a disconnection if this is the case...
                            if (!started || value != 0) {
                                started = true;
                                handler.removeCallbacks(actualCurrentTimeoutHandler);
                                handler.postDelayed(actualCurrentTimeoutHandler, CONNECTION_TIMEOUT);
                            }

                            // Update the UI if the value has changed...
                            if (value != lastActualCurrentReading) {
                                lastActualCurrentReading = value;
                                broadcastNotification(Actions.ACTION_ACTUAL_CURRENT_NOTIFICATION, value);
                            }

                            // Or a duration update...
                        }
                        else if (TDCS_ACTIVE_MODE_DURATION_UUID.equals(characteristic.getUuid())) {

                            // Make sure we are in Playing Mode....
                            setState(ConnectionStatus.PLAYING);

                            value = ProgramEntity.getInteger(valueData, 0);
                            broadcastNotification(Actions.ACTION_ACTIVE_MODE_DURATION_NOTIFICATION, value);

                        } // ... or Remaining time notification?
                        else if (TDCS_ACTIVE_MODE_REMAINING_TIME_UUID.equals(characteristic.getUuid())) {

                            // If the time remaining has reached 0, we know the program has completed!
                            value = ProgramEntity.getInteger(valueData, 0);
                            if (value == 0) {
                                setState(ConnectionStatus.CONNECTED);
                            }
                        }
                    }

                    if (TDCS_CONTROL_RESPONSE_UUID.equals(characteristic.getUuid())) {

                        if (valueData.length > 1) {

                            // We can get errors via this response notifications
                            if (valueData[1] != COMMAND_RESPONSE_SUCCESS) {
                                if (valueData[0] == COMMAND_ID_MANAGE_PROGRAMS) {

                                    if (connectionState == ConnectionStatus.PLAYING) {
                                        connectionState = ConnectionStatus.CONNECTED;
                                        broadcastConnectionNotification(true);
                                    }
                                    else if (connectionState == ConnectionStatus.CONNECTING) {
                                        connectionState = ConnectionStatus.DISCONNECTED;
                                        broadcastConnectionNotification(true);
                                    }
                                }
                            }
                        }
                    }
                }

                /**
                 * Deal with the Update Notifications descriptor having been successfully written.
                 *
                 * @param gatt Gatt
                 * @param descriptor Descriptor
                 * @param status status
                 */
                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        if (descriptor.getUuid().equals(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)) {

                            // Notifications descriptor has been written to
                            // Now play the program...
                            try {

                                byte[] data = descriptor.getValue();

                                if (data.length == 2) {

                                    // If enabled - start the program
                                    if (data[0] == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0]
                                            && data[1] == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[1]) {
                                        writeCommand(SUB_COMMAND_ID_START_PROG, currentProgramId, null);
                                    }
                                }
                            }
                            catch (InvalidCharacteristic invalidCharacteristic) {
                                abortConnection(invalidCharacteristic);
                            }
                        }
                    }
                    else {
                        Log.d(Logger.TAG, "Callback: Error writing GATT Descriptor: " + status);
                    }
                }

                private void abortConnection(Exception invalidCharacteristic) {

                    Log.e(Logger.TAG, "Invalid characteristic", invalidCharacteristic);
                    connectionState = ConnectionStatus.DISCONNECTED;
                    broadcastConnectionNotification();
                }
            };

    private void setState(ConnectionStatus connectionState) {

        if (this.connectionState != connectionState) {

            // If we were playing, remove any callbacks that are still active...
            if (this.connectionState == ConnectionStatus.PLAYING) {
                try {
                    handler.removeCallbacks(actualCurrentTimeoutHandler);
                    handler.removeCallbacks(durationTimeoutHandler);
                }
                catch (Exception e) {
                    // do nothing.
                }
            }

            this.connectionState = connectionState;
            broadcastConnectionNotification();
        }
    }

    private void disconnectFromDevice() {
        // Otherwise, we should stop the notifications and end.
        try {
            started = false;
            stopped = true;
            stopProgram();
//            setPlayModeNotifications(false);
        }
        catch (Exception e) {
            // to nothing...
        }
//        setState(ConnectionStatus.CONNECTED);
    }

//    private void broadcastBatteryLevel(final int batteryLevel) {
//
//        Log.e(Logger.TAG, "Battery Level: " + String.valueOf(batteryLevel));
//
//        final Intent intent = new Intent(Actions.ACTION_BATTERY_LEVEL);
//        intent.putExtra(Actions.EXTRA_BATTERY_LEVEL, batteryLevel);
//        sendBroadcast(intent);
//    }


    private void broadcastInvalidVersionNumber(final float versionNumber) {

        Log.e(Logger.TAG, "Battery Level: " + String.valueOf(versionNumber));

        final Intent intent = new Intent(Actions.ACTION_INVALID_VERSION_LEVEL);
        intent.putExtra(Actions.EXTRA_VERSION_NUMBER, versionNumber);
        sendBroadcast(intent);
    }

    private void broadcastNotification(final String action,
                                       final int value) {

        final Intent intent = new Intent(action);
        intent.putExtra(Actions.EXTRA_NOTIFICATION_VALUE, value);
        sendBroadcast(intent);
    }

    private void broadcastProgramAttributesRead(final String programName) {

        final Intent intent = new Intent(Actions.ACTION_PROGRAM_ATTRIBUTES_READ);
        intent.putExtra(Actions.EXTRA_PROGRAM_NAME, programName);
        sendBroadcast(intent);
    }

    private void broadcastConnectionNotification() {

        broadcastConnectionNotification(null, null);
    }

    private void broadcastConnectionNotification(Boolean playFailed) {
        broadcastConnectionNotification(playFailed, null);
    }

    private void broadcastConnectionNotification(Integer statusCode) {
        broadcastConnectionNotification(null, statusCode);
    }

    private void broadcastConnectionNotification(Boolean playFailed, Integer statusCode) {

        final Intent intent = new Intent(Actions.ACTION_CONNECTION_STATE_CHANGED);
        intent.putExtra(Actions.EXTRA_CONNECTION_STATUS, connectionState);
        if (playFailed != null) {
            intent.putExtra(Actions.EXTRA_PLAY_FAILED, playFailed);
        }
        if (statusCode != null) {
            intent.putExtra(Actions.EXTRA_STATUS_CODE, statusCode);
        }
        sendBroadcast(intent);
    }

    /**
     * Get GATT Characteristic Utility function.
     *
     * @param serviceUUID        service UUID
     * @param characteristicUUID characteristic UUID
     * @return characteristic
     * @throws InvalidCharacteristic
     */
    private BluetoothGattCharacteristic getCharacteristic(UUID serviceUUID,
                                                          UUID characteristicUUID) throws InvalidCharacteristic {

        BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
        if (service == null) {
            throw new InvalidCharacteristic(serviceUUID, null);
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            throw new InvalidCharacteristic(serviceUUID, characteristicUUID);
        }

        return characteristic;
    }

    /**
     * Write command to the device.  Once the response is received, this will be followed
     * by a read command if the original command is for data retrieval.
     *
     * @param subCommandId Sub Command Id
     * @param programId Program Id
     * @param descriptorId descriptor Id
     * @throws InvalidCharacteristic
     */
    private void writeCommand(byte subCommandId, Byte programId, Byte descriptorId) throws InvalidCharacteristic {

        currentSubCommandId = subCommandId;
        currentProgramId = programId;
        currentDescriptorId = descriptorId;

        byte[] commandData = new byte[5];
        commandData[0] = COMMAND_ID_MANAGE_PROGRAMS;
        commandData[1] = subCommandId;
        commandData[2] = programId != null ? programId : 0;
        commandData[3] = descriptorId != null ? descriptorId : 0;

        writeCharacteristic(TDCS_SERVICE_UUID, TDCS_CONTROL_UUID, commandData);
    }

    private void writeDataBuffer(byte descriptorId) throws InvalidCharacteristic, SQLException {

        currentDescriptorId = descriptorId;
        byte[] data = currentProgramEntity.getData(descriptorId);
        writeCharacteristic(TDCS_SERVICE_UUID, TDCS_DATA_BUFFER_UUID, data);
    }


    /**
     * Issues a read characteristic command to the device.  The onCharacteristicRead call back
     * will be called in response.
     *
     * @param serviceUUID Service UUID
     * @param characteristicUUID Characteristic UUID
     * @throws InvalidCharacteristic
     */
    private void readCharacteristic(UUID serviceUUID,
                                    UUID characteristicUUID) throws InvalidCharacteristic {

        BluetoothGattCharacteristic characteristic = getCharacteristic(serviceUUID, characteristicUUID);

        // This will result in onCharacteristicRead() being called.
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Issues a write characteristic command to the device.  The onCharacteristicWrite call back
     * will be called in response.
     *
     * @param serviceUUID Service UUID
     * @param characteristicUUID Characteristic UUID
     * @param data Data
     * @throws InvalidCharacteristic
     */
    private void writeCharacteristic(UUID serviceUUID,
                                     UUID characteristicUUID,
                                     byte[] data) throws InvalidCharacteristic {

        BluetoothGattCharacteristic characteristic = getCharacteristic(serviceUUID, characteristicUUID);
        characteristic.setValue(data);

        // This will result in onCharacteristicRead() being called.
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    private void setPlayModeNotifications(boolean enabled) throws InvalidCharacteristic {

        writePlayModCharacteristic(TDCS_ACTUAL_CURRENT_UUID, enabled);
        writePlayModCharacteristic(TDCS_ACTIVE_MODE_DURATION_UUID, enabled);
        writePlayModCharacteristic(TDCS_ACTIVE_MODE_REMAINING_TIME_UUID, enabled);
        writePlayModCharacteristic(TDCS_CONTROL_RESPONSE_UUID, enabled);
    }

    private void writePlayModCharacteristic(UUID characteristicUUID,
                                            boolean enabled) throws InvalidCharacteristic {

        BluetoothGattCharacteristic characteristic = getCharacteristic(TDCS_SERVICE_UUID, characteristicUUID);
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    private final BroadcastReceiver programStateChangeRequestReceiver = new BroadcastReceiver() {

        @Override public void onReceive(Context context, final Intent intent) {

            String requestName = intent.getStringExtra(Actions.EXTRA_STATE_CHANGE);

            if (requestName != null) {

                if (currentProgramEntity == null) {
                    try {
                        currentProgramEntity = programEntityDao.getFirstEntry();
                    }
                    catch (SQLException e) {
                        Log.e(Logger.TAG, "Unable to get first program!", e);
                    }
                }

                String newState = MessageActions.REQUEST_STOP_PROGRAM;

                if (currentProgramEntity != null) {

                    try {
                        if (MessageActions.REQUEST_START_PROGRAM.equals(requestName)) { // start program if required

                            if (connectionState == ConnectionStatus.CONNECTED) {
                                playCurrentProgram();
                                newState = MessageActions.RESPONSE_STARTED;

                            }
                        }
                        else if (MessageActions.REQUEST_STOP_PROGRAM.equals(requestName)) { // stop program if required
                            stopProgram();
                            newState = MessageActions.RESPONSE_STOPPED;
                        }
                        else { // otherwise just broadcast the current program state
                            newState = (connectionState == ConnectionStatus.PLAYING) ?
                                    MessageActions.RESPONSE_STARTED : MessageActions.RESPONSE_STOPPED;
                        }
                    }
                    catch (InvalidCharacteristic invalidCharacteristic) {
                        Log.e(Logger.TAG, "Invalid characteristic", invalidCharacteristic);
                    }
                    catch (SQLException e) {
                        Log.e(Logger.TAG, "Error running program", e);
                    }
                }

                // Send the response to the Wear device.
                Intent response = new Intent(ApiService.this, MobileAppStateResponseService.class);
                response.setAction(newState);
                startService(response);
            }
        }
    };

    private final Runnable durationTimeoutHandler = new Runnable() {
        @Override
        public void run() {
            disconnectFromDevice();
        }
    };

    private final Runnable actualCurrentTimeoutHandler = new Runnable() {
        @Override
        public void run() {
            disconnectFromDevice();
        }
    };
}
