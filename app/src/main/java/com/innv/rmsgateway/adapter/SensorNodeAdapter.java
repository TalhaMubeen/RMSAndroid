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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.innv.rmsgateway.BleManager;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.graph.RealtimeTemperature;

import java.util.ArrayList;
import java.util.List;

public class SensorNodeAdapter extends BaseAdapter {
    private static final String TAG = "sensorScanner";
    private final Context context;
    LayoutInflater inflater;
    private final List<String> bleDeviceList;
    List<String> profileNameList;
    ListView lv_defrostIntervals;
    static CustomListAdapter customListAdapter;
    Profile selectedProfile;

    //constructor function
    public SensorNodeAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        bleDeviceList = new ArrayList<>();
        profileNameList = ProfileManager.getAllProfilesName();
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

        LinearLayout ll_details = nodeView.findViewById(R.id.ll_details);

        String macAddress = getItem(position);
        EditText node_name = (EditText) nodeView.findViewById(R.id.editTV_name);

        TextView tv_address = (TextView) nodeView.findViewById(R.id.tv_address);
        tv_address.setText(macAddress);

        CheckBox add_checkbox = (CheckBox) nodeView.findViewById(R.id.add_checkbox);

        Spinner sp_profile = nodeView.findViewById(R.id.sp_profile);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.context, android.R.layout.simple_spinner_item, profileNameList);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_profile.setAdapter(dataAdapter);

        LinearLayout ll_profile_details = (LinearLayout) nodeView.findViewById(R.id.ll_profile_details);
        EditText et_maxTemp = (EditText) nodeView.findViewById(R.id.et_maxTemp);
        EditText et_minTemp = (EditText) nodeView.findViewById(R.id.et_minTemp);
        EditText et_maxHumidity = (EditText) nodeView.findViewById(R.id.et_maxHumidity);
        EditText et_minHumidity = (EditText) nodeView.findViewById(R.id.et_minHumidity);


        if(profileNameList.size() == 0){
            ll_profile_details.setVisibility(View.GONE);
            sp_profile.setEnabled(false);
        }
        else{
            selectedProfile = ProfileManager.DefaultProfile;
            int index = profileNameList.indexOf("Default");
            sp_profile.setSelection(index, false);

            et_maxTemp.setText(Double.toString(selectedProfile.getHighTempThreshold()));
            et_minTemp.setText(Double.toString(selectedProfile.getLowTempThreshold()));
            et_maxHumidity.setText(Integer.toString(selectedProfile.getHighHumidityThreshold()));
            et_minHumidity.setText(Integer.toString(selectedProfile.getLowHumidityThreshold()));

        }

        sp_profile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String name = profileNameList.get(position);
                selectedProfile = ProfileManager.getProfile(name);

                assert selectedProfile != null;
                et_maxTemp.setText(Double.toString(selectedProfile.getHighTempThreshold()));
                et_minTemp.setText(Double.toString(selectedProfile.getLowTempThreshold()));
                et_maxHumidity.setText(Integer.toString(selectedProfile.getHighHumidityThreshold()));
                et_minHumidity.setText(Integer.toString(selectedProfile.getLowHumidityThreshold()));
                // your code here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        View finalNodeView = nodeView;

        ImageView iv_addDefrostInterval = (ImageView) nodeView.findViewById(R.id.iv_addDefrostInterval);
        iv_addDefrostInterval.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(lv_defrostIntervals == null){
                    if(customListAdapter == null) { customListAdapter = new CustomListAdapter(context); }

                    customListAdapter.addDefrostProfile();
                    lv_defrostIntervals = (ListView) finalNodeView.findViewById(R.id.lv_defrostIntervals);
                    lv_defrostIntervals.setAdapter(customListAdapter);
                    setListViewHeightBasedOnChildren(lv_defrostIntervals);

                }else {
                    customListAdapter.addDefrostProfile();
                    customListAdapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(lv_defrostIntervals);
                }
            }
        });



        add_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    if(node_name.getText().length() >0 && node_name.isEnabled()) {
                        String profileSelected = sp_profile.getSelectedItem().toString();
                        Profile prof = ProfileManager.getProfile(profileSelected);

                        try {
                            assert prof != null;
                            prof.setHighTempThreshold(Double.parseDouble(et_maxTemp.getText().toString()));
                            prof.setLowTempThreshold(Double.parseDouble(et_minTemp.getText().toString()));
                            prof.setHighHumidityThreshold(Integer.parseInt(et_maxHumidity.getText().toString()));
                            prof.setLowHumidityThreshold(Integer.parseInt(et_minHumidity.getText().toString()));

                            if(customListAdapter!= null) {
                                List<Profile.DefrostTimeProfile> profiels = customListAdapter.getDefrostProfile();
                                for (Profile.DefrostTimeProfile defrost : profiels) {
                                    if (defrost.isOk()) {
                                        if (!defrost.isEmpty()) {
                                            prof.addDefrostProfile(defrost);
                                        }
                                    } else {
                                        Toast.makeText(context, "Invalid defrost interval!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            }

                            add_checkbox.setChecked(true);
                            add_checkbox.setEnabled(false);
                            node_name.setEnabled(false);
                            NodeDataManager.AddNodeToDB(node_name.getText().toString(), tv_address.getText().toString(), prof);
                            Toast.makeText(context,node_name.getText() + " added successfully", Toast.LENGTH_SHORT).show();
                            setViewAndChildrenEnabled(finalNodeView, false);

                        }catch (Exception e){
                            Toast.makeText(context, "Exception while adding node", Toast.LENGTH_SHORT).show();
                        }

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
