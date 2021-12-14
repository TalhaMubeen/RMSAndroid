package com.innv.rmsgateway.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.innv.rmsgateway.classes.Globals;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper{
    // Database Version
    private static DBHandler sInstance;
    private static final int DATABASE_VERSION = Globals.Db_Version;

    // Database Name
    private static final String DATABASE_NAME = "RMS";

    //TABLE NAME
    private static final String TABLE_DEVICES_LOOKUPS = "devicelookup";
    private static final String TABLE_DEVICES_LOG = "devicelog";
    private static final String TABLE_DEVICES_ALERTS = "alertsdata";
    private static final String TABLE_RMS_PROFILES = "profileList";
    private static final String TABLE_RMS_DEFROST_PROFILES = "defrostProfileList";

    public static synchronized DBHandler getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    public DBHandler(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DYNAPPLOOKUP_TABLE="CREATE TABLE " + TABLE_DEVICES_LOOKUPS
                + " ( ORGCODE TEXT NOT NULL,"
                + " LISTNAME  NOT NULL,"
                + " CODE TEXT  NOT NULL,"
                + " DESCRIPTION TEXT,"
                + " OPTPARAM1 TEXT,"
                + " OPTPARAM2 TEXT,"
                + "PRIMARY KEY(ORGCODE,LISTNAME,CODE))";
        db.execSQL(CREATE_DYNAPPLOOKUP_TABLE);

        String CREATE_DYNLOGLOOKUP_TABLE="CREATE TABLE " + TABLE_DEVICES_LOG
                + " ( ORGCODE TEXT NOT NULL,"
                + " LISTNAME  NOT NULL,"
                + " CODE TEXT  NOT NULL,"
                + " DATE TEXT NOT NULL,"
                + " TIMESTAMP TEXT NOT NULL,"
                + " OPTPARAM1 TEXT,"
                + "PRIMARY KEY(ORGCODE,LISTNAME,CODE, DATE, TIMESTAMP))";
        db.execSQL(CREATE_DYNLOGLOOKUP_TABLE);

        String CREATE_ALERTS_TABLE="CREATE TABLE " + TABLE_DEVICES_ALERTS
                + " ( ORGCODE TEXT NOT NULL,"
                + " LISTNAME  NOT NULL,"
                + " CODE TEXT  NOT NULL,"
                + " DATE TEXT NOT NULL,"
                + " TIMESTAMP TEXT NOT NULL,"
                + " ALERTTYPE TEXT NOT NULL,"
                + " ALERTSTATUS TEXT NOT NULL,"
                + " OPTPARAM1 TEXT,"
                + "PRIMARY KEY(ORGCODE,LISTNAME,CODE, DATE, TIMESTAMP, ALERTTYPE))";
        db.execSQL(CREATE_ALERTS_TABLE);

        String CREATE_PROFILES_TABLE="CREATE TABLE " + TABLE_RMS_PROFILES
                + " ( ORGCODE TEXT NOT NULL,"
                + " LISTNAME  NOT NULL,"
                + " TITLE TEXT NOT NULL,"
                + " OPTPARAM1 TEXT,"
                + "PRIMARY KEY(ORGCODE, LISTNAME, TITLE))";
        db.execSQL(CREATE_PROFILES_TABLE);

        String CREATE_DEFROST_PROFILES_TABLE="CREATE TABLE " + TABLE_RMS_DEFROST_PROFILES
                + " ( ORGCODE TEXT NOT NULL,"
                + " LISTNAME  NOT NULL,"
                + " TITLE TEXT NOT NULL,"
                + " OPTPARAM1 TEXT,"
                + "PRIMARY KEY(ORGCODE, LISTNAME, TITLE))";
        db.execSQL(CREATE_DEFROST_PROFILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES_LOOKUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES_ALERTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RMS_PROFILES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RMS_DEFROST_PROFILES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES_LOOKUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES_ALERTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RMS_PROFILES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RMS_DEFROST_PROFILES);
        onCreate(db);
    }

    public boolean clearAllData() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_DEVICES_LOOKUPS);
            db.execSQL("DELETE FROM " + TABLE_DEVICES_LOG);
            db.execSQL("DELETE FROM " + TABLE_DEVICES_ALERTS);
            db.execSQL("DELETE FROM " + TABLE_RMS_PROFILES);
            db.execSQL("DELETE FROM " + TABLE_RMS_DEFROST_PROFILES);
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.e("clearAllData","DBHandler:"+e.toString());
            return  false;
        }
        return  true;
    }

    public void RemoveList(String listName,String orgCode) {
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_DEVICES_LOOKUPS +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"'";
        db.execSQL(sql);
    }

    public void RemoveList(String listName,String orgCode,String code) {
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_DEVICES_LOOKUPS +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"' AND " +
                "code='"+ code+"'";

        db.execSQL(sql);
    }

    public void RemoveLogs(String listName,String orgCode,String code) {
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_DEVICES_LOG +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"' AND " +
                "CODE='"+ code+"'";

        db.execSQL(sql);
    }

    public void RemoveAlerts(String listName,String orgCode,String code) {

        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_DEVICES_ALERTS +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"' AND " +
                "code='"+ code+"'";

        db.execSQL(sql);
    }

    public List<StaticListItem> getDefrostProfileList(String listName, String orgCode, String title) {
        List<StaticListItem> items = new ArrayList<StaticListItem>();
        String sql = "SELECT * FROM  " + TABLE_RMS_DEFROST_PROFILES +
                " WHERE LISTNAME='" + listName + "' AND " +
                " ORGCODE = '" + orgCode + "'";

        if (!title.isEmpty()) {
            sql += " AND TITLE='" + title + "'";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                StaticListItem item = new StaticListItem();
                item.setOrgCode(cursor.getString(0));
                item.setListName(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setOptParam1(cursor.getString(3));
                items.add(item);

            } while (cursor.moveToNext());
        }
        return items;
    }

    public boolean AddorUpdateDefrostProfileList(String listName,String orgCode, String title, StaticListItem array){
        List<StaticListItem> items = getDefrostProfileList(listName, orgCode, title);
        if(items.size()==1)
        {
            return UpdateDefrostProfile(listName, orgCode, title, array) > 0;
        }else
        {
            return AddDefrostProfile(listName, orgCode, title, array) > 0;
        }
    }

    public void RemoveDefrostProfile(String listName,String orgCode, String title){
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_RMS_DEFROST_PROFILES +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"' AND " +
                "TITLE='"+ title+"'";

        db.execSQL(sql);
    }

    private int UpdateDefrostProfile(String listName,String orgCode, String title, StaticListItem array) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = 0;
        String optParam1 = replaceSingleQoute(array.getOptParam1());

        try {
            String sql = "UPDATE " + TABLE_RMS_DEFROST_PROFILES + " SET "
                    + " OPTPARAM1='" + optParam1 + "'"
                    + " WHERE ORGCODE='" + orgCode + "' AND LISTNAME='" + listName + "' AND TITLE='" + title + "'";
            db.execSQL(sql);
            count++;
        } catch (Exception e) {
            Log.e("UpdateProfile", "Error:" + listName + ":" + array.getCode());

        }

        return count;
    }

    private int AddDefrostProfile(String listName,String orgCode, String title, StaticListItem array) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = 0;

        try {
            ContentValues values = new ContentValues();
            values.put("ORGCODE", orgCode);
            values.put("LISTNAME", listName);
            values.put("TITLE", title);
            values.put("OPTPARAM1", array.getOptParam1());
            db.insert(TABLE_RMS_DEFROST_PROFILES, null, values);
            count++;
        } catch (Exception e) {

        }

        return count;

    }



    public List<StaticListItem> getProfileList(String listName, String orgCode, String title) {
        List<StaticListItem> items = new ArrayList<StaticListItem>();
        String sql = "SELECT * FROM  " + TABLE_RMS_PROFILES +
                " WHERE LISTNAME='" + listName + "' AND " +
                " ORGCODE = '" + orgCode + "'";

        if (!title.isEmpty()) {
            sql += " AND TITLE='" + title + "'";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                StaticListItem item = new StaticListItem();
                item.setOrgCode(cursor.getString(0));
                item.setListName(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setOptParam1(cursor.getString(3));
                items.add(item);

            } while (cursor.moveToNext());
        }
        return items;
    }

    public boolean AddorUpdateProfileList(String listName,String orgCode, String title, StaticListItem array){
        List<StaticListItem> items = getProfileList(listName, orgCode, title);
        if(items.size()==1)
        {
            return UpdateProfile(listName, orgCode, title, array) > 0;
        }else
        {
            return AddProfile(listName, orgCode, title, array) > 0;
        }
    }

    private int UpdateProfile(String listName,String orgCode, String title, StaticListItem array) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = 0;
        String optParam1 = replaceSingleQoute(array.getOptParam1());

        try {
            String sql = "UPDATE " + TABLE_RMS_PROFILES + " SET "
                    + " OPTPARAM1='" + optParam1 + "'"
                    + " WHERE ORGCODE='" + orgCode + "' AND LISTNAME='" + listName + "' AND TITLE='" + title + "'";
            db.execSQL(sql);
            count++;
        } catch (Exception e) {
            Log.e("UpdateProfile", "Error:" + listName + ":" + array.getCode());

        }

        return count;
    }

    public void RemoveProfile(String listName,String orgCode, String title){
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_RMS_PROFILES +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"' AND " +
                "TITLE='"+ title+"'";

        db.execSQL(sql);
    }

    private int AddProfile(String listName,String orgCode, String title, StaticListItem array) {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = 0;

        try {
            ContentValues values = new ContentValues();
            values.put("ORGCODE", orgCode);
            values.put("LISTNAME", listName);
            values.put("TITLE", title);
            values.put("OPTPARAM1", array.getOptParam1());
            db.insert(TABLE_RMS_PROFILES, null, values);
            count++;
        } catch (Exception e) {

        }

        return count;

    }



    public int AddOrUpdateList(String listName,String orgCode, String code, StaticListItem array) {
        List<StaticListItem> items = getListItems(listName, orgCode, "", "code='" + code + "'");
        if(items.size()==1)
        {
            return UpdateList(listName,orgCode,array);
        }else
        {
            return AddList(listName,orgCode,array);
        }
    }

    public int AddSensorNodeLogs(String listName, String orgCode, String code, String date, String timeStamp , StaticListItem data){
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;

        try {
            ContentValues values = new ContentValues();
            values.put("ORGCODE", orgCode);
            values.put("LISTNAME", listName);
            values.put("CODE", code);
            values.put("DATE", date);
            values.put("TIMESTAMP", timeStamp);
            values.put("OPTPARAM1", data.getOptParam1());
            db.insert(TABLE_DEVICES_LOG, null, values);
            count++;
        } catch (Exception e) {

        }
        return count;
    }

    public int AddSensorNodeAlerts(String listName, String orgCode, String code, String date, String timeStamp, String type, String status, StaticListItem data){
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;

        try {
            ContentValues values = new ContentValues();
            values.put("ORGCODE", orgCode);
            values.put("LISTNAME", listName);
            values.put("CODE", code);
            values.put("DATE", date);
            values.put("TIMESTAMP", timeStamp);
            values.put("ALERTTYPE", type);
            values.put("ALERTSTATUS", status);
            values.put("OPTPARAM1", data.getOptParam1());
            db.insert(TABLE_DEVICES_ALERTS, null, values);
            count++;
        } catch (Exception e) {

        }
        return count;
    }

    public int UpdateSensorNodeAlerts(String listName,String orgCode, String code, String date, String timeStamp,String type, String alertStatus, StaticListItem data) {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;
        String optParam1=replaceSingleQoute(data.getOptParam1());

        try {
            String sql="UPDATE " + TABLE_DEVICES_ALERTS + " SET "
                    + " ALERTSTATUS='" + alertStatus +"'"
                    + ", OPTPARAM1='" + optParam1 +"'"
                    +" WHERE ORGCODE='" + orgCode
                    +"' AND LISTNAME='" + listName
                    +"' AND CODE='" + code
                    +"' AND DATE='" + date
                    +"' AND TIMESTAMP='" + timeStamp
                    +"' AND ALERTTYPE='" + type +"'";
            db.execSQL(sql);
            count++;
        }
        catch (Exception e)
        {
            Log.e("UpdateList","Error:"+listName+":"+data.getCode());

        }

        return count;

    }

/*    public int AddOrUpdateNodeAlerts(String listName, String orgCode, String code,
                                     String date, String timeStamp, String type, String alertStatus, StaticListItem data) {
       // List<StaticListItem> items = getSensorNodeAlerts(listName, orgCode, code, prevType + code + "'");
        if(alertStatus.length() > 0) {
            return UpdateSensorNodeAlerts(listName, orgCode, code, type, alertStatus, data);
        }else {
            return AddSensorNodeAlerts(listName, orgCode, code, date, timeStamp, type, data);
        }
    }*/

    public List<StaticListItem> getSensorNodeLogsBetween(String listName, String orgCode, String code, String date1, String date2)
    {
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT * FROM  " + TABLE_DEVICES_LOG +
                " WHERE LISTNAME='" + listName +
                "' AND " + " ORGCODE= '" + orgCode +
                "' AND CODE='" + code +
                "' AND DATE BETWEEN '" +date1 +"' AND '"+ date2 + "'";

        try {
            SQLiteDatabase db=this.getReadableDatabase();
            Cursor cursor= db.rawQuery(sql,null);
            if(cursor.moveToFirst())
            {
                do{
                    StaticListItem item=new StaticListItem();
                    item.setOrgCode(cursor.getString(0));
                    item.setListName(cursor.getString(1));
                    item.setCode(cursor.getString(2));
                    item.setDate(cursor.getString(3));
                    item.setTimeStamp(cursor.getString(4));
                    item.setOptParam1(cursor.getString(5));
                    items.add(item);

                }while(cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }


    public List<StaticListItem> getSensorNodeLogs(String listName, String orgCode, String code, String date)
    {
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT * FROM  " + TABLE_DEVICES_LOG +
                " WHERE LISTNAME='" + listName +
                "' AND " + " ORGCODE = '" + orgCode +"'" +
                 " AND code='" + code + "'";

        if(!date.equals("") )
        {
            sql += " AND DATE='" + date + "'";
        }
        try {
            SQLiteDatabase db=this.getReadableDatabase();
            Cursor cursor= db.rawQuery(sql,null);
            if(cursor.moveToFirst())
            {
                do{
                    StaticListItem item=new StaticListItem();
                    item.setOrgCode(cursor.getString(0));
                    item.setListName(cursor.getString(1));
                    item.setCode(cursor.getString(2));
                    item.setDate(cursor.getString(3));
                    item.setTimeStamp(cursor.getString(4));
                    item.setOptParam1(cursor.getString(5));
                    items.add(item);

                }while(cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<StaticListItem> GetSensorNodeAlerts(String listName, String orgCode, String code, String type, String startDay, String endDay) {
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT * FROM  " + TABLE_DEVICES_ALERTS +
                " WHERE LISTNAME='" + listName +
                "' AND " + " ORGCODE = '" + orgCode +"'";

        if(!code.isEmpty()) {
            sql += " AND code = '" + code + "'";
        }

        if(!type.isEmpty()) {
            sql += " AND ALERTTYPE='" + type + "'";
        }

        if(!startDay.isEmpty() && !endDay.isEmpty()){
          //  sql +=  " AND  DATE_FORMAT(DATE,'%Y-%m-%d') " +     " BETWEEN '" + startDay + "' AND '" + endDay + "'";
            sql += " AND DATE >= '" + startDay +"'" + " AND DATE <= '" + endDay +"'"  ;;
        }

        try {
            SQLiteDatabase db=this.getReadableDatabase();
            Cursor cursor= db.rawQuery(sql,null);
            if(cursor.moveToFirst()) {
                do{
                    StaticListItem item = new StaticListItem();
                    item.setOrgCode(cursor.getString(0));
                    item.setListName(cursor.getString(1));
                    item.setCode(cursor.getString(2));
                    item.setDate(cursor.getString(3));
                    item.setTimeStamp(cursor.getString(4));
/*                    item.setAlertType(cursor.getString(5));*/
/*                    item.setAlertStatus(cursor.getString(6));*/
                    item.setOptParam1(cursor.getString(7));
                    items.add(item);

                }while(cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public int AddList(String listName,String orgCode, StaticListItem array)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;

        try {

            ContentValues values=new ContentValues();
            values.put("ORGCODE",orgCode);
            values.put("LISTNAME",listName);
            values.put("CODE",array.getCode());
            values.put("DESCRIPTION",array.getDescription());
            values.put("OPTPARAM1",array.getOptParam1());
            values.put("OPTPARAM2",array.getOptParam2());
            db.insert(TABLE_DEVICES_LOOKUPS,null,values);
            count++;
        }
        catch (Exception e)
        {

        }

        return count;

    }

    public int AddList(String listName,String orgCode, JSONArray array)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;

        for(int i=0;i<array.length();i++)
        {
            try {
                JSONObject jo = array.getJSONObject(i);
                ContentValues values=new ContentValues();
                values.put("ORGCODE",orgCode);
                values.put("LISTNAME",listName);
                values.put("CODE",jo.getString("code"));
                values.put("DESCRIPTION",jo.getString("desc"));
                values.put("OPTPARAM1",jo.getString("opt1"));
                values.put("OPTPARAM2",jo.getString("opt2"));
                db.insert(TABLE_DEVICES_LOOKUPS,null,values);
                count++;
            }
            catch (Exception e)
            {
                break;
            }
        }
        return count;

    }

    public int UpdateList(String listName,String orgCode, StaticListItem array)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;
        String optParam1=replaceSingleQoute(array.getOptParam1());

        try {
            String sql="UPDATE " + TABLE_DEVICES_LOOKUPS + " SET "
                    + "  DESCRIPTION='" + array.getDescription() +"'"
                    + ", OPTPARAM1='" + optParam1 +"'"
                    + ", OPTPARAM2='" + array.getOptParam2() +"'"
                    +" WHERE ORGCODE='" + orgCode +"' AND LISTNAME='" + listName +"' AND CODE='" + array.getCode() +"'";
            db.execSQL(sql);
            count++;
        }
        catch (Exception e)
        {
            Log.e("UpdateList","Error:"+listName+":"+array.getCode());

        }

        return count;

    }

    public List<StaticListItem> getListItems(String listName , String orgCode , String code)
    {
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT * FROM  " + TABLE_DEVICES_LOOKUPS +
                " WHERE LISTNAME='" + listName + "' AND " +
                " ORGCODE = '" + orgCode +"'" ;
        if(!code.equals("") )
        {
            sql += " AND code='" + code + "'";
        }
        try {
            SQLiteDatabase db=this.getReadableDatabase();
            Cursor cursor= db.rawQuery(sql,null);
            if(cursor.moveToFirst())
            {
                do{
                    StaticListItem item=new StaticListItem();
                    item.setOrgCode(cursor.getString(0));
                    item.setListName(cursor.getString(1));
                    item.setCode(cursor.getString(2));
                    item.setDescription(cursor.getString(3));
                    item.setOptParam1(cursor.getString(4));
                    item.setOptParam2(cursor.getString(5)) ;
                    items.add(item);

                }while(cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<StaticListItem> getListItems(String listName , String orgCode , String code , String  criteria)
    {
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT * FROM  " + TABLE_DEVICES_LOOKUPS +
                " WHERE LISTNAME='" + listName + "' AND " +
                " ORGCODE = '" + orgCode +"'" ;
        if(!criteria.equals("") )
        {
            sql+= " AND "+criteria;
        }
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,null);
        //System.out.println("Row Count:"+cursor.getCount());
        if(cursor.moveToFirst())
        {
            do{
                StaticListItem item=new StaticListItem();
                item.setOrgCode(cursor.getString(0));
                item.setListName(cursor.getString(1));
                item.setCode(cursor.getString(2));
                item.setDescription(cursor.getString(3));
                item.setOptParam1(cursor.getString(4));
                item.setOptParam2(cursor.getString(5)) ;
                items.add(item);

            }while(cursor.moveToNext());
        }
        return items;
    }

    public int getListItemCount(String listName , String orgCode , String  criteria)
    {
        int count=0;
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT count(*) FROM  " + TABLE_DEVICES_LOOKUPS +
                " WHERE LISTNAME='" + listName + "' AND " +
                " ORGCODE = '" + orgCode +"'" ;
        if(!criteria.equals("") )
        {
            sql+= " AND "+criteria;
        }
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,null);
        if(cursor.moveToFirst())
        {
            count=cursor.getInt(0);
        }
        return count;
    }

    private String replaceSingleQoute(String value)
    {
        String strOut=value;
        strOut=strOut.replaceAll("'", "\''");
        return strOut;
    }

    /*Lists*/
    public HashMap<String, String> getDeviceLookupList(String strListName)
    {
        String sql="SELECT CODE, DESCRIPTION FROM "+ TABLE_DEVICES_LOOKUPS
                +" WHERE ORGCODE='"+ Globals.orgCode+"' AND LISTNAME='"+ Globals.APPLICATION_LOOKUP_LIST_NAME
                +"' AND OPTPARAM2='"+  strListName +"'";
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,null);
        HashMap<String ,String >list=new HashMap<>();
        if(cursor.moveToFirst()) {

            do{
                String strKey=cursor.getString(0);
                String strItem=cursor.getString(1);
                if(!cursor.getString(0).equals(""))
                {

                    list.put(strKey,strItem);
                }
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;
    }

    public HashMap<String, String> getDeviceLookupList(String strListName, String desc, int mode)
    {
        String sql="SELECT CODE, DESCRIPTION,OPTPARAM1 FROM "+ TABLE_DEVICES_LOOKUPS
                +" WHERE ORGCODE='"+Globals.orgCode+"' AND LISTNAME='"+ Globals.APPLICATION_LOOKUP_LIST_NAME
                +"' AND OPTPARAM2='"+  strListName +"'" + (!desc.equals("")?(" AND DESCRIPTION='"+ desc +"'"):""  );
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,null);
        HashMap<String ,String >list=new HashMap<>();
        if(cursor.moveToFirst()) {

            do{
                String strKey=cursor.getString(0);
                if(mode==1){
                    strKey=cursor.getString(1);
                }
                String strItem=cursor.getString(2);
                if(!cursor.getString(0).equals(""))
                {

                    list.put(strKey,strItem);
                }
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;

    }

    public HashMap<String, String> geDeviceLookupList(String strListName, String desc)
    {
        String sql="SELECT CODE, DESCRIPTION,OPTPARAM1 FROM "+ TABLE_DEVICES_LOOKUPS
                +" WHERE ORGCODE='"+Globals.orgCode+"' AND LISTNAME='"+ Globals.APPLICATION_LOOKUP_LIST_NAME
                +"' AND OPTPARAM2='"+  strListName +"'" + (!desc.equals("")?(" AND DESCRIPTION='"+ desc +"'"):""  );
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,null);
        HashMap<String ,String >list=new HashMap<>();
        if(cursor.moveToFirst()) {

            do{
                String strKey=cursor.getString(1);
                String strItem=cursor.getString(2);
                if(!cursor.getString(0).equals(""))
                {

                    list.put(strKey,strItem);
                }
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;

    }

    public HashMap<String, String> getLookupListObj(String strListName, String desc)
    {
        String sql="SELECT CODE, DESCRIPTION,OPTPARAM1 FROM "+ TABLE_DEVICES_LOOKUPS
                +" WHERE ORGCODE='"+Globals.orgCode+"' AND LISTNAME='"+ strListName
                +"' AND DESCRIPTION='"+ desc +"'" ;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,null);
        HashMap<String ,String >list=new HashMap<>();
        if(cursor.moveToFirst()) {

            do{
                String strKey=cursor.getString(1);
                String strItem=cursor.getString(2);
                if(!cursor.getString(0).equals(""))
                {

                    list.put(strKey,strItem);
                }
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;

    }

    public HashMap<String, String> getLookupList(String strListName)
    {
        String sql="SELECT CODE, DESCRIPTION FROM "+ TABLE_DEVICES_LOOKUPS
                +" WHERE ORGCODE='"+Globals.orgCode+"' AND LISTNAME='"+  strListName +"'";
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(sql,null);
        HashMap<String ,String >list=new HashMap<>();
        if(cursor.moveToFirst()) {

            do{
                String strKey=cursor.getString(0);
                String strItem=cursor.getString(1);
                if(!cursor.getString(0).equals(""))
                {

                    list.put(strKey,strItem);
                }
            }while(cursor.moveToNext());

        }
        cursor.close();
        return list;
    }

}
