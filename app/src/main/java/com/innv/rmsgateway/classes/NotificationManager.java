package com.innv.rmsgateway.classes;

import android.widget.Toast;

import com.innv.rmsgateway.data.IConvertHelper;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationManager {

    enum AlertType {
        LOW_TEMP,
        HIGH_TEMP,
        LOW_HUMIDITY,
        HIGH_HUMIDITY,
        RSSI,
    }

    static class AlertData implements IConvertHelper {
        AlertType type;
        Date alertTime;
        SensorNode data;

        AlertData(AlertType type, Date time, SensorNode node) {
            this.type = type;
            this.alertTime = time;
            this.data = node;
        }

        @Override
        public boolean parseJsonObject(JSONObject jsonObject) {
            return false;
        }

        @Override
        public JSONObject getJsonObject() {
            return null;
        }
    }

    //to deal with multiple alerts against one node
    static List<AlertData> alertList = new ArrayList<>();

    public NotificationManager() {
    }

    private static  void ProcessAlerts(){
        //Alert notifications logic here
        //Defrost / Low,High temp / Low,High humidity logic, what to do here ?

/*                switch (alertType) {
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
        List<AlertType> nodeRetAlerts = isNodeOk(data, nodeProf);

        if (nodeRetAlerts.size() > 0) {
            List<Profile.DefrostTimeProfile> defrostProfile = nodeProf.getDefrostProfile();
/*            if(!warningList.containsKey(data.getMacID())){
                warningList.put(data.getMacID(), new ArrayList<>());
            }*/

            Calendar rightNow = Calendar.getInstance();
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);
            int minutes = rightNow.get(Calendar.MINUTE);

            String time = Integer.toString(hour) + ":" + Integer.toString(minutes);
            boolean isDefrostCycle = false;
            for(Profile.DefrostTimeProfile prof : defrostProfile){
                if(prof.isTimeInBetween(time)){
                    isDefrostCycle = true;
                    break;
                }
            }

            for (AlertType alertType : nodeRetAlerts) {
                if(alertType == AlertType.HIGH_TEMP && isDefrostCycle) {
                    //Defrost cycle going on
                }else {
                    AlertData alert = new AlertData(alertType, new Date(), data);
                    alertList.add(alert);
                }
            }

        }
    }

    private static List<AlertType> isNodeOk(SensorNode data, Profile prof) {

        double temp = data.getTemperature();
        int humidity = data.getHumidity();
        double rssi = data.getRssi();

        List<AlertType> retAlertTypes = new ArrayList<>();


        if (temp <= prof.getHighTempThreshold() &&
                temp >= prof.getLowTempThreshold()) {

            if (humidity <= prof.getHighHumidityThreshold() &&
                    humidity >= prof.getLowHumidityThreshold()) {

                if (rssi <= prof.getRssiThreshold()) {
                    return retAlertTypes;
                } else {
                    retAlertTypes.add(AlertType.RSSI);
                }

            } else {

                if (humidity > prof.getHighHumidityThreshold()) {
                    retAlertTypes.add(AlertType.HIGH_HUMIDITY);
                } else {
                    retAlertTypes.add(AlertType.LOW_HUMIDITY);
                }
            }

        } else {

            if (temp > prof.getHighTempThreshold()) {
                retAlertTypes.add(AlertType.HIGH_TEMP);

            } else {
                retAlertTypes.add(AlertType.LOW_TEMP);
            }
        }


        return retAlertTypes;
    }


}
