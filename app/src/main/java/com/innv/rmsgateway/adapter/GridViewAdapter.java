/*
package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.FullscreenActivity;
import com.innv.rmsgateway.activity.FullscreenData;
import com.innv.rmsgateway.activity.GraphViewActivity;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridViewAdapter extends BaseAdapter {


    public static int COLOR_TEST_NOT_ACTIVE= Color.parseColor("darkgray");
    public static int NODE_ACTIVE = Color.parseColor("#FF00CC00");
    public static int NODE_INACTIVE =Color.parseColor("#FFFF0000");


    List<SensorNode> mRMSDevices= new ArrayList<>();
    private static Map<String, View> deviceViewList = new HashMap();
    LayoutInflater inflater;
    Context context;

    static Thread thread = null;
    public GridViewAdapter(Context ctx, List<SensorNode> list){
        mRMSDevices = list;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
        deviceViewList.clear();

    }

    public Map<String, View> getCardViewList(){
        return deviceViewList;
    }


    @Override
    public int getCount() {
        return mRMSDevices.size();
    }

    @Override
    public SensorNode getItem(int position) {
        return mRMSDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mRMSDevices.get(position).hashCode();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rmsDeviceCardView = convertView;
        SensorNode item = getItem(position);
        if(rmsDeviceCardView == null) {
            rmsDeviceCardView = inflater.inflate(R.layout.gridview_monitoring_item, parent, false);
        }

        RelativeLayout sensor_card_view = (RelativeLayout) rmsDeviceCardView.findViewById(R.id.sensor_card_view);
        sensor_card_view.setOnClickListener((View v) -> {
            Intent intent = new Intent(context, GraphViewActivity.class);

            context.startActivity(intent);
        });

        TextView sensor_name = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_name);
        sensor_name.setText(item.getName());

        TextView sensor_bd_address = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_bd_address);
        sensor_bd_address.setText(item.getMacID());

        TextView temperature_value = (TextView) rmsDeviceCardView.findViewById(R.id.temperature_value);
        temperature_value.setText(Double.toString(item.getTemperature()));

        TextView humidity_value = (TextView) rmsDeviceCardView.findViewById(R.id.humidity_value);
        humidity_value.setText(Integer.toString(item.getHumidity()) + "%");

        TextView sensor_rssi = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_rssi);
        sensor_rssi.setText("0.0 dbm");

        TextView last_Updated_On = (TextView) rmsDeviceCardView.findViewById(R.id.tv_last_updated);

        Date lastUpdate = item.getLastUpdatedDate();
        if(lastUpdate != null) {
            final int[] elapsedTime = item.elapsedCalculator(new Date(), lastUpdate);

            String days = elapsedTime[0] + "d";
            String hours = elapsedTime[1] + "h";
            String mins = elapsedTime[2] + "m";
            String sec = elapsedTime[3] + "s";

            if (elapsedTime[2] >= 6 || elapsedTime[1] > 0 || elapsedTime[0] > 0) {
                View color = (View) rmsDeviceCardView.findViewById(R.id.colorNA);
                color.setBackgroundTintList(ColorStateList.valueOf(NODE_INACTIVE));
            } else {
                View color = (View) rmsDeviceCardView.findViewById(R.id.colorNA);
                color.setBackgroundTintList(ColorStateList.valueOf(NODE_ACTIVE));
            }

            if (elapsedTime[0] > 0) {
                last_Updated_On.setText(days + " ago");
            } else {
                last_Updated_On.setText(hours + " " + mins + " " + sec + " ago");
            }
        }
        deviceViewList.put(item.getMacID(), rmsDeviceCardView);
        return rmsDeviceCardView;
    }


}
*/
