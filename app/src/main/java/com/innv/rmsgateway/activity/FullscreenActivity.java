package com.innv.rmsgateway.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.graph.GraphBase;
import com.innv.rmsgateway.graph.RealtimeHumidity;
import com.innv.rmsgateway.graph.RealtimeTemperature;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

public class FullscreenActivity extends AppCompatActivity {
    private GraphBase mLogic;

    @Override
    protected void onStop() {
        super.onStop();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name = getIntent().getStringExtra(FullscreenData.ARG_ID);
        Boolean isTemp = getIntent().getBooleanExtra(FullscreenData.TYPE, false);
        FullscreenData fullscreenGraph = FullscreenData.valueOf(name);

        setContentView(fullscreenGraph.contentView);
        try {
            mLogic = fullscreenGraph.callingClass.newInstance();
            if(isTemp){
                mLogic.onCreate(this, Globals.temperatureData,this);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Temperature Graph");
            }else{
                mLogic.onCreateHumid(this,  Globals.humidityData,this);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Humidity Graph");
            }


        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogic.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLogic.onPause();
    }
}
