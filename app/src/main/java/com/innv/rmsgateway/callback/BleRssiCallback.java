package com.innv.rmsgateway.callback;

import com.innv.rmsgateway.exception.BleException;

public abstract class BleRssiCallback extends BleBaseCallback{

    public abstract void onRssiFailure(BleException exception);

    public abstract void onRssiSuccess(int rssi);
}
