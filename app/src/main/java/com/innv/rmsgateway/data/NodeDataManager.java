package com.innv.rmsgateway.data;

import android.util.Log;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.AlertType;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.NodeState;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class NodeDataManager {


    static TreeMap<String, SensorNode> allNodesData = new TreeMap<>();

    public static boolean isStopUpdates() {
        return stopUpdates;
    }

    public static void setStopUpdates(boolean stop) {
        stopUpdates = stop;
    }

    static boolean stopUpdates = false;

    public static void init(){
/*        allNodesData.put("All", new HashMap<>());
        allNodesData.put("Checked", new HashMap<>());*/

        List<StaticListItem> dataSaved = getAllNodeList();

        for (StaticListItem item : dataSaved){
            SensorNode node = new SensorNode();
            if(node.parseListItem(item)) {
                allNodesData.put(node.getMacID(), node);
            }
        }

        //Loading Alerts Data
        updateAlertManager();
    }

    public static void updateAlertManager(){//24h alert list update
        List<StaticListItem> alertList = getTodaysAlertList();
        List<SensorNode> data = getPreCheckedNodes();

        List<StaticListItem> retAlerts = new ArrayList<>();

        for(StaticListItem item : alertList){
            if(data.stream().anyMatch(x -> x.getMacID().equals(item.getCode()))){
                retAlerts.add(item);
            }else{
                Log.i("DATAMANAGER", "Not Found");
            }
        }

        AlertManager.parseListItems(retAlerts);
    }

    public static List<StaticListItem> getAllProfilesList(){
        List<StaticListItem> items =  Globals.db.getProfileList(Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, "");
        return items;
    }

    public static List<StaticListItem> getAllDefrostProfilesList(){
        List<StaticListItem> items =  Globals.db.getDefrostProfileList(Globals.dbContext.getString(R.string.RMS_DEVICES),
                Globals.orgCode, "");
        return items;
    }

    public static boolean AddorUpdateProfile(String title, Profile profile, boolean updateProfiles){

        String opt1 = profile.getJsonObject().toString();
        StaticListItem item = new StaticListItem(Globals.orgCode,
                Globals.dbContext.getString(R.string.RMS_DEVICES),
                "", title,
                opt1, "");

       if(Globals.db.AddorUpdateProfileList( Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, title, item)){
           if(updateProfiles) {
               ProfileManager.init();
           }
           return true;
       }
       return false;
    }

    public static boolean AddorUpdateDefrostProfile(String title, DefrostProfile profile, boolean updateProfiles){

        String opt1 = profile.getJsonObject().toString();
        StaticListItem item = new StaticListItem(Globals.orgCode,
                Globals.dbContext.getString(R.string.RMS_DEVICES),
                "", title,
                opt1, "");

        if(Globals.db.AddorUpdateDefrostProfileList( Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, title, item)){
            if(updateProfiles) {
                DefrostProfileManager.init();
            }
            return true;
        }
        return false;
    }



    public static void UpdateNodeDetails(String macAddress, SensorNode node){
        SaveSensorNodeData(node);
    }

    public static void AddNodeToDB(String name, String macAddress, String profileName, String defrostProfileName){
        name = Globals.capitalize(name);
        SensorNode sn1 = new SensorNode(
                macAddress, name, "0:00am", 0,
                0, 0, true, 0.0,
                0, 0.0, 0.0, true, profileName, defrostProfileName);

        sn1.setLastUpdatedOn(new Date());
        SaveSensorNodeData(sn1);
    }

    public static List<SensorNode> getAllNodesLst(){
        return new ArrayList<>(allNodesData.values());
    }

    public static List<SensorNode> getPreCheckedNodes(){
        List<SensorNode> retList = new ArrayList<>();
        allNodesData.values().forEach(node ->{

            if(node.isPreChecked()){
                retList.add(node);
            }
        });

        return retList;
    }

    public static List<StaticListItem> getAllNodeList(){
        List<StaticListItem> dataSaved =  Globals.db.getListItems(Globals.dbContext.getString(R.string.RMS_DEVICES), Globals.orgCode, "");
        return  dataSaved;
    }

    public static void UpdateNodeData(SensorNode node, boolean logdata){
        if(node != null){
            SaveSensorNodeData(node);

            if(logdata) {
                LogSensorNodeData(node);
            }
        }
    }


    public static SensorNode getPreCheckedNodeFromMac(String mac){
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

    private static void AddorUpdateData(SensorNode data) {

        if(allNodesData.containsKey(data.getMacID())){
            allNodesData.replace(data.getMacID(), data);
        } else{
            allNodesData.put(data.getMacID(), data);
/*            AlertData alert = new AlertData(data.getMacID(), AlertType.DEFAULT, NodeState.Normal, new Date(), 0, 0);
            SaveAlertData(alert);*/

        }
     //   updateAlertManager();
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

    public static List<StaticListItem> getAlertList(){
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

        updateAlertManager();
        allNodesData.clear();
        init();
    }

    public static void SaveAlertData(AlertData data) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
        String dateToday = sdf.format(new Date());
        data.setAlertDay(dateToday);

        Globals.db.AddSensorNodeAlerts(
                Globals.dbContext.getString(R.string.RMS_DEVICES),
                Globals.orgCode,
                data.getNodeMacAddress(),
                dateToday,
                data.getAlertStartTimeString(),
                data.getTypeString(),
                data.getStatusString(),
                data.getDataAsStaticListItem());

        updateAlertManager();
    }

    //Alerts data handling
    public static void UpdateAlertData (AlertData data) {

        Globals.db.UpdateSensorNodeAlerts(
                Globals.dbContext.getString(R.string.RMS_DEVICES),
                Globals.orgCode,
                data.getNodeMacAddress(),
                data.getAlertDay(),
                data.getAlertStartTimeString(),
                data.getTypeString(),
                data.getStatusString(),
                data.getDataAsStaticListItem());

        updateAlertManager();

    }

}
