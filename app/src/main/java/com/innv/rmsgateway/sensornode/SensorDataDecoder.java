package com.innv.rmsgateway.sensornode;


import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.data.BleDevice;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 *  BLE sensor scanned data structure
 *  Structure 1
 // structure 1
 * ================
 *  flags_len      = 0x2;     // Length of field.
 *  flags_type     = 0x01;
 *  flags          = 0x06;    // Flags: LE General Discoverable Mode, BR/EDR is disabled.
 *
 *   // structure 2
 *   ====================
 *  mandata_len                = 0x1A;    // user data length
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
 *   data[30 bytes]
 *
 *
 *
 */




public class SensorDataDecoder {


    public  static final int STRUCTURE1_SIZE_LOCATION =0;
    public  static final int AD_FLAG_LOCATION =1;
    public  static final int BLE_TYPE_LOCATION =2;
    public static final int MAN_DATA_SIZE_LOCATION =3;
    public static final int MAN_DATA_TYPE_LOCATION =4;
    public static final int MAN_ID0_LOCATION =5;
    public static final int MAN_ID1_LOCATION =6;
    public static final int DEVICE_TYPE_LOCATION =7;
    public static final int USER_DATA_SIZE_LOCATION =8;

    public static final int TEMPERATURE0_LOCATION =28;
    public static final int TEMPERATURE1_LOCATION =27;
    public static final int HUMIDITY_LOCATION =    29;


    //structure 1
    private final byte sizeOfStructure1 = 0x02;
    private final byte flagType = 0x01;
    private final byte bleType = 0x06;
    //structure 2
    private final byte manDataLen = 26;
    private final int manDataType = -1;
    private final byte manuID0 = -1; //
    private final byte manuID1 = 2; //

    private final byte DeviceType_Beacon = 2;
    private final int dataSize = 21;

    byte[] sensorData;

    public SensorDataDecoder() {
    }


    static class BeaconInfo implements Serializable {

        public byte[] batteryVoltage = new byte[12];
        public byte timeSyncOnWakeup;
        public byte timeSynced;
        public byte timeSyncRequired;
        public byte[] sequenceNumber = new byte[4];
        public byte[] reserved = new byte[13];


        private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
            return;
        }

        private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
            return;
        }

        private void readObjectNoData() throws ObjectStreamException {
            return;
        }

/*        public BeaconInfo(byte[] data) {
            this(new DataInputStream(new ByteArrayInputStream(data)));
        }
        public BeaconInfo(DataInput in) {
            try {
                in.readFully(batteryVoltage, 0, 12);
                timeSyncOnWakeup = in.readByte();
                timeSynced = in.readByte();
                timeSyncRequired = in.readByte();
                in.readFully(sequenceNumber, 15, 4);
                in.readFully(batteryVoltage, 19, 13);

            } catch (IOException e) {
                e.printStackTrace();
            }
       }*/
    }


    public boolean nodeValid(BleDevice bleDevice) {
        boolean bResult = true;
        sensorData =  bleDevice.getScanRecord();
        //check structure 1
        if((sensorData[STRUCTURE1_SIZE_LOCATION] != sizeOfStructure1) &&
                (sensorData[AD_FLAG_LOCATION] != flagType) &&
                (sensorData[BLE_TYPE_LOCATION] != bleType)){
            return false;
        }

        if(sensorData[MAN_DATA_SIZE_LOCATION] != manDataLen){
            return  false;
        }

        if(sensorData[MAN_DATA_TYPE_LOCATION] != (int)manDataType){
            return  false;
        }

        if((sensorData[MAN_ID0_LOCATION] != manuID0) || (sensorData[MAN_ID1_LOCATION] != manuID1)){
            return false;
        }

        if((sensorData[DEVICE_TYPE_LOCATION] != DeviceType_Beacon) || (sensorData[USER_DATA_SIZE_LOCATION] != dataSize)){
            return false;
        }

        return bResult;
    }

    public double getTemperature(BleDevice bleDevice){
        byte valueLS = 0;
        byte valueMS = 0;
        sensorData =  bleDevice.getScanRecord();

        boolean isNegative = false;

        valueLS = (sensorData[TEMPERATURE0_LOCATION]);
        if((valueLS & 0x80) == 0x80){
            isNegative = true;
        }
        valueMS = (sensorData[TEMPERATURE1_LOCATION]);

        int temp = 0;

        if(isNegative) {
            String tempS = Byte.toString(valueLS) + Byte.toString(valueMS);
            tempS = tempS.replace("-" , "");
            temp = -1 * Integer.parseInt(tempS);
        }else{
            temp = (( valueLS & 0xff) << 8) | (valueMS  & 0xff);
        }

        
        double ret = round((double)temp*0.01, 2);
        return  ret;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public int getHumidity(BleDevice bleDevice){
        sensorData =  bleDevice.getScanRecord();
        byte humidity = sensorData[HUMIDITY_LOCATION];
        return (int)humidity;
    }

    public Integer getSequencenumber(BleDevice bleDevice){
        sensorData =  bleDevice.getScanRecord();
        byte[] data = new byte[4];
        data[0] = sensorData[23];
        data[1]= sensorData[24];
        data[2]= sensorData[25];
        data[3]= sensorData[26];

        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            BeaconInfo info = (BeaconInfo) in.readObject();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }


/*        long  temp = (( data[3] & 0xff) << 32 | (data[3] & 0xff) << 24 | (data[3] & 0xff) << 16 | (data[3] & 0xff) << 8 | (data[3] & 0xff)) ;

        DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data));
        BeaconInfo info = new BeaconInfo(ds);*/
        return 1;
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
 *     uint32_t bytes_uint32; 4
 *   } BitEncodedInfo;
 *   int16_t     temperature;      // supported range -32,768 (-327.68 degree centigrade) to 32,767 (327.67 degree centigrade) //27,28
 *   uint8_t     humidity; //29
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
