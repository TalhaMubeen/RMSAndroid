package com.innv.rmsgateway.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.adapter.SensorNodeAdapter;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;
import com.innv.rmsgateway.service.BLEBackgroundService;
import com.innv.rmsgateway.service.OnBLEDeviceCallback;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener, OnBLEDeviceCallback{

    private static final String TAG = ScanActivity.class.getSimpleName();

    private ImageView iv_refresh;
    private SensorNodeAdapter mDeviceAdapter;
    List<SensorNode> allSavedNodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scan_nodes);

        getSupportActionBar().hide();
      //  getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BLEBackgroundService.addBLEUpdateListener(this.getClass().getSimpleName(), this);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_refresh:
                if (iv_refresh.getAnimation() != null) {
                    if (!iv_refresh.getAnimation().hasEnded()) {
                        //BLEBackgroundService.stopScan();
                        iv_refresh.clearAnimation();

                    }
                }else {//start animation here
                    mDeviceAdapter.clear();
                    mDeviceAdapter.notifyDataSetChanged();
                    Animation rotationAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                    iv_refresh.startAnimation(rotationAnimation);
                }
                break;

            case R.id.iv_scanqr:
                try {

                    Intent intent = new Intent(ScanActivity.this, QrCodeScanner.class);
                    startActivityForResult(intent, 0);

                } catch (Exception e) {
                    Toast.makeText(this, " Failed to start QR camera ", Toast.LENGTH_SHORT).show();
                }

                break;

        }
    }

    private void initView() {

        iv_refresh = (ImageView) findViewById(R.id.iv_refresh);
        iv_refresh.setOnClickListener(this);

        mDeviceAdapter = new SensorNodeAdapter(this);
        mDeviceAdapter.addDevices(BLEBackgroundService.getScannedBLEDeviceList());

        GridView listView_device = (GridView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);

        Animation rotationAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        iv_refresh.startAnimation(rotationAnimation);

        ImageView iv_scanqr = (ImageView) findViewById(R.id.iv_scanqr);
        iv_scanqr.setOnClickListener(this);

    }

    public void addDevice(BleDevice device){

        allSavedNodes = NodeDataManager.getAllNodesLst();
        for(SensorNode node : allSavedNodes){
            if(device.getMac().equals(node.getMacID())){
                return;
            }
        }

        if(mDeviceAdapter.addDevice(device.getMac())) {
            mDeviceAdapter.notifyDataSetChanged();
        }
    }

    private void addDevice(String mac){
        allSavedNodes = NodeDataManager.getAllNodesLst();
        for(SensorNode node : allSavedNodes){
            if(mac.equals(node.getMacID())){
                Toast.makeText(this,"Node " + mac + " is already added", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(mDeviceAdapter.addDevice(mac)) {
            mDeviceAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onBLEDeviceCallback(BleDevice device) {
        addDevice(device);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                long count = contents.chars().filter(ch -> ch == ':').count();
                if(count == 5 && contents.length() == 17){ //This is a mac address
                    addDevice(contents);
                }else{
                    Toast.makeText(this, contents + " is not a valid MAC Address", Toast.LENGTH_SHORT).show();
                }
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }

    }
}
