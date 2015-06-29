package us.foc.transcranial.dcs.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import java.util.ArrayList;

/**
 * BLE Device Scanner for Lollipop and later.
 */
public class BLEDeviceScannerLollipop {

    private final ScanCallback scanCallback;
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private final BluetoothLeScanner scanner;
    private final ScanSettings settings;
    private final ArrayList<ScanFilter> filters;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BLEDeviceScannerLollipop(Context context, ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        handler = new Handler();

        BluetoothAPIManager bluetoothAPIManager = new BluetoothAPIManager(context);
        bluetoothAdapter = bluetoothAPIManager.getBluetoothAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();

        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<>();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void start() { // Stops scanning after a pre-defined scan period.
        handler.postDelayed(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                scanner.stopScan(scanCallback);
            }
        }, BluetoothAPIManager.SCAN_PERIOD);

        scanner.startScan(filters, settings, scanCallback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stop() {
        scanner.stopScan(scanCallback);
    }

}
