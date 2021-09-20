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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.innv.rmsgateway.BleManager;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.callback.BleScanCallback;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.scan.BleScanRuleConfig;

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

    private List<BleDevice> _scannedList;
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


    public void StopScan(){
        BleManager.getInstance().cancelScan();
        BleManager.getInstance().destroy();
    }

    public void startBleService() {
        startService(new Intent(getApplicationContext(), BLEBackgroundService.class));
        scanBLE();
    }

    private void scanBLE(){
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                _scannedList = new ArrayList<>();

                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                String name = cn.getShortClassName();
                name = name.replace(".", "");

                if (onBLEUpdateCallbacks.containsKey(name)) {
                    Objects.requireNonNull(onBLEUpdateCallbacks.get(name)).onReadyScanCallback(success);
                }

                    //send scan started callback event
/*
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
*/

  /*              Animation rotationAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                iv_refresh.startAnimation(rotationAnimation);
 */           }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if(!_scannedList.contains(bleDevice)) {
                    _scannedList.add(bleDevice);

                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    String name = cn.getShortClassName();
                    name = name.replace(".", "");

                    if (onBLEUpdateCallbacks.containsKey(name)) {
                        Objects.requireNonNull(onBLEUpdateCallbacks.get(name)).onBLEDeviceCallback(bleDevice);
                    }
                }

                //send device to callback

/*                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();*/
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                if(scanResultList.size() != _scannedList.size() && scanResultList.size() > 0 ){
                    for(BleDevice device : scanResultList){
                        if(!_scannedList.contains(device)){
                            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                            String name = cn.getShortClassName();
                            name = name.replace(".", "");

                            if (onBLEUpdateCallbacks.containsKey(name)) {
                                Objects.requireNonNull(onBLEUpdateCallbacks.get(name)).onBLEDeviceCallback(device);
                            }
                        }
                    }
                }

                //scanBLE();

            }
        });
    }

    public List<BleDevice> getScannedBLEDeviceList(){
        return _scannedList != null? _scannedList : new ArrayList<>();
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
