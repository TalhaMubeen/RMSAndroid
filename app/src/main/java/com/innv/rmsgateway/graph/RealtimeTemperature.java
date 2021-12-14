package com.innv.rmsgateway.graph;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.FullscreenActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TreeMap;

public class RealtimeTemperature extends GraphBase {

    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private double graphLastXValue = 5d;
    private LineGraphSeries<DataPoint> mSeries;

    public TreeMap<Date, Double> getData() {
        return data;
    }

    private transient TreeMap<Date, Double> data;

    @Override
    public void onCreate(FullscreenActivity activity, TreeMap<Date, Double> tempData,  Context ctx) {
        GraphView graph = (GraphView) activity.findViewById(R.id.graph);
        generateGraphs(graph, tempData, ctx, 10);
    }


    @Override
    public void initHumidityGraph(GraphView graph, TreeMap<Date, Integer> data, Context ctx) {

    }

    @Override
    public void initTemperatureGraph(GraphView graph, TreeMap<Date, Double> tempData, Context ctx) {
        generateGraphs(graph, tempData, ctx, 7);
    }


    private void generateGraphs(GraphView graph, TreeMap<Date, Double> tempData, Context ctx, int labelCount){
        data = tempData;
        graph.removeAllSeries();

        graph.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graph.getViewport().setScrollable(true);  // activate horizontal scrolling
        graph.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScrollableY(true);  // activate vertical scrolling

        mSeries = new LineGraphSeries<>();


        if(tempData.size() > 0) {
            graph.getViewport().setMinX(tempData.firstEntry().getKey().getTime());
            graph.getViewport().setMaxX(tempData.lastEntry().getKey().getTime());


            mSeries.setDrawAsPath(true);
            mSeries.setDrawDataPoints(true);
          //  mSeries.setDrawBackground(true);

            for (Date key : tempData.keySet()) {
                try {
                    mSeries.appendData(new DataPoint(key, tempData.get(key)), false, tempData.size(), true);
                } catch (Exception e) {

                }
            }
        }


        graph.addSeries(mSeries);

        mSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series2, DataPointInterface dataPoint) {
                Date date = new Date((long)dataPoint.getX());
                SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                String strDate = formatter.format(date);
                Toast.makeText(ctx,  strDate  +" : " + dataPoint.getY(), Toast.LENGTH_SHORT).show();
            }
        });

        DateFormat dFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(150);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(ctx, dFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(labelCount);
        graph.getViewport().setXAxisBoundsManual(true);
    }

    @Override
    public void onCreateHumid(FullscreenActivity activity, TreeMap<Date, Integer> data, Context ctx) {

    }

    public void UpdateTemp(){
    }

    public void onResume() {
    }

    public void onPause() {
    }
}
