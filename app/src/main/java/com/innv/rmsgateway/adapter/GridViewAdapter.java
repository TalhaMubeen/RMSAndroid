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

import com.innv.rmsgateway.MainActivity;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.Globals;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;

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
            item.setLastUpdatedOn(new Date());

            final int[] elapsedTime = item.elapsedCalculator(new Date(), lastUpdate);

            String hours = elapsedTime[1] + "h";
            String mins = elapsedTime[2] + "m";
            String sec = elapsedTime[3] + "s";

            if (elapsedTime[2] >= 6 || elapsedTime[1] > 0) {
                View color = (View) rmsDeviceCardView.findViewById(R.id.colorNA);
                color.setBackgroundTintList(ColorStateList.valueOf(NODE_INACTIVE));
            } else {
                View color = (View) rmsDeviceCardView.findViewById(R.id.colorNA);
                color.setBackgroundTintList(ColorStateList.valueOf(NODE_ACTIVE));
            }

            last_Updated_On.setText(hours + " "  + mins + " " + sec + " ago");
        }
        deviceViewList.put(item.getMacID(), rmsDeviceCardView);
        return rmsDeviceCardView;
    }


/*
    public void UpdateTime() {

        if(updatingTime)
            return;

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    updatingTime = true;
                    while (true) {

                        context.runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {

                                              if (deviceViewList.size() > 0) {
                                                  mRMSDevices = NodeDataManager.getPreCheckedNodes();
                                                  for (SensorNode node : mRMSDevices) {
                                                      if (deviceViewList.containsKey(node.getMacID())) {
                                                          TextView last_Updated_On = (TextView) deviceViewList.get(node.getMacID()).findViewById(R.id.tv_last_updated);

                                                          Date lastUpdate = node.getLastUpdatedDate();
                                                          if (lastUpdate != null) {

                                                              final int[] elapsedTime = node.elapsedCalculator(new Date(), lastUpdate);

                                                              String hours = elapsedTime[1] + "h";
                                                              String mins = elapsedTime[2] + "m";
                                                              String sec = elapsedTime[3] + "s";

                                                              if (elapsedTime[2] >= 6 || elapsedTime[1] > 0) {
                                                                  View color = (View) deviceViewList.get(node.getMacID()).findViewById(R.id.colorNA);
                                                                  color.setBackgroundTintList(ColorStateList.valueOf(NODE_INACTIVE));
                                                              } else {
                                                                  View color = (View) deviceViewList.get(node.getMacID()).findViewById(R.id.colorNA);
                                                                  color.setBackgroundTintList(ColorStateList.valueOf(NODE_ACTIVE));
                                                              }

                                                              last_Updated_On.setText(hours + " " + mins + " " + sec + " ago");
                                                          }
                                                      }
                                                  }
                                              }
                                          }
                                      });
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }


*/


    @SuppressLint("SetTextI18n")
    public void updateValues(String Mac, double temp, int humidity, int rssi){


        if(deviceViewList.size() >0 && deviceViewList.containsKey(Mac)) {

            SensorNode node = getNode(Mac);
            if(node == null){
                return;
            }

            node.setLastUpdatedOn(new Date());

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
            NodeDataManager.LogSensorNodeData(node);

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
