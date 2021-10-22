package com.innv.rmsgateway.classes;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.util.Log;

import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.interfaces.NotificationAlertsCallback;
import com.innv.rmsgateway.sensornode.SensorNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlertManager {

    static Map<String,NotificationAlertsCallback> alertCallback = new HashMap<>();
    private static Handler mHandler;
    private static long mDelay = 0;
    private static long mTimeNow = System.currentTimeMillis();

    public static List<AlertData> getAllNodesAlertList() {
        List<AlertData> data = new ArrayList<>();
        alertsMap.keySet().forEach(x ->{
            data.addAll(alertsMap.get(x));
        });

        return data;
    }


    static Map<String, List<AlertData>> alertsMap = new HashMap<>();

    static Runnable r = new Runnable() {
        @Override
        public void run() {
            mTimeNow += mDelay;
            ProcessAlerts();
            if (mHandler != null) {
                mHandler.postDelayed(r, mDelay);
            }
        }
    };

    public static void stopTimer() {
        if (mHandler != null) {
            mHandler = null;
            mTimeNow = 0;
        }
    }

    public static void parseListItems(List<StaticListItem> data) {

        if(mHandler == null){
            mDelay = 1000;
            mHandler = new Handler();
            ProcessAlerts();
            mHandler.postDelayed(r, mDelay);
        }

        alertsMap.clear();
        List<SensorNode> lst = NodeDataManager.getPreCheckedNodes();
        data.forEach(item -> {
            try {
                JSONObject jsonObject = new JSONObject(item.getOptParam1());
                AlertData newAlert = new AlertData();
                if (newAlert.parseJsonObject(jsonObject)) {
                    lst.forEach(node ->{

                        if(node.getMacID().equals(newAlert.getNodeMacAddress())){

                            if (!alertsMap.containsKey(newAlert.getNodeMacAddress())) {
                                alertsMap.put(newAlert.getNodeMacAddress(),new ArrayList<>());
                                alertsMap.get(newAlert.getNodeMacAddress()).add(newAlert);
                            }else {
                                alertsMap.get(newAlert.getNodeMacAddress()).forEach(alertData -> {

                                    if (!alertData.getNodeState().equals(newAlert.getNodeState())) {
                                        alertsMap.get(newAlert.getNodeMacAddress()).add(newAlert);
                                    }
                                });
                            }

                        }

                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public static List<AlertData> getAlertList(String mac){
        List<AlertData>  ret ;
        ret = alertsMap.getOrDefault(mac, new ArrayList<>());
        return ret;
    }

    public static int getAlertsCount(String mac, NodeState status){
        List<AlertData> ret = null;
        if(mac.equals("All")){
            ret = getAllNodesAlertList();
        }else{
            ret = getAlertList(mac);
        }
        int count = 0;
        for(AlertData data : ret){
            if(data.getNodeState().equals(status)) {
                count++;
            }
        }
        return count;
    }

    public static void setNotificationAlertCallback(String name, NotificationAlertsCallback callback){
        if(!alertCallback.containsKey(name)){
            alertCallback.put(name, callback);
        }
    }

    public AlertManager() { }

    private static  void ProcessAlerts(){
        List<SensorNode> nodesList = NodeDataManager.getPreCheckedNodes();

        AtomicBoolean updateView = new AtomicBoolean(false);
        nodesList.forEach(node ->{

            long lastUpdated = node.getLastUpdatedDate().getTime();
            long currTime = System.currentTimeMillis();
            long secs = (currTime - lastUpdated);
            if(secs < 0){ secs *= -1; }

            long minute = (secs / (1000 * 60)) % 60;
            long hour = (secs / (1000 * 60 * 60)) % 24;
            long days = (secs / (1000 * 60 * 60 * 24));

            if (minute >= 1 || hour > 0 || days > 0) {
                setAlertStatus(node.getMacID(), NodeState.Offline);
                updateView.set(true);
            }
        });

        if(updateView.get()) {
            alertCallback.values().forEach(NotificationAlertsCallback::updateData);
        }
    }

    private static boolean isWarningInterval(AlertData data){
        long start = data.getAlertStartTime().getTime();
        long end = System.currentTimeMillis();
        long elapsedSec = (end - start);
        if (elapsedSec < 0) {
            elapsedSec *= -1;
        }
        long minute = (elapsedSec / (1000 * 60)) % 60;
        boolean retVal = true;
        if (minute >= 1) {
            retVal = false;
        }
        return retVal;
    }

    public static void onSensorNodeDataRcvd(SensorNode data) {

        SensorNode node = NodeDataManager.getPrecheckedNodeFromMac(data.getMacID());
        if (node == null) {
            return;
        }
        Profile nodeProf = node.getProfile();
        List<AlertType> nodeRetAlerts = isNodeDataOk(data, nodeProf);

        if (nodeRetAlerts.size() > 0) {
            DefrostProfile defrostProfile = node.getDefrostProfile();

            boolean isDefrostCycle = false;
            if (!defrostProfile.getName().equals("None")) {
                Calendar rightNow = Calendar.getInstance();
                int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                int minutes = rightNow.get(Calendar.MINUTE);

                String time = Integer.toString(hour) + ":" + Integer.toString(minutes);

                if (defrostProfile.isTimeInBetween(time)) {
                    isDefrostCycle = true;
                }
            }

            final boolean[] updateData = new boolean[1];
            for (AlertType alertType : nodeRetAlerts) {
                if (alertType == AlertType.HIGH_TEMP && isDefrostCycle) {
                    //Defrost cycle going on
                } else {

                    if (!alertsMap.containsKey(data.getMacID())) {
                        AlertData alert = new AlertData(
                                data.getMacID(),
                                alertType,
                                NodeState.Warning,
                                new Date(),
                                data.getTemperature(),
                                data.getHumidity());
                        alertsMap.put(data.getMacID(), new ArrayList<>());
                        alertsMap.get(data.getMacID()).add(alert);
                        NodeDataManager.SaveAlertData(alert);
                    }else {

                        alertsMap.get(data.getMacID()).forEach(alertData -> {

                            if (alertData.getNodeState().equals(NodeState.Offline)) {
                                alertData.setNodeState(NodeState.Normal);
                            }

                            switch (alertData.getNodeState()) {
                                case Alert:
                                    break;
                                case Normal:
                                    alertData.setNodeState(NodeState.Warning);
                                    break;
                                case Warning:
                                    if (!isWarningInterval(alertData)) {
                                        alertData.setNodeState(NodeState.Alert);
                                        NodeDataManager.SaveAlertData(alertData); // Adding alert into db as Warning -> Alert
                                        updateData[0] = true;
                                    }
                                    break;

                                case Defrost:
                                    break;
                            }
                        });
                    }
                }
            }

            if (updateData[0]) {

                if(alertCallback!= null) {
                    alertCallback.values().forEach(NotificationAlertsCallback::updateData);
                }
            }

        }
        else{//Save alert end here

            Date endTime = new Date();
            alertsMap.get(data.getMacID()).forEach(x -> {
                    x.setNodeState(NodeState.Normal);
                    x.setAlertEndTime(endTime);
                    NodeDataManager.SaveAlertData(x);
                });
        }
    }

    //checks sensor data to see if any alert is required or not
    private static List<AlertType> isNodeDataOk(SensorNode data, Profile prof) {

        double temp = data.getTemperature();
        int humidity = data.getHumidity();
        double rssi = data.getRssi();

        List<AlertType> retAlertTypess = new ArrayList<>();


        if (temp <= prof.getHighTempThreshold() &&
                temp >= prof.getLowTempThreshold()) {

            if (humidity <= prof.getHighHumidityThreshold() &&
                    humidity >= prof.getLowHumidityThreshold()) {

                if (rssi <= prof.getRssiThreshold()) {
                    return retAlertTypess;
                } else {
                    retAlertTypess.add(AlertType.RSSI);
                }

            } else {

                if (humidity > prof.getHighHumidityThreshold()) {
                    retAlertTypess.add(AlertType.HIGH_HUMIDITY);
                } else {
                    retAlertTypess.add(AlertType.LOW_HUMIDITY);
                }
            }

        } else {

            if (temp > prof.getHighTempThreshold()) {
                retAlertTypess.add(AlertType.HIGH_TEMP);

            } else {
                retAlertTypess.add(AlertType.LOW_TEMP);
            }
        }


        return retAlertTypess;
    }

    public static void setAlertStatus(String mac, NodeState offline) {

        try {
            alertsMap.get(mac).forEach(x -> {
                if (!x.getNodeState().equals(offline)) {
                    x.setNodeState(offline);
                    x.setAlertEndTime(new Date());
                    NodeDataManager.SaveAlertData(x);
                }
            });

        }catch(Exception e){
            Log.e("AlertManager", "Failed to find node");
        }

    }
}
