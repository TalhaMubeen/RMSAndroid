package com.innv.rmsgateway.data;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.AlertTypes;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class NodeDataManager {


    static TreeMap<String, List<SensorNode>> allNodesData = new TreeMap<>();

    public static boolean isStopUpdates() {
        return stopUpdates;
    }

    public static void setStopUpdates(boolean stop) {
        stopUpdates = stop;
    }

    static boolean stopUpdates = false;

    public static void init(){
        allNodesData.put("All", new ArrayList<>());
        allNodesData.put("Checked", new ArrayList<>());

        List<StaticListItem> dataSaved = getAllNodeList();

        for (StaticListItem item : dataSaved){
            SensorNode node = new SensorNode();
            if(node.parseListItem(item)) {
                allNodesData.get("All").add(node);

                if(node.isPreChecked()){
                    allNodesData.get("Checked").add(node);
                }

            }
        }


        //Loading Alerts Data
        AlertManager.parseListItems(getAllAlertList());

    }

    public static void AddDummyDatainDB(){
        SensorNode sn1 = new SensorNode("11:11:11:11","Dummy 1", "2:45am", 4, 6, 1,true,22.5,40,13.5, -99.6, true, ProfileManager.DefaultProfile);
        SensorNode sn2 = new SensorNode("12:12:12:12","Dummy 2", "3:45am", 5, 6, 1,false,22.5,40,13.5, -99.6, true, ProfileManager.DefaultProfile);
        SensorNode sn3 = new SensorNode("13:13:13:13","Dummy 3", "4:45am", 6, 6, 1,true,22.5,40,13.5, -99.6, false, ProfileManager.DefaultProfile);
        SensorNode sn4 = new SensorNode("14:14:14:14","Dummy 4", "5:45am", 7, 6, 1,false,22.5,40,13.5, -99.6, true, ProfileManager.DefaultProfile);
        SensorNode sn5 = new SensorNode("15:15:15:15","Dummy 5", "6:45am", 8, 6, 1,true,22.5,40,13.5, -99.6, true, ProfileManager.DefaultProfile);
        SensorNode sn6 = new SensorNode("16:16:16:16","Dummy 6", "7:45am", 9, 6, 1,false,22.5,40,13.5, -99.6, false, ProfileManager.DefaultProfile);

        SaveSensorNodeData(sn1);
        SaveSensorNodeData(sn2);
        SaveSensorNodeData(sn3);
        SaveSensorNodeData(sn4);
        SaveSensorNodeData(sn5);
        SaveSensorNodeData(sn6);
    }


    public static void AddNodeToDB(String name, String macAddress, Profile profile){
        name = name.toUpperCase();
        SensorNode sn1 = new SensorNode(
                macAddress, name, "0:00am", 0,
                0, 0, true, 0.0,
                0, 0.0, 0.0, true, profile);

        sn1.setLastUpdatedOn(new Date());
        SaveSensorNodeData(sn1);
    }

    public static List<SensorNode> getAllNodesLst(){
        return allNodesData.get("All");
    }

    public static List<SensorNode> getPreCheckedNodes(){
        return allNodesData.get("Checked");
    }

    public static List<StaticListItem> getAllNodeList(){
        List<StaticListItem> dataSaved =  Globals.db.getListItems(Globals.dbContext.getString(R.string.RMS_DEVICES), Globals.orgCode, "");
        return  dataSaved;
    }

/*    public static void SaveSensorNodeData(String mac, double temp,int humidity, int rssi){
        SensorNode node = getPrecheckedNodeFromMac(mac);
        if(node != null){
            node.setLastUpdatedOn(new Date());
            node.setTemperature(temp);
            node.setHumidity(humidity);
            node.setRssi(rssi);

            SaveSensorNodeData(node);
            LogSensorNodeData(node);
        }
    }*/

    public static void UpdateNodeData(SensorNode node, boolean logdata){
        if(node != null){
            SaveSensorNodeData(node);

            if(logdata) {
                LogSensorNodeData(node);
            }
        }
    }


    public static SensorNode getPrecheckedNodeFromMac(String mac){
        for(SensorNode node : getPreCheckedNodes()){
            if(node.getMacID().equals(mac)){
                return node;
            }
        }
        return null;
    }


    public static SensorNode getAllNodeFromMac(String mac){
        for(SensorNode node : getAllNodesLst()){
            if(node.getMacID().equals(mac)){
                return node;
            }
        }
        return null;
    }


    private static void SaveSensorNodeData(SensorNode node, boolean... forceUpdate){
        if(forceUpdate.length == 0) {
            if (stopUpdates)
                return;
        }

        StaticListItem item = node.getDataAsStaticListItem();
        Globals.db.AddOrUpdateList( Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, node.getMacID(),item);

        if(forceUpdate.length > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
            String dateToday = sdf.format(new Date());
            Globals.db.AddSensorNodeLogs(Globals.dbContext.getString(R.string.RMS_DEVICES), Globals.orgCode, node.getMacID(), dateToday, node.getLastUpdatedOn(), item);
        }

        AddorUpdateData(node);

    }

    private static void AddorUpdateData(SensorNode data){

        boolean updated = false;
        for(SensorNode node : allNodesData.get("All")){
            if(node.getMacID().equals(data.getMacID())){

                allNodesData.get("All").remove(node);
                allNodesData.get("All").add(data);

                if(node.isPreChecked()){
                    allNodesData.get("Checked").remove(node);
                }

                if(data.isPreChecked()){
                    allNodesData.get("Checked").add(data);
                }


                updated = true;
                break;
            }
        }
        if(!updated){
            allNodesData.get("All").add(data);
            if(data.isPreChecked()){
                allNodesData.get("Checked").add(data);
            }
        }
    }


    public static void LogSensorNodeData(SensorNode node){

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
        String dateToday = sdf.format(new Date());

        StaticListItem item = node.getDataAsStaticListItem();
        Globals.db.AddSensorNodeLogs(
                Globals.dbContext.getString(R.string.RMS_DEVICES),
                Globals.orgCode,
                node.getMacID(),
                dateToday,
                node.getLastUpdatedOn(),
                item);
    }


    public static TreeMap<Date, Double> getTemerature(String mac){

        List<StaticListItem> dataSaved = getAllLoggedData(mac);

/*        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
        String dateToday = sdf.format(new Date());*/

        TreeMap<Date, Double> temp = new TreeMap<>();

        for (StaticListItem item : dataSaved){
            SensorNode node = new SensorNode();
            if(node.parseListItem(item)) {
                temp.put(item.getTimeStampdate(),node.getTemperature());
            }
        }
        return  temp;
    }


    public static List<StaticListItem> getAllLoggedData(String mac){

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
        String dateToday = sdf.format(new Date());

        List<StaticListItem> dataSaved =  Globals.db.getSensorNodeLogs(Globals.dbContext.getString(R.string.RMS_DEVICES), Globals.orgCode, mac, dateToday);


        return  dataSaved;
    }

    public static List<StaticListItem> getTodaysAlertList(){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
        String dateToday = sdf.format(new Date());

        List<StaticListItem> alertsLsit = Globals.db.GetSensorNodeAlerts(
                Globals.dbContext.getString(R.string.RMS_DEVICES),
                Globals.orgCode,
                "",
                "",
                dateToday,
                dateToday);
        return alertsLsit;
    }

    public static List<StaticListItem> getAllAlertList(){
        List<StaticListItem> alertsLsit = Globals.db.GetSensorNodeAlerts(
                Globals.dbContext.getString(R.string.RMS_DEVICES),
                Globals.orgCode,
                "",
                "",
                "",
                "");
        return alertsLsit;
    }


    public static void RemoveNode(SensorNode node){
        Globals.db.RemoveList(Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, node.getMacID());
        Globals.db.RemoveLogs(Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, node.getMacID());
        Globals.db.RemoveAlerts(Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, node.getMacID());

        allNodesData.clear();
        init();
    }


    //Alerts data handling

    public static void SaveAlertData(AlertData data){

        if(data.getAlertDay().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
            String dateToday = sdf.format(new Date());
            data.setAlertDay(dateToday);

            Globals.db.AddSensorNodeAlerts(
                    Globals.dbContext.getString(R.string.RMS_DEVICES),
                    Globals.orgCode,
                    data.getNodeMacAddress(),
                    data.getAlertDay(),
                    data.getAlertStartTimeString(),
                    data.getTypeString(),
                    data.getStatusString(),
                    data.getDataAsStaticListItem());
        }else{
            Globals.db.UpdateSensorNodeAlerts(
                    Globals.dbContext.getString(R.string.RMS_DEVICES),
                    Globals.orgCode,
                    data.getNodeMacAddress(),
                    data.getTypeString(),
                    data.getStatusString(),
                    data.getDataAsStaticListItem());
        }
    }

}
