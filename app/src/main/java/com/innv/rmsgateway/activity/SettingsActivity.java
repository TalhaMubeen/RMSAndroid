package com.innv.rmsgateway.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.adapter.SettingsAdapter;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.service.BLEBackgroundService;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    GridView gv_settings;
    SettingsAdapter settingsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Globals.setScreenOrientation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(gv_settings == null){
            gv_settings = findViewById(R.id.gv_settings);
            settingsAdapter = new SettingsAdapter(this);
            gv_settings.setAdapter(settingsAdapter);
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
