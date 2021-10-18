package com.innv.rmsgateway.classes;

import java.io.Serializable;

public enum AlertTypes implements Serializable {
    LOW_TEMP,
    HIGH_TEMP,
    LOW_HUMIDITY,
    HIGH_HUMIDITY,
    RSSI,
}
