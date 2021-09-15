package com.innv.rmsgateway.callback;

import com.innv.rmsgateway.data.BleDevice;

public abstract class  BleScanAndConnectCallback  extends BleGattCallback implements BleScanPresenterImp{


    public abstract void onScanFinished(BleDevice scanResult);

    public void onLeScan(BleDevice bleDevice) {
    }

}
