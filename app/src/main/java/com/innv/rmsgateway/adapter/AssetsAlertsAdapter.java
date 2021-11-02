package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.activity.AssetsActivity;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.NodeState;
import com.innv.rmsgateway.classes.Globals;

import java.util.ArrayList;
import java.util.List;

public class AssetsAlertsAdapter extends BaseAdapter {
    LayoutInflater inflater;
    Context context;

    public AssetsAlertsAdapter(Context ctx){
        inflater = LayoutInflater.from(ctx);
        context = ctx;
    }

    @Override
    public int getCount() {
        return Globals.AlertType.length -1;
    }

    @Override
    public Object getItem(int position) {
        return Globals.AlertType[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        position = position + 1;
        View alert_view = convertView;
        if (alert_view == null) {
            alert_view = inflater.inflate(R.layout.summary_status_item, parent, false);
        }
        //Setting Alerts Here
        TextView tv_total_nodes_count = (TextView) alert_view.findViewById(R.id.tv_total_nodes_count);
        ImageView iv_alert_icon = (ImageView) alert_view.findViewById(R.id.iv_alert_icon);
        LinearLayout ll_bg_color = (LinearLayout) alert_view.findViewById(R.id.ll_bg_color);
        TextView tv_alert_title = (TextView) alert_view.findViewById(R.id.tv_alert_title);
        int alertsCount;
        switch (position) {
            case 1:
                alertsCount = AlertManager.getNodeStateCount(NodeState.Alert);
                tv_total_nodes_count.setText(Integer.toString(alertsCount));
                iv_alert_icon.setBackgroundResource(R.drawable.alert);
                ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_alert));
                tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_alert));
                break;

            case 2:
                alertsCount = AlertManager.getNodeStateCount(NodeState.Warning);
                tv_total_nodes_count.setText(Integer.toString(alertsCount));
                iv_alert_icon.setBackgroundResource(R.drawable.warning);
                ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_warning));
                tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
                break;

            case 3:
                alertsCount = AlertManager.getNodeStateCount(NodeState.Normal);
                tv_total_nodes_count.setText(Integer.toString(alertsCount));
                iv_alert_icon.setBackgroundResource(R.drawable.ok_icon);
                ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_normal));
                tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_normal));
                break;

            case 4:
                alertsCount = AlertManager.getNodeStateCount(NodeState.Defrost);
                tv_total_nodes_count.setText(Integer.toString(alertsCount));
                iv_alert_icon.setBackgroundResource(R.drawable.defrost_icon_white);
                ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_defrost));
                tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_defrost));
                break;

            case 5:
                alertsCount = AlertManager.getNodeStateCount(NodeState.Offline);
                tv_total_nodes_count.setText(Integer.toString(alertsCount));
                iv_alert_icon.setBackgroundResource(R.drawable.offline_icon_white);
                ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_offline));
                tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_offline));
                break;

            case 6:
                alertsCount = AlertManager.getNodeStateCount(NodeState.ComFailure);
                tv_total_nodes_count.setText(Integer.toString(alertsCount));
                iv_alert_icon.setBackgroundResource(R.drawable.offline_icon_white);
                ll_bg_color.setBackgroundColor(ContextCompat.getColor(context, R.color.color_dark_grey));
                tv_total_nodes_count.setTextColor(ContextCompat.getColor(context, R.color.color_dark_grey));
                break;

            default:
                break;
        }


        int finalPosition = position;
        ll_bg_color.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, AssetsActivity.class);
                intent.putExtra("Position", finalPosition);
                intent.putExtra("ShowOne", true);
                context.startActivity(intent);
            }
        });


        tv_alert_title.setText((int) getItem(position));

        return alert_view;
    }



}

