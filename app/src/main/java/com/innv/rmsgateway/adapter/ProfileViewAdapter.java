package com.innv.rmsgateway.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.DataGridViewActivity;
import com.innv.rmsgateway.activity.DefrostProfileActivity;
import com.innv.rmsgateway.activity.TypeProfileActivity;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.data.NodeDataManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileViewAdapter extends BaseAdapter {

    Context context;
    DataGridViewActivity typeProfileActivity;
    List<Profile> profileList;
    List<DefrostProfile> defrostProfiles;
    LayoutInflater inflater;

    public void updateProfiles(List<Profile> profiles, List<DefrostProfile> defrostProfiles) {
        profileList = profiles;
        if (defrostProfiles != null) {
            defrostProfiles.removeIf(dprof -> {
                if (dprof.getName().equals("None")) {
                    return true;
                }
                return false;
            });
            this.defrostProfiles = defrostProfiles;
        }
    }

    public ProfileViewAdapter(Context ctx, List<Profile> profiles, List<DefrostProfile> defrostProfiles) {
        context = ctx;
        typeProfileActivity = (DataGridViewActivity) ctx;

        profileList = profiles;

        if (defrostProfiles != null) {
            defrostProfiles.removeIf(dprof -> {
                if (dprof.getName().equals("None")) {
                    return true;
                }
                return false;
            });
            this.defrostProfiles = defrostProfiles;
        }

        inflater = LayoutInflater.from(ctx);
    }


    @Override
    public int getCount() {
        return profileList != null ? profileList.size() : defrostProfiles.size();
    }

    @Override
    public Object getItem(int position) {
        return profileList != null ? profileList.get(position) : defrostProfiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return profileList != null ? profileList.get(position).hashCode() : defrostProfiles.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View profView = convertView;

        if (profView == null) {
            profView = inflater.inflate(R.layout.settings_item, parent, false);
        }

        ImageView iv_settingType = profView.findViewById(R.id.iv_settingType);
        ImageView iv_delAsset = profView.findViewById(R.id.iv_delAsset);
        iv_delAsset.setVisibility(View.VISIBLE);

        TextView tv_settingName = profView.findViewById(R.id.tv_settingName);
        LinearLayout ll_settings = profView.findViewById(R.id.ll_settings);
        Profile prof;
        DefrostProfile defrostProfile;
        if (profileList != null) {
            prof = (Profile) getItem(position);

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

            if( prof.isDefault()) {
                iv_delAsset.setVisibility(View.GONE);
            }else {

                iv_delAsset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(context.getResources().getString(R.string.confirmation))
                                .setMessage("Do you want to permanently delete " + prof.getTitle() + " ?")
                                .setIcon(android.R.drawable.ic_dialog_alert);
                        builder.setPositiveButton("YES", null);
                        builder.setNegativeButton("NO", null);
                        builder.setCancelable(false);

                        final AlertDialog alertDialog = builder.create();

                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.setCancelable(true);
                        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(final DialogInterface _dialog) {
                                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                positiveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertDialog.dismiss();
                                        AtomicBoolean inUse = new AtomicBoolean(false);
                                        NodeDataManager.getAllNodesLst().forEach(node -> {
                                            if (node.getProfileTitle().equals(prof.getTitle())) {
                                                inUse.set(true);
                                                return;
                                            }
                                        });

                                        if (inUse.get()) {
                                            Toast.makeText(context, prof.getTitle() + " is currently in use!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            NodeDataManager.RemoveProfile(prof.getTitle());
                                            Toast.makeText(context, prof.getTitle() + " removed successfully!", Toast.LENGTH_SHORT).show();
                                            typeProfileActivity.update();
                                        }
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
                    }

                });
            }

        } else {

            defrostProfile = (DefrostProfile) getItem(position);
            iv_settingType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.defrost_icon_black));
            tv_settingName.setText(defrostProfile.getName());

            ll_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DefrostProfileActivity.class);
                    intent.putExtra("Title", defrostProfile.getName());
                    context.startActivity(intent);
                }
            });

            iv_delAsset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getResources().getString(R.string.confirmation))
                            .setMessage("Do you want to permanently delete " + defrostProfile.getName() + " ?")
                            .setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setPositiveButton("YES", null);
                    builder.setNegativeButton("NO", null);
                    builder.setCancelable(false);

                    final AlertDialog alertDialog = builder.create();

                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setCancelable(true);
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface _dialog) {
                            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            positiveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                    AtomicBoolean inUse = new AtomicBoolean(false);
                                    NodeDataManager.getAllNodesLst().forEach(node -> {
                                        if (node.getDefrostProfileTitle().equals(defrostProfile.getName())) {
                                            inUse.set(true);
                                            return;
                                        }
                                    });

                                    if (inUse.get()) {
                                        Toast.makeText(context, defrostProfile.getName() + " is currently in use!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        NodeDataManager.RemoveDefrostProfile(defrostProfile.getName());
                                        Toast.makeText(context, defrostProfile.getName() + " removed successfully!", Toast.LENGTH_SHORT).show();
                                        typeProfileActivity.update();
                                    }
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
                }
            });
        }
        return profView;
    }
}
