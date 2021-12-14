/*
package com.innv.rmsgateway.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.JsonWebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class EndPointActivity extends AppCompatActivity implements Observer {



    ListView lvEndPoint;
    Button btAddServerInfo;
    endPointAdapter endPointAdapter;
    ArrayList<String> serverInfoList = new ArrayList<String>();
    SharedPref pref;
    int entryPosition;
    ProgressDialog progressDialog;
    DialogInterface dialog;
    EditText serverPort;
    EditText serverName;
    EditText serverDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setLocale(this);
        setTitle(getString(R.string.title_activity_end_point));
        setContentView(R.layout.activity_end_point);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = new SharedPref(this);
        lvEndPoint = (ListView) findViewById(R.id.lv_server_info);
        serverInfoList = getArrayList(PREFS_KEY_SERVER);

        if (getArrayList(PREFS_KEY_SERVER).size() > 0) {
            endPointAdapter = new endPointAdapter(EndPointActivity.this, serverInfoList, EndPointActivity.this);
        } else {
            endPointAdapter = new endPointAdapter(EndPointActivity.this, new ArrayList<String>(), EndPointActivity.this);
        }
        lvEndPoint.setAdapter(endPointAdapter);
        //new ServerTestAsyncTask().execute();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(null);
               */
/* Snackbar.make(view, getString(R.string.replace_action), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*//*

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        //code it to launch an intent to the activity you want
        finish();
        return true;
    }

*/
/*    @Override
    public void onClick(String value, int position) {
        entryPosition = position;
        showDialog(serverInfoList.get(position));
    }*//*


    @Override
    public void update(Observable o, Object arg) {
        Intent intent = (Intent) arg;
        Bundle b1 = intent.getExtras();
        final String messageName = b1.getString("messageName");
        final String messageData = b1.getString("messageData");
        String message = "";
        switch (messageName) {
            case Globals.OBSERVABLE_MESSAGE_LANGUAGE_CHANGED:
                EndPointActivity.this.recreate();
        }
    }

    public void showDialog(final String info) {
        // get prompts.xml view
        LayoutInflater li = this.getLayoutInflater();
        View promptsView = li.inflate(R.layout.dialog_server_info, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle(getString(R.string.dialog_server_info));

        serverDisplay = (EditText) promptsView
                .findViewById(R.id.et_server_display);
        serverName = (EditText) promptsView
                .findViewById(R.id.et_server_name);
        serverPort = (EditText) promptsView
                .findViewById(R.id.et_server_port);

        if (info != null) {
            String[] server = info.split(PREFS_STRING_DIVIDER);
            serverDisplay.setText(server[0]);
            serverName.setText(server[1]);
            serverPort.setText(server[2]);
        }
        alertDialogBuilder.setPositiveButton(getString(R.string.btn_ok), null);
        alertDialogBuilder.setNegativeButton(getString(R.string.btn_cancel), null);
        alertDialogBuilder.setNeutralButton(getString(R.string.btn_test), null);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface _dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (serverDisplay.getText().toString().equals("")) {
                            Toast.makeText(EndPointActivity.this, getString(R.string.toast_display), Toast.LENGTH_LONG).show();
                            serverDisplay.requestFocus();
                        } else if (serverName.getText().toString().equals("")) {
                            serverName.requestFocus();
                            Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_name), Toast.LENGTH_LONG).show();
                        } else if (serverPort.getText().toString().equals("")) {
                            serverPort.requestFocus();
                            Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_port), Toast.LENGTH_LONG).show();
                        } else {
                            if (isNetworkAvailable(EndPointActivity.this)) {
                                Boolean isDuplicate = false;
                                for (int i = 0; i < serverInfoList.size(); i++) {
                                    String _serverString = serverInfoList.get(i);
                                    String[] _info = _serverString.split(PREFS_STRING_DIVIDER);
                                    if (serverName.getText().toString().equals(_info[1]) && serverPort.getText().toString().equals(_info[2])) {
                                        Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_exist), Toast.LENGTH_LONG).show();
                                        isDuplicate = true;
                                        //_dialog.dismiss();
                                    }
                                }
                                if (!isDuplicate) {
                                    String displayName = serverDisplay.getText().toString();
                                    String server = serverName.getText().toString().trim();
                                    String port = serverPort.getText().toString().trim();
                                    if (info != null) {
                                        serverInfoList.set(entryPosition, displayName + PREFS_STRING_DIVIDER + server + PREFS_STRING_DIVIDER + port);
                                    } else {
                                        serverInfoList.add(displayName + PREFS_STRING_DIVIDER + server + PREFS_STRING_DIVIDER + port);
                                    }
                                    saveArrayList(serverInfoList, PREFS_KEY_SERVER);
                                    endPointAdapter.notifyDataSetChanged();
                                    _dialog.dismiss();
                                }

                            } else {
                                Toast.makeText(EndPointActivity.this, getString(R.string.toast_internet_unavail), Toast.LENGTH_LONG).show();
                            }

                        }

                    }
                });

                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _dialog.dismiss();
                    }
                });
                Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (serverDisplay.getText().toString().equals("")) {
                            Toast.makeText(EndPointActivity.this, getString(R.string.toast_display), Toast.LENGTH_LONG).show();
                            serverDisplay.requestFocus();
                        } else if (serverName.getText().toString().equals("")) {
                            serverName.requestFocus();
                            Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_name), Toast.LENGTH_LONG).show();
                        } else if (serverPort.getText().toString().equals("")) {
                            serverPort.requestFocus();
                            Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_port), Toast.LENGTH_LONG).show();
                        } else {
                            if (isNetworkAvailable(EndPointActivity.this)) {
                                dialog = _dialog;
                                new ServerAsyncTask().execute();
                            } else {
                                Toast.makeText(EndPointActivity.this, getString(R.string.toast_internet_unavail), Toast.LENGTH_LONG).show();
                            }

                        }

                    }
                });
            }
        });
        // show it
        alertDialog.show();
    }

    private class ServerAsyncTask extends AsyncTask<Void, Void, Void> {

        String server;
        String port;

        public User user;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(EndPointActivity.this);
            progressDialog.setMessage(getString(R.string.dialog_wait_title));
            progressDialog.show();
            server = serverName.getText().toString();
            port = serverPort.getText().toString();
            Globals.lastWsReturnCode = 0;
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {
            // Parse response data
            JSONArray jaUser = new JSONArray();
            //String url = "http://" + server + ":"+ port + "/api/List/JourneyPlan/pull";
            String userString = JsonWebService.getJSON(getPingAddress(server, port), 5000);
            try {
                if (userString != null) {
                    jaUser = new JSONArray(userString);
                    user = new User(jaUser.getJSONObject(0));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (Globals.lastWsReturnCode == 200 || Globals.lastWsReturnCode == 201) {
                if (user != null) {
                    if (!user.getRemoved()) {

                        Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_working), Toast.LENGTH_LONG).show();
                    }
                }
            } else if (Globals.lastWsReturnCode == 401) {
                Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_working), Toast.LENGTH_LONG).show();

            } else if (Globals.lastWsReturnCode == 404) {
                Toast.makeText(EndPointActivity.this, getString(R.string.toast_timps_no_run), Toast.LENGTH_LONG).show();
            } else if (Globals.lastWsReturnCode == 0) {
                Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_unreach), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(EndPointActivity.this, getString(R.string.toast_server_unreach), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }
}
*/
