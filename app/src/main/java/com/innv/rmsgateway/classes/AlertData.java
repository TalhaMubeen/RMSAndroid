package com.innv.rmsgateway.classes;

import android.annotation.SuppressLint;
import android.util.Log;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.Globals;
import com.innv.rmsgateway.data.IConvertHelper;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorNode;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class AlertData implements IConvertHelper {

    public enum AlertStatus implements Serializable {
        Normal,
        Warning,
        Alert,
        Offline,
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

    AlertTypes type;
    String alertStartTime = "";
    String alertEndTime = "";
    AlertStatus status;
    String alertDay = "";
    String nodeMacAddress = "";

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

    public AlertStatus getStatus() { return status; }

    public String getStatusString() { return status.name(); }

    public void setStatus(AlertStatus status) { this.status = status; }

    public String getTypeString() { return type.name(); }

    public AlertTypes getType() { return type; }

    public void setType(AlertTypes type) {
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

    public AlertData(String mac, AlertTypes type, AlertStatus status,  Date alertStartTime) {
        this.nodeMacAddress = mac;
        this.type = type;
        this.status = status;
        setAlertStartTime(alertStartTime);
    }

    @Override
    public boolean parseJsonObject(JSONObject jsonObject) {
        try {
            setNodeMacAddress(jsonObject.optString(("NodeMacAddress")));
            setType(AlertTypes.valueOf(jsonObject.optString("AlertTypes")));
            setAlertStartTimeString(jsonObject.optString(("AlertStartTime")));
            setAlertEndTimeString(jsonObject.optString(("AlertEndTime")));
            setStatus(AlertStatus.valueOf(jsonObject.optString(("AlertStatus"))));
            setAlertDay(jsonObject.optString(("AlertDay")));
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

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jo;


    }

}
