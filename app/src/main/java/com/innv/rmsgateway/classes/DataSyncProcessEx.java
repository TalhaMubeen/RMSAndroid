package com.innv.rmsgateway.classes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.innv.rmsgateway.ActivityDashboard;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataSyncProcessEx extends AsyncTask<Context,Integer,String> {

    boolean isNetAvailable;
    boolean prevStatus;
    boolean blnFirstTime;
    boolean isTaskCanceled=false;
    boolean isRunning=false;
    boolean threadInterrupted=false;
    private DataSyncProcessListener listener=null;

    public interface  DataSyncProcessListener{
        void onStatusChanged(int status);

    }

    public static void addToSyncDataList(JSONObject obj) {
        syncDataList.add(obj);
    }

     static List<JSONObject> syncDataList = new ArrayList<>();

    public void setDataSyncProcessListener(DataSyncProcessListener listener){
        this.listener=listener;
    }
    Context context;

    public DataSyncProcessEx(){

    }
    public DataSyncProcessEx(Context context){
        this.context=context;
    }

    public void CancelTask() { isTaskCanceled=true; }


    int intCount;
    final int serviceHandle=2;
    //public boolean isNewDashEnable = true;
    private void raiseStatusChangeEvent(final int status){
        if(listener !=null && context !=null){
            ((ActivityDashboard) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onStatusChanged(status);
                }
            });

        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPreExecute() {
        if(context == null)
            isNetAvailable = Globals.isInternetAvailable(Globals.dbContext);
        else isNetAvailable = Globals.isInternetAvailable(context);

        if(isNetAvailable){
            raiseStatusChangeEvent(Globals.SERVICE_STATUS_CONNECTED);
        }else {
            raiseStatusChangeEvent(Globals.SERVICE_STATUS_NOT_CONNECTED);
        }

        blnFirstTime=true;
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {

        isRunning=false;
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        if(context == null) {
            isNetAvailable = Globals.isInternetAvailable(Globals.dbContext);
        }
        else{
            isNetAvailable = Globals.isInternetAvailable(context);
        }
        blnFirstTime=false;
        prevStatus=isNetAvailable;
        if(isNetAvailable)
        {
            raiseStatusChangeEvent(Globals.SERVICE_STATUS_CONNECTED);
        }
        else
        {
            raiseStatusChangeEvent(Globals.SERVICE_STATUS_NOT_CONNECTED);
        }


        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(Context... params) {
        intCount=0;
        this.context=params[0];

        while(Globals.IsAutoUpdateEnabled) {
            //final MenuItem actionCloud=Globals.menuIcon;

            if(Globals.dbContext != null && context == null) { this.context=Globals.dbContext; }

            Log.i("DataSyncServiceEx","doInBackground:"+ !isCancelled()+":");
            if(isTaskCanceled)
            {
                Log.i("DataSyncServiceEx","doInBackground:canceled  ");
                return "";
            }
            //isNetAvailable = Globals.isInternetAvailable(params[0]);
            //isNetAvailable = false;
            if(!Globals.orgCode.equals("") && !Globals.appid.equals("") && !Globals.isServiceProcessing && syncDataList.size() > 0)
            {
                Globals.isServiceProcessing=true;
                if(isNetAvailable) {
                    if (Globals.serviceHandle == Globals.SERVICE_AVAILABLE || Globals.serviceHandle == this.serviceHandle) {

                        Globals.serviceHandle = this.serviceHandle;
                        Globals.sendBroadcastMessage(Globals.OBSERVABLE_MESSAGE_NETWORK_PUSH,"");
                        raiseStatusChangeEvent(Globals.SERVICE_STATUS_PUSH);
                        int dataSent=Globals.webUploadMessageLists(this.context,Globals.orgCode, syncDataList.get(0));
                        syncDataList.remove(0);

                        Globals.serviceHandle = Globals.SERVICE_AVAILABLE;

                        if(Globals.lastWsReturnCode==Globals.WS_RET_UNAUTHORIZED ){
                            Globals.tokenExpired=true;
                            Globals.sendBroadcastMessage(Globals.OBSERVABLE_MESSAGE_TOKEN_STATUS,"");
                        }else
                        {
                            Globals.tokenExpired = false;
                            Globals.sendBroadcastMessage(Globals.OBSERVABLE_MESSAGE_NETWORK_CONNECTED,"");
                        }


                        try {
                            Thread.sleep(200);
                        }catch (Exception e) { }


                        intCount++;
                    }
                }else
                {
                    Globals.sendBroadcastMessage(Globals.OBSERVABLE_MESSAGE_NETWORK_DISCONNECTED,"");
                }
                Globals.isServiceProcessing=false;
            }
            publishProgress(intCount);

            try {
                long count=0;
                long maxValue=Globals.syncTimeout;
                while(count<maxValue) {
                    Thread.sleep(1000);
                    count++;
                    if(threadInterrupted){
                        threadInterrupted=false;
                        Log.i("DataSyncProcessEx","Thread Interrupted");
                        break;
                    }
                }
            }catch (Exception e) {
                Log.i("DataSyncProcessEx","Thread Interrupted");
            }
        }
        return "";
    }
    public void InterruptThread(){
        this.threadInterrupted=true;
    }
}
