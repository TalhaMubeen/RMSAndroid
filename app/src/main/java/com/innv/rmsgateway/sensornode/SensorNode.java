package com.innv.rmsgateway.sensornode;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.Globals;
import com.innv.rmsgateway.data.IConvertHelper;
import com.innv.rmsgateway.data.StaticListItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 *    this class contains sensor node data elements for UI and database
 *
 */


public class  SensorNode implements IConvertHelper {

    private String macID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String advertisingTime;
    private int timeAtWakeup; // wakeup time in hours,
    private int timeSinceWakeup; // time of device since working
    private int timeSlot;     //node slot
    private boolean timeSynced; // synced = 1, not synced =0;
    private double temperature;
    private int humidity;
    private double batteryVoltage;

    private String lastUpdated;


    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String defaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static Date getUTCdatetimeFromString(String dateValue) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date dt = sdf.parse(dateValue);
            return dt;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String FormatDateTime(Date dateValue, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(dateValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

/*    public String getTimeStampFormatted() {
        if (!timeStamp.equals("")) {
            Date dt = getUTCdatetimeFromString(timeStamp);
            return FormatDateTime(dt, defaultDateFormat);
        }
        return null;
    }*/

    public Date ConvertToDate(String dateValue) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
        try {
            Date d = sdf.parse(dateValue);
            return d;

        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;

        }
    }

    public Date getLastUpdatedDate() {
        if(lastUpdated.isEmpty()){
            return  null;
        }
        SimpleDateFormat sm = new SimpleDateFormat(defaultDateFormat, Locale.getDefault());
        Date dt = null;
        try {
            dt = sm.parse(lastUpdated);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public void setLastUpdatedOn(Date dt) {
        SimpleDateFormat sm = new SimpleDateFormat(defaultDateFormat, Locale.getDefault());
        lastUpdated = sm.format(dt);
    }

    public String getLastUpdatedOn() {
        return lastUpdated;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public boolean isPreChecked() {
        return isPreChecked;
    }

    public void setPreChecked(boolean preChecked) {
        isPreChecked = preChecked;
    }

    private boolean isPreChecked;

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    private double rssi;


    public SensorNode() {

    }


    public SensorNode(String macID, String name, String advertisingTime,
                      int timeAtWakeup, int timeSinceWakeup, int timeSlot,
                      boolean timeSynced, double temperature, int humidity,
                      double batteryVoltage, double rssi, boolean isPreChecked) {
        this.macID = macID;
        this.name = name;
        this.advertisingTime = advertisingTime;
        this.timeAtWakeup = timeAtWakeup;
        this.timeSinceWakeup = timeSinceWakeup;
        this.timeSlot = timeSlot;
        this.timeSynced = timeSynced;
        this.temperature = temperature;
        this.humidity = humidity;
        this.batteryVoltage = batteryVoltage;
        this.rssi = rssi;
        this.isPreChecked = isPreChecked;

    }

    public StaticListItem getDataAsStaticListItem() {

        try {
            String opt1 = getJsonObject().toString();
            return new StaticListItem(Globals.orgCode,
                    Globals.dbContext.getString(R.string.RMS_DEVICES),
                    getMacID(),
                    getName(),
                    opt1, "");
        } catch (Exception Ignore) {

        }
        return null;
    }

    public String getMacID() {
        return macID;
    }

    public void setMacID(String macID) {
        this.macID = macID;
    }

    public String getAdvertisingTime() {
        return advertisingTime;
    }

    public void setAdvertisingTime(String advertisingTime) {
        this.advertisingTime = advertisingTime;
    }

    public int getTimeAtWakeup() {
        return timeAtWakeup;
    }

    public void setTimeAtWakeup(int timeAtWakeup) {
        this.timeAtWakeup = timeAtWakeup;
    }

    public int getTimeSinceWakeup() {
        return timeSinceWakeup;
    }

    public void setTimeSinceWakeup(int timeSinceWakeup) {
        this.timeSinceWakeup = timeSinceWakeup;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isTimeSynced() {
        return timeSynced;
    }

    public void setTimeSynced(boolean timeSynced) {
        this.timeSynced = timeSynced;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temp) {
        this.temperature = temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    @Override
    public boolean parseJsonObject(JSONObject jsonObject) {

        try {
            setMacID(jsonObject.optString("macID"));
            setName(jsonObject.optString("name"));
            setAdvertisingTime(jsonObject.optString("advertisingTime"));
            setTimeAtWakeup(jsonObject.optInt("timeAtWakeup"));
            setTimeSinceWakeup(jsonObject.optInt("timeSinceWakeup"));
            setTimeSlot(jsonObject.optInt("timeSlot"));
            setTimeSynced(jsonObject.optBoolean("timeSynced"));
            setTemperature(jsonObject.optDouble("temperature"));
            setHumidity(jsonObject.optInt("humidity"));
            setBatteryVoltage(jsonObject.optDouble("batteryVoltage"));
            setRssi(jsonObject.optDouble("rssi"));
            setPreChecked(jsonObject.optBoolean("isPreChecked"));
            setPreChecked(jsonObject.optBoolean("isPreChecked"));
            lastUpdated = jsonObject.optString("LastUpdateTime");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    }

    public Boolean parseListItem(StaticListItem item) {
        try {
            JSONObject jsonObject = new JSONObject(item.getOptParam1());

            setMacID(jsonObject.optString("macID"));
            setName(jsonObject.optString("name"));
            setAdvertisingTime(jsonObject.optString("advertisingTime"));
            setTimeAtWakeup(jsonObject.optInt("timeAtWakeup"));
            setTimeSinceWakeup(jsonObject.optInt("timeSinceWakeup"));
            setTimeSlot(jsonObject.optInt("timeSlot"));
            setTimeSynced(jsonObject.optBoolean("timeSynced"));
            setTemperature(jsonObject.optDouble("temperature"));
            setHumidity(jsonObject.optInt("humidity"));
            setBatteryVoltage(jsonObject.optDouble("batteryVoltage"));
            setRssi(jsonObject.optDouble("rssi"));
            setPreChecked(jsonObject.optBoolean("isPreChecked"));
            lastUpdated = jsonObject.optString("LastUpdateTime");
            return true;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public JSONObject getJsonObject() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("macID", getMacID());
            jo.put("name", getName());
            jo.put("advertisingTime", getAdvertisingTime());
            jo.put("timeAtWakeup", getTimeAtWakeup());
            jo.put("timeSinceWakeup", getTimeSinceWakeup());
            jo.put("timeSlot", getTimeSlot());
            jo.put("timeSynced", isTimeSynced());
            jo.put("temperature", getTemperature());
            jo.put("humidity", getHumidity());
            jo.put("batteryVoltage", getBatteryVoltage());
            jo.put("rssi", getRssi());
            jo.put("isPreChecked", isPreChecked());
            jo.put("LastUpdateTime", getLastUpdatedOn());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            jo = null;
        }

        return jo;
    }


}

/**
*
*struct ADV_DATA{
 *   uint8_t flags_len;     // Length of the Flags field.
 *   uint8_t flags_type;    // Type of the Flags field.
 *   uint8_t flags;         // Flags field.
 *   uint8_t mandata_len;   // Length of the Manufacturer Data field.
 *   uint8_t mandata_type;  // Type of the Manufacturer Data field.
 *   uint8_t comp_id[2];    // Company ID field.
 *   uint8_t beac_type[2];  // Beacon Type field.
 *   uint8_t node_data[RMS_ADV_BEACON_MAX_PAYLOAD]; // User Frame Data
 * };
 *
* struct stFrameHeader
 * {
 *   uint32_t    ProductIdentifier;
 *   uint8_t     ProtocolVer;    // Current communication Protocol Version for the Frame Type
 *   uint16_t    TimeSlot;     // Time Slot of this device i.e. when to transmit beacon
 *   union
 *   {
 *     struct
 *     {
 *       uint8_t     DeviceType  : 2;     // Device Type i.e. Gateway, Sensor Node, etc.
 *       uint8_t     FrameType   : 3;      // Frame Type i.e. Beacon, Sync, etc.
 *       uint8_t     TimePeriod  : 3;   // Time Period after which device gets to broadcast its beacon again
 *     } data;
 *     uint8_t byte;
 *   } BitEncodedInfo;
 * };
 *
 *
 *struct stSensorNodeBeaconInfo
 * {
 *   uint16_t    timeAtWakeup;           // in hours. How long was this device asleep after production?
 *   uint32_t    timeSinceWakeup;        // in seconds. Time since the device woke up using reed switch
 *   union
 *   {
 *     struct
 *     {
 *       uint32_t  batteryVoltage  : 12; // supported range 0 (0 mV) to 3300 (3300 mV)
 *       uint32_t  timeSyncOnWakeup: 1;  // 1: system wants to sync on wakeup. 0: system has already been synced on wakeup
 *       uint32_t  timeSynced      : 1;  // timeSynced Flag. 0 if time hasnot been synced since 24 hours Nearest Gateway will sync the time of this device upon reading this flag as true.
 *       uint32_t  timeSyncRequired: 1;  // 1: Time syncing is required after few milliseconds
 *       uint32_t  sequenceNumber  : 4;  // Sequence Number (0 to 15)
 *       uint32_t  reserved        : 13;  // reserved
 *     } data;
 *     uint32_t bytes_uint32;
 *   } BitEncodedInfo;
 *   int16_t     temperature;      // supported range -32,768 (-327.68 degree centigrade) to 32,767 (327.67 degree centigrade)
 *   uint8_t     humidity;
 * };
 *
 *
*
*
*
 *
 *
 * data in structure format
 *
 * // structure 1
 * ================
 *  flags_len      = 0x2;     // Length of field.
 *  flags_type     = 0x01;
 *  flags          = 0x06;    // Flags: LE General Discoverable Mode, BR/EDR is disabled.
 *
 *   // structure 2
 *   ====================
 *  mandata_len                = 0;    // user data length
 *  mandata_type               = 0xFF;      // user data specific field. must be 0xFF
 *   rms_adv_data.comp_id[0]   = 0xFF
 *   rms_adv_data.comp_id[1]   = 0x02
 *   comp_id = 0x02FF
 *
 *
 *   rms_adv_data.beac_type[0]    = 0x15
 *   rms_adv_data.beac_type[1]    = 0x02
 *   beac_type = 0x0215
 *
 *   // Header Fram data
 *   ========================
 *   ProductIdentifier = 0x00000001 (4 Bytes)
 *   ProtocolVer = 0x01   (1 bytes
 *   TimeSlot = 0x0001   (2 bytes
 *   BitEncodedInfo    (1 Byte)
 *       DeviceType  b01 (2 bits)
 *       FrameType   b001 (3 bits)
 *       TimePeriod  b001 (3 bits)
 *
 *   stSensorNodeBeaconInfo
 *   =======================
 *
 *    timeAtWakeup  = 0x0000   (2 bytes)       in hours. How long was this device asleep after production?
 *    timeSinceWakeup = 0x00000000 (4 bytes)   in seconds. Time since the device woke up using reed switch
 *    BitEncodedInfo     (4 bytes)
 *       batteryVoltage = b000000111111;     12 bits, // supported range 0 (0 mV) to 3300 (3300 mV)
 *        timeSyncOnWakeup = b0;   1 bit,  1: system wants to sync on wakeup. 0: system has already been synced on wakeup
 *       timeSynced      : b0;  1 bit, timeSynced Flag. 0 if time hasnot been synced since 24 hours Nearest Gateway will sync the time of this device upon reading this flag as true
 *       timeSyncRequired: b0;  1 bit,  1: Time syncing is required after few milliseconds
 *       sequenceNumber  : b0000;  4 bits,  Sequence Number (0 to 15)
 *       reserved        :b0000000000000, 13 bits
 *   temperature = 0x00ff; (2 bytes signed int)
 *   humidity = 0x05;  (1 byte)
 *
 *
 * sample buffer from node: [device detected  ------  name: null  mac: 68:0A:E2:DA:17:A0  Rssi: -49
 * scanRecord:
 * structure 1
 * 02 01 06
 * structure 2
 * 1a
 * ff    = 0xFF
 * ff 02  = 0x02FF
 * 02 15  = 0x0215
 * header
 * ProductIdentifier = 01 00 00 00
 * ProtocolVer = 01
 * TimeSlot = 0f 00  = 0x000F -15 =>
 * BitEncodedInfo 65
 *
 * beaconInfo
 *
 * timeAtWakeup = 00 00
 * timeSinceWakeup = 1e 78 00 00 = 0x0000781E
 *
 * BitEncodedInfo = cb cb 08 00 = 0x0008CBCB
 *
 *
 * temperature = f3 0b = 0x0BF3
 * humidity = 59 =0x59
 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
*/