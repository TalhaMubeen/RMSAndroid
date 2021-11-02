package com.innv.rmsgateway.classes;

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
    static List<AlertData> allAlerts = new ArrayList<>();
    private static Handler mHandler;
    private static long mDelay = 0;
    private static long mTimeNow = System.currentTimeMillis();

    public static List<AlertData> getAllNodesAlertList() {
        List<AlertData> data = new ArrayList<>(alertsMap.values());
        return data;
    }


    static Map<String, AlertData> alertsMap = new HashMap<>();

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

    public static void parseListItems(List<StaticListItem> alertsData) {

        //  alertsMap.clear();
        List<SensorNode> nodeList = NodeDataManager.getPreCheckedNodes();

        alertsData.forEach(item -> {
            try {
                JSONObject jsonObject = new JSONObject(item.getOptParam1());
                AlertData newAlert = new AlertData();
                if (newAlert.parseJsonObject(jsonObject)) {
                    allAlerts.add(newAlert);

                    nodeList.forEach(node -> {
                        if (node.getMacID().equals(newAlert.getNodeMacAddress())) {

                            if (!alertsMap.containsKey(newAlert.getNodeMacAddress())) {
                                alertsMap.put(newAlert.getNodeMacAddress(), newAlert);
                            } else {
                                AlertType type = alertsMap.get(newAlert.getNodeMacAddress()).getType();
                                if (type == AlertType.HIGH_HUMIDITY || type == AlertType.LOW_HUMIDITY) {
                                    if (newAlert.getType() == AlertType.LOW_TEMP ||
                                            newAlert.getType() == AlertType.HIGH_TEMP) {

                                        alertsMap.put(newAlert.getNodeMacAddress(), newAlert);

                                    }
                                }
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        if (mHandler == null) {
            mDelay = 3000;
            mHandler = new Handler();
            ProcessAlerts();
            mHandler.postDelayed(r, mDelay);
        }

    }


    public static List<SensorNode> getNodesFromStatus(String alertStaus) {
        List<SensorNode> nodesList = new ArrayList<>(NodeDataManager.getPreCheckedNodes());
        if (!alertStaus.equals("All")) {
            List<AlertData> alertData = new ArrayList<>(getAllNodesAlertList());

            if(alertData.size() == 0 && alertStaus.equals("Normal")){
                return nodesList;
            }


            alertData.removeIf(alert -> !alert.getStatusString().equals(alertStaus));

            if(alertData.size() == 0){
                nodesList.clear();
            }else {
                alertData.forEach(alert -> {
                    nodesList.removeIf(node -> !node.getMacID().equals(alert.getNodeMacAddress()));
                });
            }

        }

        return nodesList;
    }


    public static int getNodeStateCount(NodeState state){

        List<SensorNode> dataList = new ArrayList<>(NodeDataManager.getPreCheckedNodes());

        int count = (int) dataList.stream().filter(node ->{

            if(node.getNodeState().equals(state)){
                return true;
            }
            return false;

        }).count();
        return count;
    }

    public static int getAlertsCount(String mac, NodeState status){

        int count = 0;
        for(AlertData data : allAlerts){

            if(data.getNodeMacAddress().equals(mac)) {
                if (data.getNodeState().equals(status)) {
                 //   if (data.getAlertEndTime() != null) {
                        count++;
                 //   }
                }
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

            if (minute >= Globals.NODE_OFFLINE_TIME || hour > 0 || days > 0) {//Offline timeout
                setAlertStatus(node.getMacID(), NodeState.Offline);
                updateView.set(true);
            }
        });

        if(updateView.get()) {
            alertCallback.values().forEach(NotificationAlertsCallback::updateData);
        }
    }

    private static boolean isWarningInterval(AlertData data, int warn2AlertTime){
        long start = data.getAlertStartTime().getTime();
        long end = System.currentTimeMillis();
        long elapsedSec = (end - start);
        if (elapsedSec < 0) {
            elapsedSec *= -1;
        }
        long minute = (elapsedSec / (1000 * 60)) % 60;

        boolean retVal = true;
        if(minute >= warn2AlertTime){
            retVal = false;
        }

        return retVal;
    }

    public static void onSensorNodeDataRcvd(SensorNode data) {

        SensorNode node = NodeDataManager.getPreCheckedNodeFromMac(data.getMacID());

        if (node == null) { return; }



        String profName = node.getProfileTitle();
        Profile nodeProf = ProfileManager.getProfile(profName);

        List<AlertType> nodeRetAlerts = isNodeDataOk(data, nodeProf);

        if (nodeRetAlerts.size() > 0) {
            String defrostProfileName = node.getDefrostProfileTitle();
            boolean isDefrostCycle = false;
            if(!defrostProfileName.isEmpty()) {
                DefrostProfile defrostProfile = DefrostProfileManager.getDefrostProfile(defrostProfileName);
                if (!defrostProfile.getName().equals("None")) {
                    Calendar rightNow = Calendar.getInstance();
                    int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                    int minutes = rightNow.get(Calendar.MINUTE);

                    String time = Integer.toString(hour) + ":" + Integer.toString(minutes);

                    if (defrostProfile.isTimeInBetween(time)) {
                        isDefrostCycle = true;
                    }
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
                        alertsMap.put(data.getMacID(),alert);
       /*                 alertsMap.get(data.getMacID()).add(alert);*/
                        NodeDataManager.SaveAlertData(alert);
                        node.setNodeState(NodeState.Warning);
                        NodeDataManager.UpdateNodeDetails(node.getMacID(), node);
                    } else {

/*                        alertsMap.get(data.getMacID()).forEach(alertData -> {

                            if (alertData.getNodeState().equals(NodeState.Offline)) {
                                alertData.setNodeState(NodeState.Normal);
                            }*/

                            switch (alertsMap.get(data.getMacID()).getNodeState()) {
                                case Alert:
                                    break;

                                case Offline:
                                case Normal:
                                    alertsMap.get(data.getMacID()).setNodeState(NodeState.Warning);
                                    NodeDataManager.UpdateAlertData(alertsMap.get(data.getMacID()));
                                    node.setNodeState(NodeState.Warning);
                                    NodeDataManager.UpdateNodeDetails(node.getMacID(), node);
                                    updateData[0] = true;
                                    break;
                                case Warning:
                                    if (!isWarningInterval(alertsMap.get(data.getMacID()), nodeProf.getWarningToAlertTime())) {
                                        alertsMap.get(data.getMacID()).setNodeState(NodeState.Alert);
                                        NodeDataManager.UpdateAlertData(alertsMap.get(data.getMacID())); // Adding alert into db as Warning -> Alert
                                        node.setNodeState(NodeState.Alert);
                                        NodeDataManager.UpdateNodeDetails(node.getMacID(), node);
                                        updateData[0] = true;
                                    }
                                    break;

                                case Defrost:
                                    break;
                            }
                       // });
                    }
                }
            }

            if (updateData[0]) {

                if (alertCallback != null) {
                    alertCallback.values().forEach(NotificationAlertsCallback::updateData);
                }
            }

        }

        else {//Save alert end here
            Date endTime = new Date();
            if(alertsMap.containsKey(data.getMacID())) {
                    if(alertsMap.get(data.getMacID()).getAlertEndTime() == null) {
                        alertsMap.get(data.getMacID()).setNodeState(NodeState.Normal);
                        alertsMap.get(data.getMacID()).setAlertEndTime(endTime);
                        NodeDataManager.UpdateAlertData(alertsMap.get(data.getMacID()));

                        node.setNodeState(NodeState.Normal);
                        NodeDataManager.UpdateNodeDetails(node.getMacID(), node);
                    }else{
                        if(alertsMap.get(data.getMacID()).getNodeState() != NodeState.Normal) {
                            alertsMap.get(data.getMacID()).setNodeState(NodeState.Normal);
                            NodeDataManager.UpdateAlertData(alertsMap.get(data.getMacID()));
                            node.setNodeState(NodeState.Normal);
                            NodeDataManager.UpdateNodeDetails(node.getMacID(), node);
                        }
                    }


                alertCallback.values().forEach(NotificationAlertsCallback::updateData);
            }
        }
    }

    //checks sensor data to see if any alert is required or not
    private static List<AlertType> isNodeDataOk(SensorNode data, Profile prof) {

        double temp = data.getTemperature();
        int humidity = data.getHumidity();
        double rssi = data.getRssi();

        List<AlertType> alertReasonType = new ArrayList<>();


        if (temp <= prof.getHighTempThreshold() &&
                temp >= prof.getLowTempThreshold()) {

            if (humidity <= prof.getHighHumidityThreshold() &&
                    humidity >= prof.getLowHumidityThreshold()) {

                return alertReasonType;

            } else {

                if (humidity > prof.getHighHumidityThreshold()) {
                    alertReasonType.add(AlertType.HIGH_HUMIDITY);
                } else {
                    alertReasonType.add(AlertType.LOW_HUMIDITY);
                }
            }

        } else {

            if (temp > prof.getHighTempThreshold()) {
                alertReasonType.add(AlertType.HIGH_TEMP);
                alertReasonType.remove(AlertType.LOW_HUMIDITY);
            } else {
                alertReasonType.add(AlertType.LOW_TEMP);
                alertReasonType.remove(AlertType.HIGH_HUMIDITY);
            }
        }


        return alertReasonType;
    }

    public static void setAlertStatus(String mac, NodeState status) {
        if (alertsMap.size() == 0) {
            return;
        }
        try {
            if (!alertsMap.get(mac).getNodeState().equals(status)) {
                alertsMap.get(mac).setNodeState(status);
                NodeDataManager.UpdateAlertData(alertsMap.get(mac));

                NodeDataManager.getPreCheckedNodeFromMac(mac).setNodeState(status);
                NodeDataManager.UpdateNodeDetails(mac, NodeDataManager.getPreCheckedNodeFromMac(mac));
            }
        } catch (Exception e) {
            Log.e("AlertManager", "Failed to find node");
        }

    }

    public static AlertData getAlert(String macID) {
        if(alertsMap.containsKey(macID)){
            return  alertsMap.get(macID);
        }else{
            return null;
        }
    }
}
