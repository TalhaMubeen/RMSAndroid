package com.innv.rmsgateway.adapter;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.textfield.TextInputLayout;
import com.innv.rmsgateway.R;
import com.innv.rmsgateway.classes.DefrostProfile;
import com.innv.rmsgateway.classes.Profile;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomListAdapter extends BaseAdapter {
    private final Context context;
    Map<Integer, DefrostProfile> defrostProfileMap = new HashMap<>();

    int count = 0;

    //public constructor
    public CustomListAdapter(Context context) {
        this.context = context;
    }


    public void addDefrostProfile() {
        defrostProfileMap.put(count, new DefrostProfile());
        count++;
    }

    public List<DefrostProfile> getDefrostProfile() {
        return (List<DefrostProfile>) defrostProfileMap.values();
    }

    @Override
    public int getCount() {
        return defrostProfileMap.values().size(); //returns total of items in the list
    }

    @Override
    public DefrostProfile getItem(int position) {
        return defrostProfileMap.get(position);
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

        TextView intervalCount = (TextView) convertView.findViewById(R.id.intervalCount);
        intervalCount.setText(Integer.toString(position + 1));

        EditText et_startInterval = (EditText) convertView.findViewById(R.id.et_startInterval);
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
                DefrostProfile prof = getItem(position);
                if (isStart) {
                    prof.setStartHour(selectedHour);
                    prof.setStartMinute(selectedMinute);
                } else {
                    prof.setEndHour(selectedHour);
                    prof.setEndMinute(selectedMinute);
                }
                editText.setText(selectedHour + ":" + selectedMinute);

/*                if (!prof.isEmpty() && prof.isOk()) {
                    Objects.requireNonNull(profileMap.get(selectedNode)).addDefrostProfile(defrostTimeProfile);
                }*/
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

}