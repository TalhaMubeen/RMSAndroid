package com.innv.rmsgateway.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.innv.rmsgateway.BleManager;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.callback.BleScanCallback;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.scan.BleScanRuleConfig;
import com.innv.rmsgateway.sensornode.SensorDataDecoder;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BLEBackgroundService extends Service {

    private static final String TAG = BLEBackgroundService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();

    private Handler mServiceHandler;
    private Context mContext = null;

    private static Map<String, BleDevice> _scannedList;
    private static SensorDataDecoder sensorDataDecoder;
    private static Boolean isScanning = false;
    private static List<SensorNode> allSavedNodes = NodeDataManager.getAllNodesLst();
    static Thread thread = null;
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }


    public BLEBackgroundService() {
    }

    @Override
    public void onCreate() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mContext = this;
        sensorDataDecoder = new SensorDataDecoder();
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setOperateTimeout(50000);

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(null)      // Only scan the equipment of the specified service, optional
                .setDeviceName(true, "")   // Only scan devices with specified broadcast name, optional
                .setDeviceMac("")                  // Only scan devices of specified mac, optional
                .setAutoConnect(false)      // AutoConnect parameter when connecting, optional, default false
                .setScanTimeOut(1000 * 60 * 3)              // Scan timeout time, optional, 3-min
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

    }

    private static Map<String,OnBLEDeviceCallback> onBLEUpdateCallbacks = new HashMap<>();

    public static void addBLEUpdateListener(@NonNull String className, @NonNull OnBLEDeviceCallback listener) {
        if (onBLEUpdateCallbacks == null) {
            onBLEUpdateCallbacks = new HashMap<>();
        }

        onBLEUpdateCallbacks.put(className, listener);
    }

    public static void removeBLEUpdateListener(@NonNull String className) {
        if (onBLEUpdateCallbacks == null) {
            return;
        }

        onBLEUpdateCallbacks.remove(className);
    }


    public static void stopScan(){
        BleManager.getInstance().cancelScan();
        thread.interrupt();
    }

    public void startBleService() {
        startService(new Intent(getApplicationContext(), BLEBackgroundService.class));

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (!isScanning) {
                            scanBLE();
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }


    public static void restartBLEScan(){
        try {
            if(isScanning) {
                stopScan();
            }
            scanBLE();
        }catch(Exception ignored){

        }
    }

    private static void scanBLE(){
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                _scannedList = new HashMap<>();
                Log.i(TAG, "Started");
                if(success) {
                    isScanning = true;
                }
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if(sensorDataDecoder.nodeValid(bleDevice)) { //Checking if the scanned node is really RMS node

                    for (String name : onBLEUpdateCallbacks.keySet()) {
                        onBLEUpdateCallbacks.get(name).onBLEDeviceCallback(bleDevice);
                    }

                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                isScanning = false;

            }
        });
    }

    public static List<BleDevice> getScannedBLEDeviceList(){
        return _scannedList != null? new ArrayList<BleDevice>(_scannedList.values()) : new ArrayList<>();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public class LocalBinder extends Binder {
        public BLEBackgroundService getService() {
            return BLEBackgroundService.this;
        }
    }

}
