package com.innv.rmsgateway.adapter;

import com.innv.rmsgateway.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.innv.rmsgateway.BleManager;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class SensorNodeAdapter extends BaseAdapter {
    private static final String TAG = "sensorScanner";
    private final Context context;
    LayoutInflater inflater;
    private final List<BleDevice> bleDeviceList;

    //constructor function
    public SensorNodeAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        bleDeviceList = new ArrayList<>();
    }

    private boolean isDeviceAdded(BleDevice device){
        for(BleDevice dev : bleDeviceList){
            if(dev.getMac().equals(device.getMac())){
                return true;
            }
        }
        return false;
    }

    //add node
    public void addDevice(BleDevice bleDevice) {
        Log.d(TAG,"add ble devices into list");
        if(!isDeviceAdded(bleDevice)) {
            bleDeviceList.add(bleDevice);
        }
    }
    public void addDevices(List<BleDevice> list){
        clear();
        bleDeviceList.clear();

        bleDeviceList.addAll(list);
    }
    //remove device
    public void removeDevice(BleDevice bleDevice) {
        Log.d(TAG,"remove device from list");
        for (int index = 0; index < bleDeviceList.size(); index++) {
            BleDevice device = bleDeviceList.get(index);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(index);
            }
        }
    }

    //clear scan devices from list
    public void clearScanDevice() {
        Log.d(TAG,"clear scanned devices");
        for (int index = 0; index < bleDeviceList.size(); index++) {
            BleDevice device = bleDeviceList.get(index);
            if (!BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(index);
            }
        }
    }

    public void clear() {
        Log.d(TAG,"clear all devices");
        clearScanDevice();
    }

    //get number of nodes into list
    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        Log.d(TAG,"get item position in list");
        if (position > bleDeviceList.size())
            return null;
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return bleDeviceList.get(position).hashCode();
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View nodeView = convertView;

        if (nodeView == null) {
            nodeView = inflater.inflate(R.layout.scan_rms_items, parent, false);
        }
        BleDevice device = getItem(position);
        EditText node_name = (EditText) nodeView.findViewById(R.id.editTV_name);

        TextView tv_address = (TextView) nodeView.findViewById(R.id.tv_address);
        tv_address.setText(device.getMac());

        CheckBox add_checkbox = (CheckBox) nodeView.findViewById(R.id.add_checkbox);
        add_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    if(node_name.getText().length() >0 && node_name.isEnabled()) {
                        add_checkbox.setChecked(true);
                        node_name.setEnabled(false);

                        NodeDataManager.AddNodeToDB(node_name.getText().toString(), tv_address.getText().toString());

                    }else{
                        Toast.makeText(context, "Please enter node name", Toast.LENGTH_SHORT).show();
                        add_checkbox.setChecked(false);
                        node_name.setEnabled(true);
                    }

                }else if(!node_name.isEnabled()){
                    add_checkbox.setChecked(false);
                    node_name.setEnabled(true);
                }
                //NodeDataManager.SaveSensorNodeData(item);
            }
        });
        return nodeView;

    }


/*    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SensorNodeAdapter.ViewNodeHolder holder;
        if (convertView != null) {
            holder = (SensorNodeAdapter.ViewNodeHolder) convertView.getTag();
        }
        else {
//            convertView = View.inflate(context, R.layout.adapter_device, null);
            convertView = View.inflate(context, R.layout.node_card_view_layout, null);
            holder = new SensorNodeAdapter.ViewNodeHolder();
            convertView.setTag(holder);
            holder.temp_image = (ImageView) convertView.findViewById(R.id.temp_image);
            holder.sensor_name = (TextView) convertView.findViewById(R.id.sensor_name);
            holder.sensor_bd_address = (TextView) convertView.findViewById(R.id.sensor_bd_address);
            holder.sensor_rssi = (TextView) convertView.findViewById(R.id.sensor_rssi);
            holder.temperature_value = (TextView)convertView.findViewById(R.id.temperature_value);
            holder.humidity_value = (TextView)convertView.findViewById(R.id.humidity_value);
            //holder.time_stamp = (TextView) convertView.findViewById(R.id.time_stamp);


            //holder.layout_idle = (LinearLayout) convertView.findViewById(R.id.layout_idle);
            //holder.layout_connected = (LinearLayout) convertView.findViewById(R.id.layout_connected);
            //holder.btn_disconnect = (Button) convertView.findViewById(R.id.btn_disconnect);
            //holder.btn_connect = (Button) convertView.findViewById(R.id.btn_connect);
            //holder.btn_detail = (Button) convertView.findViewById(R.id.btn_detail);
        }

        final BleDevice bleDevice = getItem(position);
        if (bleDevice != null) {
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            String name = "Sensor Node";
            String mac = bleDevice.getMac();
            int rssi = bleDevice.getRssi();
            float temperature = 10;
            int humidity =50;
            int battery_volt=2;
            //Date currentTime = Calendar.getInstance().getTime();
            //String strTime = String.valueOf(currentTime);
            holder.sensor_name.setText(name);
            holder.sensor_bd_address.setText(mac);
            byte[] rawBytes =  bleDevice.getScanRecord();
            //byte[] rawBytes = record.getBytes();
            String RSSI_value = String.valueOf(rssi)+" dbm";
            holder.sensor_rssi.setText(RSSI_value);
            //temperature = (float)
            holder.temperature_value.setText(String.valueOf(temperature)+" C");
            holder.humidity_value.setText(String.valueOf(humidity)+" %");
            //holder.txt_volts.setText(String.valueOf(battery_volt));
            //holder.time_stamp.setText(strTime);
            if (isConnected) {
                holder.temp_image.setImageResource(R.mipmap.ic_blue_connected);
                holder.sensor_name.setTextColor(0xFF1DE9B6);
                holder.sensor_bd_address.setTextColor(0xFF1DE9B6);
                //holder.layout_idle.setVisibility(View.GONE);
                //holder.layout_connected.setVisibility(View.VISIBLE);
            } else {
                holder.temp_image.setImageResource(R.mipmap.temp_icon_red);
                holder.sensor_name.setTextColor(0xFF000000);
                holder.sensor_bd_address.setTextColor(0xFF000000);
                //holder.layout_idle.setVisibility(View.VISIBLE);
                //holder.layout_connected.setVisibility(View.GONE);
            }
        }

//        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onConnect(bleDevice);
//                }
//            }
//        });

//        holder.btn_disconnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onDisConnect(bleDevice);
//                }
//            }
//        });

//        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onDetail(bleDevice);
//                }
//            }
//        });

        return convertView;
    }*/

   /* class ViewNodeHolder {
        ImageView temp_image;
        TextView sensor_name;
        TextView sensor_bd_address;
        TextView sensor_rssi;
        TextView temperature_value;
        TextView humidity_value;
        TextView time_stamp;
        TextView txt_volts;
        //Button btn_list_graph;

    }
*/

}
