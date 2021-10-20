package com.innv.rmsgateway;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.Globals;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;
import com.innv.rmsgateway.service.BLEBackgroundService;
import com.innv.rmsgateway.service.OnBLEDeviceCallback;

import java.util.ArrayList;
import java.util.List;

public class ActivityDashboard extends AppCompatActivity implements OnBLEDeviceCallback {

    private static final String TAG = "sensorScanner";
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_CODE_PERMISSION_BLUETOOTH = 3;

    GridView gv_rms_categories, gv_alerts;
    DeviceViewAdapter gv_adapter;
    AlertsViewAdapter gv_alertsAdapter;

    private final int[] AlertsLabel = new int[]{
            R.string.alert,
            R.string.warning,
            R.string.normal,
            R.string.defrost,
            R.string.offline,
            R.string.comfail,
    };


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

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermissions();

        Globals.setDbContext(getApplicationContext());

       // AlertManager.setNotificationAlertCallback(this);

        NodeDataManager.init();

        if (!mBound) {
            bindService(new Intent(ActivityDashboard.this, BLEBackgroundService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!mBound) {
            bindService(new Intent(ActivityDashboard.this, BLEBackgroundService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }

        BLEBackgroundService.addBLEUpdateListener(this.getClass().getSimpleName(), this);

        if(gv_rms_categories == null) {
            List<SensorNode> list = NodeDataManager.getPreCheckedNodes();
            gv_rms_categories = (GridView) findViewById(R.id.gv_rms_categories);
            gv_adapter = new DeviceViewAdapter(this, list);
            gv_rms_categories.setAdapter(gv_adapter);



            gv_alerts = (GridView) findViewById(R.id.gv_alerts);
            gv_alertsAdapter = new AlertsViewAdapter(this);
            gv_alerts.setAdapter(gv_alertsAdapter);

            //Setting Callbacks here
            LinearLayout ll_reports = (LinearLayout) findViewById(R.id.ll_reports);

            LinearLayout ll_assets = (LinearLayout) findViewById(R.id.ll_assets);
            ll_assets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityDashboard.this, AssetsActivity.class);
                    intent.putExtra("Filter", 0); //All Nodes
                    intent.putExtra("ShowOne", false);
                    startActivity(intent);
                }
            });

            LinearLayout ll_settings = (LinearLayout) findViewById(R.id.ll_settings);

        }else{
            gv_alertsAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        updateData();
    }


    static class DeviceViewAdapter extends BaseAdapter {

        List<SensorNode> mRMSDevices= new ArrayList<>();
        LayoutInflater inflater;
        Context context;

        public DeviceViewAdapter(Context ctx, List<SensorNode> list){
            mRMSDevices = list;
            inflater = LayoutInflater.from(ctx);
            context = ctx;
        }

        public void UpdateListData(List<SensorNode> list){
            mRMSDevices = list;
        }


        @Override
        public int getCount() {
            return 4;// mRMSDevices.size();
        }

        @Override
        public SensorNode getItem(int position) {
            return mRMSDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;// mRMSDevices.get(position).hashCode();
        }


        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rms_device = convertView;
           // SensorNode item = getItem(position);
           // Profile itemProf = item.getProfile();

            if (rms_device == null) {
                rms_device = inflater.inflate(R.layout.summary_category_item, parent, false);
            }
            TextView card_category_title = (TextView) rms_device.findViewById(R.id.card_category_title);
            //card_category_title.setText(item.getName());

            ImageView iv_device_profile_image = (ImageView) rms_device.findViewById(R.id.iv_device_profile_image);
            //iv_device_profile_image

            TextView tv_total_nodes_count = (TextView) rms_device.findViewById(R.id.tv_total_nodes_count);
           // tv_total_nodes_count.setText(item.getName());




            return rms_device;
        }


    }


    class AlertsViewAdapter extends BaseAdapter {
        LayoutInflater inflater;
        Context context;

        public AlertsViewAdapter(Context ctx){
            inflater = LayoutInflater.from(ctx);
            context = ctx;
        }

        @Override
        public int getCount() {
            return AlertsLabel.length;
        }

        @Override
        public Object getItem(int position) {
            return AlertsLabel[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View alert_view = convertView;

            if (alert_view == null) {
                alert_view = inflater.inflate(R.layout.summary_status_item, parent, false);
            }
            //Setting Alerts Here
            TextView tv_total_nodes_count = (TextView) alert_view.findViewById(R.id.tv_total_nodes_count);
            ImageView iv_alert_icon = (ImageView) alert_view.findViewById(R.id.iv_alert_icon);
            LinearLayout ll_bg_color = (LinearLayout) alert_view.findViewById(R.id.ll_bg_color);
            TextView tv_alert_title = (TextView) alert_view.findViewById(R.id.tv_alert_title);
            int alertsCount;
            switch (position) {
                case 0 :
                    alertsCount = AlertManager.getAlertsCount("All", AlertData.NodeState.Alert);
                    tv_total_nodes_count.setText(Integer.toString(alertsCount));
                    iv_alert_icon.setBackgroundResource(R.drawable.alert);
                    ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_alert));
                    tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_alert));
                    break;

                case 1:
                    alertsCount = AlertManager.getAlertsCount("All", AlertData.NodeState.Warning);
                    tv_total_nodes_count.setText(Integer.toString(alertsCount));
                    iv_alert_icon.setBackgroundResource(R.drawable.warning);
                    ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_warning));
                    tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
                    break;

                case 2:
                    alertsCount = AlertManager.getAlertsCount("All", AlertData.NodeState.Normal);
                    tv_total_nodes_count.setText(Integer.toString(alertsCount));
                    iv_alert_icon.setBackgroundResource(R.drawable.ok_icon);
                    ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_normal));
                    tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_normal));
                    break;

                case 3:
/*                    alertsCount = AlertManager.getAlertsCount("All", AlertData.AlertStatus.Alert);*/
                    tv_total_nodes_count.setText(Integer.toString(0));

                    iv_alert_icon.setBackgroundResource(R.drawable.defrost_icon_white);
                    ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_defrost));
                    tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_defrost));
                    break;

                case 4:
                    alertsCount = AlertManager.getAlertsCount("All", AlertData.NodeState.Offline);
                    tv_total_nodes_count.setText(Integer.toString(alertsCount));
                    iv_alert_icon.setBackgroundResource(R.drawable.offline_icon_white);
                    ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_offline));
                    tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_offline));
                    break;

                case 5:
/*                    alertsCount = AlertManager.getAlertsCount("All", AlertData.AlertStatus.Alert);*/
                    tv_total_nodes_count.setText(Integer.toString(0));
                    iv_alert_icon.setBackgroundResource(R.drawable.offline_icon_white);
                    ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_dark_grey));
                    tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_dark_grey));
                    break;

                default:
                    break;
            }

            if(position != 3 && position != 5) {

                ll_bg_color.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(ActivityDashboard.this, AssetsActivity.class);
                        intent.putExtra("Filter", position + 1);
                        intent.putExtra("ShowOne", true);
                        startActivity(intent);
                    }
                });
            }

            tv_alert_title.setText((int)getItem(position));

            return alert_view;
        }


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
                Log.d(TAG,"Location Permission request");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            //done
        }
    }
}
