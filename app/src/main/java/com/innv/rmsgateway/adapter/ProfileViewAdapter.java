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
import com.innv.rmsgateway.classes.Profile;

import java.util.List;

public class ProfileViewAdapter extends BaseAdapter {

    Context context;
    List<Profile> profileList;
    LayoutInflater inflater;

    public ProfileViewAdapter(Context ctx, List<Profile> profiles) {
        context = ctx;
        profileList = profiles;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return profileList.size();
    }

    @Override
    public Profile getItem(int position) {
        return profileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return profileList.get(position).hashCode();
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

        Profile prof = getItem(position);

        int imageId = prof.getProfileImageId();
        iv_settingType.setImageDrawable(ContextCompat.getDrawable(context, imageId));

        tv_settingName.setText(prof.getTitle());

        ll_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TypeProfileActivity.class);
                intent.putExtra("Title", prof.getTitle());
                context.startActivity(intent);
            }
        });


        return profView;
    }
}
