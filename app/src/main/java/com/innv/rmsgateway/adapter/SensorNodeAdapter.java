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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.innv.rmsgateway.BleManager;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class SensorNodeAdapter extends BaseAdapter {
    private static final String TAG = "sensorScanner";
    private final Context context;
    LayoutInflater inflater;
    private final List<String> bleDeviceList;
    List<String> profileList;

    //constructor function
    public SensorNodeAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        bleDeviceList = new ArrayList<>();
        profileList = ProfileManager.getAllProfilesName();
    }

    private boolean isDeviceAdded(String device){
        for(String dev : bleDeviceList){
            if(dev.equals(device)){
                return true;
            }
        }
        return false;
    }

    //add node
    public boolean addDevice(String bleDevice) {
        Log.d(TAG,"add ble devices into list");
        if(!isDeviceAdded(bleDevice)) {
            bleDeviceList.add(bleDevice);
            return true;
        }
        return false;
    }
    public void addDevices(List<String> list){
        clear();
        bleDeviceList.clear();
        bleDeviceList.addAll(list);
    }


    //clear scan devices from list
    public void clearScanDevice() {
        Log.d(TAG,"clear scanned devices");
        for (int index = 0; index < bleDeviceList.size(); index++) {
            String device = bleDeviceList.get(index);
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
    public String getItem(int position) {
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

        String device = getItem(position);
        EditText node_name = (EditText) nodeView.findViewById(R.id.editTV_name);

        TextView tv_address = (TextView) nodeView.findViewById(R.id.tv_address);
        tv_address.setText(device);

        CheckBox add_checkbox = (CheckBox) nodeView.findViewById(R.id.add_checkbox);

        Spinner sp_profile = nodeView.findViewById(R.id.sp_profile);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, profileList);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_profile.setAdapter(dataAdapter);
        sp_profile.setSelection(0, false);
        if(profileList.size() == 0){
            sp_profile.setEnabled(false);
        }

        add_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    if(node_name.getText().length() >0 && node_name.isEnabled()) {
                        add_checkbox.setChecked(true);
                        node_name.setEnabled(false);
                        String profileSelected = sp_profile.getSelectedItem().toString();
                        NodeDataManager.AddNodeToDB(node_name.getText().toString(), tv_address.getText().toString(),ProfileManager.getProfile(profileSelected));

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

}
