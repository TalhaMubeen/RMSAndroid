package com.innv.rmsgateway;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.innv.rmsgateway.activity.AssetsActivity;
import com.innv.rmsgateway.activity.SettingsActivity;
import com.innv.rmsgateway.adapter.AssetsAlertsAdapter;
import com.innv.rmsgateway.adapter.DeviceViewAdapter;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.interfaces.NotificationAlertsCallback;
import com.innv.rmsgateway.service.BLEBackgroundService;
import com.innv.rmsgateway.service.OnBLEDeviceCallback;

import java.util.ArrayList;
import java.util.List;

public class ActivityDashboard extends AppCompatActivity implements OnBLEDeviceCallback , NotificationAlertsCallback {

    private static final String TAG = "sensorScanner";
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_SETTINGS = 2;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_CODE_PERMISSION_BLUETOOTH = 3;

    RecyclerView rv_rms_categories;
    GridView gv_alerts;
    DeviceViewAdapter rv_CategoryAdapter;
    AssetsAlertsAdapter gv_alertsAdapter;


    @SuppressLint("StaticFieldLeak")
    private static BLEBackgroundService mService = null;
    // Tracks the bound state of the service.
    private static boolean mBound = false;
    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mService == null) {
                BLEBackgroundService.LocalBinder binder = (BLEBackgroundService.LocalBinder) service;
                mService = binder.getService();
                try {
                    mService.startBleService();
                    BLEBackgroundService.addBLEUpdateListener(this.getClass().getSimpleName(), ActivityDashboard.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                mService.startBleService();
            }

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    public void updateData() {
        if(gv_alertsAdapter!= null) {
            gv_alertsAdapter.notifyDataSetChanged();
        }

        if(rv_CategoryAdapter != null) {
            rv_CategoryAdapter.update();
            rv_CategoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setTitle("Summary View");
        checkPermissions();

        Globals.setDbContext(getApplicationContext());
        AlertManager.setNotificationAlertCallback( this.getClass().getSimpleName() , this);
        NodeDataManager.init();
    }

    @Override
    protected void onResume() {
        super.onResume();

/*        if (!mBound) {
            bindService(new Intent(ActivityDashboard.this, BLEBackgroundService.class),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }*/

        if(rv_rms_categories == null) {

            rv_rms_categories = (RecyclerView) findViewById(R.id.rv_rms_categories);
            GridLayoutManager horizontalLayoutManager =
                    new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
            rv_rms_categories.setLayoutManager(horizontalLayoutManager);

            rv_CategoryAdapter = new DeviceViewAdapter(this);
            rv_rms_categories.setAdapter(rv_CategoryAdapter);

            gv_alerts = (GridView) findViewById(R.id.gv_alerts);
            gv_alertsAdapter = new AssetsAlertsAdapter(this);
            gv_alerts.setAdapter(gv_alertsAdapter);

            //Setting Callbacks here
            LinearLayout ll_reports = (LinearLayout) findViewById(R.id.ll_reports);
            LinearLayout ll_assets = (LinearLayout) findViewById(R.id.ll_assets);
            ll_assets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityDashboard.this, AssetsActivity.class);
                    intent.putExtra("Position", -1); //All Nodes
                    intent.putExtra("ShowOne", false);
                    startActivity(intent);
                }
            });

            LinearLayout ll_settings = (LinearLayout) findViewById(R.id.ll_settings);
            ll_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityDashboard.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });

        }else{
            updateData();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBound) {
            mBound = false;
            unbindService(mServiceConnection);
            BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBLEDeviceCallback(BleDevice device) {
        //updateData();
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            //Log.d(TAG,"BLE adapter is not enabled");
            // Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_CODE_PERMISSION_BLUETOOTH);
        }

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };

        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }else{
            bindService(new Intent(ActivityDashboard.this, BLEBackgroundService.class),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        registerReceiver(bluetoothStateRcvr, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (!checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                }
                break;

            case Manifest.permission.CAMERA:
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private final BroadcastReceiver bluetoothStateRcvr = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(mBound){
                            unbindService(mServiceConnection);
                            mBound = false;
                        }

                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_CODE_PERMISSION_BLUETOOTH);
                        break;

                    case BluetoothAdapter.STATE_ON:
                        bindService(new Intent(ActivityDashboard.this, BLEBackgroundService.class),
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE);
                        break;
                }
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            bindService(new Intent(ActivityDashboard.this, BLEBackgroundService.class),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);

        }else if (requestCode == REQUEST_CODE_PERMISSION_BLUETOOTH) {
            if(resultCode == 0){
                finish();
            }
        }
    }

}
