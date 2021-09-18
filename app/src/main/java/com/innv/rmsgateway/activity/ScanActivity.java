package com.innv.rmsgateway.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.innv.rmsgateway.BleManager;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.adapter.SensorNodeAdapter;
import com.innv.rmsgateway.callback.BleGattCallback;
import com.innv.rmsgateway.callback.BleMtuChangedCallback;
import com.innv.rmsgateway.callback.BleRssiCallback;
import com.innv.rmsgateway.callback.BleScanCallback;
import com.innv.rmsgateway.comm.ObserverManager;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.exception.BleException;
import com.innv.rmsgateway.operation.OperationActivity;
import com.innv.rmsgateway.scan.BleScanRuleConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = ScanActivity.class.getSimpleName();

    private ImageView iv_refresh;
    private SensorNodeAdapter mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_nodes);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectedDevice();
    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
   }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_refresh:
                if (iv_refresh.getAnimation() != null) {
                    if (!iv_refresh.getAnimation().hasEnded()) {
                       /* BleManager.getInstance().cancelScan();*/
                        iv_refresh.clearAnimation();
                    }
                }else {//start animation here
                    Animation rotationAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                    iv_refresh.startAnimation(rotationAnimation);
                }
                break;

        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        iv_refresh = (ImageView) findViewById(R.id.iv_refresh);
        iv_refresh.setOnClickListener(this);

        mDeviceAdapter = new SensorNodeAdapter(this);

        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);
    }

    private void showConnectedDevice() {
      /*  List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();*/
    }
}
