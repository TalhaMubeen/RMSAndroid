package com.innv.rmsgateway.graph;

import com.innv.rmsgateway.activity.FullscreenActivity;
import com.jjoe64.graphview.GraphView;

public abstract class GraphBase {
    public abstract void onCreate(FullscreenActivity activity, String mac);
    public abstract void initGraph(GraphView graph, String mac);

    public void onPause() {}
    public void onResume() {}
}
