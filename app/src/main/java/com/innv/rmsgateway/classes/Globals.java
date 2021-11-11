package com.innv.rmsgateway.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.DBHandler;
import com.innv.rmsgateway.sensornode.SensorDataDecoder;

import java.util.ResourceBundle;

public class Globals {
    private static final int MODE_PRIVATE = 0;
    public static String orgCode = "ps19";
    public static final String APPLICATION_LOOKUP_LIST_NAME = "ApplicationLookups";
    public static DBHandler db;
    public static Context dbContext;
    public static int Db_Version = 4;

    public static boolean useCelsius = true;
    public static int NORMAL = Color.parseColor("#99C24D");
    public static int ALERT = Color.parseColor("#C1272D");
    public static int INACTIVE = Color.parseColor("#006E90");
    public static int WARNING = Color.parseColor("#F18F01");
    public static int BELOW_THRESHOLD = Color.parseColor("#41BBD9");
    public static int NODE_OFFLINE_TIME = 1; //minutes

    static SharedPreferences pref;

    public static  double CtoF(double c){
        double ret =  c*9/5+32;
        return SensorDataDecoder.round(ret, 1);
    }

    public static double FtoC(double F){
        double b=F-32;
        double ret = b*5/9;
        return SensorDataDecoder.round(ret, 1);
    }

    public static final int[] AlertType = new int[]{
            R.string.all,
            R.string.alert,
            R.string.warning,
            R.string.normal,
            R.string.defrost,
            R.string.offline,
            R.string.comfail,
    };

    public static String capitalize(String string) {
        String[] arr = string.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static void storeSharedPref(){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isTempC", useCelsius);
        editor.apply();
    }


    public static void setDbContext(Context context) {
        dbContext = context;
        pref = context.getSharedPreferences("RMS_SHARED_PREF", MODE_PRIVATE);


        useCelsius = pref.getBoolean("isTempC", true);  // getting boolean

        db=DBHandler.getInstance(context);
        ProfileManager.init();
        DefrostProfileManager.init();
    }

}