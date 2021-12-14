package com.innv.rmsgateway.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.graph.GraphBase;
import com.innv.rmsgateway.graph.RealtimeHumidity;
import com.innv.rmsgateway.graph.RealtimeTemperature;
import com.innv.rmsgateway.sensornode.SensorNode;
import com.jjoe64.graphview.GraphView;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import static android.view.View.NO_ID;

public class AssetsReport extends AppCompatActivity implements AdapterView.OnItemClickListener  {

    private static final String TAG = "AssetsReport";

/*    LineChart temperatureChart, humidityChart;
    LimitLine ll_Temperature, ll_Humidity;*/
    MaterialButtonToggleGroup mbtg_IntervalButtons;
    AutoCompleteTextView actv_nodeSelector;
    int interval = 1;
    List<String> nodesList;
    int selectedItemPos = 0;

    private GraphBase mLogicTempScrolling;
    private GraphBase mLogicHumidityScrolling;

    private GraphView temp_graph;
    private GraphView humidity_graph;
    SensorNode selectedNode;
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
        if(nodesList != null && nodesList.size() > 0) {
            actv_nodeSelector.setText(nodesList.get(0), false);
            onItemClick(null, null, selectedItemPos, 0);
            mLogicTempScrolling.onResume();
            mLogicHumidityScrolling.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLogicTempScrolling.onPause();
        mLogicHumidityScrolling.onPause();
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
        setContentView(R.layout.assets_grphview_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Objects.requireNonNull(getSupportActionBar()).setTitle(TAG);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mbtg_IntervalButtons = findViewById(R.id.mbtg_IntervalButtons);

        mbtg_IntervalButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

            MaterialButton checkedButton = findViewById(checkedId);
            String buttonText = checkedButton.getText().toString();

            if (checkedId != NO_ID && isChecked) {
                switch (buttonText) {
                    case "1H":
                        interval = 1;
                        break;
                    case "8H":
                        interval = 8;
                        break;
                    case "24H":
                        interval = 24;
                        break;
                }

                onItemClick(null, null, selectedItemPos, -1);
            }else if(!isChecked){
                interval = 0;
            }
        });

        actv_nodeSelector = findViewById(R.id.actv_nodeSelector);
        nodesList = NodeDataManager.getPreCheckedNodesNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, nodesList);
        actv_nodeSelector.setAdapter(adapter);
        actv_nodeSelector.setOnItemClickListener(this);

        mLogicTempScrolling = new RealtimeTemperature();
        mLogicHumidityScrolling = new RealtimeHumidity();

        temp_graph = (GraphView) findViewById(R.id.temp_graph);
        humidity_graph = (GraphView) findViewById(R.id.humidity_graph);

        findViewById(R.id.img_fullScreenTemp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullscreen(FullscreenData.TEMPERATURE_SCROLLING,true);
            }
        });
        findViewById(R.id.img_fullScreenHumidity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullscreen(FullscreenData.HUMIDITY_SCROLLING, false);
            }
        });
    }

    private void openFullscreen(FullscreenData data, Boolean isTemp ) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra(FullscreenData.ARG_ID, data.name());
        intent.putExtra(FullscreenData.TYPE, isTemp);
        startActivity(intent);
    }

    private Date getDateFromInterval(int interval){
        long DAY = interval * 60 * 60 * 1000;
        long time =  new Date().getTime() - DAY;
        Date date = new Date(time);
        return date;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(position == ListView.INVALID_POSITION){
            Toast.makeText(this, "Please select valid RMS node", Toast.LENGTH_SHORT).show();
        }else if(id == -1 && selectedNode == null){
            Toast.makeText(this, "Please select valid RMS node", Toast.LENGTH_SHORT).show();
        }
        else {

            String name = nodesList.get(position);
            selectedNode = NodeDataManager.getPreCheckedNodeFromName(name);

            if (interval != 0 && selectedNode != null) {
                selectedItemPos = position;
                Date date1 = getDateFromInterval(interval);
                Date date2 = new Date();
                List<SensorNode> logs = NodeDataManager.getSensorLogsBetween(selectedNode.getMacID(), date1, date2);

                if (date1.getDay() == date2.getDay()) {
                    final long diff = (long) (interval * 60 * 60 * 1000);
                    logs.removeIf(node1 -> {
                        if(selectedNode.getLastUpdatedDate().after(date1)){
                            return false;
                        }
                        return true;
                    });
                }


                Globals.temperatureData.clear();
                Globals.humidityData.clear();
                temp_graph.removeAllSeries();
                humidity_graph.removeAllSeries();

                for ( SensorNode log : logs){
                    Globals.temperatureData.put(log.getLastUpdatedDate(),log.getTemperature());
                    Globals.humidityData.put(log.getLastUpdatedDate(),log.getHumidity());
                }
                Integer maxHumid, minHumid;
                Double minTemp, maxTemp;

                if(Globals.humidityData.size() > 0 ) {
                     minHumid = Globals.humidityData.firstEntry().getValue();
                     maxHumid = Globals.humidityData.lastEntry().getValue();
                    mLogicHumidityScrolling.initHumidityGraph(humidity_graph, Globals.humidityData, this);
                }

                if(Globals.temperatureData.size() > 0) {
                    minTemp = Globals.temperatureData.firstEntry().getValue();
                    maxTemp = Globals.temperatureData.lastEntry().getValue();
                    mLogicTempScrolling.initTemperatureGraph(temp_graph,  Globals.temperatureData, this);
                }

            } else {
               // Toast.makeText(this, "Please select valid interval", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
