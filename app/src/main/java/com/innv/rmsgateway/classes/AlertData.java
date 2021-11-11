package com.innv.rmsgateway.classes;

import android.util.Log;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.IConvertHelper;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorNode;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class AlertData implements IConvertHelper {
    AlertType type;
    NodeState nodeState;
    String alertStartTime = "";
    String alertEndTime = "";
    String alertDay = "";
    String nodeMacAddress = "";
    double temperature;
    int humidity;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public StaticListItem getDataAsStaticListItem() {

        try {
            String opt1 = getJsonObject().toString();
            return new StaticListItem(Globals.orgCode,
                    Globals.dbContext.getString(R.string.RMS_DEVICES),
                    nodeMacAddress,
                    "",
                    opt1, "");
        } catch (Exception Ignore) {

        }
        return null;
    }

    public String getAlertDay() {
        return alertDay;
    }

    public void setAlertDay(String alertDay) {
        this.alertDay = alertDay;
    }

    public String getNodeMacAddress() {
        return nodeMacAddress;
    }

    public void setNodeMacAddress(String nodeMacAddress) {
        this.nodeMacAddress = nodeMacAddress;
    }

    public NodeState getNodeState() { return nodeState; }

    public String getStatusString() { return nodeState.name(); }

    public void setNodeState(NodeState nodeState) { this.nodeState = nodeState; }

    public String getTypeString() { return type.name(); }

    public AlertType getType() { return type; }

    public void setType(AlertType type) {
        this.type = type;
    }

    public Date getAlertStartTime() {
        if(alertStartTime.isEmpty()){
            return  null;
        }
        SimpleDateFormat sm = new SimpleDateFormat(SensorNode.defaultDateFormat, Locale.getDefault());
        Date dt = null;
        try {
            dt = sm.parse(alertStartTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public String getAlertStartTimeString(){ return alertStartTime; }

    public String getAlertEndTimeString(){ return alertEndTime; }

    public void setAlertStartTimeString(String alertStartTime) {
        this.alertStartTime = alertStartTime;
    }

    public void setAlertStartTime(Date start){
        SimpleDateFormat sm = new SimpleDateFormat(SensorNode.defaultDateFormat, Locale.getDefault());
        this.alertStartTime = sm.format(start);
    }

    public void setAlertEndTime(Date end){
        SimpleDateFormat sm = new SimpleDateFormat(SensorNode.defaultDateFormat, Locale.getDefault());
        this.alertEndTime = sm.format(end);
    }

    public Date getAlertEndTime() {
        if(alertEndTime.isEmpty()){
            return  null;
        }
        SimpleDateFormat sm = new SimpleDateFormat(SensorNode.defaultDateFormat, Locale.getDefault());
        Date dt = null;
        try {
            dt = sm.parse(alertEndTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public void setAlertEndTimeString(String alertEndTime) {
        this.alertEndTime = alertEndTime;
    }

    public AlertData(){}
    public AlertData(JSONObject obj){
        parseJsonObject(obj);
    }

    public AlertData(String mac, AlertType type, NodeState status, Date alertStartTime, double temperature, int humidity) {
        setNodeMacAddress(mac);
        setType(type);
        setNodeState(status);
        setAlertStartTime(alertStartTime);
        setTemperature(temperature);
        setHumidity(humidity);
    }

    @Override
    public boolean parseJsonObject(JSONObject jsonObject) {
        try {
            setNodeMacAddress(jsonObject.optString(("NodeMacAddress")));
            setType(AlertType.valueOf(jsonObject.optString("AlertTypes")));
            setAlertStartTimeString(jsonObject.optString(("AlertStartTime")));
            setAlertEndTimeString(jsonObject.optString(("AlertEndTime")));
            setNodeState(NodeState.valueOf(jsonObject.optString(("AlertStatus"))));
            setAlertDay(jsonObject.optString(("AlertDay")));
            setTemperature(jsonObject.optDouble("Temperature"));
            setHumidity(jsonObject.optInt("Humidity"));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    }

    @Override
    public JSONObject getJsonObject() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("NodeMacAddress", getNodeMacAddress());
            jo.put("AlertTypes", getType());
            jo.put("AlertStartTime", getAlertStartTimeString());
            jo.put("AlertEndTime", getAlertEndTimeString());
            jo.put("AlertStatus", getStatusString());
            jo.put("AlertDay", getAlertDay());
            jo.put("Temperature", getTemperature());
            jo.put("Humidity", getHumidity());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jo;


    }

}
