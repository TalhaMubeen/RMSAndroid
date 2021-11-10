package com.innv.rmsgateway.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.classes.InputFilterMinMax;
import com.innv.rmsgateway.classes.Profile;
import com.innv.rmsgateway.classes.ProfileManager;
import com.innv.rmsgateway.data.NodeDataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeProfileActivity extends AppCompatActivity {

    Profile selectedProfile;
    Profile updatedDataProfile;
    EditText et_profile_name ;
    EditText et_min_temp;
    EditText et_max_temp;
    EditText et_min_humidity;
    EditText et_max_humidity;
    ImageView iv_profile_image;
    Spinner sp_warn2Alert;
    Context context;
    AlertDialog alertDialog;
    List<Integer> warn2AlertTimeList = new ArrayList<Integer>(Arrays.asList(0,5,10,20,30,60));
    String title ;
    boolean updatingProfileInfo = false;
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
        setContentView(R.layout.add_type_profile);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;
        title = getIntent().getStringExtra("Title");

        if(!title.isEmpty()) {
            updatingProfileInfo = true;
            getSupportActionBar().setTitle(title);
            selectedProfile = ProfileManager.getProfile(title);
        }else{
            updatingProfileInfo = false;
            selectedProfile = null;
            getSupportActionBar().setTitle("Add Profile");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
    }

    private Boolean isDataChanged(boolean showAlerts) {
        try {


            if(et_profile_name.getText().toString().isEmpty()){
               if(showAlerts) Toast.makeText(context, "Profile name can not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            String profileTitle = Globals.capitalize(et_profile_name.getText().toString());
            Profile prof = ProfileManager.getProfile(profileTitle);
            if(prof!= null && showAlerts && !updatingProfileInfo) {
                Toast.makeText(context, profileTitle + " already exists!", Toast.LENGTH_SHORT).show();
                return false;
            }

            updatedDataProfile = new Profile();
            updatedDataProfile.setTitle(Globals.capitalize(et_profile_name.getText().toString()));

            if(et_min_temp.getText().toString().isEmpty()){
                if(showAlerts)  Toast.makeText(context, "Minimum temperature threshold can not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            updatedDataProfile.setLowTempThreshold(Double.parseDouble(et_min_temp.getText().toString()));

            if(et_max_temp.getText().toString().isEmpty()){
                if(showAlerts)  Toast.makeText(context, "Maximum temperature threshold can not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            updatedDataProfile.setHighTempThreshold(Double.parseDouble(et_max_temp.getText().toString()));

            if(Double.parseDouble(et_min_temp.getText().toString()) > Double.parseDouble(et_max_temp.getText().toString())){
                if(showAlerts)  Toast.makeText(context, "Please enter correct temperature thresholds", Toast.LENGTH_SHORT).show();
                return false;
            }

            if(et_min_humidity.getText().toString().isEmpty()){
                if(showAlerts)   Toast.makeText(context, "Minimum humidity can not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            updatedDataProfile.setLowHumidityThreshold(Integer.parseInt(et_min_humidity.getText().toString()));

            if(et_max_humidity.getText().toString().isEmpty()){
                if(showAlerts)   Toast.makeText(context, "Maximum humidity can not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }

            if(Integer.parseInt(et_min_humidity.getText().toString()) > Integer.parseInt(et_max_humidity.getText().toString())){
                if(showAlerts)  Toast.makeText(context, "Please enter correct humidity thresholds", Toast.LENGTH_SHORT).show();
                return false;
            }
            updatedDataProfile.setHighHumidityThreshold(Integer.parseInt(et_max_humidity.getText().toString()));

            updatedDataProfile.setWarningToAlertTime((Integer) sp_warn2Alert.getSelectedItem());

            if (selectedProfile != null) {
                return !selectedProfile.isEqual(updatedDataProfile); //data not changed if equal
            }

        }catch (Exception e){
            return false;
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    private void initView(){

        TextView tv_maxTempLabel = findViewById(R.id.tv_maxTempLabel);
        TextView tv_minTempLabel = findViewById(R.id.tv_minTempLabel);

        if(!Globals.useCelsius){
            tv_minTempLabel.setText("Minimum Temperature (°F)");
            tv_maxTempLabel.setText("Maximum Temperature (°F)");
        }else{
            tv_minTempLabel.setText("Minimum Temperature (°C)");
            tv_maxTempLabel.setText("Maximum Temperature (°C)");
        }

        et_profile_name = findViewById(R.id.et_profile_name);
        et_min_temp = findViewById(R.id.et_min_temp);
        et_max_temp = findViewById(R.id.et_max_temp);
        et_min_humidity = findViewById(R.id.et_min_humidity);
        et_min_humidity.setFilters(new InputFilter[]{new InputFilterMinMax("0", "100")});

        et_max_humidity = findViewById(R.id.et_max_humidity);
        et_max_humidity.setFilters(new InputFilter[]{new InputFilterMinMax("0", "100")});

        iv_profile_image = findViewById(R.id.iv_profile_image);
        sp_warn2Alert = findViewById(R.id.sp_warn2Alert);

        if(selectedProfile != null){
            et_profile_name.setText(selectedProfile.getTitle());
            et_profile_name.setEnabled(false);
            et_min_temp.setText(Double.toString(selectedProfile.getLowTempThreshold()));
            et_max_temp.setText(Double.toString(selectedProfile.getHighTempThreshold()));
            et_min_humidity.setText(Integer.toString(selectedProfile.getLowHumidityThreshold()));
            et_max_humidity.setText(Integer.toString(selectedProfile.getHighHumidityThreshold()));
            iv_profile_image.setImageDrawable(ContextCompat.getDrawable(this, selectedProfile.getProfileImageId()));
        }


        ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item, warn2AlertTimeList);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_warn2Alert.setAdapter(dataAdapter);
        if(selectedProfile != null) {
            sp_warn2Alert.setSelection(dataAdapter.getPosition(selectedProfile.getWarningToAlertTime()));
        }else{
            sp_warn2Alert.setSelection(0);
        }


        Button btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataChanged(true)){
                    //save Profile data here
                    //updatedDataProfile

                    if(NodeDataManager.AddorUpdateProfile(updatedDataProfile.getTitle(), updatedDataProfile, true)){
                        String toastTitle = "Profile " + updatedDataProfile.getTitle();
                        if(updatingProfileInfo){
                            toastTitle +=   " updated successfully";
                        }else{
                            toastTitle +=   " added successfully";
                        }

                        Toast.makeText(context, toastTitle, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });


        Button btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataChanged(false)) {
                    showAlertifRequired();
                }else{
                    finish();
                }
            }
        });

    }

    private void showAlertifRequired() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Discard Changes ? ")
                .setMessage("Changes made will be lost")
                .setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("YES", null);
        builder.setNegativeButton("NO", null);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface _dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _dialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(alertDialog != null){
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(alertDialog != null){
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(alertDialog != null){
            alertDialog.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(isDataChanged(false)) {
                    showAlertifRequired();
                }else{
                    finish();
                }
               // finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
