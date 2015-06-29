package us.foc.transcranial.dcs.ui.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;
import us.foc.transcranial.dcs.R;
import us.foc.transcranial.dcs.bluetooth.BLEDeviceScanner;
import us.foc.transcranial.dcs.bluetooth.BLEDeviceScannerLollipop;
import us.foc.transcranial.dcs.common.Logger;

/**
 * Displays results of a bluetooth scan in a dialog
 */
public class ScanResultsDialog extends BaseDialogFragment implements BLEDeviceScanner.BLEDeviceScanLister {

    private boolean scanning = false;

    public static ScanResultsDialog newInstance(Bundle args) {
        ScanResultsDialog fragment = new ScanResultsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @InjectView(R.id.no_results) TextView noResultsText;
    @InjectView(R.id.dialog_title) TextView dialogTitle;
    @InjectView(R.id.scan_results_list) ListView scanResultsList;
    @InjectView(R.id.progress_bar) ProgressBar progressBar;
    @InjectView(R.id.btn_cancel) Button cancelButton;

    @OnClick(R.id.btn_cancel) void onCancel() {
        cancelScan();
        dismiss();
    }

    private LeDeviceListAdapter leDeviceListAdapter;
    private DeviceSelectedListener deviceSelectedListener;
    private BLEDeviceScanner bleDeviceScanner;
    private BLEDeviceScannerLollipop bleDeviceScannerLollipop;

    @Override protected int getLayoutResId() {
        return R.layout.dialog_scan_results;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (getActivity() instanceof DeviceSelectedListener) {
            deviceSelectedListener = (DeviceSelectedListener) getActivity();
        }
        else {
            Log.e(Logger.TAG, "ScanResultsDialog called from wrong activity!");
            dismiss();
        }

        leDeviceListAdapter = new LeDeviceListAdapter();
        scanResultsList.setAdapter(leDeviceListAdapter);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            bleDeviceScanner = new BLEDeviceScanner(getActivity(),
                                                    new BluetoothAdapter.LeScanCallback() {
                                                        @Override
                                                        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                                                            storeNewDevice(device);
                                                        }
                                                    }, this);
        }
        else {
            bleDeviceScannerLollipop = new BLEDeviceScannerLollipop(getActivity(), new ScanCallback() {

                @Override
                public void onScanFailed(int errorCode) {

                    int errorResId;
                    switch (errorCode) {
                        case SCAN_FAILED_ALREADY_STARTED:
                            errorResId = R.string.scan_error_already_started;
                            break;
                        case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                            errorResId = R.string.scan_error_registration_failed;
                            break;
                        case SCAN_FAILED_FEATURE_UNSUPPORTED:
                            errorResId = R.string.scan_error_feature_unsupported;
                            break;
                        case SCAN_FAILED_INTERNAL_ERROR:
                            errorResId = R.string.scan_error_internal_error;
                            break;
                        default:
                            errorResId = R.string.scan_error_unknown;
                            break;
                    }

                    onScanError(errorResId);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    final BluetoothDevice device = result.getDevice();
                    storeNewDevice(device);
                }
            });
        }
        startScan();
    }

    private void storeNewDevice(final BluetoothDevice device) {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    leDeviceListAdapter.addDevice(device);
                    leDeviceListAdapter.notifyDataSetChanged();

                    scanResultsList.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void startScan() {

        scanning = true;

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bleDeviceScanner.start();
        }
        else {
            bleDeviceScannerLollipop.start();
        }

        progressBar.setVisibility(View.VISIBLE);
        scanResultsList.setVisibility(View.INVISIBLE);
        dialogTitle.setText(R.string.scanning);
    }

    @Override
    public void onScanFinished() {
        scanning = false;

        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);

            if (leDeviceListAdapter.getCount() == 0) {
                noResultsText.setVisibility(View.VISIBLE);
                dialogTitle.setText(R.string.no_devices);
                cancelButton.setText(R.string.ok_button_label);
            }
            else {
                dialogTitle.setText(R.string.devices);
            }
        }
    }

    public void onScanError(int errorResId) {
        scanning = false;

        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        dialogTitle.setText(getString(R.string.scan_error_title));

        noResultsText.setVisibility(View.VISIBLE);

        String prompt = getString(errorResId) + getString(R.string.scan_error_guidance);
        noResultsText.setText(prompt);
    }

    private void cancelScan() {
        if (bleDeviceScanner != null) {
            bleDeviceScanner.stop();
        }
        if (bleDeviceScannerLollipop != null) {
            bleDeviceScannerLollipop.stop();
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {

        private final ArrayList<BluetoothDevice> bluetoothDevices;
        private final LayoutInflater inflater;

        public LeDeviceListAdapter() {
            super();
            bluetoothDevices = new ArrayList<>();
            inflater = getActivity().getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!bluetoothDevices.contains(device)) {
                bluetoothDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return bluetoothDevices.get(position);
        }

        public void clear() {
            bluetoothDevices.clear();
        }

        @Override
        public int getCount() {
            return bluetoothDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return bluetoothDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ViewHolder viewHolder;

            // General ListView optimization code.
            if (view == null) {
                view = inflater.inflate(R.layout.scan_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final BluetoothDevice device = bluetoothDevices.get(i);
            final String deviceName = device.getName();

            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            }
            else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            viewHolder.deviceAddress.setText(device.getAddress());

            // Handle when a device is selected...
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (scanning) {
                        cancelScan();
                    }
                    deviceSelectedListener.onDeviceSelected(device);
                    dismiss();
                }
            });

            return view;
        }

        private class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
    }

}
