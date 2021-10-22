package com.innv.rmsgateway.classes;

import java.io.Serializable;

public enum NodeState implements Serializable {
    Alert,
    Warning,
    Normal,
    Defrost,
    Offline,
    ComFailure,
}

