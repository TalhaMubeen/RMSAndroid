package com.innv.rmsgateway.service;

import android.location.Location;

import com.innv.rmsgateway.data.BleDevice;

public interface OnBLEDeviceCallback {
    void onBLEDeviceCallback(BleDevice device);
    void onReadyScanCallback(Boolean isReady);
}
