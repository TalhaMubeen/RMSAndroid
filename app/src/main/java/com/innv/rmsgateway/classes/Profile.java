package com.innv.rmsgateway.classes;
import android.content.Context;
import android.util.Log;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.IConvertHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class Profile implements IConvertHelper {

    public int getProfileImageId(){
        int retVal;
        switch (title){
            case "Ice Cream":
                retVal = R.drawable.icecream;
                break;

            case "Frozen Food":
                retVal = R.drawable.meat;
                break;

            case "Walk-in Chiller":
                retVal = R.drawable.chiller_icon;
                break;

            case "Fruit, Veg, Cold Drinks & Dairy":
            default:
                retVal = R.drawable.rms_icon;
                break;
        }
        return  retVal;

    }

    public int getProfileColor(){
        int retVal;
        switch (title){
            case "Ice Cream":
                retVal = R.color.color_normal;
                break;

            case "Frozen Food":
                retVal = R.color.color_warning;
                break;

            case "Walk-in Chiller":
                retVal = R.color.color_defrost;
                break;

            case "Fruit, Veg, Cold Drinks & Dairy":
            default:
                retVal = R.color.color_offline;
                break;
        }
        return  retVal;
    }

    public int getLowHumidityThreshold() { return lowHumidityThreshold; }

    public void setLowHumidityThreshold(int lowHumidityThreshold) { this.lowHumidityThreshold = lowHumidityThreshold; }

    public int getHighHumidityThreshold() { return highHumidityThreshold; }

    public void setHighHumidityThreshold(int highHumidityThreshold) { this.highHumidityThreshold = highHumidityThreshold; }

    public double getLowTempThreshold() {
        if(!Globals.useCelsius) {
            return Globals.CtoF(lowTempThreshold);
        }

        return lowTempThreshold;
    }

    public void setLowTempThreshold(double lowTempThreshold) {
        if(!Globals.useCelsius) {
            this.lowTempThreshold = Globals.FtoC(lowTempThreshold);
        }else {
            this.lowTempThreshold = lowTempThreshold;
        }
    }

    public double getHighTempThreshold() {
        if(!Globals.useCelsius) {
            return Globals.CtoF(highTempThreshold);
        }
        return highTempThreshold;
    }

    public void setHighTempThreshold(double highTempThreshold) {
        if(!Globals.useCelsius) {
            this.highTempThreshold = Globals.FtoC(highTempThreshold);
        }else {
            this.highTempThreshold = highTempThreshold;
        }
    }

    public double getRssiThreshold() {
        return rssiThreshold;
    }

    public void setRssiThreshold(double rssiThreshold) {
        this.rssiThreshold = rssiThreshold;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPictureID() {
        return pictureID;
    }

    public void setPictureID(int pictureID) {
        this.pictureID = pictureID;
    }

  //  private String name;
    private String title;
    private double lowTempThreshold;
    private double highTempThreshold;
    private int    lowHumidityThreshold;
    private int    highHumidityThreshold;
    private double rssiThreshold;
    private int warningToAlertTime;
    private int pictureID;

    public void set(Profile second){
        this.lowTempThreshold =second.lowTempThreshold ;
                this.highTempThreshold = second.highTempThreshold ;
                this.lowHumidityThreshold = second.lowHumidityThreshold;
                this.highHumidityThreshold = second.highHumidityThreshold;
                this.warningToAlertTime =second.warningToAlertTime ;
                this.pictureID = second.getPictureID();
                this.title = second.title;
    }

    public Boolean isEqual(Profile second){

        return this.lowTempThreshold == second.lowTempThreshold &&
                this.highTempThreshold == second.highTempThreshold &&
                this.lowHumidityThreshold == second.lowHumidityThreshold &&
                this.highHumidityThreshold == second.highHumidityThreshold &&
                this.warningToAlertTime == second.warningToAlertTime &&
                this.getProfileImageId() == second.getProfileImageId() &&
                this.title.equals(second.title);
    }

    public int getWarningToAlertTime() {
        return warningToAlertTime;
    }

    public void setWarningToAlertTime(int warningToAlertTime) {
        this.warningToAlertTime = warningToAlertTime;
    }


    public Profile(JSONObject obj){
        parseJsonObject(obj);
    }

    public Profile(){
        this.lowTempThreshold = 0;
        this.highTempThreshold = 0;
        this.lowHumidityThreshold = 0;
        this.highHumidityThreshold = 0;
        this.rssiThreshold = 0;
        this.warningToAlertTime = 0;
        this.pictureID =  R.drawable.rms_icon;
    }

    public Profile(String title, double lowtempTH, double highTempTh, double rssiTH, int lowHumidityTH, int highHumidityTH, int alertTime){
        this.title = title;
        this.lowTempThreshold = lowtempTH;
        this.highTempThreshold = highTempTh;
        this.rssiThreshold = rssiTH;
        this.lowHumidityThreshold = lowHumidityTH;
        this.highHumidityThreshold = highHumidityTH;
        this.warningToAlertTime = alertTime;
    }

    @Override
    public boolean parseJsonObject(JSONObject obj) {
        try {
            setTitle(obj.optString("Title"));
            lowTempThreshold = obj.optDouble("LowTempTh");
            highTempThreshold = obj.optDouble("HighTempTh");
            setRssiThreshold(obj.getDouble("RssiTh"));
            setLowHumidityThreshold(obj.getInt("LowHumidityTh"));
            setHighHumidityThreshold(obj.getInt("HighHumidityTh"));
            setWarningToAlertTime(obj.getInt("Warn2AlertTime"));
            setPictureID(obj.getInt("PictureID"));

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
            jo.put("Title", getTitle());
            jo.put("LowTempTh", lowTempThreshold);
            jo.put("HighTempTh", highTempThreshold);
            jo.put("RssiTh", getRssiThreshold());
            jo.put("LowHumidityTh", getLowHumidityThreshold());
            jo.put("HighHumidityTh", getHighHumidityThreshold());
            jo.put("Warn2AlertTime", getWarningToAlertTime());
            jo.put("PictureID", getPictureID());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jo;
    }

}
