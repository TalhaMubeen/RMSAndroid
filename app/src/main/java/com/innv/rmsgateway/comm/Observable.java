package com.innv.rmsgateway.comm;

import com.innv.rmsgateway.data.BleDevice;

public interface Observable {


    void addObserver(Observer obj);

    void deleteObserver(Observer obj);

    void notifyObserver(BleDevice bleDevice);

}
