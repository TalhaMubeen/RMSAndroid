package com.innv.rmsgateway.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.innv.rmsgateway.graph.GraphBase;

public class FullscreenActivity extends AppCompatActivity {
    private GraphBase mLogic;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String name = getIntent().getStringExtra(FullscreenData.ARG_ID);
        String mac = getIntent().getStringExtra(FullscreenData.NODE_MAC);
        FullscreenData fullscreenGraph = FullscreenData.valueOf(name);

        setContentView(fullscreenGraph.contentView);
        try {
            mLogic = fullscreenGraph.callingClass.newInstance();
            mLogic.onCreate(this, mac);

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
