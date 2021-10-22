package com.innv.rmsgateway.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.activity.ScanActivity;
import com.innv.rmsgateway.adapter.AssetFiltersAdapter;
import com.innv.rmsgateway.adapter.AssetsViewAdapter;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.UpdateCounter;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.interfaces.NotificationAlertsCallback;
import com.innv.rmsgateway.sensornode.SensorNode;
import com.innv.rmsgateway.service.BLEBackgroundService;
import com.innv.rmsgateway.service.OnBLEDeviceCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


public class AssetsActivity extends AppCompatActivity implements
        View.OnClickListener,
        OnBLEDeviceCallback,
        NotificationAlertsCallback {

    private static final String TAG = "AssetsActivity";

    private ImageView ivAddDevices;
    private GridView gvDevices;

    // A reference to the service used to get BLE Updates
    AssetsViewAdapter gv_assetsAdapter;
    static boolean showOne = false;
    RecyclerView rvAssetFilters;
    AssetFiltersAdapter assets_adapter;
    Map<String, SensorNode> preCheckedNodes;

    static int selectedFilterPosition = 0;
    static String selectedText = "All";

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
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setTitle("Assets");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int position = intent.getIntExtra("Position", 0);
        showOne = intent.getBooleanExtra("ShowOne", false);

        if (position > -1) {
            selectedFilterPosition = position;
            selectedText = getString(Globals.AlertType[selectedFilterPosition]);

        }

        AlertManager.setNotificationAlertCallback( this.getClass().getSimpleName() , this);
/*        ivAddDevices = (ImageView) findViewById(R.id.iv_add);
        ivAddDevices.setOnClickListener(this);*/
    }


    public void scrollToSelection(int position, String txt) {

        if (position > selectedFilterPosition) {
            rvAssetFilters.smoothScrollToPosition(Math.min(position + 1, 6));
        } else {
            rvAssetFilters.smoothScrollToPosition(Math.max(position - 1, 0));
        }

        selectedFilterPosition = position;
        selectedText = txt;
    }

    @Override
    protected void onResume() {
        super.onResume();

        BLEBackgroundService.addBLEUpdateListener(this.getClass().getSimpleName(), this);

        UpdatePreCheckedNodes();

        assets_adapter = new AssetFiltersAdapter(this);
        rvAssetFilters = findViewById(R.id.rvAssetFilters);
        rvAssetFilters.setAdapter(assets_adapter);
        if (showOne) {
            assets_adapter.setSelectedText(selectedText);
            assets_adapter.setSelectedFilterPosition(selectedFilterPosition);
            rvAssetFilters.scrollToPosition(selectedFilterPosition);
        }

        updateData();
    }

    public void UpdatePreCheckedNodes() {
        List<SensorNode> list = NodeDataManager.getPreCheckedNodes();
        preCheckedNodes = new HashMap<>();
        for (SensorNode node : list) {
            preCheckedNodes.put(node.getMacID(), node);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
/*        if (v.getId() == R.id.iv_add) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        }*/
    }


    @Override
    public void onBLEDeviceCallback(BleDevice device) {
        updateData();
    }

    @Override
    public void updateData() {
        List<SensorNode> dataList = new ArrayList<>(NodeDataManager.getPreCheckedNodes());
        List<AlertData> alerts = new ArrayList<>(AlertManager.getAllNodesAlertList());

        selectedFilterPosition = assets_adapter.getSelectedFilterPosition();
        selectedText = assets_adapter.getSelectedText();

        switch (selectedFilterPosition) {
            //Generic logic to deal with every kind of filter
            case 0: //ALL
                break;
            case 1: //Alert
            case 2: //Warning
            case 3: //Normal
            case 4: //Defrost
            case 5: //Offline
            case 6: //ComFail

                alerts.removeIf(x -> !x.getStatusString().equals(selectedText));
                dataList.removeIf(data -> {

                    AtomicBoolean ret = new AtomicBoolean(true);

                    alerts.forEach(alert -> {

                        if (alert.getNodeMacAddress().equals(data.getMacID())) {
                            ret.set(false);
                        }

                    });

                    return ret.get();
                });

                break;

            default:
                break;
        }

        if (gv_assetsAdapter == null) {
            gv_assetsAdapter = new AssetsViewAdapter(this, dataList);
            gvDevices = (GridView) findViewById(R.id.gv_devices);
            gvDevices.setAdapter(gv_assetsAdapter);
        } else {

            gv_assetsAdapter.UpdateListData(dataList);
            gv_assetsAdapter.notifyDataSetChanged();

        }
    }
}