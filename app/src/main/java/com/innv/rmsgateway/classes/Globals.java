package com.innv.rmsgateway.classes;

import android.content.Context;
import android.graphics.Color;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.DBHandler;

import java.util.ResourceBundle;

public class Globals {
    public static String orgCode = "ps19";
    public static final String APPLICATION_LOOKUP_LIST_NAME = "ApplicationLookups";
    public static DBHandler db;
    public static Context dbContext;

    public static boolean useCelsius = true;
    public static int NORMAL = Color.parseColor("#99C24D");
    public static int ALERT = Color.parseColor("#C1272D");
    public static int INACTIVE = Color.parseColor("#006E90");
    public static int WARNING = Color.parseColor("#F18F01");
    public static int BELOW_THRESHOLD = Color.parseColor("#41BBD9");


    public static final int[] AlertType = new int[]{
            R.string.all,
            R.string.alert,
            R.string.warning,
            R.string.normal,
            R.string.defrost,
            R.string.offline,
            R.string.comfail,
    };


    public static void setDbContext(Context context) {
        dbContext = context;
        db=DBHandler.getInstance(context);
        ProfileManager.init();
        DefrostProfileManager.init();
    }

}