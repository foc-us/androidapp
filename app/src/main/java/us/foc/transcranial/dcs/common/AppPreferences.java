package us.foc.transcranial.dcs.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Shared Preferences...
 */
public class AppPreferences {

    private static final String PREF_DEVICE_NAME = "PREF_DEVICE_NAME";
    private static final String PREF_DEVICE_ADDR = "PREF_DEVICE_ADDR";

    /**
     * @return the name of the last paired BLE device, or null
     */
    public static String getPairedDeviceName(Context context) {
        return getSharedPrefs(context).getString(PREF_DEVICE_NAME, null);
    }

    /**
     * Sets the name of the last paired BLE device
     *
     * @param name the BLE's name
     */
    public static void setPairedDeviceName(Context context, String name) {
        getSharedPrefs(context).edit().putString(PREF_DEVICE_NAME, name).apply();
    }

    /**
     * @return the address of the last paired BLE device, or null
     */
    public static String getPairedDeviceAddress(Context context) {
        return getSharedPrefs(context).getString(PREF_DEVICE_ADDR, null);
    }

    /**
     * Sets the address of the last paired BLE device
     *
     * @param address the BLE's address
     */
    public static void setPairedDeviceAddress(Context context, String address) {
        getSharedPrefs(context).edit().putString(PREF_DEVICE_ADDR, address).apply();
    }

    private static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
