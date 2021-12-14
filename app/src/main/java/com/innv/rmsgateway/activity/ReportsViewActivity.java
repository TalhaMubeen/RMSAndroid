package com.innv.rmsgateway.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.service.BLEBackgroundService;

public class ReportsViewActivity extends AppCompatActivity {

    private static final String TAG = "ReportsViewActivity";

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
    protected void onResume() {
        super.onResume();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Globals.setScreenOrientation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reportview_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setTitle("Reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        TextView tv_24hReport = findViewById(R.id.tv_24hReport);
        TextView tv_abnormalReport = findViewById(R.id.tv_abnormalReport);


        TextView tv_assetsReport = findViewById(R.id.tv_assetsReport);
        tv_assetsReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportsViewActivity.this, AssetsReport.class);
                startActivity(intent);
            }
        });

    }
}
