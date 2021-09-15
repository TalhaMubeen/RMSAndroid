package com.innv.rmsgateway.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper{
    // Database Version
    private static DBHandler sInstance;
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "rms";

    //TABLE NAME
    private static final String TABLE_DEVICES_LOOKUPS ="devicelookups";
   // private static final String TABLE_MSG_APPLICATIONLOOKUPS="msg_applicationlookups";

    private static final  String pullListName="EmployeePullCriteriaList";

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

/*

        String CREATE_MSGAPPLOOKUP_TABLE="CREATE TABLE " + TABLE_MSG_APPLICATIONLOOKUPS
                + " ( ORGCODE TEXT NOT NULL,"
                + " LISTNAME  NOT NULL,"
                + " CODE TEXT  NOT NULL,"
                + " DESCRIPTION TEXT,"
                + " OPTPARAM1 TEXT,"
                + " OPTPARAM2 TEXT,"
                + " STATUS INTEGER,"
                + "PRIMARY KEY(ORGCODE,LISTNAME,CODE))";
*/

        db.execSQL(CREATE_DYNAPPLOOKUP_TABLE);
      //  db.execSQL(CREATE_MSGAPPLOOKUP_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES_LOOKUPS);
    //    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSG_APPLICATIONLOOKUPS);
        onCreate(db);
    }
    public boolean clearAllData()
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
       //     db.execSQL("DELETE FROM " + TABLE_MSG_APPLICATIONLOOKUPS);
            db.execSQL("DELETE FROM " + TABLE_DEVICES_LOOKUPS);
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.e("clearAllData","DBHandler:"+e.toString());
            return  false;
        }
        return  true;
    }
    public boolean isDataAvailable(String orgCode,String empCode)
    {
        List<StaticListItem> items=getListItems(pullListName,orgCode,"optparam2=''");
        if(items.size()==0)
        {
            return false;
        }
        for(StaticListItem item:items)
        {
            String listName=item.getDescription();
            String criteria=item.getOptParam1();
            if(!empCode.equals("")) {
                criteria = criteria.replace("@EMPCODE", String.valueOf(empCode));
            }else
            {
                criteria="";
            }
            int count=getListItemCount(listName,orgCode,criteria);
            if(count==0)
            {
                return  false;
            }
        }
        return true;
    }
    public boolean removeEmployeeData(String orgCode, String empCode)
    {
        List<StaticListItem> items=getListItems(pullListName,orgCode,"");
        if(items.size()==0)
        {
            return true;
        }
        for(StaticListItem item:items)
        {
            String listName=item.getDescription();
            String criteria=item.getOptParam1();
            if(!empCode.equals("")) {
                criteria = criteria.replace("@EMPCODE", String.valueOf(empCode));
            }
            else
            {
                criteria="";
            }
            RemoveList(listName, orgCode,criteria);
        }
        return true;
    }

    public void RemoveList(String listName,String orgCode)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_DEVICES_LOOKUPS +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"'";
        db.execSQL(sql);
    }
    public void RemoveList(String listName,String orgCode,String criteria)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_DEVICES_LOOKUPS +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"'";
        if(!criteria.equals(""))
        {
            sql+=" AND "+ criteria;
        }
        db.execSQL(sql);
    }

    public int AddOrUpdateList(String listName,String orgCode, StaticListItem array)
    {
        List<StaticListItem> items = getListItems(listName, orgCode, "", "code='" + array.getCode() + "'");
        if(items.size()==1)
        {
            return UpdateList(listName,orgCode,array);
        }else
        {
            return AddList(listName,orgCode,array);
        }
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


  /*  *//*message lists CURD methods*//*
    public List<StaticListItem> getMsgListItems(String orgCode , String  criteria)
    {
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT * FROM  " + TABLE_MSG_APPLICATIONLOOKUPS +
                " WHERE ORGCODE ='" + orgCode+"'";


        if(!criteria.equals("") )
        {
            sql+= " AND "+criteria;
        }
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
                item.setStatus(cursor.getInt(cursor.getColumnIndex("STATUS")));
                items.add(item);

            }while(cursor.moveToNext());
        }
        return items;

    }
    public List<StaticListItem> getMsgListItems(String listName , String orgCode , String  criteria)
    {
        List<StaticListItem> items=new ArrayList<StaticListItem>();
        String sql="SELECT * FROM  " + TABLE_MSG_APPLICATIONLOOKUPS +
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
            do{
                StaticListItem item=new StaticListItem();
                item.setOrgCode(cursor.getString(0));
                item.setListName(cursor.getString(1));
                item.setCode(cursor.getString(2));
                item.setDescription(cursor.getString(3));
                item.setOptParam1(cursor.getString(4));
                item.setOptParam2(cursor.getString(5)) ;
                item.setStatus(cursor.getInt(cursor.getColumnIndex("STATUS")));
                items.add(item);

            }while(cursor.moveToNext());
        }
        return items;
    }

    public void RemoveMsgList(String listName,String orgCode)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        String sql="Delete from " + TABLE_MSG_APPLICATIONLOOKUPS +
                " WHERE ORGCODE='" + orgCode +"' AND "+
                " LISTNAME='" + listName +"'";
        db.execSQL(sql);
    }

    public int AddMsgList(String listName,String orgCode, StaticListItem array,int status)
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
            values.put("STATUS",status);
            db.insert(TABLE_MSG_APPLICATIONLOOKUPS,null,values);
            count++;
        }
        catch (Exception e)
        {

        }

        return count;

    }
    public int AddMsgList(String listName,String orgCode, JSONArray array)
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
                values.put("STATUS",Globals.MESSAGE_STATUS_CREATED);
                db.insert(TABLE_MSG_APPLICATIONLOOKUPS,null,values);
                count++;
            }
            catch (Exception e)
            {
                break;
            }
        }
        return count;

    }

    public int UpdateMsgList(String listName,String orgCode, StaticListItem array,int status)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;
        String optParam1=replaceSingleQoute(array.getOptParam1());

        try {
            String sql="UPDATE " + TABLE_MSG_APPLICATIONLOOKUPS + " SET "
                    + "  DESCRIPTION='" + array.getDescription() +"'"
                    + ", OPTPARAM1='" + optParam1 +"'"
                    + ", OPTPARAM2='" + array.getOptParam2() +"'"
                    + ", STATUS=" + status
                    +" WHERE ORGCODE='" + orgCode +"' AND LISTNAME='" + listName +"' AND CODE='" + array.getCode()+"'";
            db.execSQL(sql);
            count++;
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }

        return count;

    }
    public int RemoveMsgListItems(List<StaticListItem> array,int status)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;
        for(StaticListItem item:array) {
            if(item.getStatus()==status) {
                item.setHoldCode(false);
                String sql = "Delete from " + TABLE_MSG_APPLICATIONLOOKUPS +
                        " WHERE ORGCODE='" + item.getOrgCode() + "' AND " +
                        " LISTNAME='" + item.getListName() + "' AND Code='"+ item.getCode() +"'";
                db.execSQL(sql);
                count++;
            }
        }
        return count;
    }
*/
    private String replaceSingleQoute(String value){
        String strOut=value;
        strOut=strOut.replaceAll("'", "\''");
        return strOut;
    }
 /*   public int UpdateMsgListStatus(String orgCode,List<StaticListItem> items,int status)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;

        try {
            for(StaticListItem array:items) {
                String optParam1=replaceSingleQoute(array.getOptParam1());
                String sql = "UPDATE " + TABLE_MSG_APPLICATIONLOOKUPS + " SET "
                        + " STATUS=" + status
                        + " WHERE ORGCODE='" + orgCode + "' AND LISTNAME='" + array.getListName() + "' AND CODE='" + array.getCode()+"'";
                db.execSQL(sql);
                count++;
            }
        }
        catch (Exception e)
        {

            Log.e("UpdateMsgList",""+e.toString());
        }

        return count;

    }

    public int UpdateMsgList(String orgCode,List<StaticListItem> items,int status)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        int count=0;

        try {
            for(StaticListItem array:items) {
                String optParam1=replaceSingleQoute(array.getOptParam1());
                String sql = "UPDATE " + TABLE_MSG_APPLICATIONLOOKUPS + " SET "
                        + "  DESCRIPTION='" + array.getDescription() + "'"
                        + ", OPTPARAM1='" + optParam1 + "'"
                        + ", OPTPARAM2='" + array.getOptParam2() + "'"
                        + ", STATUS=" + status
                        + " WHERE ORGCODE='" + orgCode + "' AND LISTNAME='" + array.getListName() + "' AND CODE='" + array.getCode()+"'";
                db.execSQL(sql);
                count++;
            }
        }
        catch (Exception e)
        {

            Log.e("UpdateMsgList",""+e.toString());
        }

        return count;

    }


    public int AddOrUpdateMsgList(String listName,String orgCode, StaticListItem array,int status)
    {
        List<StaticListItem> items=getMsgListItems(listName,orgCode,"code='" +array.getCode()+"'");
        if(items.size()==1)
        {
            return UpdateMsgList(listName,orgCode,array,status);
        }else
        {
            return AddMsgList(listName,orgCode,array,status);
        }
    }*/

    /*Lists*/
    public HashMap<String, String> getDeviceLookupList(String strListName){
        String sql="SELECT CODE, DESCRIPTION FROM "+ TABLE_DEVICES_LOOKUPS
                +" WHERE ORGCODE='"+Globals.orgCode+"' AND LISTNAME='"+ Globals.APPLICATION_LOOKUP_LIST_NAME
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
    public HashMap<String, String> getDeviceLookupList(String strListName, String desc, int mode){
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
    public HashMap<String, String> geDeviceLookupList(String strListName, String desc){
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
    public HashMap<String, String> getLookupListObj(String strListName, String desc){
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

    public HashMap<String, String> getLookupList(String strListName){
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

    /*SQL*/
/*    public int  getTotalOrders(int empCode)
    {
        String sql="SELECT COUNT(CODE) FROM "+TABLE_MSG_APPLICATIONLOOKUPS
                + " WHERE LISTNAME='OrderList' AND DESCRIPTION='" + empCode +"'";
        Cursor c=fetch(sql);
        if(c.moveToNext())
        {
            return c.getInt(0);
        }
        c.close();

        return 0;
    }*/

/*    public Cursor fetch(String strSQL)
    {
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor= db.rawQuery(strSQL,null);
        db.close();
        return  cursor;
    }*/
}
