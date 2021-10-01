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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.activity.AddNodesActivity;
import com.innv.rmsgateway.activity.GraphViewActivity;
import com.innv.rmsgateway.adapter.TextViewTimeCounter;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.Globals;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorDataDecoder;
import com.innv.rmsgateway.sensornode.SensorNode;
import com.innv.rmsgateway.service.BLEBackgroundService;
import com.innv.rmsgateway.service.OnBLEDeviceCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnBLEDeviceCallback {
    private static final String TAG = "sensorScanner";
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_CODE_PERMISSION_BLUETOOTH = 3;


    public static int NODE_ACTIVE = Color.parseColor("#FF00CC00");
    public static int NODE_INACTIVE =Color.parseColor("#FFFF0000");

    private static boolean FirstRun = true;

    private ImageView ivAddDevices;
    private ListView gvDevices;

    // A reference to the service used to get BLE Updates
    @SuppressLint("StaticFieldLeak")
    private static BLEBackgroundService mService = null;
    // Tracks the bound state of the service.
    private static boolean mBound = false;
    GridViewAdapter gv_adapter;
    Map<String, SensorNode> precheckedNodes;


    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mService == null) {
                BLEBackgroundService.LocalBinder binder = (BLEBackgroundService.LocalBinder) service;
                mService = binder.getService();
                checkPermissions();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(TAG,"Gateway App started");
        checkPermissions();
        Globals.setDbContext(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ivAddDevices = (ImageView) findViewById(R.id.iv_add);
        ivAddDevices.setOnClickListener(this);

        NodeDataManager.init();


        if (!mBound) {
            bindService(new Intent(MainActivity.this, BLEBackgroundService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBound) {
            bindService(new Intent(MainActivity.this, BLEBackgroundService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }

        BLEBackgroundService.addBLEUpdateListener(this.getClass().getSimpleName(), this);
        UpdatePreCheckedNodes();

        List<SensorNode> list = NodeDataManager.getPreCheckedNodes();
        gv_adapter = new GridViewAdapter(this, list);
        gvDevices = (ListView) findViewById(R.id.gv_devices);
        gvDevices.setAdapter(gv_adapter);

    }

    public void UpdatePreCheckedNodes(){
        List<SensorNode> list = NodeDataManager.getPreCheckedNodes();
        precheckedNodes = new HashMap<>();
        for(SensorNode node : list){
            precheckedNodes.put(node.getMacID(), node);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
/*        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
           // BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
            unbindService(mServiceConnection);
            mBound = false;
        }*/
    }

    @Override
    protected void onPause(){
        super.onPause();
/*        thread.interrupt();
        updatingTime = false;*/
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
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                // do something here
                //gps.unbindService();
                if (mBound) {
                    // Unbind from the service. This signals to the service that this activity is no longer
                    // in the foreground, and the service can respond by promoting itself to a foreground
                    // service.
                   // BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
                    //unbindService(mServiceConnection);
                   // mBound = false;
                }
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_add) {
            Intent intent = new Intent(this, AddNodesActivity.class);
            startActivity(intent);
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
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
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            //done
        }
    }

    @Override
    public void onBLEDeviceCallback(BleDevice device) {
        gv_adapter.UpdateListData(NodeDataManager.getPreCheckedNodes());
        gv_adapter.notifyDataSetChanged();
    }


    class GridViewAdapter extends BaseAdapter {

        List<SensorNode> mRMSDevices= new ArrayList<>();
        LayoutInflater inflater;
        Context context;

        public GridViewAdapter(Context ctx, List<SensorNode> list){
            mRMSDevices = list;
            inflater = LayoutInflater.from(ctx);
            context = ctx;
        }

        public void UpdateListData(List<SensorNode> list){
            mRMSDevices = list;
        }


        @Override
        public int getCount() {
            return mRMSDevices.size();
        }

        @Override
        public SensorNode getItem(int position) {
            return mRMSDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mRMSDevices.get(position).hashCode();
        }


        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rmsDeviceCardView = convertView;
            SensorNode item = getItem(position);
            if (rmsDeviceCardView == null) {
                rmsDeviceCardView = inflater.inflate(R.layout.gridview_monitoring_item, parent, false);
            }

            CardView sensor_card_view = (CardView) rmsDeviceCardView.findViewById(R.id.sensor_card_view);
            sensor_card_view.setOnClickListener((View v) -> {
                Intent intent = new Intent(context, GraphViewActivity.class);
                intent.putExtra("MAC", item.getMacID());
                context.startActivity(intent);
            });

            TextView sensor_name = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_name);
            sensor_name.setText(item.getName());

            TextView sensor_bd_address = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_bd_address);
            sensor_bd_address.setText(item.getMacID());

            TextView temperature_value = (TextView) rmsDeviceCardView.findViewById(R.id.temperature_value);
            temperature_value.setText(Double.toString(item.getTemperature()));

            TextView humidity_value = (TextView) rmsDeviceCardView.findViewById(R.id.humidity_value);
            humidity_value.setText(Integer.toString(item.getHumidity()) + "%");

            TextView sensor_rssi = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_rssi);
            sensor_rssi.setText(Double.toString(item.getRssi()) + " dbm");

            View color = (View) rmsDeviceCardView.findViewById(R.id.colorNA);


            TextViewTimeCounter tv_last_updated = (TextViewTimeCounter) rmsDeviceCardView.findViewById(R.id.tv_last_updated);

            if(tv_last_updated.isTimerRunning()){
                tv_last_updated.updateStartTime(item);
            }else {
                if (item.getLastUpdatedOn() != null) {
                    tv_last_updated.startTimer(item, color, 1000, "", " ago");
                }
            }




            return rmsDeviceCardView;
        }


    }



}