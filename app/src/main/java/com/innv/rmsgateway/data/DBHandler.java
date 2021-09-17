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
    private static final String DATABASE_NAME = "RMS";

    //TABLE NAME
    private static final String TABLE_DEVICES_LOOKUPS = "devicelookup";

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
            db.execSQL("DELETE FROM " + TABLE_DEVICES_LOOKUPS);
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.e("clearAllData","DBHandler:"+e.toString());
            return  false;
        }
        return  true;
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

    public int AddOrUpdateList(String listName,String orgCode, String code, StaticListItem array)
    {
        List<StaticListItem> items = getListItems(listName, orgCode, "", "code='" + code + "'");
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
