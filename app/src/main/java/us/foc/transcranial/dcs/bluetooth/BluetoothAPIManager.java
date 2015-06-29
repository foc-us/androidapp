package us.foc.transcranial.dcs.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

public class BluetoothAPIManager {

    public static final int REQUEST_ENABLE_BT = 101;

    // Stops scanning after 10 seconds.
    public static final long SCAN_PERIOD = 10000;

    private final BluetoothAdapter bluetoothAdapter;

    public BluetoothAPIManager(Context context) {

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void checkBluetoothIsEnabled(Activity activity) {

        if( !bluetoothAdapter.isEnabled() ) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }


//    public void reconnectToDevice(Context context) {
//
//        String address = AppPreferences.getPairedDeviceAddress(context);
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
//
//        bluetoothAdapter.getRemoteDevice(address);
//    }
}
