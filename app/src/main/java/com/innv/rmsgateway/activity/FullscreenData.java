package com.innv.rmsgateway.activity;

import androidx.annotation.LayoutRes;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.graph.GraphBase;
import com.innv.rmsgateway.graph.RealtimeHumidity;
import com.innv.rmsgateway.graph.RealtimeTemperature;

public enum FullscreenData {

    TEMPERATURE_SCROLLING(R.layout.fullscreen, RealtimeTemperature .class),
    HUMIDITY_SCROLLING(R.layout.fullscreen, RealtimeHumidity .class);

    public static final String ARG_ID = "FEID";
    public static final String NODE_MAC = "MAC";

    public final @LayoutRes
    int contentView;
    public final Class<? extends GraphBase> callingClass;


    FullscreenData(@LayoutRes int contentView, Class<? extends GraphBase> callingClass) {
        this.contentView = contentView;
        this.callingClass = callingClass;
    }
}
