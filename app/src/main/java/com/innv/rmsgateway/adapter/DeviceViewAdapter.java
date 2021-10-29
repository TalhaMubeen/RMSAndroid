package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceViewAdapter extends RecyclerView.Adapter<DeviceViewAdapter.MyViewHolder> {

    List<SensorNode> mRMSDevices= new ArrayList<>();
    List<Profile> rmsProfiles = ProfileManager.getProfileList();
    LayoutInflater inflater;
    Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView card_category_title, tv_total_nodes_count;
        ImageView iv_device_profile_image;
        LinearLayout ll_title_color;


        MyViewHolder(View itemView) {
            super(itemView);
            this.card_category_title = (TextView) itemView.findViewById(R.id.card_category_title);
            this.iv_device_profile_image = (ImageView) itemView.findViewById(R.id.iv_device_profile_image);
            this.ll_title_color = (LinearLayout) itemView.findViewById(R.id.ll_title_color);
            this.tv_total_nodes_count = (TextView) itemView.findViewById(R.id.tv_total_nodes_count);
        }
    }

    public DeviceViewAdapter(Context ctx){
        context = ctx;
        mRMSDevices = NodeDataManager.getPreCheckedNodes();
        inflater = LayoutInflater.from(ctx);

    }

    public void update(){
        mRMSDevices = NodeDataManager.getPreCheckedNodes();
    }

    public Profile getItem(int position) {
        return rmsProfiles.get(position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.inflater.inflate(R.layout.summary_category_item, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Profile profile = getItem(position);
        List<SensorNode> collected = mRMSDevices.stream().filter(x -> x.getProfile().getTitle().equals(profile.getTitle())).collect(Collectors.toList());
        holder.card_category_title.setText(profile.getTitle());
        holder.iv_device_profile_image.setImageDrawable(ContextCompat.getDrawable(context, profile.getProfileImageId()));
        holder.ll_title_color.setBackgroundColor( ContextCompat.getColor(context, profile.getProfileColor()));
        holder.tv_total_nodes_count.setText(Integer.toString(collected.size()));
    }

    @Override
    public int getItemCount() {
        return  rmsProfiles.size();
    }

}