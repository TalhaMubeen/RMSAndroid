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
        switch (name){
            case "IceCream":
                retVal = R.drawable.icecream;
                break;

            case "FrozenFood":
                retVal = R.drawable.meat;
                break;

            case "WalkinChiller":
                retVal = R.drawable.chiller_icon;
                break;

            case "FruitVegDrinksDairy":
            default:
                retVal = R.drawable.rms_icon;
                break;
        }
        return  retVal;

    }

    public int getProfileColor(){
        int retVal;
        switch (name){
            case "IceCream":
                retVal = R.color.color_normal;
                break;

            case "FrozenFood":
                retVal = R.color.color_warning;
                break;

            case "WalkinChiller":
                retVal = R.color.color_defrost;
                break;

            case "FruitVegDrinksDairy":
            default:
                retVal = R.color.color_offline;
                break;
        }
        return  retVal;
    }

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


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String name;
    private String title;
    private double lowTempThreshold;
    private double highTempThreshold;
    private int    lowHumidityThreshold;
    private int    highHumidityThreshold;
    private double rssiThreshold;


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

    public Profile(String profileName, String title, double lowtempTH, double highTempTh, double rssiTH, int lowHumidityTH, int highHumidityTH){
        this.name = profileName;
        this.title = title;
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
            setTitle(obj.optString("Title"));
            setLowTempThreshold(obj.optDouble("LowTempTh"));
            setHighTempThreshold(obj.optDouble("HighTempTh"));
            setRssiThreshold(obj.getDouble("RssiTh"));
            setLowHumidityThreshold(obj.getInt("LowHumidityTh"));
            setHighHumidityThreshold(obj.getInt("HighHumidityTh"));

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
            jo.put("Title", getName());
            jo.put("LowTempTh", getLowTempThreshold());
            jo.put("HighTempTh", getHighTempThreshold());
            jo.put("RssiTh", getRssiThreshold());
            jo.put("LowHumidityTh", getLowHumidityThreshold());
            jo.put("HighHumidityTh", getHighHumidityThreshold());

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return jo;
    }

}
