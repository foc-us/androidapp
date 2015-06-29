package us.foc.transcranial.dcs.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;

/**
 * BLE Device Scanner Android versions prior to Lollipop.
 */
public class BLEDeviceScanner {

    public interface BLEDeviceScanLister {
        void onScanFinished();
    }

    private final BluetoothAdapter.LeScanCallback scanCallback;
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private final BLEDeviceScanLister bleDeviceScanLister;

    public BLEDeviceScanner(Context context,
                            BluetoothAdapter.LeScanCallback scanCallback,
                            BLEDeviceScanLister bleDeviceScanLister) {
        this.scanCallback = scanCallback;
        this.bleDeviceScanLister = bleDeviceScanLister;
        handler = new Handler();

        BluetoothAPIManager bluetoothAPIManager = new BluetoothAPIManager(context);
        bluetoothAdapter = bluetoothAPIManager.getBluetoothAdapter();
    }

    public void start() { // Stops scanning after a pre-defined scan period.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(scanCallback);
                bleDeviceScanLister.onScanFinished();
            }
        }, BluetoothAPIManager.SCAN_PERIOD);

        bluetoothAdapter.startLeScan(scanCallback);
    }

    public void stop() {
        bluetoothAdapter.stopLeScan(scanCallback);
    }
}
