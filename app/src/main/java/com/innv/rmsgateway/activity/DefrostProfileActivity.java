package com.innv.rmsgateway.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.DefrostProfileManager;
import com.innv.rmsgateway.classes.Globals;
import com.innv.rmsgateway.data.NodeDataManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefrostProfileActivity extends AppCompatActivity {

    Context context;
    String title;
    boolean updatingProfileInfo = false;
    DefrostProfile updatedDefrostProfile;
    DefrostProfile selectedProfile;
    LinearLayout ll_parent;
    GridView gv_defrostInterval;
    DefrostProfileAdapter defrostProfileAdapter;
    EditText et_defrost_profile_name;
    AlertDialog alertDialog;
    List<DefrostProfile.Interval> defrostIntervals;

    ImageView btn_addNew;

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
        setContentView(R.layout.add_defrost_profile);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;
        title = getIntent().getStringExtra("Title");

        if(!title.isEmpty()) {
            updatingProfileInfo = true;
            getSupportActionBar().setTitle(title);
            selectedProfile = DefrostProfileManager.getDefrostProfile(title);
        }

        else{
            updatingProfileInfo = false;
            getSupportActionBar().setTitle("Add Defrost Profile");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initView() {
        gv_defrostInterval = findViewById(R.id.gv_defrostInterval);
        ll_parent = findViewById(R.id.ll_parent);
        List<DefrostProfile.Interval> intervals ;

        if (selectedProfile != null) {
            intervals = selectedProfile.getDefrostIntervals();
        }else{
            intervals = new ArrayList<>();
        }

        defrostProfileAdapter = new DefrostProfileAdapter(this, intervals);
        gv_defrostInterval.setAdapter(defrostProfileAdapter);

        et_defrost_profile_name = findViewById(R.id.et_defrost_profile_name);

        if (selectedProfile != null) {
            et_defrost_profile_name.setText(selectedProfile.getName());
        }

        if (updatingProfileInfo) {
            et_defrost_profile_name.setEnabled(false);
        }

        Button btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataChanged(true, true)) {
                    //save Profile data here
                    //updatedDataProfile

                    if (NodeDataManager.AddorUpdateDefrostProfile(
                            updatedDefrostProfile.getName(),
                            updatedDefrostProfile,
                            true)) {
                        String toastTitle = "Defrost Profile " + updatedDefrostProfile.getName();
                        if (updatingProfileInfo) {
                            toastTitle += " updated successfully";
                        } else {
                            toastTitle += " added successfully";
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
                if (isDataChanged(false, false)) {
                    showAlert();
                } else {
                    finish();
                }
            }
        });


        btn_addNew = findViewById(R.id.btn_addNew);


        btn_addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defrostIntervals.add(new DefrostProfile.Interval());
                defrostProfileAdapter.notifyDataSetChanged();

                setListViewHeightBasedOnChildren(gv_defrostInterval);

                ViewGroup.LayoutParams ll_lp = ll_parent.getLayoutParams();
                ll_lp.height = gv_defrostInterval.getLayoutParams().height;
                ll_parent.setLayoutParams(ll_lp);
                ll_parent.requestLayout();

            }
        });

        if (!updatingProfileInfo) { //Add one row automatically for new Defrost Profile
            btn_addNew.callOnClick();
        }

        setListViewHeightBasedOnChildren(gv_defrostInterval);

        ViewGroup.LayoutParams ll_lp = ll_parent.getLayoutParams();
        ll_lp.height = gv_defrostInterval.getLayoutParams().height;
        ll_parent.setLayoutParams(ll_lp);
        ll_parent.requestLayout();

    }


    private Boolean isDataChanged(boolean showAlerts, boolean updatedReq) {
        try {

            if(et_defrost_profile_name.getText().toString().isEmpty()){
                if(showAlerts) Toast.makeText(context, "Defrost Profile name can not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            String profileTitle = Globals.capitalize(et_defrost_profile_name.getText().toString());
            DefrostProfile prof = DefrostProfileManager.getDefrostProfile(profileTitle);
            if(prof!= null && !updatingProfileInfo) {
                if(showAlerts) {
                    Toast.makeText(context, profileTitle + " already exists!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            updatedDefrostProfile = new DefrostProfile();
            updatedDefrostProfile.setName(Globals.capitalize(et_defrost_profile_name.getText().toString()));

            AtomicBoolean ret = new AtomicBoolean(false);
            defrostIntervals.forEach(interval -> {
                if(interval.isOk()) {
                    updatedDefrostProfile.addDefrostInterval(interval);
                }
                else if(interval.isEmpty()){ }
                else{
                    ret.set(true);
                }
            });

            if(ret.get()){
                if(showAlerts) {
                    Toast.makeText(context, "Please input correct interval", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            if(updatedDefrostProfile.getDefrostIntervals().size() == 0){
                if(showAlerts) {
                    Toast.makeText(context, "Add at-least 1 defrost interval", Toast.LENGTH_SHORT).show();
                }
                return  false;
            }

            if (selectedProfile != null) {
                boolean retVal = selectedProfile.isEqual(updatedDefrostProfile);
                if(retVal && updatedReq){
                    Toast.makeText(context, "No change detected", Toast.LENGTH_SHORT).show();
                    return false;
                }else if(retVal){
                    return false;
                }
                else {
                    return true;
                }

            }

        }catch (Exception e){
            return false;
        }

        return true;
    }


    private void showAlert() {
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
        initView();
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
                if(isDataChanged(false, false)) {
                    showAlert();
                }else{
                    finish();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void setListViewHeightBasedOnChildren(GridView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight=0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            view = listAdapter.getView(i, view, listView);

            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + ((listView.getVerticalSpacing()) * (listAdapter.getCount()));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }


    class DefrostProfileAdapter extends BaseAdapter {
        private final Context context;

        //public constructor
        public DefrostProfileAdapter(Context context, List<DefrostProfile.Interval> list) {
            defrostIntervals = new ArrayList<>();
            list.forEach(interval -> {
                defrostIntervals.add(new DefrostProfile.Interval(interval));
            });

            this.context = context;
        }

        @Override
        public int getCount() {
            return defrostIntervals.size(); //returns total of items in the list
        }

        @Override
        public DefrostProfile.Interval getItem(int position) {
            return defrostIntervals.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // inflate the layout for each list row
            // selectedNode = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.defrost_child, parent, false);
            }
            //Start Interval
            EditText et_startInterval = (EditText) convertView.findViewById(R.id.et_startInterval);
            ImageView iv_delInterval = (ImageView) convertView.findViewById(R.id.iv_delInterval);
            et_startInterval.setClickable(true);
            et_startInterval.setLongClickable(false);
            et_startInterval.setInputType(InputType.TYPE_NULL);

            TextInputLayout start = (TextInputLayout) convertView.findViewById(R.id.tinputstart);

            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_startInterval.callOnClick();
                }
            });


            et_startInterval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePicker(position, true, et_startInterval);
                }
            });

            //End Interval
            EditText et_endInterval = (EditText) convertView.findViewById(R.id.et_endInterval);
            et_endInterval.setClickable(true);
            et_endInterval.setLongClickable(false);
            et_endInterval.setInputType(InputType.TYPE_NULL);

            TextInputLayout end = (TextInputLayout) convertView.findViewById(R.id.tinputEnd);

            end.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_endInterval.callOnClick();
                }
            });

            et_endInterval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePicker(position, false, et_endInterval);
                }
            });

            if (updatingProfileInfo) {
                DefrostProfile.Interval prof = getItem(position);
                if (!prof.isEmpty()) {
                    et_startInterval.setText(Integer.toString(prof.getStartHour()) + ":" + Integer.toString(prof.getStartMinute()));
                    et_endInterval.setText(Integer.toString(prof.getEndHour()) + ":" + Integer.toString(prof.getEndMinute()));
                } else {
                    et_startInterval.setText("");
                    et_endInterval.setText("");
                }
            }

            iv_delInterval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    defrostIntervals.remove(position);
                    defrostProfileAdapter.notifyDataSetChanged();

                    setListViewHeightBasedOnChildren(gv_defrostInterval);

                    ViewGroup.LayoutParams ll_lp = ll_parent.getLayoutParams();
                    ll_lp.height = gv_defrostInterval.getLayoutParams().height;
                    ll_parent.setLayoutParams(ll_lp);
                    ll_parent.requestLayout();
                }
            });

            // returns the view for the current row
            return convertView;
        }

        private void showTimePicker(int position, boolean isStart, EditText editText) {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

                @SuppressLint("SetTextI18n")
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    DefrostProfile.Interval prof = getItem(position);
                    if (isStart) {
                        prof.setStartHour(selectedHour);
                        prof.setStartMinute(selectedMinute);
                    } else {
                        prof.setEndHour(selectedHour);
                        prof.setEndMinute(selectedMinute);
                    }
                    editText.setText(selectedHour + ":" + selectedMinute);
                }
            }, hour, minute, true);
            timePickerDialog.show();
        }

    }

}
