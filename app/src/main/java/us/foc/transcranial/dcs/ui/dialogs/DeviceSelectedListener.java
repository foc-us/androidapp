package us.foc.transcranial.dcs.ui.dialogs;

import android.bluetooth.BluetoothDevice;

public interface DeviceSelectedListener {

    void onDeviceSelected(final BluetoothDevice device);
}
