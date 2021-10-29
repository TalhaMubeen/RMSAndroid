package com.innv.rmsgateway.adapter;

import com.innv.rmsgateway.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
    private final List<String> bleDeviceList;
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



    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View nodeView = convertView;

        if (nodeView == null) {
            nodeView = inflater.inflate(R.layout.scan_rms_items, parent, false);
        }

  //      LinearLayout ll_monitor_node = (LinearLayout) nodeView.findViewById(R.id.ll_monitor_node);
        Button btn_addNode = (Button) nodeView.findViewById(R.id.btn_addNode);
/*        Button btn_updateNode = (Button) nodeView.findViewById(R.id.btn_updateNode);
        CheckBox add_checkbox = (CheckBox) nodeView.findViewById(R.id.add_checkbox);*/

        EditText node_name = (EditText) nodeView.findViewById(R.id.editTV_name);
        TextView tv_address = (TextView) nodeView.findViewById(R.id.tv_address);

        // Drop down layout style - list view with radio button
        Spinner sp_profile = nodeView.findViewById(R.id.sp_profile);
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, profileNameList);
        profileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_profile.setAdapter(profileAdapter);

        // Drop down layout style - list view with radio button
        Spinner sp_defrostProfile = nodeView.findViewById(R.id.sp_defrostProfile);
        ArrayAdapter<String> defrostAdapter = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, defrostProfileNames);
        defrostAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_defrostProfile.setAdapter(defrostAdapter);

        if(profileNameList.size() == 0){
            sp_profile.setEnabled(false);
        }
        else{
            selectedProfile = ProfileManager.IceCream;
            int index = profileNameList.indexOf(selectedProfile.getTitle());
            sp_profile.setSelection(index, false);
        }

        if(defrostProfileNames.size() == 0){
            sp_defrostProfile.setEnabled(false);
        }else{
            selectedDefrostProf = DefrostProfileManager.None;
            int index = defrostProfileNames.indexOf(selectedDefrostProf.getName());
            sp_defrostProfile.setSelection(index, false);
        }


        ImageView iv_profileIcon = nodeView.findViewById(R.id.iv_profileIcon);

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

/*        if(updateNode){
            btn_addNode.setVisibility(View.GONE);
            ll_monitor_node.setVisibility(View.VISIBLE);
            btn_updateNode.setVisibility(View.VISIBLE);

            SensorNode node = NodeDataManager.getAllNodeFromMac(macAddress);
            node_name.setText(node.getName());

            add_checkbox.setChecked(node.isPreChecked());

            selectedProfile = node.getProfile();
            selectedDefrostProf = node.getDefrostProfile();
            sp_profile.setSelection(profileAdapter.getPosition(selectedProfile.getTitle()));
            sp_defrostProfile.setSelection(defrostAdapter.getPosition(selectedDefrostProf.getName()));

            iv_profileIcon.setImageDrawable(ContextCompat.getDrawable(context, selectedProfile.getProfileImageId()));
        }*/

        View finalNodeView = nodeView;

/*        btn_updateNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (node_name.getText().length() > 0 && node_name.isEnabled()){
                    String profileSelected = sp_profile.getSelectedItem().toString();
                    Profile prof = ProfileManager.getProfile(profileSelected);

                    String defrostSelected = sp_defrostProfile.getSelectedItem().toString();
                    DefrostProfile defrostProfile = DefrostProfileManager.getDefrostProfile(defrostSelected);

                    SensorNode node = NodeDataManager.getAllNodeFromMac(tv_address.getText().toString());

                    node.setDefrostProfile(defrostProfile);
                    node.setProfile(prof);

                    node.setName(node_name.getText().toString());
                    node.setPreChecked(add_checkbox.isSelected());

                    try {

                        btn_updateNode.setEnabled(false);
                        node_name.setEnabled(false);
                        NodeDataManager.UpdateNodeDetails(tv_address.getText().toString(),  node);
                        Toast.makeText(context, node_name.getText() + " Updated Successfully", Toast.LENGTH_SHORT).show();
                        setViewAndChildrenEnabled(finalNodeView, false);

                    } catch (Exception e) {
                        Toast.makeText(context, "Exception while updating node data", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(context, "Please enter node name", Toast.LENGTH_SHORT).show();
                    node_name.setEnabled(true);
                }
            }
        });*/


        btn_addNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (node_name.getText().length() > 0 && node_name.isEnabled()) {

                    String profileSelected = sp_profile.getSelectedItem().toString();
                    Profile prof = ProfileManager.getProfile(profileSelected);

                    String defrostSelected = sp_defrostProfile.getSelectedItem().toString();
                    DefrostProfile defrostProfile = DefrostProfileManager.getDefrostProfile(defrostSelected);

                    try {

                        btn_addNode.setEnabled(false);
                        node_name.setEnabled(false);
                        NodeDataManager.AddNodeToDB(node_name.getText().toString(), tv_address.getText().toString(), prof, defrostProfile);
                        Toast.makeText(context, node_name.getText() + " added successfully", Toast.LENGTH_SHORT).show();
                        setViewAndChildrenEnabled(finalNodeView, false);

                    } catch (Exception e) {
                        Toast.makeText(context, "Exception while adding node", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(context, "Please enter node name", Toast.LENGTH_SHORT).show();
                    node_name.setEnabled(true);
                }
            }
        });

        return nodeView;

    }
/*

    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight=0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            view = listAdapter.getView(i, view, listView);

            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + ((listView.getDividerHeight()) * (listAdapter.getCount()));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

*/

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
