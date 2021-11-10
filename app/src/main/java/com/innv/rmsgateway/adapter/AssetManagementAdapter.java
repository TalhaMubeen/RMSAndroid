package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.UpdateSensorNodeDialogue;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.NodeState;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorDataDecoder;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class AssetManagementAdapter extends BaseAdapter {

    List<SensorNode> mRMSDevices = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    boolean hideDetails;

    public AssetManagementAdapter(Context ctx, List<SensorNode> list, boolean hideDetails) {
        mRMSDevices = list;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
        this.hideDetails = hideDetails;
    }

    public void UpdateListData(List<SensorNode> list) {
        mRMSDevices.clear();
        mRMSDevices = list;
    }


    @Override
    public int getCount() {
        return mRMSDevices.size();
    }

    @Override
    public SensorNode getItem(int position) {
        return mRMSDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mRMSDevices.get(position).hashCode();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rmsDeviceCardView = convertView;
        SensorNode item = getItem(position);

        Profile itemProf = ProfileManager.getProfile(item.getProfileTitle());

        if (rmsDeviceCardView == null) {
            rmsDeviceCardView = inflater.inflate(R.layout.gridview_monitoring_item_new, parent, false);
        }

        TextView sensor_name = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_name);
        sensor_name.setText(item.getName());

        TextView sensor_subTitle = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_subTitle);
        sensor_subTitle.setText(item.getProfileTitle());
        ImageView iv_settings = (ImageView) rmsDeviceCardView.findViewById(R.id.iv_settings);
        ImageView iv_node_type = (ImageView) rmsDeviceCardView.findViewById(R.id.iv_node_type);
        iv_node_type.setImageDrawable(ContextCompat.getDrawable(context, itemProf.getProfileImageId()));

        if (hideDetails) {

            CardView sensor_card_view = (CardView) rmsDeviceCardView.findViewById(R.id.sensor_card_view);
            sensor_card_view.setOnLongClickListener(v -> {

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
                                mRMSDevices.remove(position);
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

            LinearLayout ll_details_view = (LinearLayout) rmsDeviceCardView.findViewById(R.id.ll_details_view);
            ll_details_view.setVisibility(View.GONE);
            iv_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UpdateSensorNodeDialogue dialogue = new UpdateSensorNodeDialogue(context, item.getMacID());
                    dialogue.showDialog();
                }
            });
        }

        else {
            iv_settings.setVisibility(View.GONE);

            TextView temp_scale_type = (TextView) rmsDeviceCardView.findViewById(R.id.temp_scale_type);
            if (!Globals.useCelsius) {
                temp_scale_type.setText("Temp °F");
            } else {
                temp_scale_type.setText("Temp °C");
            }

            TextView temperature_value = (TextView) rmsDeviceCardView.findViewById(R.id.temperature_value);
            temperature_value.setText(Double.toString(SensorDataDecoder.round(item.getTemperature(), 1)));


            if (item.getTemperature() > itemProf.getHighTempThreshold()) {
                temperature_value.setTextColor(Globals.ALERT);
            } else if (item.getTemperature() < itemProf.getLowTempThreshold()) {
                temperature_value.setTextColor(Globals.BELOW_THRESHOLD);
            }

            TextView humidity_value = (TextView) rmsDeviceCardView.findViewById(R.id.humidity_value);
            humidity_value.setText(Integer.toString(item.getHumidity()) + "%");

            if (item.getHumidity() > itemProf.getHighHumidityThreshold()) {
                humidity_value.setTextColor(Globals.ALERT);
            } else if (item.getHumidity() < itemProf.getLowHumidityThreshold()) {
                humidity_value.setTextColor(Globals.BELOW_THRESHOLD);
            }

            TextView sensor_rssi = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_rssi);
            sensor_rssi.setText(Double.toString(SensorDataDecoder.round(item.getRssi(), 1))/* + " dbm"*/);

            int alertsCount = AlertManager.getAlertsCount(item.getMacID(), NodeState.Alert);

            TextView tv_alerts = (TextView) rmsDeviceCardView.findViewById(R.id.tv_alerts);
            if (alertsCount > 0) {
                tv_alerts.setText(Integer.toString(alertsCount));
                tv_alerts.setTextColor(Globals.ALERT);
            }

        }

        View colorView = (View) rmsDeviceCardView.findViewById(R.id.colorNA);
        int color = R.color.color_normal;

        if(!item.isPreChecked()){
            color = R.color.color_dark_grey;
        } else {

            AlertData alerts = AlertManager.getAlert(item.getMacID());
            if (alerts != null) {
                switch (alerts.getNodeState()) {
                    case Alert:
                        color = R.color.color_alert;
                        break;

                    case Normal:
                        color = R.color.color_normal;
                        break;

                    case Defrost:
                        color = R.color.color_defrost;
                        break;

                    case Warning:
                        color = R.color.color_warning;
                        break;

                    case Offline:
                        color = R.color.color_offline;
                        break;

                    case ComFailure:
                        color = R.color.color_dark_grey;
                        break;
                }
            }
        }
        colorView.setBackgroundColor(ContextCompat.getColor(context, color));

        return rmsDeviceCardView;
    }

}

