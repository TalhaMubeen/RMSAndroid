package com.innv.rmsgateway.data;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class NodeDataManager {

    public static void AddDummyDatainDB(){
        SensorNode sn1 = new SensorNode("11:11:11:11","Dummy 1", "2:45am", 4, 6, 1,true,22.5,40,13.5, -99.6, true);
        SensorNode sn2 = new SensorNode("12:12:12:12","Dummy 2", "3:45am", 5, 6, 1,false,22.5,40,13.5, -99.6, true);
        SensorNode sn3 = new SensorNode("13:13:13:13","Dummy 3", "4:45am", 6, 6, 1,true,22.5,40,13.5, -99.6, false);
        SensorNode sn4 = new SensorNode("14:14:14:14","Dummy 4", "5:45am", 7, 6, 1,false,22.5,40,13.5, -99.6, true);
        SensorNode sn5 = new SensorNode("15:15:15:15","Dummy 5", "6:45am", 8, 6, 1,true,22.5,40,13.5, -99.6, true);
        SensorNode sn6 = new SensorNode("16:16:16:16","Dummy 6", "7:45am", 9, 6, 1,false,22.5,40,13.5, -99.6, false);

        SaveSensorNodeData(sn1);
        SaveSensorNodeData(sn2);
        SaveSensorNodeData(sn3);
        SaveSensorNodeData(sn4);
        SaveSensorNodeData(sn5);
        SaveSensorNodeData(sn6);
    }


    public static void AddNodeToDB(String name, String macAddress){
        SensorNode sn1 = new SensorNode(macAddress,
                name,
                "0:00am",
                0,
                0,
                0,
                true,
                0.0,
                0,
                0.0,
                0.0,
                true);
        SaveSensorNodeData(sn1);
    }

    public static List<SensorNode> getAllNodesLst(){
        List<SensorNode> retList = new ArrayList<>();
        List<StaticListItem> dataSaved = getAllNodeList();

        for (StaticListItem item : dataSaved){
            SensorNode node = new SensorNode();
            if(node.parseListItem(item)) {
                retList.add(node);
            }
        }
        return retList;
    }

    public static List<SensorNode> getPreCheckedNodes(){
        List<SensorNode> retList = new ArrayList<>();
        List<StaticListItem> dataSaved = getAllNodeList();

        for (StaticListItem item : dataSaved){
            SensorNode node = new SensorNode();
            if(node.parseListItem(item)) {
                if(node.isPreChecked()){
                    retList.add(node);
                }
            }
        }
        return retList;
    }

    public static List<StaticListItem> getPreCheckedNodesList(){
        //Retrieve all data without giving device code
        List<StaticListItem> retList = new ArrayList<>();
        List<StaticListItem> dataSaved =  getAllNodeList();
        for (StaticListItem item : dataSaved){
            SensorNode node = new SensorNode();
            if(node.parseListItem(item)){
                if(node.isPreChecked()){
                    retList.add(item);
                }
            }
        }

        return  retList;
    }

    public static List<StaticListItem> getAllNodeList(){
        List<StaticListItem> dataSaved =  Globals.db.getListItems(Globals.dbContext.getString(R.string.RMS_DEVICES), Globals.orgCode, "");
        return  dataSaved;
    }


    public static void SaveSensorNodeData(SensorNode node){
        StaticListItem item = node.getDataAsStaticListItem();
        Globals.db.AddOrUpdateList( Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, node.getMacID(),item);
    }

    public static void RemoveNode(SensorNode node){
        Globals.db.RemoveList(Globals.dbContext.getString(R.string.RMS_DEVICES),Globals.orgCode, node.getMacID());
    }

}
