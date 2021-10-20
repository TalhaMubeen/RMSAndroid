package com.innv.rmsgateway.classes;

import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.interfaces.NotificationAlertsCallback;
import com.innv.rmsgateway.sensornode.SensorNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlertManager {

    static NotificationAlertsCallback alertCallback;

    public static List<AlertData> getAllNodesAlertList() {
        return nodesAlertList;
    }

    //to deal with multiple alerts against one node
    static  List<AlertData> nodesAlertList = new ArrayList<>();

    public static void parseListItems(List<StaticListItem> data){
        for(StaticListItem item : data) {
            try {
                JSONObject jsonObject = new JSONObject(item.getOptParam1());
                AlertData newAlert = new AlertData();
                if (newAlert.parseJsonObject(jsonObject)) {
                    nodesAlertList.add(newAlert);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<AlertData> getAlertList(String mac){
        List<AlertData>  ret = new ArrayList<>();
        for(AlertData data : nodesAlertList){
            if(data.getNodeMacAddress().equals(mac)) {
                ret.add(data);
            }
        }

        return ret;
    }

/*    public static AlertData.AlertStatus getAlertStatus(String mac){
        AlertData.AlertStatus retStatus = AlertData.AlertStatus.Normal;
        List<AlertData> data = getAlertList(mac);
        for(AlertData alert : data){
            AlertData.AlertStatus status = alert.getStatus();
            if(!status.equals(retStatus)){
                if(status == AlertData.AlertStatus.Alert){

                }
            }
        }
    }*/

    public static  int getAlertsCount(String mac, AlertData.NodeState status){
        List<AlertData> ret = null;
        if(mac.equals("All")){
            ret = getAllNodesAlertList();
        }else{
            ret = getAlertList(mac);
        }
        int count = 0;
        for(AlertData data : ret){
            if(data.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    public static void setAlertList(List<AlertData> data){
        nodesAlertList.addAll(data);
    }

    public static void setNotificationAlertCallback(NotificationAlertsCallback callback){
        alertCallback = callback;
    }

    public AlertManager() { }

    private static  void ProcessAlerts(){
        //AlertManager notifications logic here
        //Defrost / Low,High temp / Low,High humidity logic, what to do here ?

        /*                switch (AlertTypes) {
                    case RSSI:
                        break;

                    case LOW_TEMP:
                        break;

                    case HIGH_TEMP:
                        break;

                    case LOW_HUMIDITY:
                        break;

                    case HIGH_HUMIDITY:
                        break;
                }*/
    }

    public static void onSensorNodeDataRcvd(SensorNode data) {

        SensorNode node = NodeDataManager.getPrecheckedNodeFromMac(data.getMacID());
        if (node == null) {
            return;
        }
        Profile nodeProf = node.getProfile();
        List<AlertTypes> nodeRetAlerts = isNodeDataOk(data, nodeProf);

        if (nodeRetAlerts.size() > 0) {
            List<Profile.DefrostTimeProfile> defrostProfile = nodeProf.getDefrostProfile();
/*            if(!warningList.containsKey(data.getMacID())){
                warningList.put(data.getMacID(), new ArrayList<>());
            }*/
            boolean isDefrostCycle = false;
            if (defrostProfile.size() > 0) {
                Calendar rightNow = Calendar.getInstance();
                int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                int minutes = rightNow.get(Calendar.MINUTE);

                String time = Integer.toString(hour) + ":" + Integer.toString(minutes);

                for (Profile.DefrostTimeProfile prof : defrostProfile) {
                    if (prof.isTimeInBetween(time)) {
                        isDefrostCycle = true;
                        break;
                    }
                }
            }

            boolean updateData = false;
            for (AlertTypes alertType : nodeRetAlerts) {
                if (alertType == AlertTypes.HIGH_TEMP && isDefrostCycle) {
                    //Defrost cycle going on
                } else {
                    boolean alertExists = false; //for specific alert type
                    for (int i = 0; i < nodesAlertList.size(); i++) {
                        if (nodesAlertList.get(i).getNodeMacAddress().equals(data.getMacID())) {
                            if (nodesAlertList.get(i).getType().equals(alertType)) {

                                if(nodesAlertList.get(i).getStatus().equals(AlertData.NodeState.Offline)){
                                    nodesAlertList.get(i).setStatus(AlertData.NodeState.Normal);
                                }

                                switch (nodesAlertList.get(i).getStatus()) {
                                    case Alert:
                                        break;
                                    case Normal:
                                        nodesAlertList.get(i).setStatus(AlertData.NodeState.Warning);
                                        break;
                                    case Warning:
                                        long start = nodesAlertList.get(i).getAlertStartTime().getTime();
                                        long end = System.currentTimeMillis();
                                        long elapsedSec = (end - start);
                                        if (elapsedSec < 0) {
                                            elapsedSec *= -1;
                                        }
                                        long minute = (elapsedSec / (1000 * 60)) % 60;
                                        if (minute >= 10) {
                                            AlertData.NodeState status = nodesAlertList.get(i).getStatus();
                                            nodesAlertList.get(i).setStatus(AlertData.NodeState.Alert);
                                            NodeDataManager.SaveAlertData(nodesAlertList.get(i)); // Adding alert into db as Warning -> Alert
/*                                            node.setWarningStatus(false);
                                            node.setAlertStatus(true);*/
                                            updateData = true;
                                        }
                                        break;
                                }

                                alertExists = true;
                                break;
                            }

                        }
                    }

                    if (!alertExists) {
                        AlertData alert = new AlertData(data.getMacID(), alertType, AlertData.NodeState.Warning, new Date());
                        nodesAlertList.add(alert);
                        NodeDataManager.SaveAlertData(alert);
/*                        if (!node.getAlertStatus()) {
                            updateData = true;
                            node.setWarningStatus(true);
                            node.setAlertStatus(false);
                        }*/
                    }

                }
            }

            if (updateData) {
              //  List<AlertData> alerts = getAlertList(data.getMacID());
               // NodeDataManager.UpdateNodeData(node, false);
                if(alertCallback!= null) {
                    alertCallback.updateData();
                }
            }

        }
        else{//Save alert end here
            for (int i = 0; i < nodesAlertList.size(); i++) {
                if (nodesAlertList.get(i).getNodeMacAddress().equals(data.getMacID())) {
                    nodesAlertList.get(i).setStatus(AlertData.NodeState.Normal);
                    nodesAlertList.get(i).setAlertEndTime(new Date());

                    AlertData alertData  = nodesAlertList.get(i);
                    NodeDataManager.SaveAlertData(alertData); //Alert Cycle completed

                }
            }
        }
    }

    //checks sensor data to see if any alert is required or not
    private static List<AlertTypes> isNodeDataOk(SensorNode data, Profile prof) {

        double temp = data.getTemperature();
        int humidity = data.getHumidity();
        double rssi = data.getRssi();

        List<AlertTypes> retAlertTypess = new ArrayList<>();


        if (temp <= prof.getHighTempThreshold() &&
                temp >= prof.getLowTempThreshold()) {

            if (humidity <= prof.getHighHumidityThreshold() &&
                    humidity >= prof.getLowHumidityThreshold()) {

                if (rssi <= prof.getRssiThreshold()) {
                    return retAlertTypess;
                } else {
                    retAlertTypess.add(AlertTypes.RSSI);
                }

            } else {

                if (humidity > prof.getHighHumidityThreshold()) {
                    retAlertTypess.add(AlertTypes.HIGH_HUMIDITY);
                } else {
                    retAlertTypess.add(AlertTypes.LOW_HUMIDITY);
                }
            }

        } else {

            if (temp > prof.getHighTempThreshold()) {
                retAlertTypess.add(AlertTypes.HIGH_TEMP);

            } else {
                retAlertTypess.add(AlertTypes.LOW_TEMP);
            }
        }


        return retAlertTypess;
    }

    public static void setAlertStatus(String mac, AlertData.NodeState offline) {

        for (int i = 0; i < nodesAlertList.size(); i++) {
            if(nodesAlertList.get(i).getNodeMacAddress().equals(mac) &&
                    !nodesAlertList.get(i).getStatus().equals(offline)){

                nodesAlertList.get(i).setStatus(offline);
                NodeDataManager.SaveAlertData( nodesAlertList.get(i));
            }
        }

    }
}
