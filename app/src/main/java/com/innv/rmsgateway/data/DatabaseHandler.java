package com.innv.rmsgateway.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.innv.rmsgateway.utils.DBUtil;

/**
 *   created on 10 Sep-2021, it will handle data functionality or Create, Read, Update and Delete (CRUD)
 *
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {


    public DatabaseHandler( Context context,  String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DBUtil.DATABASE_NAME, null, DBUtil.DATABASE_VERSION);
    }
    public DatabaseHandler(Context context) {
        super(context, DBUtil.DATABASE_NAME, null, DBUtil.DATABASE_VERSION);
    }

    //we create our table
    @Override
    public void onCreate(SQLiteDatabase db) {
        //SQL - Structured Query Language
        String SENSOR_NODE_TABLE = "CREATE TABLE "+DBUtil.TABLE_NAME_NODE_DATA +"("
                                    +DBUtil.COLUMN_NAME_KEY_ID+ " INTEGER PRIMARY KEY,"+DBUtil.COLUMN_NAME_NODE_ADDRESS + "TEXT,"
                                    +DBUtil.COLUMN_NAME_NODE_NAME+" TEXT,"+DBUtil.COLUMN_NAME_TIME_AT_WAKEUP +"TEXT,"
                                    +DBUtil.COLUMN_NAME_TIME_SINCE_WAKEUP+" TEXT,"+DBUtil.COLUMN_NAME_NODE_TIME_SLOT+" TEXT,"
                                    +DBUtil.COLUMN_NAME_BATTERY_VOLTAGE+" TEXT,"+DBUtil.COLUMN_NAME_TIME_SYNCED+" TEXT,"
                                    +DBUtil.COLUMN_NAME_TIME_SYNC_REQUESTED+" TEXT,"+DBUtil.COLUMN_NAME_TEMPERATURE+" TEXT,"
                                    +DBUtil.COLUMN_NAME_HUMIDITY+"TEXT,"+")";

    }

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
    public static final String COLUMN_NAME_TIME_TEMPERATURE = "Temperature";
    public static final String COLUMN_NAME_TIME_HUMIDITY = "Humidity";


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
