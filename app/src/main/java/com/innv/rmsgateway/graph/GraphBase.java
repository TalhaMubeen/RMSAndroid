package com.innv.rmsgateway.graph;

import android.content.Context;

import com.innv.rmsgateway.activity.FullscreenActivity;
import com.jjoe64.graphview.GraphView;

import java.util.Date;
import java.util.TreeMap;

public abstract class GraphBase {
    public abstract void onCreate(FullscreenActivity activity, TreeMap<Date, Double> data, Context ctx);
    public abstract void initTemperatureGraph(GraphView graph, TreeMap<Date, Double> data, Context ctx);

    public abstract void onCreateHumid(FullscreenActivity activity, TreeMap<Date, Integer> data, Context ctx);
    public abstract void initHumidityGraph(GraphView graph, TreeMap<Date, Integer> data, Context ctx);

    public void onPause() {}
    public void onResume() {}
}
