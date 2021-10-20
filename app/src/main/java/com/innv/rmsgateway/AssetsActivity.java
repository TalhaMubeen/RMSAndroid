package com.innv.rmsgateway;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.innv.rmsgateway.activity.AddNodesActivity;
import com.innv.rmsgateway.classes.AlertData;
import com.innv.rmsgateway.classes.AlertManager;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.UpdateCounter;
import com.innv.rmsgateway.data.BleDevice;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.interfaces.NotificationAlertsCallback;
import com.innv.rmsgateway.sensornode.SensorDataDecoder;
import com.innv.rmsgateway.sensornode.SensorNode;
import com.innv.rmsgateway.service.BLEBackgroundService;
import com.innv.rmsgateway.service.OnBLEDeviceCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class AssetsActivity extends AppCompatActivity implements View.OnClickListener, OnBLEDeviceCallback, NotificationAlertsCallback {

    private static final String TAG = "AssetsActivity";
    public static int NORMAL = Color.parseColor("#99C24D");
    public static int ALERT = Color.parseColor("#C1272D");
    public static int INACTIVE = Color.parseColor("#006E90");
    public static int WARNING = Color.parseColor("#F18F01");
    public static int BELOW_THRESHOLD = Color.parseColor("#41BBD9");

    private static boolean FirstRun = true;

    private ImageView ivAddDevices;
    private GridView gvDevices;


    static Map<String, UpdateCounter> counterList = new HashMap<>();
    // A reference to the service used to get BLE Updates

    GridViewAdapter gv_adapter;


    static boolean showOne = false;
    RecyclerView rvAssetFilters;
    AssetFiltersAdapter assets_adapter;
    Map<String, SensorNode> precheckedNodes;

    static int selectedFilterPosition = 0;
    static String selectedText = "All";

    private final int[] FilterLable = new int[]{
            R.string.all,
            R.string.alert,
            R.string.warning,
            R.string.normal,
            R.string.offline,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        int filter = intent.getIntExtra("Filter", 0);
        showOne = intent.getBooleanExtra("ShowOne", false);
        if(filter > 3){
            filter = filter - 1;
        }

        selectedFilterPosition = filter;
        selectedText = getString(FilterLable[selectedFilterPosition]);


        AlertManager.setNotificationAlertCallback(this);

        ivAddDevices = (ImageView) findViewById(R.id.iv_add);
        ivAddDevices.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BLEBackgroundService.addBLEUpdateListener(this.getClass().getSimpleName(), this);

        UpdatePreCheckedNodes();

        assets_adapter = new AssetFiltersAdapter(this);
        rvAssetFilters = findViewById(R.id.rvAssetFilters);
        rvAssetFilters.setAdapter(assets_adapter);

        updateData();


    }

    public void UpdatePreCheckedNodes(){
        List<SensorNode> list = NodeDataManager.getPreCheckedNodes();
        precheckedNodes = new HashMap<>();
        for(SensorNode node : list){
            precheckedNodes.put(node.getMacID(), node);
        }
    }

    private void stopUIUpdates(){
        for (String key : counterList.keySet()) {
            Objects.requireNonNull(counterList.get(key)).stopTimer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
        stopUIUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopUIUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
        stopUIUpdates();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                BLEBackgroundService.removeBLEUpdateListener(this.getClass().getSimpleName());
                stopUIUpdates();
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_add) {
            Intent intent = new Intent(this, AddNodesActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public void onBLEDeviceCallback(BleDevice device) {
        updateData();
    }

    @Override
    public void updateData() {
        List<SensorNode> dataList = new ArrayList<>(NodeDataManager.getPreCheckedNodes());
        List<String> selectedNodes = new ArrayList<>();
        List<AlertData> alerts =  new ArrayList<>(AlertManager.getAllNodesAlertList());

        switch (selectedFilterPosition) {
            //Generic logic to deal with every kind of filter
            case 0 : //ALL
                break;
            case 1: //Normal
            case 2: //Warning
            case 3: //Alert
            case 4://Offline

                alerts.removeIf(x -> !x.getStatusString().equals(selectedText));
                for(AlertData alert :  alerts){ selectedNodes.add(alert.getNodeMacAddress()); }

                if(counterList.size() > 0) {

                    for (String key : counterList.keySet()) {
                        if (!selectedNodes.contains(key)) {
                            Objects.requireNonNull(counterList.get(key)).stopTimer();
                        }
                    }
                    counterList.entrySet().removeIf(entry -> !selectedNodes.contains(entry.getValue()));
                }

                dataList.removeIf( x -> !selectedNodes.contains(x.getMacID()));
                break;

            default:
                break;
        }

        if(gv_adapter == null){
            gv_adapter = new GridViewAdapter(this, dataList);
            gvDevices = (GridView) findViewById(R.id.gv_devices);
            gvDevices.setAdapter(gv_adapter);
        }else {

            gv_adapter.UpdateListData(dataList);
            gv_adapter.notifyDataSetChanged();

        }
       // assets_adapter.notifyDataSetChanged();
    }


    static class GridViewAdapter extends BaseAdapter {

        List<SensorNode> mRMSDevices= new ArrayList<>();
        LayoutInflater inflater;
        Context context;

        public GridViewAdapter(Context ctx, List<SensorNode> list){
            mRMSDevices = list;
            if(list.size() == 0){

                counterList.clear();
            }
            inflater = LayoutInflater.from(ctx);
            context = ctx;
        }

        public void UpdateListData(List<SensorNode> list){
            mRMSDevices.clear();
            mRMSDevices = list;
        }


        @Override
        public int getCount() {
            return mRMSDevices.size();
        }

        @Override
        public SensorNode getItem(int position) {
            return mRMSDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mRMSDevices.get(position).hashCode();
        }


        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rmsDeviceCardView = convertView;
            SensorNode item = getItem(position);
            Profile itemProf = item.getProfile();

            if (rmsDeviceCardView == null) {
                rmsDeviceCardView = inflater.inflate(R.layout.gridview_monitoring_item_new, parent, false);
            }

            CardView sensor_card_view = (CardView) rmsDeviceCardView.findViewById(R.id.sensor_card_view);
/*            sensor_card_view.setOnClickListener((View v) -> {
                Intent intent = new Intent(context, GraphViewActivity.class);
                intent.putExtra("MAC", item.getMacID());
                context.startActivity(intent);
            });*/

            TextView sensor_name = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_name);
            sensor_name.setText(item.getName());

            TextView temperature_value = (TextView) rmsDeviceCardView.findViewById(R.id.temperature_value);
            temperature_value.setText(Double.toString(SensorDataDecoder.round(item.getTemperature(), 1)) /*+ "°C"*/ );


            List<AlertData> alerts = AlertManager.getAlertList(item.getMacID());



            if(item.getTemperature() > itemProf.getHighTempThreshold()){
                temperature_value.setTextColor(ALERT);
            }else if(item.getTemperature() < itemProf.getLowTempThreshold()){
                temperature_value.setTextColor(BELOW_THRESHOLD);
            }

            TextView humidity_value = (TextView) rmsDeviceCardView.findViewById(R.id.humidity_value);
            humidity_value.setText(Integer.toString(item.getHumidity()) + "%");

            if(item.getHumidity() > itemProf.getHighHumidityThreshold()){
                humidity_value.setTextColor(ALERT);
            }else if(item.getHumidity() < itemProf.getLowHumidityThreshold()){
                humidity_value.setTextColor(BELOW_THRESHOLD);
            }

            TextView sensor_rssi = (TextView) rmsDeviceCardView.findViewById(R.id.sensor_rssi);
            sensor_rssi.setText(Double.toString(SensorDataDecoder.round(item.getRssi(), 1))/* + " dbm"*/);

            int alertsCount = AlertManager.getAlertsCount(item.getMacID(), AlertData.NodeState.Alert);

            TextView tv_alerts = (TextView) rmsDeviceCardView.findViewById(R.id.tv_alerts);
            tv_alerts.setText(Integer.toString(alertsCount));
            if(alertsCount > 0){
                tv_alerts.setTextColor(ALERT);
            }

            if(counterList.containsKey(item.getMacID())){
                Objects.requireNonNull(counterList.get(item.getMacID())).stopTimer();
            }

            View colorView = (View) rmsDeviceCardView.findViewById(R.id.colorNA);
            UpdateCounter counter = new UpdateCounter();
            counter.startTimer(item, colorView,  1000, "", " ago");
            counterList.put(item.getMacID(), counter);


/*
            TextViewTimeCounter tv_last_updated = (TextViewTimeCounter) rmsDeviceCardView.findViewById(R.id.tv_last_updated);

            if(tv_last_updated.isTimerRunning()){
                tv_last_updated.updateStartTime(item);
            }else {
                if (item.getLastUpdatedOn() != null) {
                    tv_last_updated.startTimer(item, color, 1000, "", " ago");
                }
            }

*/
            return rmsDeviceCardView;
        }


    }


    public class AssetFiltersAdapter extends RecyclerView.Adapter<AssetFiltersAdapter.MyViewHolder> {
        Context _context;
        LayoutInflater mInflater;
        List<AssetFiltersAdapter.MyViewHolder> viewList = new ArrayList<>();

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvFilterText;
            View viewFilterColor;
            LinearLayout selected_filter;
            CardView filter;

            MyViewHolder(View itemView) {
                super(itemView);
                this.viewFilterColor = (View) itemView.findViewById(R.id.viewFilterColor);
                this.tvFilterText = (TextView) itemView.findViewById(R.id.tvFilterText);
                this.selected_filter = (LinearLayout) itemView.findViewById(R.id.selected_filter);
                this.filter = (CardView) itemView.findViewById(R.id.filter);

                this.filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!showOne) {

                            for (AssetFiltersAdapter.MyViewHolder holder : viewList) {
                                LinearLayout selection = holder.selected_filter;
                                selection.setBackgroundColor(ContextCompat.getColor(_context, R.color.card_background_color_lite));
                            }

                            selectedFilterPosition = getAdapterPosition();
                            selected_filter.setBackgroundColor(ContextCompat.getColor(_context, R.color.color_defrost));
                            selectedText = tvFilterText.getText().toString();
                            rvAssetFilters.scrollToPosition(selectedFilterPosition);
                            updateData();
                        }
                    }
                });
            }
        }

        public AssetFiltersAdapter(Context ctx) {
            _context = ctx;
            this.mInflater = LayoutInflater.from(ctx);
        }

        @NonNull
        @Override
        public AssetFiltersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = this.mInflater.inflate(R.layout.asset_filter_view, parent, false);
            MyViewHolder holder = new AssetFiltersAdapter.MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(AssetFiltersAdapter.MyViewHolder holder, int position) {
            if(showOne){
                position = selectedFilterPosition;
            }
            TextView tvFilterText = holder.tvFilterText;
            tvFilterText.setText(this._context.getResources().getString(FilterLable[position]));
            View viewFilterColor = holder.viewFilterColor;

            switch (position) {
                case 0 :
                    viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    break;
                case 1:
                    viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_alert)));
                    break;
                case 2:
                    viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_warning)));
                    break;
                case 3:
                    viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_normal)));
                    break;
                case 4:
                    viewFilterColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(_context, R.color.color_offline)));
                    break;
                default:
                    break;
            }

            if(position == selectedFilterPosition){
                holder.selected_filter.setBackgroundColor(ContextCompat.getColor(_context,R.color.color_defrost));
                selectedText = tvFilterText.getText().toString();
            }

            viewList.add(holder);
        }



        @Override
        public int getItemCount() {
            return showOne? 1 : FilterLable.length;
        }
    }


}