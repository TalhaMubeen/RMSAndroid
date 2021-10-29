package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class AllNodesAdapter extends BaseAdapter {

    List<StaticListItem> nodes= new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    public AllNodesAdapter(Context ctx, List<StaticListItem> list){
        nodes = list;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
    }


    @Override
    public int getCount() {
        return nodes.size();
    }

    @Override
    public SensorNode getItem(int position) {
        SensorNode node = new SensorNode();
        if(node.parseListItem(nodes.get(position))){
            return node;
        }
        return  new SensorNode();
    }

    @Override
    public long getItemId(int position) {
        return nodes.get(position).hashCode();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View nodeView = convertView;
        SensorNode item = getItem(position);
        if(nodeView == null) {
            nodeView = inflater.inflate(R.layout.add_rms_node_item, parent, false);
        }

        LinearLayout ll_details = (LinearLayout) nodeView.findViewById(R.id.ll_details);

        ll_details.setOnLongClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getResources().getString(R.string.confirmation))
                    .setMessage("Do you want to permanently delete rms node ?")
                    .setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton("YES", null);
            builder.setNegativeButton("NO", null);
            builder.setCancelable(false);

            final AlertDialog alertDialog = builder.create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface _dialog) {
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NodeDataManager.setStopUpdates(true);
                            NodeDataManager.RemoveNode(item);
                            nodes.remove(position);
                            notifyDataSetChanged();
                            _dialog.dismiss();
                            NodeDataManager.setStopUpdates(false);

                        }
                    });

                    Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            _dialog.dismiss();
                        }
                    });
                }
            });

            alertDialog.show();
            return false;
        });

        TextView node_name = (TextView) nodeView.findViewById(R.id.editTV_name);
        node_name.setText(item.getName());

        TextView tv_address = (TextView) nodeView.findViewById(R.id.tv_address);
        tv_address.setText(item.getMacID());

        TextView tv_profile = (TextView) nodeView.findViewById(R.id.tv_profile);
        Profile prof = item.getProfile();
        tv_profile.setText(prof.getTitle());


        EditText et_maxTemp = (EditText) nodeView.findViewById(R.id.et_maxTemp);
        EditText et_minTemp = (EditText) nodeView.findViewById(R.id.et_minTemp);
        EditText et_maxHumidity = (EditText) nodeView.findViewById(R.id.et_maxHumidity);
        EditText et_minHumidity = (EditText) nodeView.findViewById(R.id.et_minHumidity);

        et_maxTemp.setText(Double.toString(prof.getHighTempThreshold()));
        et_minTemp.setText(Double.toString(prof.getLowTempThreshold()));
        et_maxHumidity.setText(Integer.toString(prof.getHighHumidityThreshold()));
        et_minHumidity.setText(Integer.toString(prof.getLowHumidityThreshold()));


        CheckBox add_checkbox = (CheckBox) nodeView.findViewById(R.id.add_checkbox);
        if(item.isPreChecked()){
            add_checkbox.setChecked(true);
        }else{
            add_checkbox.setChecked(false);
        }

        add_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()){
                    add_checkbox.setChecked(true);
                    item.setPreChecked(true);
                } else {
                    add_checkbox.setChecked(false);
                    item.setPreChecked(false);
                }
                NodeDataManager.UpdateNodeData(item, false);
                NodeDataManager.updateAlertManager();
            }
        });
        return nodeView;

    }
}
