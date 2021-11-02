package com.innv.rmsgateway.classes;

import android.util.Log;

import com.innv.rmsgateway.data.IConvertHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.ContentValues.TAG;

public class DefrostProfile implements IConvertHelper {

    public static class Interval implements IConvertHelper{
        int startHour =0;
        int startMinute =0;
        int endHour = 0;
        int endMinute = 0;

        public Interval(){ }
        public Interval(JSONObject jsonObject){ parseJsonObject(jsonObject); }

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

        public Interval( int startHour, int startMinute, int endHour, int endMinute){
            //Start Interval
            this.startHour = startHour;
            this.startMinute = startMinute;

            //End Interval
            this.endHour = endHour;
            this.endMinute = endMinute;
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
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Boolean isEqual(DefrostProfile second){

        boolean ret = this.defrostIntervals.equals(second) &&
                this.name.equals(second.name);
        return ret;
    }


    String name;

    public List<Interval> getDefrostIntervals() {
        return defrostIntervals;
    }

    public void setDefrostIntervals(List<Interval> defrostIntervals) {
        this.defrostIntervals = defrostIntervals;
    }

    public void addDefrostInterval(Interval interval){
        defrostIntervals.add(interval);
    }

    List<Interval> defrostIntervals = new ArrayList<>();


    public DefrostProfile(){ }

    public DefrostProfile(String name, int startHour, int startMinute, int endHour, int endMinute){
        //Profile Name
        this.name = name;
        Interval interval = new Interval(startHour, startMinute, endHour, endMinute);
        defrostIntervals.add(interval);
    }


    public static Date parseDate(String date) {

        final String inputFormat = "HH:mm";
        SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.getDefault());
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    public Boolean isTimeInBetween(String date){

        AtomicBoolean ret = new AtomicBoolean(false);
        defrostIntervals.forEach( interval -> {
            ret.set(interval.isTimeInBetween(date));
            if(ret.get()){
                return;
            }
        });

        return ret.get();

    }

    public DefrostProfile(JSONObject jsonObject){
        parseJsonObject(jsonObject);
    }

    @Override
    public boolean parseJsonObject(JSONObject jsonObject) {
        try {
            setName(jsonObject.optString("Name"));
            JSONArray arr = jsonObject.optJSONArray("Intervals");


            for (int i = 0 ; i< arr.length() ; i++){
                 JSONObject jo =  arr.optJSONObject(i);
                 Interval interval = new Interval(jo);
                defrostIntervals.add(interval);
            }

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
            jo.put("Name", getName());
            JSONArray arr = new JSONArray();
            defrostIntervals.forEach(interval -> {
                arr.put(interval.getJsonObject());
            });
            jo.put("Intervals",arr);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jo;
    }
}
