package com.innv.rmsgateway.sensornode;


import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.data.BleDevice;

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
    private double temperature = 25.6;
    private byte humidity = 10;


    public SensorDataDecoder() {
    }

    public boolean nodeValid(BleDevice bleDevice){
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
        humidity = sensorData[HUMIDITY_LOCATION];
        return (int)humidity;
    }



}
