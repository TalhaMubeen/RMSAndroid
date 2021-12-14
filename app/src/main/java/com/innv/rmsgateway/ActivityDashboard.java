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
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.innv.rmsgateway.activity.AssetsActivity;
import com.innv.rmsgateway.activity.LoginActivity;
import com.innv.rmsgateway.activity.ReportsViewActivity;
import com.innv.rmsgateway.activity.SettingsActivity;
import com.innv.rmsgateway.adapter.AssetsAlertsAdapter;
import com.innv.rmsgateway.adapter.DeviceViewAdapter;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.DataSyncProcessEx;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.ObservableObject;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.interfaces.NotificationAlertsCallback;
import com.innv.rmsgateway.service.BLEBackgroundService;
import com.innv.rmsgateway.service.OnBLEDeviceCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.innv.rmsgateway.activity.LoginActivity.PREFS_KEY_SERVER_SYNC;

public class ActivityDashboard extends AppCompatActivity implements OnBLEDeviceCallback, Observer, NotificationAlertsCallback {

    private static final String TAG = "sensorScanner";
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_SETTINGS = 2;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_CODE_PERMISSION_BLUETOOTH = 3;

    RecyclerView rv_rms_categories;
    GridView gv_alerts;
    DeviceViewAdapter rv_CategoryAdapter;
    AssetsAlertsAdapter gv_alertsAdapter;

    static DataSyncProcessEx dataSyncProcessEx=null;
    ImageView iv_cloudSyncIcon;
    ActivityResultLauncher<Intent> activityLauncher;

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

    private  void StartDataSyncService(){
        Globals.IsAutoUpdateEnabled=true;
        dataSyncProcessEx =new DataSyncProcessEx();
        Globals.dataSyncProcessEx=dataSyncProcessEx;
        StartAsyncTaskInParallel(dataSyncProcessEx, this);
        Log.i("StartDataSyncService","Starting Data Sync Service");
    }

    private void StartAsyncTaskInParallel(DataSyncProcessEx task, Context context){
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,context);
    }



    @Override
    public void update(Observable o, Object arg) {
        Intent intent=(Intent)arg;
        Bundle b1= intent.getExtras();
        final String messageName=b1.getString("messageName");
        final String messageData=b1.getString("messageData");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message="";
                switch (messageName){
                    case Globals.OBSERVABLE_MESSAGE_DATA_SENT:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_cloudSyncIcon.setImageDrawable(ContextCompat.getDrawable(ActivityDashboard.this, R.drawable.ic_cloud_queue_white_24dp));
                            }
                        });

                        break;
                    case Globals.OBSERVABLE_MESSAGE_NETWORK_PULL:
                        Log.i("Info","pull");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_cloudSyncIcon.setImageDrawable(ContextCompat.getDrawable(ActivityDashboard.this, R.drawable.ic_cloud_download_white_24dp));

                            }
                        });
                        break;
                    case Globals.OBSERVABLE_MESSAGE_NETWORK_PUSH:
                        Log.i("Info","push");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_cloudSyncIcon.setImageDrawable(ContextCompat.getDrawable(ActivityDashboard.this,R.drawable.ic_cloud_upload_white_24dp));
                            }
                        });
                        break;
                    case Globals.OBSERVABLE_MESSAGE_NETWORK_CONNECTED:
                        Log.i("Info","connected");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_cloudSyncIcon.setImageDrawable(ContextCompat.getDrawable(ActivityDashboard.this,R.drawable.ic_cloud_white_24dp));
                            }
                        });
                        break;

                    case Globals.OBSERVABLE_MESSAGE_TOKEN_STATUS:
                    case Globals.OBSERVABLE_MESSAGE_NETWORK_DISCONNECTED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_cloudSyncIcon.setImageDrawable(ContextCompat.getDrawable(ActivityDashboard.this,R.drawable.ic_cloud_off_white_24dp));
                            }
                        });
                        break;
                    case Globals.OBSERVABLE_MESSAGE_LANGUAGE_CHANGED:
                       // ActivityDashboard.this.recreate();

                }
                if(!message.equals(""))
                    Toast.makeText(ActivityDashboard.this,message,Toast.LENGTH_SHORT ).show();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Globals.setScreenOrientation(this);

        checkPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_layout);

        iv_cloudSyncIcon = (ImageView) findViewById(R.id.iv_cloudSyncIcon);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ObservableObject.getInstance().addObserver(this);


        Globals.setDbContext(getApplicationContext());
        AlertManager.setNotificationAlertCallback( this.getClass().getSimpleName() , this);
        NodeDataManager.init();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(rv_rms_categories == null) {

            rv_rms_categories = (RecyclerView) findViewById(R.id.rv_rms_categories);
            int orientation = this.getResources().getConfiguration().orientation;
            int spanCount;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                spanCount = 2;
            } else {
                spanCount = 1;
            }

            GridLayoutManager horizontalLayoutManager =
                    new GridLayoutManager(this, spanCount, GridLayoutManager.HORIZONTAL, false);
            rv_rms_categories.setLayoutManager(horizontalLayoutManager);


            rv_CategoryAdapter = new DeviceViewAdapter(this);
            rv_rms_categories.setAdapter(rv_CategoryAdapter);

            gv_alerts = (GridView) findViewById(R.id.gv_alerts);
            gv_alertsAdapter = new AssetsAlertsAdapter(this);
            gv_alerts.setAdapter(gv_alertsAdapter);

            //Setting Callbacks here
            LinearLayout ll_reports = (LinearLayout) findViewById(R.id.ll_reports);
            ll_reports.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityDashboard.this, ReportsViewActivity.class);
                    startActivity(intent);
                }
            });

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

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    if(LoginActivity.checkCloudSync(ActivityDashboard.this)) {
                        startDataSyncService();
                    }else{
                        if(Globals.pref.getBoolean(PREFS_KEY_SERVER_SYNC, true)) {
                            Intent intent = new Intent(ActivityDashboard.this, LoginActivity.class);
                            intent.putExtra("FIRST_START", true);
                            startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public static void stopDataSyncService(){
        if(dataSyncProcessEx != null) {
            dataSyncProcessEx.CancelTask();
            Globals.dataSyncProcessEx = null;
        }
    }

    public static void startSync(){

    }


    private void startDataSyncService(){

        if (Globals.dataSyncProcessEx == null) {
            StartDataSyncService();
            dataSyncProcessEx.setDataSyncProcessListener(new DataSyncProcessEx.DataSyncProcessListener() {
                @Override
                public void onStatusChanged(int status) {
                    final int _status = status;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*
                            switch(_status){
                                case 2:
                                    syncStatusIcon.setImageResource(R.drawable.ic_sync_green_24dp);
                                    break;
                                case 1:
                                    syncStatusIcon.setImageResource(R.drawable.ic_sync_red_24dp);
                                    break;
                                case 3:
                                    syncStatusIcon.setImageResource(R.drawable.ic_sync_green_24dp);
                                    break;
                                    default:
                                        syncStatusIcon.setImageResource(R.drawable.ic_sync_grey_24dp);
                                        break;
                            }*/
/*                            if (_status == 1) {
                                iv_cloudSyncIcon.setImageResource(R.drawable.ic_sync_green_24dp);
                            } else if (_status == 2) {
                                iv_cloudSyncIcon.setImageResource(R.drawable.ic_sync_red_24dp);
                            } else if (_status == 3) {
                                iv_cloudSyncIcon.setImageResource(R.drawable.ic_sync_grey_24dp);
                            }*/
                           // Toast.makeText(getApplicationContext(), "Status : " + _status, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
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
        try{
            BLEBackgroundService.stopScan();
            stopDataSyncService();
            mBound = false;
            unbindService(mServiceConnection);
            BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
        }catch (Exception ignored){ }
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
        registerReceiver(bluetoothStateRcvr, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

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
            checkBluetoothPermissions();
        }

    }

    private void checkBluetoothPermissions(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            //Log.d(TAG,"BLE adapter is not enabled");
            if(!Globals.BLE_Available) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_CODE_PERMISSION_BLUETOOTH);
                Globals.BLE_Available = true;
            }
        }else {
            Globals.BLE_Available = true;
            bindService(new Intent(ActivityDashboard.this, BLEBackgroundService.class),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
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
                }else{
                    checkBluetoothPermissions();
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
                            Globals.BLE_Available = false;
                            mBound = false; }

                        if(!Globals.BLE_Available) {
                            Globals.BLE_Available = true;
                            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_CODE_PERMISSION_BLUETOOTH);
                        }
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Globals.BLE_Available = false;
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Globals.BLE_Available = true;
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
            checkBluetoothPermissions();

        }else if (requestCode == REQUEST_CODE_PERMISSION_BLUETOOTH) {
            if(resultCode == 0){
                Globals.BLE_Available = false;
                finish();
            }
        }
    }

}
