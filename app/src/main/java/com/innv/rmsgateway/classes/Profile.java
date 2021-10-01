package com.innv.rmsgateway.classes;
import android.util.Log;

import com.innv.rmsgateway.data.IConvertHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class Profile implements IConvertHelper {

    public double getLowTempThreshold() {
        return lowTempThreshold;
    }

    public void setLowTempThreshold(double lowTempThreshold) { this.lowTempThreshold = lowTempThreshold; }

    public int getHumidityThreshold() {
        return humidityThreshold;
    }

    public void setHumidityThreshold(int humidityThreshold) { this.humidityThreshold = humidityThreshold; }

    public double getRssiThreshold() {
        return rssiThreshold;
    }

    public void setRssiThreshold(double rssiThreshold) {
        this.rssiThreshold = rssiThreshold;
    }


    public void addDefrostStartProfile(DefrostTimeProfile cycle){
        defrostStartTime.add(cycle);
    }

    public void addDefrostEndProfile(DefrostTimeProfile cycle){
        defrostEndTime.add(cycle);
    }

    public List<DefrostTimeProfile> getDefrostStartTimeProfiles(){
        return defrostStartTime;
    }

    public List<DefrostTimeProfile> getDefrostEndTimeProfiles(){
        return defrostEndTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private double lowTempThreshold;

    public double getHighTempThreshold() { return highTempThreshold; }

    public void setHighTempThreshold(double highTempThreshold) { this.highTempThreshold = highTempThreshold; }

    private double highTempThreshold;
    private int    humidityThreshold;
    private double rssiThreshold;

    private List<DefrostTimeProfile> defrostStartTime = new ArrayList<>();
    private List<DefrostTimeProfile> defrostEndTime = new ArrayList<>();

    public Profile(JSONObject obj){
        parseJsonObject(obj);
    }

    public Profile(){
        this.name = "Default";
        this.lowTempThreshold = 10;
        this.highTempThreshold = 20;
        this.humidityThreshold = 45;
        this.rssiThreshold = -80;
    }

    public Profile(String profileName, double lowtempTH, double highTempTh, double rssiTH, int humidityTH){
        this.name = profileName;
        this.lowTempThreshold = lowtempTH;
        this.highTempThreshold = highTempTh;
        this.rssiThreshold = rssiTH;
        this.humidityThreshold = humidityTH;
    }

    @Override
    public boolean parseJsonObject(JSONObject obj) {
        try {
            setName(obj.optString("Name"));
            setLowTempThreshold(obj.optDouble("LowTempTh"));
            setHighTempThreshold(obj.optDouble("HighTempTh"));
            setRssiThreshold(obj.getDouble("RssiTh"));
            setHumidityThreshold(obj.getInt("HumidityTh"));

            JSONArray jaStart = obj.optJSONArray("DefrostStartProfile");
            if(jaStart !=null && jaStart.length()>0){
                for(int i = 0; i< jaStart.length(); i++){
                    addDefrostStartProfile(new DefrostTimeProfile(jaStart.getJSONObject(i)));
                }
            }

            JSONArray jaEnd = obj.optJSONArray("DefrostEndProfile");
            if(jaEnd !=null && jaEnd.length()>0){
                for(int i = 0; i< jaEnd.length(); i++){
                    addDefrostEndProfile(new DefrostTimeProfile(jaEnd.getJSONObject(i)));
                }
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @Override
    public JSONObject getJsonObject() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("Name", getName());
            jo.put("LowTempTh", getLowTempThreshold());
            jo.put("HighTempTh", getHighTempThreshold());
            jo.put("RssiTh", getRssiThreshold());
            jo.put("HumidityTh", getHumidityThreshold());

            JSONArray jaStart = new JSONArray();
            for(DefrostTimeProfile profile : getDefrostStartTimeProfiles()){
                jaStart.put(profile.getJsonObject());
            }
            jo.put("DefrostStartProfile", jaStart);

            JSONArray jaEnd = new JSONArray();
            for(DefrostTimeProfile profile : getDefrostEndTimeProfiles()){
                jaEnd.put(profile.getJsonObject());
            }
            jo.put("DefrostEndProfile", jaEnd);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jo;
    }

    static public class DefrostTimeProfile implements IConvertHelper {
        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        int hour =0;
        int minute =0;

        public DefrostTimeProfile(JSONObject jsonObject){
            parseJsonObject(jsonObject);
        }

        @Override
        public boolean parseJsonObject(JSONObject jsonObject) {
            try {
                setHour(jsonObject.optInt("Hour"));
                setMinute(jsonObject.optInt("Minute"));

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }

            return false;
        }

        @Override
        public JSONObject getJsonObject() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("Hour", getHour());
                jo.put("Minute", getMinute());

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return jo;
        }
    }
}
