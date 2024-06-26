package com.innv.rmsgateway.sensornode;
import android.annotation.SuppressLint;
import android.util.Log;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.NodeState;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.data.IConvertHelper;
import com.innv.rmsgateway.data.StaticListItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 *    this class contains sensor node data elements for UI and database
 *
 */

@SuppressLint("SimpleDateFormat")
public class SensorNode implements IConvertHelper, Cloneable {

    private String macID;

    public String getName() {
        name = Globals.capitalize(name); return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String advertisingTime;
    private int timeAtWakeup; // wakeup time in hours,
    private int timeSinceWakeup; // time of device since working
    private int timeSlot;     //node slot
    private boolean timeSynced; // synced = 1, not synced =0;
    private double temperature;
    private int humidity;
    private double batteryVoltage;
    private String lastUpdated;
    private String profileTitle;
    private String defrostProfileTitle;
    public NodeState nodeState;

    public NodeState getNodeState() {
        return nodeState;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    public String getProfileTitle() {
        return profileTitle;
    }

    public void setProfileTitle(String profileTitle) {
        this.profileTitle = profileTitle;
    }

    public String getDefrostProfileTitle() {
        return defrostProfileTitle;
    }

    public void setDefrostProfileTitle(String defrostProfileTitle) { this.defrostProfileTitle = defrostProfileTitle; }

    public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String defaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static Date getUTCdatetimeFromString(String dateValue) {
        try {
             final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date dt = sdf.parse(dateValue);
            return dt;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String FormatDateTime(Date dateValue, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(dateValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

/*    public String getTimeStampFormatted() {
        if (!timeStamp.equals("")) {
            Date dt = getUTCdatetimeFromString(timeStamp);
            return FormatDateTime(dt, defaultDateFormat);
        }
        return null;
    }*/

    public Date ConvertToDate(String dateValue) {
       SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
        try {
            Date d = sdf.parse(dateValue);
            return d;

        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;

        }
    }

    public Date getLastUpdatedDate() {
        if(lastUpdated.isEmpty()){
            return  null;
        }
        SimpleDateFormat sm = new SimpleDateFormat(defaultDateFormat, Locale.getDefault());
        Date dt = null;
        try {
            dt = sm.parse(lastUpdated);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public void setLastUpdatedOn(Date dt) {
        SimpleDateFormat sm = new SimpleDateFormat(defaultDateFormat, Locale.getDefault());
        lastUpdated = sm.format(dt);
    }

    public String getLastUpdatedOn() {
        return lastUpdated;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public boolean isPreChecked() {
        return isPreChecked;
    }

    public void setPreChecked(boolean preChecked) {
        isPreChecked = preChecked;
    }

    private boolean isPreChecked;

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    private double rssi;

    public SensorNode(){}

    public SensorNode(JSONObject obj) { parseJsonObject(obj); }

    public SensorNode(String mac, String profile){
        this.macID = mac;
    }

    public SensorNode(String macID, String name, String advertisingTime,
                      int timeAtWakeup, int timeSinceWakeup, int timeSlot,
                      boolean timeSynced, double temperature, int humidity,
                      double batteryVoltage, double rssi, boolean isPreChecked,
                      String profileTitle, String defrostProfileTitle) {
        this.macID = macID;
        this.name = name;
        this.advertisingTime = advertisingTime;
        this.timeAtWakeup = timeAtWakeup;
        this.timeSinceWakeup = timeSinceWakeup;
        this.timeSlot = timeSlot;
        this.timeSynced = timeSynced;
        this.temperature = temperature;
        this.humidity = humidity;
        this.batteryVoltage = batteryVoltage;
        this.rssi = rssi;
        this.isPreChecked = isPreChecked;
        this.profileTitle = profileTitle;
        this.defrostProfileTitle = defrostProfileTitle;
        this.nodeState = NodeState.Normal;

    }

    public StaticListItem getDataAsStaticListItem() {

        try {
            String opt1 = getJsonObject().toString();
            return new StaticListItem(Globals.orgCode,
                    Globals.dbContext.getString(R.string.RMS_DEVICES),
                    getMacID(),
                    getName(),
                    opt1, "");
        } catch (Exception Ignore) {

        }
        return null;
    }

    public String getMacID() {
        return macID;
    }

    public void setMacID(String macID) {
        this.macID = macID;
    }

    public String getAdvertisingTime() {
        return advertisingTime;
    }

    public void setAdvertisingTime(String advertisingTime) { this.advertisingTime = advertisingTime; }

    public int getTimeAtWakeup() {
        return timeAtWakeup;
    }

    public void setTimeAtWakeup(int timeAtWakeup) {
        this.timeAtWakeup = timeAtWakeup;
    }

    public int getTimeSinceWakeup() {
        return timeSinceWakeup;
    }

    public void setTimeSinceWakeup(int timeSinceWakeup) {
        this.timeSinceWakeup = timeSinceWakeup;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isTimeSynced() {
        return timeSynced;
    }

    public void setTimeSynced(boolean timeSynced) {
        this.timeSynced = timeSynced;
    }

    public double getTemperature() { if(!Globals.useCelsius) { return Globals.CtoF(temperature); } return temperature; }

    public void setTemperature(double temp) { this.temperature = temp; }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    @Override
    public boolean parseJsonObject(JSONObject jsonObject) {
        try {
            setMacID(jsonObject.optString("macID"));
            setName(jsonObject.optString("name"));
            setAdvertisingTime(jsonObject.optString("advertisingTime"));
            setTimeAtWakeup(jsonObject.optInt("timeAtWakeup"));
            setTimeSinceWakeup(jsonObject.optInt("timeSinceWakeup"));
            setTimeSlot(jsonObject.optInt("timeSlot"));
            setTimeSynced(jsonObject.optBoolean("timeSynced"));
            temperature = jsonObject.optDouble("temperature");
            setHumidity(jsonObject.optInt("humidity"));
            setBatteryVoltage(jsonObject.optDouble("batteryVoltage"));
            setRssi(jsonObject.optDouble("rssi"));
            setPreChecked(jsonObject.optBoolean("isPreChecked"));
            setPreChecked(jsonObject.optBoolean("isPreChecked"));
            lastUpdated = jsonObject.optString("LastUpdateTime");
            setProfileTitle(jsonObject.optString("ProfileTitle"));
            setDefrostProfileTitle(jsonObject.optString("DefrostProfileTitle"));
            setNodeState(NodeState.valueOf(jsonObject.optString("NodeState")));

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    }

    public Boolean parseListItem(StaticListItem item) {
        try {
            JSONObject jsonObject = new JSONObject(item.getOptParam1());
            if(parseJsonObject(jsonObject)) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public JSONObject getJsonObject() {
        JSONObject jo = new JSONObject();
        try {

            jo.put("macID", getMacID());
            jo.put("name", getName());
            jo.put("advertisingTime", getAdvertisingTime());
            jo.put("timeAtWakeup", getTimeAtWakeup());
            jo.put("timeSinceWakeup", getTimeSinceWakeup());
            jo.put("timeSlot", getTimeSlot());
            jo.put("timeSynced", isTimeSynced());
            jo.put("temperature", temperature);
            jo.put("humidity", getHumidity());
            jo.put("batteryVoltage", getBatteryVoltage());
            jo.put("rssi", getRssi());
            jo.put("isPreChecked", isPreChecked());
            jo.put("LastUpdateTime", getLastUpdatedOn());
            jo.put("ProfileTitle", getProfileTitle());
            jo.put("DefrostProfileTitle", getDefrostProfileTitle());
            jo.put("NodeState", getNodeState().name());

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            jo = null;
        }

        return jo;
    }

    public JSONObject getJsonObjectMSG(){

        JSONObject jo = new JSONObject();
        try {

            String mac = new String(getMacID());
            mac = mac.replace(":", "");
            jo.put("macAddress", mac);
            jo.put("rssi", getRssi());
            jo.put("unixTimeStamp", (getLastUpdatedDate().getTime() / 1000));
            jo.put("protocolVersion", 1);
            jo.put("timeSlot", getTimeSlot());
            jo.put("productIdentifier", 1);
            jo.put("deviceType", 1);
            jo.put("frameType", 1);
            jo.put("timePeriod", 3);
            jo.put("timeAtWakeup", getTimeAtWakeup());
            jo.put("timeSinceWakeup", getTimeSinceWakeup());
            jo.put("batteryVoltage", getBatteryVoltage());
            jo.put("timeSyncOnWakeup", 0);
            jo.put("timeSynced", 1);
            jo.put("timeSyncRequired", 0);
            jo.put("sequenceNumber", 1);
            jo.put("temperature", temperature);
            jo.put("humidity", getHumidity());
/*
            jo.put("isPreChecked", isPreChecked());
            jo.put("LastUpdateTime", getLastUpdatedOn());
            jo.put("ProfileTitle", getProfileTitle());
            jo.put("DefrostProfileTitle", getDefrostProfileTitle());
            jo.put("NodeState", getNodeState().name());*/

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            jo = null;
        }

        return jo;

    }
}