package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.Globals;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;

public class GridViewAdapter extends BaseAdapter {


    public static int COLOR_TEST_NOT_ACTIVE= Color.parseColor("darkgray");
    public static int NODE_ACTIVE = Color.parseColor("#FF00CC00");
    public static int NODE_INACTIVE =Color.parseColor("#FFFF0000");

    List<SensorNode> mRMSDevices= new ArrayList<>();
    Map<String, View> deviceViewList = new HashMap();
    LayoutInflater inflater;
    Context context;
    public GridViewAdapter(Context ctx, List<SensorNode> list){
        mRMSDevices = list;
/*        for(StaticListItem item : list){
            SensorNode node = new SensorNode();
            node.parseListItem(item);
            devivesList.put(node.getMacID(), item);
        }*/
        inflater = LayoutInflater.from(ctx);
        deviceViewList.clear();
        context = ctx;
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

        deviceViewList.put(item.getMacID(), rmsDeviceCardView);
        return rmsDeviceCardView;
    }

    @SuppressLint("SetTextI18n")
    public void updateValues(String Mac, double temp, int humidity, int rssi){

        if(deviceViewList.size() >0 && deviceViewList.containsKey(Mac)) {

            SensorNode node = getNode(Mac);
            if(node == null){
                return;
            }

            temp = Math.floor(temp * 100 +.5)/100;
            TextView temperature_value = (TextView) deviceViewList.get(Mac).findViewById(R.id.temperature_value);
            temperature_value.setText(Double.toString(temp) +  "°C");
            node.setTemperature(temp);

            TextView humidity_value = (TextView) deviceViewList.get(Mac).findViewById(R.id.humidity_value);
            humidity_value.setText(Integer.toString(humidity) + "%");
            node.setHumidity(humidity);

            TextView sensor_rssi = (TextView) deviceViewList.get(Mac).findViewById(R.id.sensor_rssi);
            sensor_rssi.setText(Integer.toString(rssi) + " dbm");
            node.setRssi(rssi);

            View color = (View) deviceViewList.get(Mac).findViewById(R.id.colorNA);
            color.setBackgroundTintList (ColorStateList.valueOf(NODE_ACTIVE));


            NodeDataManager.SaveSensorNodeData(node);

        }
    }



    private SensorNode getNode(String mac){
        for(SensorNode node : mRMSDevices){
            if(node.getMacID().equals(mac)){
                return node;
            }
        }
        return null;
    }





}
