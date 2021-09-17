package com.innv.rmsgateway.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class AllNodesAdapter extends BaseAdapter {

    List<StaticListItem> nodes= new ArrayList<>();
    LayoutInflater inflater;
    public AllNodesAdapter(Context ctx, List<StaticListItem> list){
        nodes = list;
        inflater = LayoutInflater.from(ctx);
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
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View nodeView = convertView;
        SensorNode item = getItem(position);
        if(nodeView == null) {
            nodeView = inflater.inflate(R.layout.add_rms_node_item, parent, false);
        }

        LinearLayout ll_details = (LinearLayout) nodeView.findViewById(R.id.ll_details);

        ll_details.setOnLongClickListener(v -> {

            return false;
        });

        TextView node_name = (TextView) nodeView.findViewById(R.id.editTV_name);
        node_name.setText(item.getName());

        TextView tv_address = (TextView) nodeView.findViewById(R.id.tv_address);
        tv_address.setText(item.getMacID());

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
                NodeDataManager.SaveSensorNodeData(item);
            }
        });
        return nodeView;

    }
}
