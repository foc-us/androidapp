package us.foc.transcranial.dcs;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnPageChange;
import us.foc.transcranial.dcs.bluetooth.ApiService;
import us.foc.transcranial.dcs.bluetooth.BluetoothAPIManager;
import us.foc.transcranial.dcs.bluetooth.ConnectionStatus;
import us.foc.transcranial.dcs.bluetooth.InvalidCharacteristic;
import us.foc.transcranial.dcs.common.Actions;
import us.foc.transcranial.dcs.common.AppPreferences;
import us.foc.transcranial.dcs.common.BusProvider;
import us.foc.transcranial.dcs.common.Logger;
import us.foc.transcranial.dcs.db.ProgramEntityDao;
import us.foc.transcranial.dcs.model.ProgramEntity;
import us.foc.transcranial.dcs.model.events.NavbarClickEvent;
import us.foc.transcranial.dcs.model.events.NavbarEnableEvent;
import us.foc.transcranial.dcs.model.events.NavbarUpdateEvent;
import us.foc.transcranial.dcs.ui.SlowScroller;
import us.foc.transcranial.dcs.ui.dialogs.DeviceSelectedListener;
import us.foc.transcranial.dcs.ui.dialogs.ScanResultsDialog;
import us.foc.transcranial.dcs.ui.fragments.ProgramFragment;
import us.foc.transcranial.dcs.ui.fragments.UserCommandListener;

/**
 * This activity is used to display a ViewPager which contains information about all available
 * programs.
 */
public class MainMobileActivity extends FragmentActivity implements ViewPager.PageTransformer,
        UserCommandListener,
        DeviceSelectedListener {

    private static final float MIN_PAGE_SCALE = 0.85f;
    private static final float MIN_PAGE_ALPHA = 0.60f;

    private ApiService apiService;
    private ProgramEntityDao programEntityDao;

    @InjectView(R.id.viewpager) ViewPager viewPager;

    @OnPageChange(R.id.viewpager) void onPageChanged(int position) {
        BusProvider.instance().post(new NavbarUpdateEvent(position > 0,
                                                          position < programFragmentAdapter.getCount() - 1));
    }

    private ProgramFragmentAdapter programFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        programEntityDao = new ProgramEntityDao(this);

        programFragmentAdapter = new ProgramFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(programFragmentAdapter);

        viewPager.setPageTransformer(true, this);

        viewPager.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent arg1) {
                return getConnectionStatus() == ConnectionStatus.PLAYING;
            }
        });

        fixViewpagerScrollSpeed();

        registerReceiverForAction(batteryLevelReceiver, Actions.ACTION_BATTERY_LEVEL);
        registerReceiverForAction(invalidDeviceVersionReceiver, Actions.ACTION_INVALID_VERSION_LEVEL);

        setupAPI();
    }

    private void registerReceiverForAction(BroadcastReceiver receiver, String action) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.instance().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.instance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (batteryLevelReceiver != null) {
            unregisterReceiver(batteryLevelReceiver);
        }
        if (invalidDeviceVersionReceiver != null) {
            unregisterReceiver(invalidDeviceVersionReceiver);
        }

        if (apiService != null) {
            unbindService(apiServiceConnection);
        }
    }

    /**
     * Fixes bug/oversight in how Android handles programmatic page changes
     */
    private void fixViewpagerScrollSpeed() {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(viewPager, new SlowScroller(this));
        }
        catch (Exception e) {
            // swallow
        }
    }

    @Override
    public void transformPage(View view, float position) {
        int width = view.getWidth();
        int height = view.getHeight();

        if (position < -1) { // page off-screen left
            view.setAlpha(0);
        }
        else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_PAGE_SCALE, 1 - Math.abs(position));
            float vertMargin = height * (1 - scaleFactor) / 2;
            float horzMargin = width * (1 - scaleFactor) / 2;

            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            }
            else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(MIN_PAGE_ALPHA + (scaleFactor - MIN_PAGE_SCALE) /
                    (1 - MIN_PAGE_SCALE) * (1 - MIN_PAGE_ALPHA));
        }
        else { // page off-screen right.
            view.setAlpha(0);
        }
    }

    private class ProgramFragmentAdapter extends FragmentStatePagerAdapter {

        private List<ProgramEntity> programList;

        public ProgramFragmentAdapter(FragmentManager fm) {
            super(fm);
            try {
                programList = programEntityDao.getAll();
            }
            catch (SQLException e) {
                Log.e(Logger.TAG, "Unable to read programs!", e);
            }
        }

        @Override public Fragment getItem(int position) {
            Bundle args = new Bundle();
            ProgramEntity entity = programList.get(position);

            args.putSerializable(Actions.PROGRAM_ENTITY, entity);
            return ProgramFragment.newInstance(args);
        }

        @Override public int getCount() {
            return programList.size();
        }

        public ProgramEntity getEntityForPosition(int position) {
            return programList.get(position);
        }
    }

    @Subscribe public void onNavbarClickEvent(NavbarClickEvent event) {
        if (viewPager != null) {
            int currentPos = viewPager.getCurrentItem();
            currentPos = (event.isPagedForwards()) ? ++currentPos : --currentPos;
            viewPager.setCurrentItem(currentPos, true);
        }
    }

    /**
     * Bluetooth things **
     */

    private void setupAPI() {

        // Ensure BT is enabled.
        BluetoothAPIManager btManager = new BluetoothAPIManager(this);
        btManager.checkBluetoothIsEnabled(this);

        // Start the service which will handle the comms with the device...
        Intent intent = new Intent(this, ApiService.class);
        bindService(intent, apiServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onScanForDevices() {
        ScanResultsDialog.newInstance(new Bundle()).show(getSupportFragmentManager(), null);
    }

    @Override
    public void onPlayProgram(ProgramEntity programEntity) {

        if (apiService != null) {
            try {
                programEntityDao.update(programEntity);
                apiService.playProgram(programEntity.getProgramId());
            }
            catch (InvalidCharacteristic e) {
                Log.e(Logger.TAG, "Invalid Characteristic: " + e.toString());
            }
            catch (SQLException e) {
                Log.e(Logger.TAG, "Entry DB read error: ", e);
            }

            BusProvider.instance().post(new NavbarEnableEvent(false));
        }
    }

    @Override
    public void onStopProgram() {

        if (apiService != null) {
            try {
                apiService.stopProgram();
            }
            catch (InvalidCharacteristic e) {
                Log.e(Logger.TAG, "Invalid Characteristic: " + e.toString());
            }
            catch (SQLException e) {
                Log.e(Logger.TAG, "Entry DB read error: ", e);
            }
            BusProvider.instance().post(new NavbarEnableEvent(true));
        }
    }

    @Override
    public void onReplayProgram() {

        if (apiService != null) {
            try {
                apiService.replayProgram();
            }
            catch (InvalidCharacteristic e) {
                Log.e(Logger.TAG, "Invalid Characteristic: " + e.toString());
            }
            catch (SQLException e) {
                Log.e(Logger.TAG, "Entry DB read error: ", e);
            }
        }
    }

    @Override
    public ConnectionStatus getConnectionStatus() {

        if (apiService == null) {
            return ConnectionStatus.DISCONNECTED;
        }
        return apiService.getConnectionState();
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device) {
        pairWithDevice(device);
    }

    private void pairWithDevice(BluetoothDevice device) {
        AppPreferences.setPairedDeviceName(this, device.getName());
        AppPreferences.setPairedDeviceAddress(this, device.getAddress());

        if (apiService != null) {
            apiService.connect(device);
        }
        else {
            Log.e(Logger.TAG, "API Service not running!");
        }
    }

    /**
     * API Service:
     */
    private final ServiceConnection apiServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            ApiService.ApiServiceBinder binder = (ApiService.ApiServiceBinder) service;
            MainMobileActivity.this.apiService = binder.getService();

            // Attempt to connect to the last device we connected to...
            MainMobileActivity.this.apiService.reconnection();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            MainMobileActivity.this.apiService = null;
        }
    };

    private final BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int batteryLevel = intent.getExtras().getInt(Actions.EXTRA_BATTERY_LEVEL);
            Toast.makeText(context, "Battery Level: " + String.valueOf(batteryLevel), Toast.LENGTH_LONG).show();
        }
    };

    /**
     * The service has found that the device is running an incompatible version of firmware.
     * Tell the user to update...
     */
    private final BroadcastReceiver invalidDeviceVersionReceiver = new BroadcastReceiver() {

        @Override public void onReceive(Context context, Intent intent) {

            new AlertDialog.Builder(MainMobileActivity.this)
                    .setTitle(getString(R.string.invalid_firmware_message_title))
                    .setMessage(getString(R.string.invalid_device_firmware))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(getString(R.string.ok_button_label), null)
                    .show();
        }
    };
}
