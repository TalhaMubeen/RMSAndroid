package com.innv.rmsgateway.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.adapter.AssetManagementAdapter;
import com.innv.rmsgateway.data.NodeDataManager;

public class AssetManagementActivity extends AppCompatActivity {

    FloatingActionButton btn_addNew;
    AssetManagementAdapter gv_assetsAdapter;
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
        setContentView(R.layout.activity_device_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setTitle("Asset Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        ListView list_device = (ListView) findViewById(R.id.list_device);

        if (gv_assetsAdapter == null) {
            gv_assetsAdapter = new AssetManagementAdapter(this, NodeDataManager.getAllNodesLst(), true);
            list_device.setAdapter(gv_assetsAdapter);
        }else{
            gv_assetsAdapter.UpdateListData(NodeDataManager.getAllNodesLst());
            gv_assetsAdapter.notifyDataSetChanged();
        }

        btn_addNew = findViewById(R.id.btn_addNew);
        btn_addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssetManagementActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void update(){
        gv_assetsAdapter.UpdateListData(NodeDataManager.getAllNodesLst());
        gv_assetsAdapter.notifyDataSetChanged();
    }

}
