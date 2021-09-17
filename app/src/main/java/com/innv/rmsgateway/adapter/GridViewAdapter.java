package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.se.omapi.Session;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    List<StaticListItem> mRMSDevices= new ArrayList<>();
    LayoutInflater inflater;
    public GridViewAdapter(Context ctx, List<StaticListItem> list){
        mRMSDevices = list;
        inflater = LayoutInflater.from(ctx);
    }


    @Override
    public int getCount() {
        return mRMSDevices.size();
    }

    @Override
    public SensorNode getItem(int position) {
        SensorNode node = new SensorNode();
        if(node.parseListItem(mRMSDevices.get(position))){
            return node;
        }
        return  new SensorNode();
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

        TextView sensor_name = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_name);
        sensor_name.setText(item.getName());

        TextView sensor_bd_address = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_bd_address);
        sensor_bd_address.setText(item.getMacID());

        TextView temperature_value = (TextView) rmsDeviceCardView.findViewById(R.id.temperature_value);
        temperature_value.setText(Double.toString(item.getTemperature()));

        TextView humidity_value = (TextView) rmsDeviceCardView.findViewById(R.id.humidity_value);
        humidity_value.setText(Integer.toString(item.getHumidity()) + "%");

        TextView sensor_rssi = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_rssi);

        sensor_rssi.setText("-99.6 dbm");
        //sensor_rssi.setText(item.get());

        return rmsDeviceCardView;
    }
}
