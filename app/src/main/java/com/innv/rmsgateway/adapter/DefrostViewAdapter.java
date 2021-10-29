package com.innv.rmsgateway.adapter;

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

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.TypeProfileActivity;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.Profile;

import java.util.List;

public class DefrostViewAdapter extends BaseAdapter {

    Context context;
    List<DefrostProfile> defrostProfiles;
    LayoutInflater inflater;

    public DefrostViewAdapter(Context ctx, List<DefrostProfile> profiles) {
        context = ctx;
        defrostProfiles = profiles;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return defrostProfiles.size();
    }

    @Override
    public DefrostProfile getItem(int position) {
        return defrostProfiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return defrostProfiles.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View profView = convertView;

        if(profView == null) {
            profView = inflater.inflate(R.layout.settings_item, parent, false);
        }

        ImageView iv_settingType = profView.findViewById(R.id.iv_settingType);
        TextView tv_settingName = profView.findViewById(R.id.tv_settingName);
        LinearLayout ll_settings = profView.findViewById(R.id.ll_settings);

        DefrostProfile prof = getItem(position);

/*        int imageId = prof.getProfileImageId();
        iv_settingType.setImageDrawable(ContextCompat.getDrawable(context, imageId));

        tv_settingName.setText(prof.getTitle());*/

        ll_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TypeProfileActivity.class);
                context.startActivity(intent);
            }
        });


        return profView;
    }
}
