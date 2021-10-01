package com.innv.rmsgateway.data;

import android.content.Context;

import com.innv.rmsgateway.classes.ProfileManager;

import java.util.ResourceBundle;

public class Globals {
    public static String orgCode = "ps19";
    public static final String APPLICATION_LOOKUP_LIST_NAME = "ApplicationLookups";
    public static DBHandler db;

    public static Context dbContext;

    public static void setDbContext(Context context) {
        dbContext = context;
        db=DBHandler.getInstance(context);
        ProfileManager.init();
    }
}