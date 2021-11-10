package com.innv.rmsgateway.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.adapter.ProfileViewAdapter;
import com.innv.rmsgateway.adapter.SettingsAdapter;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.ArrayList;
import java.util.List;

public class DataGridViewActivity extends AppCompatActivity {

    GridView gv_data_view;
    FloatingActionButton btn_addNew;

    List<Profile> profileList;
    List<DefrostProfile> defrostProfiles = new ArrayList<>();
    List<SensorNode> sensorNodes;
    String settingType;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_defrost_asset_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        settingType = intent.getStringExtra("SettingType");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(gv_data_view == null){
            gv_data_view = findViewById(R.id.gv_data_view);
            btn_addNew = findViewById(R.id.btn_addNew);
        }

        switch (settingType){
            case "Profile":
                getSupportActionBar().setTitle("Type Profiles");
                profileList = ProfileManager.getProfileList();
                ProfileViewAdapter profileViewAdapter = new ProfileViewAdapter(this, profileList, null);
                gv_data_view.setAdapter(profileViewAdapter);

                btn_addNew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DataGridViewActivity.this, TypeProfileActivity.class);
                        intent.putExtra("Title", "");
                        DataGridViewActivity.this.startActivity(intent);
                    }
                });

                break;

            case "Defrost":
                getSupportActionBar().setTitle("Defrost Profiles");
                defrostProfiles.clear();
                defrostProfiles.addAll(DefrostProfileManager.getDefrostProfiles());
                ProfileViewAdapter defrostProfileAdapter = new ProfileViewAdapter(this, null, defrostProfiles);
                gv_data_view.setAdapter(defrostProfileAdapter);
                btn_addNew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DataGridViewActivity.this, DefrostProfileActivity.class);
                        intent.putExtra("Title", "");
                        DataGridViewActivity.this.startActivity(intent);
                    }
                });

                break;

            case "Assets":
                getSupportActionBar().setTitle("Assets Management");
                sensorNodes = NodeDataManager.getAllNodesLst();


                btn_addNew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
/*                        Intent intent = new Intent(DataGridViewActivity.this, TypeProfileActivity.class);
                        intent.putExtra("Title", "");
                        DataGridViewActivity.this.startActivity(intent);*/
                    }
                });

                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
