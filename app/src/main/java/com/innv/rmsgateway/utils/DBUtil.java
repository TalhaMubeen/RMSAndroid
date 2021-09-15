package com.innv.rmsgateway.utils;

import android.net.wifi.WpsInfo;

/**
 *
 *    this class contains functionality required to database
 *
 *
 */
public class DBUtil {

    public  static final int DATABASE_VERSION =1; //database version
    //database
    public static final String DATABASE_NAME = "Sensor_Node_db";
    //tables inside database
    public static final String TABLE_NAME_NODE = "nodes";
    public static final String TABLE_NAME_NODE_DATA = "NodeData";
    //Node table columns
    public static final String COLUMN_NAME_KEY_ID =  "id";
    public static final String COLUMN_NAME_NODE_ADDRESS = "node_address";
    public static final String COLUMN_NAME_NODE_NAME= "node_name";
    //Node Data table columns
    public static final String COLUMN_NAME_TIME_AT_WAKEUP = "time_at_wakeup"; // wakeup time in hours,
    public static final String COLUMN_NAME_TIME_SINCE_WAKEUP = "time_since_wakeup"; // time of device since working in seconds
    public static final String COLUMN_NAME_NODE_TIME_SLOT = "node_time_slot";  //node slot number 11 to 180
    public static final String COLUMN_NAME_BATTERY_VOLTAGE = "battery_voltage"; //voltage upto 3300
    public static final String COLUMN_NAME_TIME_SYNCED = "time_synced"; // synced = 1, not synced =0;
    public static final String COLUMN_NAME_TIME_SYNC_REQUESTED = "time_sync_requested";
    public static final String COLUMN_NAME_TEMPERATURE = "Temperature";
    public static final String COLUMN_NAME_HUMIDITY = "Humidity";


}
