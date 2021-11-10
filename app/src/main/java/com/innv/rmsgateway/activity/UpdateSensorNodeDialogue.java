package com.innv.rmsgateway.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateSensorNodeDialogue {
    Context context;
    Dialog dialog;

    List<String> nodeStateList = new ArrayList<>();
    List<String> profileNameList;
    List<String> defrostProfileNames;

    Profile selectedProfile;
    DefrostProfile selectedDefrostProf;

    String macAddress;
    
    public UpdateSensorNodeDialogue(Context ctx, String mac) {
        context = ctx;
        profileNameList = ProfileManager.getProfilesTitle();
        defrostProfileNames = DefrostProfileManager.getDefrostProfileNames();
        macAddress = mac;
        nodeStateList.add("Active");
        nodeStateList.add("InActive");
    }

    public void showDialog() {
        dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.scan_rms_items);

        LinearLayout ll_monitor_node = (LinearLayout) dialog.findViewById(R.id.ll_monitor_node);
        Button btn_addNode = (Button) dialog.findViewById(R.id.btn_addNode);
        Button btn_updateNode = (Button) dialog.findViewById(R.id.btn_updateNode);

        Spinner sp_nodeStatus = dialog.findViewById(R.id.sp_nodeStatus);
        ArrayAdapter<String> nodeStateAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, nodeStateList);
        nodeStateAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_nodeStatus.setAdapter(nodeStateAdapter);
       // CheckBox add_checkbox = (CheckBox) dialog.findViewById(R.id.add_checkbox);

        EditText node_name = (EditText) dialog.findViewById(R.id.editTV_name);
        TextView tv_address = (TextView) dialog.findViewById(R.id.tv_address);

        // Drop down layout style - list view with radio button
        Spinner sp_profile = dialog.findViewById(R.id.sp_profile);
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, profileNameList);
        profileAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_profile.setAdapter(profileAdapter);

        // Drop down layout style - list view with radio button
        Spinner sp_defrostProfile = dialog.findViewById(R.id.sp_defrostProfile);
        ArrayAdapter<String> defrostAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, defrostProfileNames);
        defrostAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_defrostProfile.setAdapter(defrostAdapter);

        if (profileNameList.size() == 0) {
            sp_profile.setEnabled(false);
        } else {
            int index =0;
            selectedProfile = ProfileManager.getProfile(profileNameList.get(index));
            sp_profile.setSelection(index, false);
        }

        if (defrostProfileNames.size() == 0) {
            sp_defrostProfile.setEnabled(false);
        } else {
            int index =0;
            selectedDefrostProf = DefrostProfileManager.getDefrostProfile(defrostProfileNames.get(index));
            sp_defrostProfile.setSelection(index, false);
        }


        ImageView iv_profileIcon = dialog.findViewById(R.id.iv_profileIcon);

        tv_address.setText(macAddress);

        sp_profile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String name = profileNameList.get(position);
                selectedProfile = ProfileManager.getProfile(name);
                iv_profileIcon.setImageDrawable(ContextCompat.getDrawable(context, selectedProfile.getProfileImageId()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {// your code here
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

        btn_addNode.setVisibility(View.GONE);
        ll_monitor_node.setVisibility(View.VISIBLE);
        btn_updateNode.setVisibility(View.VISIBLE);

        SensorNode node = NodeDataManager.getAllNodeFromMac(macAddress);
        node_name.setText(node.getName());

        if(node.isPreChecked()){
            sp_nodeStatus.setSelection(0);
        }else{
            sp_nodeStatus.setSelection(1);
        }

        selectedProfile = ProfileManager.getProfile(node.getProfileTitle());
        selectedDefrostProf = DefrostProfileManager.getDefrostProfile(node.getDefrostProfileTitle());
        sp_profile.setSelection(profileAdapter.getPosition(selectedProfile.getTitle()));

        if(selectedDefrostProf != null) {
            sp_defrostProfile.setSelection(defrostAdapter.getPosition(selectedDefrostProf.getName()));
        }

        iv_profileIcon.setImageDrawable(ContextCompat.getDrawable(context, selectedProfile.getProfileImageId()));

        btn_updateNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (node_name.getText().length() > 0 && node_name.isEnabled()) {
                    String profileSelected = sp_profile.getSelectedItem().toString();
                    String defrostSelected = "";
                    if(defrostProfileNames.size() > 0) {
                        defrostSelected = sp_defrostProfile.getSelectedItem().toString();
                    }

                    SensorNode node = new SensorNode(
                            Objects.requireNonNull(
                                    NodeDataManager.getAllNodeFromMac(tv_address.getText().toString())).getJsonObject());

                    node.setDefrostProfileTitle(defrostSelected);
                    node.setProfileTitle(profileSelected);

                    node.setName(node_name.getText().toString());

                    if(sp_nodeStatus.getSelectedItemPosition() == 0){
                        node.setPreChecked(true);
                    }else{
                        node.setPreChecked(false);
                    }

                    try {
                        btn_updateNode.setEnabled(false);
                        node_name.setEnabled(false);
                        NodeDataManager.UpdateNodeDetails(tv_address.getText().toString(), node);
                        Toast.makeText(context, node_name.getText() + " Updated Successfully", Toast.LENGTH_SHORT).show();
                        AssetManagementActivity activity = (AssetManagementActivity) context;
                        activity.update();
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(context, "Exception while updating node data", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(context, "Please enter node name", Toast.LENGTH_SHORT).show();
                    node_name.setEnabled(true);
                }
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    public void dimiss(){
        try {
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
