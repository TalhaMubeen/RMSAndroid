package com.innv.rmsgateway.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.graph.GraphBase;
import com.innv.rmsgateway.graph.RealtimeHumidity;
import com.innv.rmsgateway.graph.RealtimeTemperature;
import com.jjoe64.graphview.GraphView;

public class GraphViewActivity extends AppCompatActivity {
    private GraphBase mLogicTempScrolling;
    private GraphBase mLogicHumidityScrolling;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String mac = intent.getStringExtra("MAC");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.node_graph_view);

        mLogicTempScrolling = new RealtimeTemperature();
        mLogicHumidityScrolling = new RealtimeHumidity();



        findViewById(R.id.img_fullScreenTemp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullscreen(FullscreenData.TEMPERATURE_SCROLLING, mac);
            }
        });
        findViewById(R.id.img_fullScreenHumidity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullscreen(FullscreenData.HUMIDITY_SCROLLING, mac);
            }
        });


    }

    private void openFullscreen(FullscreenData data, String mac) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra(FullscreenData.ARG_ID, data.name());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLogicTempScrolling.onResume();
        mLogicHumidityScrolling.onResume();
    }

    @Override
    public void onPause() {
        mLogicTempScrolling.onPause();
        mLogicHumidityScrolling.onPause();
        super.onPause();
    }

}
