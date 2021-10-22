package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.NodeState;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.sensornode.SensorDataDecoder;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class AssetsViewAdapter extends BaseAdapter {

    List<SensorNode> mRMSDevices= new ArrayList<>();
    LayoutInflater inflater;
    Context context;

    public AssetsViewAdapter(Context ctx, List<SensorNode> list){
        mRMSDevices = list;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
    }

    public void UpdateListData(List<SensorNode> list){
        mRMSDevices.clear();
        mRMSDevices = list;
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
        Profile itemProf = item.getProfile();

        if (rmsDeviceCardView == null) {
            rmsDeviceCardView = inflater.inflate(R.layout.gridview_monitoring_item_new, parent, false);
        }

        TextView sensor_name = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_name);
        sensor_name.setText(item.getName());

        TextView sensor_subTitle = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_subTitle);
        sensor_subTitle.setText(item.getProfile().getTitle());

        TextView temperature_value = (TextView) rmsDeviceCardView.findViewById(R.id.temperature_value);
        temperature_value.setText(Double.toString(SensorDataDecoder.round(item.getTemperature(), 1)) /*+ "°C"*/ );


        if(item.getTemperature() > itemProf.getHighTempThreshold()){
            temperature_value.setTextColor(Globals.ALERT);
        }else if(item.getTemperature() < itemProf.getLowTempThreshold()){
            temperature_value.setTextColor(Globals.BELOW_THRESHOLD);
        }

        TextView humidity_value = (TextView) rmsDeviceCardView.findViewById(R.id.humidity_value);
        humidity_value.setText(Integer.toString(item.getHumidity()) + "%");

        if(item.getHumidity() > itemProf.getHighHumidityThreshold()){
            humidity_value.setTextColor(Globals.ALERT);
        }else if(item.getHumidity() < itemProf.getLowHumidityThreshold()){
            humidity_value.setTextColor(Globals.BELOW_THRESHOLD);
        }

        TextView sensor_rssi = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_rssi);
        sensor_rssi.setText(Double.toString(SensorDataDecoder.round(item.getRssi(), 1))/* + " dbm"*/);

        int alertsCount = AlertManager.getAlertsCount(item.getMacID(), NodeState.Alert);

        TextView tv_alerts = (TextView) rmsDeviceCardView.findViewById(R.id.tv_alerts);
        if(alertsCount > 0){
            tv_alerts.setText(Integer.toString(alertsCount));
            tv_alerts.setTextColor(Globals.ALERT);
        }

        View colorView = (View) rmsDeviceCardView.findViewById(R.id.colorNA);

        int color = Globals.NORMAL;

        List<AlertData> alerts = AlertManager.getAlertList(item.getMacID());
        for (AlertData alert : alerts) {

            switch (alert.getNodeState()){
                case Alert:
                    color = R.color.color_alert;
                    break;

                case Normal:
                    color = R.color.color_normal;
                    break;

                case Defrost:
                    color = R.color.color_defrost;
                    break;

                case Warning:
                    color = R.color.color_warning;
                    break;

                case Offline:
                    color = R.color.color_offline;
                    break;

                case ComFailure:
                    color = R.color.color_dark_grey;
                    break;
            }

        }
        colorView.setBackgroundColor(ContextCompat.getColor(context, color));

        return rmsDeviceCardView;
    }
}

