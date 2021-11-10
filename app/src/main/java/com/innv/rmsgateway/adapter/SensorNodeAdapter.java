package com.innv.rmsgateway.adapter;

import com.innv.rmsgateway.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.bluetooth.BleManager;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class SensorNodeAdapter extends BaseAdapter {
    private static final String TAG = "sensorScanner";
    private final Context context;
    LayoutInflater inflater;
    private static List<String> bleDeviceList;
    List<String> profileNameList;
    List<String> defrostProfileNames;
    Profile selectedProfile;
    DefrostProfile selectedDefrostProf;
    //constructor function
    public SensorNodeAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        bleDeviceList = new ArrayList<>();
        profileNameList = ProfileManager.getProfilesTitle();
        defrostProfileNames = DefrostProfileManager.getDefrostProfileNames();
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
        return position;
    }



    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View nodeView = convertView;
        boolean initSpinner = true;
        if (nodeView == null) {
            nodeView = inflater.inflate(R.layout.scan_rms_items, parent, false);
        }else{
            initSpinner = false;
        }

        Button btn_addNode = (Button) nodeView.findViewById(R.id.btn_addNode);
        EditText node_name = (EditText) nodeView.findViewById(R.id.editTV_name);
        TextView tv_address = (TextView) nodeView.findViewById(R.id.tv_address);
        // Drop down layout style - list view with radio button
        Spinner sp_profile = nodeView.findViewById(R.id.sp_profile);
        // Drop down layout style - list view with radio button
        Spinner sp_defrostProfile = nodeView.findViewById(R.id.sp_defrostProfile);
        ImageView iv_profileIcon = nodeView.findViewById(R.id.iv_profileIcon);

        View finalNodeView = nodeView;
        btn_addNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (node_name.getText().length() > 0 && node_name.isEnabled()) {

                    String profileSelected = sp_profile.getSelectedItem().toString();
                    String defrostSelected = "";
                    if (defrostProfileNames.size() > 0) {
                        defrostSelected = sp_defrostProfile.getSelectedItem().toString();
                    }

                    try {
                        btn_addNode.setEnabled(false);
                        node_name.setEnabled(false);
                        NodeDataManager.AddNodeToDB(node_name.getText().toString(), tv_address.getText().toString(), profileSelected, defrostSelected);
                        Toast.makeText(context, node_name.getText() + " added successfully", Toast.LENGTH_SHORT).show();
                        setViewAndChildrenEnabled(finalNodeView, false);

                    } catch (Exception e) {
                        Toast.makeText(context, "Failed to add new node", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(context, "Please enter node name", Toast.LENGTH_SHORT).show();
                    node_name.setEnabled(true);
                }
            }
        });

        if(initSpinner) {
            ArrayAdapter<String> profileAdapter = new ArrayAdapter<String>(this.context, R.layout.spinner_item, profileNameList);
            profileAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            sp_profile.setAdapter(profileAdapter);

            ArrayAdapter<String> defrostAdapter = new ArrayAdapter<String>(this.context, R.layout.spinner_item, defrostProfileNames);
            defrostAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            sp_defrostProfile.setAdapter(defrostAdapter);


            if (profileNameList.size() == 0) {
                sp_profile.setEnabled(false);
            } else {
                int index = 0;
                selectedProfile = ProfileManager.getProfile(profileNameList.get(index));
                sp_profile.setSelection(index, false);
            }

            if (defrostProfileNames.size() == 0) {
                sp_defrostProfile.setEnabled(false);
            } else {
                int index = 0;
                selectedDefrostProf = DefrostProfileManager.getDefrostProfile(defrostProfileNames.get(index));
                sp_defrostProfile.setSelection(index, false);
            }
        }

        String macAddress = getItem(position);
        tv_address.setText(macAddress);

        sp_profile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String name = profileNameList.get(position);
                selectedProfile = ProfileManager.getProfile(name);
                iv_profileIcon.setImageDrawable(ContextCompat.getDrawable(context, selectedProfile.getProfileImageId()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        sp_defrostProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String name = defrostProfileNames.get(position);
                selectedDefrostProf = DefrostProfileManager.getDefrostProfile(name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        return nodeView;

    }

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

}
