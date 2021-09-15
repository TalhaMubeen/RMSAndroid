package com.innv.rmsgateway.comm;

import com.innv.rmsgateway.data.BleDevice;

public interface Observer {

    void disConnected(BleDevice bleDevice);
}
