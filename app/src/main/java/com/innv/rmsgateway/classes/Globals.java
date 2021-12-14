package com.innv.rmsgateway.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.LoginActivity;
import com.innv.rmsgateway.data.DBHandler;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorDataDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import static android.content.ContentValues.TAG;

public class Globals {
    public static final int WS_RET_UNAUTHORIZED = 401;
    private static final int MODE_PRIVATE = 0;

    public static String appid="";
    public static String appid_temp="";
    public static String orgCode = "ps19";
    public static int lastWsReturnCode=0;
    public static String lastConnectionError="";
    public static final int SERVICE_AVAILABLE=0;
    public static int serviceHandle=SERVICE_AVAILABLE;
    public static boolean isServiceProcessing=false;

    public static final String APPLICATION_LOOKUP_LIST_NAME = "ApplicationLookups";
    public static DBHandler db;
    public static Context dbContext;
    public static DataSyncProcessEx dataSyncProcessEx=null;
    public static int Db_Version = 3;
    public static long lastConnTimeDiff=0;
    public static long httpSocketTimeOut =15000;
    public static long syncTimeout=600;
    public static String LOGIN_ERROR="";

    //public static String wsDomainName ="172.19.3.141:3005";
    public static String wsDomainName = "rmsportal.eastus.cloudapp.azure.com";
    //public static String wsDomainName = "timps.eastus.cloudapp.azure.com";
    public static String defaultServerName = "RMS";
    //public static String wsDomainName ="tips1.southeastasia.cloudapp.azure.com";
    //public static String wsPort="80";
    public static String wsPort = "443";
    public static String wsDomain = "https://" + wsDomainName + (wsPort.equals("") ? "" : (":" + wsPort) );
    public static String wsBaseURL=wsDomain+"/api/";
    public static User user = null;
    public static String userID="";

    public static boolean IsAutoUpdateEnabled;
    public static int SCREEN_ORIENTATION = 1;

    public static final String  OBSERVABLE_MESSAGE_DATA_CHANGED ="1";
    public static final String  OBSERVABLE_MESSAGE_DATA_SENT ="2";
    public static final String  OBSERVABLE_MESSAGE_NETWORK_PULL ="3";
    public static final String  OBSERVABLE_MESSAGE_NETWORK_PUSH ="4";
    public static final String  OBSERVABLE_MESSAGE_NETWORK_CONNECTED ="5";
    public static final String  OBSERVABLE_MESSAGE_NETWORK_DISCONNECTED ="6";
    public static final String  OBSERVABLE_MESSAGE_UPLOADING_IMAGE ="7";
    public static final String  OBSERVABLE_MESSAGE_TOKEN_STATUS ="8";
    public static final String  OBSERVABLE_MESSAGE_LANGUAGE_CHANGED ="9";


    public static final int SERVICE_STATUS_NOT_CONNECTED =0;
    public static final int SERVICE_STATUS_CONNECTED =1;
    public static final int SERVICE_STATUS_PULL =2;
    public static final int SERVICE_STATUS_PUSH =3;

    public static boolean useCelsius = true;
    public static int NORMAL = Color.parseColor("#99C24D");
    public static int ALERT = Color.parseColor("#C1272D");
    public static int INACTIVE = Color.parseColor("#006E90");
    public static int WARNING = Color.parseColor("#F18F01");
    public static int BELOW_THRESHOLD = Color.parseColor("#41BBD9");
    public static int NODE_OFFLINE_TIME = 6; //minutes
    public static LoginActivity loginContext;

    public static SharedPreferences pref;
    public static boolean tokenExpired = false;
    public static boolean BLE_Available = false;


    public static TreeMap<Date, Double> temperatureData = new TreeMap<>();
    public static TreeMap<Date, Integer> humidityData = new TreeMap<>();

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


    private static boolean screenIsLarge(Context ctx)
    {
        int screenMask = ctx.getResources().getConfiguration().screenLayout;
        if ( ( screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return true;
        }

        else if ( (screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            return true;
        }

        return false;

    }


    public static void setDbContext(Context context) {
        dbContext = context;
        pref = context.getSharedPreferences("RMS_SHARED_PREF", MODE_PRIVATE);

        useCelsius = pref.getBoolean("isTempC", true);  // getting boolean
        wsDomainName = pref.getString("PREFS_KEY_SELECTED_SERVER", wsDomainName);  // getting boolean
        wsPort = pref.getString("PREFS_KEY_SELECTED_PORT", wsPort);  // getting boolean
        appid = pref.getString("PREFS_KEY_SERVER_TOKEN", appid);  // getting boolean

        db=DBHandler.getInstance(context);
        ProfileManager.init();
        DefrostProfileManager.init();
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getPingAddress(String server, String port){
        return "https://" + server  + "/api/sensorlog/sensordata";
    }

    public static boolean isServerAvailable() {
        // TODO: Make a new request with short reply
        String userString = JsonWebService.getJSON(getPingAddress(wsDomainName, wsPort), 5000);
        try {
            if (userString != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendBroadcastMessage(String messageName, String messageData){
        Intent intent =new Intent();
        intent.putExtra("messageName",messageName);
        intent.putExtra("messageData",messageData);
        ObservableObject.getInstance().updateValue(intent);
        return  true;
    }

    public static boolean offlineMode=true;
    public static void setOfflineMode(boolean offline){
        offlineMode=offline;
    }

    public static int webUploadMessageLists(final Context context, String orgCode, JSONObject data)
    {

        String url = "https://rmsportal.eastus.cloudapp.azure.com/api/sensorlog/sensordata";
        String jsonObject = null;
        int intRetValue=0;
        boolean dataChanged=false;

        DBHandler db = Globals.db;
        //Get data from db here;
        List<StaticListItem> items = new ArrayList<>();/*db.getMsgListItems(orgCode,"status="
                +MESSAGE_STATUS_READY_TO_POST); //Ready to be posted*/

        long connTimeDiff=Globals.lastConnTimeDiff/1000;

        if(connTimeDiff>150 || items.size()>0)
        {
            Log.i(TAG,"Last Conn Time Diff:"+ String.valueOf(connTimeDiff)+ " (s)");
            //If disconnected more than 5 mins check sod 1st
            //if(checkSODChange(context)){
            //    db.close();
            //    return intRetValue;
            //}
        }

       // if(items.size() > 0)
        {
            boolean isServerAvailable=isServerAvailable();
            setOfflineMode(!isServerAvailable);
            if(!isServerAvailable){
                return -1;
            }
/*            JSONArray ja=new JSONArray();
            for(StaticListItem item:items) {

            }*/

            String strRetValue=null;
            System.out.println("Sending Data");
            long tStart = System.currentTimeMillis();
            try {
                System.out.println(data.toString());
                strRetValue = JsonWebService.postJSON(url, data, (int)Globals.httpSocketTimeOut);
            }catch (Exception e)
            {
                Log.e(TAG,e.toString());
            }
            long tEnd = System.currentTimeMillis();
            long tDelta = tEnd - tStart;
            double elapsedSeconds = tDelta / 1000.0;
            System.out.println("elapsedSeconds");
            System.out.println(elapsedSeconds);
            if(strRetValue==null)
            {
                if(db !=null) {
                    //db.close();
                }
                //ERROR
                return -1;
            }
            strRetValue=strRetValue.replace("\"","");
            strRetValue=strRetValue.replace("\n","");
            Log.i("webUploadMessageLists","Sending data to webserivce size:"+ items.size()+" ret:"+strRetValue);
            try {
              //  ringtone1(context,0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(strRetValue.equals("success"))
            {
                for(StaticListItem item:items){
                    item.setHoldCode(false);
                }
                //intRetValue=db.UpdateMsgListStatus(orgCode,items,MESSAGE_STATUS_POSTED);
           //     db.RemoveMsgListItems(items,MESSAGE_STATUS_READY_TO_POST);
                ArrayList<String> sentItemList=new ArrayList<>();
                for(StaticListItem item:items){
                    sentItemList.add(item.getListName());
                }
                sendBroadcastMessage(OBSERVABLE_MESSAGE_DATA_SENT,sentItemList.toString());

            }else if(strRetValue.startsWith("error:") && items.size()==1){
             //   items.get(0).setStatus(MESSAGE_STATUS_ERROR);
             //   db.AddOrUpdateMsgList(items.get(0).getListName(),orgCode,items.get(0),MESSAGE_STATUS_ERROR);
            }
        }
        //db.close();
        return intRetValue;
    }



    public static int webUploadMessageLists(final Context context, String orgCode, List<StaticListItem> items)    {
        //"{appid}/{orgcode:int}"
        String url = wsBaseURL + "msglist/" ;
        String jsonObject = null;
        int intRetValue=0;
        boolean dataChanged=false;

        if(items.size()>0)
        {
            JSONArray ja=new JSONArray();
            for(StaticListItem item:items)
            {
/*                if(Inbox.isLocalJPCode(item.getCode())){
                    item.setHoldCode(true);
                }
                ja.put(item.getMultiJSONObject());*/
            }
            String strRetValue=null;

            try {
                strRetValue = JsonWebService.postJSON(url, ja.toString(), (int)Globals.httpSocketTimeOut);
                System.out.println(ja.toString());
            }catch (Exception e)
            {
                Log.e(TAG,e.toString());
                return -1;
            }
            if(strRetValue==null){
                Log.e(TAG,"Server return error");
                return 0;
            }
            strRetValue=strRetValue.replace("\"","");
            strRetValue=strRetValue.replace("\n","");
            Log.i("webUploadMessageLists","Sending data to webserivce size:"+ items.size()+" ret:"+strRetValue);


         //   ringtone1(context,0);
            if(strRetValue.equals("success"))
            {
                return 1;
            }
        }
        return 0;
    }

    public static HashMap<String , Object> getHashMapJSONObject(JSONObject jo){
        HashMap<String , Object> hmBackupValues=new HashMap<>();
        for (Iterator<String> it = jo.keys(); it.hasNext(); ) {
            String key = it.next();
            try {
                if(!(jo.get(key) instanceof JSONObject || jo.get(key) instanceof JSONArray)){
                    hmBackupValues.put(key,jo.get(key));
                }else if(jo.get(key) instanceof JSONArray){
                    hmBackupValues.put(key,jo.getJSONArray(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return hmBackupValues;
    }

    public static boolean webLogin(Context context,String orgCode,String userID,String password)
    {
        if(isInternetAvailable(context))
        {
            String url="https://rmsportal.eastus.cloudapp.azure.com/api/login";
            String jsonObject=null;
            //jsonObject=JsonWebService.getJSONPOST(url,5000);
            try {
                JSONObject joInputData = new JSONObject();
                JSONObject joInputDetail = new JSONObject();
                joInputDetail.put("email", userID);
                joInputDetail.put("password", password);
                joInputData.put("user", joInputDetail);
                jsonObject = JsonWebService.postJSON(url, joInputData.toString(), 5000);
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }
            if(jsonObject==null)
            {
                return  false;
            }
            try{
                JSONObject jo =new JSONObject(jsonObject);
                if(!jo.optString("err","").equals("")){
                    LOGIN_ERROR=jo.optString("err","");
                    return false;
                }
                LOGIN_ERROR="";

                appid="";
                appid_temp=jo.getString("token");

                //if(webPullRequest(context,"")) {
                DBHandler db = Globals.db;//new DBHandler(getDBContext());
                db.RemoveList("user", "");
                StaticListItem item = new StaticListItem();
                item.setOrgCode("");
                item.setListName("user");
                item.setCode("1");
                JSONObject joResult = jo.getJSONObject("result");
                item.setDescription(joResult.getString("name"));
                item.setOptParam1(jo.getString("token"));
                item.setOptParam2(joResult.toString());
                db.AddList("user", "", item);
                //db.close();
                loadLoginData();
                return true;
                //}

            }catch (Exception e)
            {
                return false;
            }
        }
        return  false;
    }

    public static void loadLoginData() {
        DBHandler db=Globals.db;//new DBHandler(getDBContext());
        List<StaticListItem> items= db.getListItems("user","","1");
        //db.close();
        StaticListItem item=null;
        if(items.size()>0) {
            item = items.get(0);
            userID = item.getDescription();
            try {
                JSONObject jo=new JSONObject(item.getOptParam2());
                orgCode=jo.getString("tenantId");
                appid=item.getOptParam1();//
                user = new User(jo);
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }

        }

    }

    public static void setScreenOrientation(Activity ctx){
        if(screenIsLarge(ctx)){
            ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else{
            ctx.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void setDomain() {
        wsDomain = "http://" + wsDomainName + (wsPort.equals("") ? "" : (":" + wsPort));
        wsBaseURL = wsDomain + "/api/";
    }

}