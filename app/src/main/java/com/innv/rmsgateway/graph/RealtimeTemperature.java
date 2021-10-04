package com.innv.rmsgateway.graph;

import android.os.Handler;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.FullscreenActivity;
import com.innv.rmsgateway.data.NodeDataManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class RealtimeTemperature extends GraphBase{

    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private double graphLastXValue = 5d;
    private LineGraphSeries<DataPoint> mSeries;

    @Override
    public void onCreate(FullscreenActivity activity, String mac) {
        GraphView graph = (GraphView) activity.findViewById(R.id.graph);
        initGraph(graph, mac);
    }

    @Override
    public void initGraph(GraphView graph, String mac) {

        TreeMap<Date, Double> tempData = NodeDataManager.getTemerature(mac);
        mSeries = new LineGraphSeries<>();
        graph.getViewport().setXAxisBoundsManual(true);

        if(tempData.size() > 0) {
            graph.getViewport().setMinX(tempData.firstEntry().getKey().getTime());
            graph.getViewport().setMaxX(tempData.lastEntry().getKey().getTime());

            graph.getGridLabelRenderer().setLabelVerticalWidth(100);

            // first mSeries is a line

            mSeries.setDrawDataPoints(true);
            mSeries.setDrawBackground(true);

            for (Date key : tempData.keySet()) {
                try {
                    mSeries.appendData(new DataPoint(key, tempData.get(key)), true, 1000);
                } catch (Exception e) {

                }
            }
        }


        graph.addSeries(mSeries);
    }

    public void UpdateTemp(){

    }

    public void onResume() {
/*        mTimer = new Runnable() {
            @Override
            public void run() {
                graphLastXValue += 0.25d;
                mSeries.appendData(new DataPoint(graphLastXValue, getRandom()), true, 24);
                mHandler.postDelayed(this, 330);
            }
        };
        mHandler.postDelayed(mTimer, 1500);*/
    }

    public void onPause() {
        if(mTimer != null){
            mHandler.removeCallbacks(mTimer);
        }
    }

    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }
}
