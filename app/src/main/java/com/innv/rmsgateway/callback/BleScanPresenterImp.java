package com.innv.rmsgateway.callback;

import com.innv.rmsgateway.data.BleDevice;

public interface BleScanPresenterImp {

    void onScanStarted(boolean success);

    void onScanning(BleDevice bleDevice);
}
