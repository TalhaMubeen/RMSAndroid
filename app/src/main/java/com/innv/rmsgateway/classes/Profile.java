package com.innv.rmsgateway.classes;
import android.util.Log;

import com.innv.rmsgateway.data.IConvertHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class Profile implements IConvertHelper {
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getLowHumidityThreshold() { return lowHumidityThreshold; }

    public void setLowHumidityThreshold(int lowHumidityThreshold) { this.lowHumidityThreshold = lowHumidityThreshold; }

    public int getHighHumidityThreshold() { return highHumidityThreshold; }

    public void setHighHumidityThreshold(int highHumidityThreshold) { this.highHumidityThreshold = highHumidityThreshold; }

    public double getLowTempThreshold() {
        return lowTempThreshold;
    }

    public void setLowTempThreshold(double lowTempThreshold) { this.lowTempThreshold = lowTempThreshold; }

    public double getHighTempThreshold() { return highTempThreshold; }

    public void setHighTempThreshold(double highTempThreshold) { this.highTempThreshold = highTempThreshold; }

    public double getRssiThreshold() {
        return rssiThreshold;
    }

    public void setRssiThreshold(double rssiThreshold) {
        this.rssiThreshold = rssiThreshold;
    }

    public List<DefrostTimeProfile> getDefrostProfile() { return defrostProfile; }

    public void addDefrostProfile(DefrostTimeProfile cycle){ defrostProfile.add(cycle); }

    public void setDefrostProfile(List<DefrostTimeProfile> defrostProfile) { this.defrostProfile = defrostProfile; }


    private String name;
    private double lowTempThreshold;
    private double highTempThreshold;
    private int    lowHumidityThreshold;
    private int    highHumidityThreshold;
    private double rssiThreshold;
    private List<DefrostTimeProfile> defrostProfile = new ArrayList<>();

    public Profile(JSONObject obj){
        parseJsonObject(obj);
    }

    public Profile(){
        this.name = "Default";
        this.lowTempThreshold = 10;
        this.highTempThreshold = 20;
        this.lowHumidityThreshold = 30;
        this.highHumidityThreshold = 50;
        this.rssiThreshold = -80;
    }

    public Profile(String profileName, double lowtempTH, double highTempTh, double rssiTH, int lowHumidityTH, int highHumidityTH){
        this.name = profileName;
        this.lowTempThreshold = lowtempTH;
        this.highTempThreshold = highTempTh;
        this.rssiThreshold = rssiTH;
        this.lowHumidityThreshold = lowHumidityTH;
        this.highHumidityThreshold = highHumidityTH;
    }

    @Override
    public boolean parseJsonObject(JSONObject obj) {
        try {
            setName(obj.optString("Name"));
            setLowTempThreshold(obj.optDouble("LowTempTh"));
            setHighTempThreshold(obj.optDouble("HighTempTh"));
            setRssiThreshold(obj.getDouble("RssiTh"));
            setLowHumidityThreshold(obj.getInt("LowHumidityTh"));
            setHighHumidityThreshold(obj.getInt("HighHumidityTh"));

            JSONArray jaProf = obj.optJSONArray("DefrostProfile");
            if(jaProf !=null && jaProf.length()>0){
                for(int i = 0; i< jaProf.length(); i++){
                    addDefrostProfile(new DefrostTimeProfile(jaProf.getJSONObject(i)));
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
            jo.put("LowHumidityTh", getLowHumidityThreshold());
            jo.put("HighHumidityTh", getHighHumidityThreshold());

            JSONArray jaProf = new JSONArray();
            for(DefrostTimeProfile profile : getDefrostProfile()){
                jaProf.put(profile.getJsonObject());
            }
            jo.put("DefrostProfile", jaProf);


        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jo;
    }


    static public class DefrostTimeProfile implements IConvertHelper {
        public int getStartHour() {
            return startHour;
        }

        public void setStartHour(int hour) {
            this.startHour = hour;
        }

        public int getStartMinute() {
            return startMinute;
        }

        public void setStartMinute(int startMinute) {
            this.startMinute = startMinute;
        }

        public int getEndHour() {
            return endHour;
        }

        public void setEndHour(int endHour) {
            this.endHour = endHour;
        }

        public int getEndMinute() {
            return endMinute;
        }

        public void setEndMinute(int endMinute) {
            this.endMinute = endMinute;
        }


        int startHour =0;
        int startMinute =0;
        int endHour = 0;
        int endMinute = 0;

        public boolean isEmpty(){
            boolean ret = false;
            if(startHour == 0 && startMinute == 0 &&
                    endHour == 0 && endMinute == 0){
                ret = true;
            }
            return ret;
        }

        public boolean isOk(){
            boolean ret = false;
             if(startHour!=0 && endHour != 0){
                 ret = true;
             }

             return ret;
        }

        public DefrostTimeProfile(){ }

        public DefrostTimeProfile(int startHour, int startMinute, int endHour, int endMinute){

            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;

        }


        public Date parseDate(String date) {

            final String inputFormat = "HH:mm";
            SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.getDefault());
            try {
                return inputParser.parse(date);
            } catch (java.text.ParseException e) {
                return new Date(0);
            }
        }

        public Boolean isTimeInBetween(String date){
            Date current = parseDate(date);
            Date dateCompareOne = parseDate(Integer.toString(startHour) + ":" + Integer.toString(startMinute));
            Date dateCompareTwo = parseDate(Integer.toString(endHour) + ":" + Integer.toString(endMinute));

            boolean ret = false;

            if (dateCompareTwo.before(current) && dateCompareOne.after(current)) {
                //your logic
                ret = true;
            }
            return ret;

        }

        public DefrostTimeProfile(JSONObject jsonObject){
            parseJsonObject(jsonObject);
        }

        @Override
        public boolean parseJsonObject(JSONObject jsonObject) {
            try {
                setStartHour(jsonObject.optInt("StartHour"));
                setStartMinute(jsonObject.optInt("StartMinute"));
                setEndHour(jsonObject.optInt("EndHour"));
                setEndMinute(jsonObject.optInt("EndMinute"));


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
                jo.put("StartHour", getStartHour());
                jo.put("StartMinute", getStartMinute());
                jo.put("EndHour", getEndHour());
                jo.put("EndMinute",getEndMinute());

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return jo;
        }
    }
}
