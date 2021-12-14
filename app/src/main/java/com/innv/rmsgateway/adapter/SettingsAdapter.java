package com.innv.rmsgateway.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.AssetManagementActivity;
import com.innv.rmsgateway.activity.DataGridViewActivity;
import com.innv.rmsgateway.activity.LoginActivity;
import com.innv.rmsgateway.classes.Globals;

public class SettingsAdapter extends BaseAdapter {

    final String[] settingsList = new String[]{
      "Type Profiles",
      "Defrost Profiles",
      "Assets Management",
      "Temperature Unit - ",
      "Data Sync Management"
    };


    Context context;
    LayoutInflater inflater;

    public SettingsAdapter(Context ctx){
        context = ctx;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return settingsList.length;
    }

    @Override
    public String getItem(int position) {
        return settingsList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View settingsView = convertView;

        if(settingsView == null) {
            settingsView = inflater.inflate(R.layout.settings_item, parent, false);
        }

        ImageView iv_settingType = settingsView.findViewById(R.id.iv_settingType);
        TextView tv_settingName = settingsView.findViewById(R.id.tv_settingName);
        LinearLayout ll_settings = settingsView.findViewById(R.id.ll_settings);
        tv_settingName.setText(settingsList[position]);

        switch (position){
            case 0:
                iv_settingType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.settings));
                ll_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, DataGridViewActivity.class);
                        intent.putExtra("SettingType", "Profile");
                        context.startActivity(intent);
                    }
                });
                break;

            case 1:
                iv_settingType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.defrost_icon_black));
                ll_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, DataGridViewActivity.class);
                        intent.putExtra("SettingType", "Defrost");
                        context.startActivity(intent);
                    }
                });
                break;

            case 2:
                iv_settingType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.chiller_icon));
                ll_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, AssetManagementActivity.class);
                        context.startActivity(intent);
                    }
                });
                break;

            case 3:
                String unit = settingsList[position];
                if(Globals.useCelsius){
                    unit += "°C";
                }else{
                    unit += "°F";
                }
                tv_settingName.setText(unit);


                iv_settingType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.tem_unit_icon));
                CharSequence tempUnit[] = new CharSequence[] {"Celsius (°C)", "Fahrenheit (°F)"};

                ll_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Select Temperature Unit");
                        builder.setItems(tempUnit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Globals.useCelsius = which == 0;
                                Globals.storeSharedPref();
                                notifyDataSetChanged();
                            }
                        });
                        builder.show();
                    }
                });
                break;


            case 4:
                iv_settingType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.databackup));
                ll_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    }
                });
                break;
        }


        return settingsView;
    }
}
