package com.innv.rmsgateway.classes;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.View;

import com.innv.rmsgateway.sensornode.SensorNode;

import java.util.List;


public class UpdateCounter/* extends androidx.appcompat.widget.AppCompatTextView*/ {
    private long mStartTime = 0;
    private long mTimeNow = 0;
    private long mDelay = 0;
    private String mPart1 = "";
    private String mPart2 = "";
    private Handler mHandler;
    private View colorView;
    private List<AlertData> alerts;
    private String mac;

/*    public TextViewTimeCounter(Context context) {
        super(context, null, 0);
    }*/

/*
    public TextViewTimeCounter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
*/

/*    public TextViewTimeCounter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }*/

    public void startTimer(SensorNode data, View view, long delay, String part1, String part2) {
       // alerts = data.getAlertsList();
        mac = data.getMacID();
        mStartTime = data.getLastUpdatedDate().getTime();
        mTimeNow = System.currentTimeMillis();
        mDelay = delay;
        mHandler = new Handler();
        colorView = view;
        mPart2 = part2;
        convertDatesToMinutes(mStartTime, mTimeNow);
        mHandler.postDelayed(r, delay);

    }
    public void updateStartTime(SensorNode data){
      //  alerts = data.getAlertsList();
        mStartTime = data.getLastUpdatedDate().getTime();
        convertDatesToMinutes(mStartTime, mTimeNow);
    }


    public void stopTimer() {
        if (mHandler != null) {
            mHandler = null;
            mStartTime = 0;
            mTimeNow = 0;
        }
    }

    public boolean isTimerRunning() {
        return mHandler != null;
    }


    Runnable r = new Runnable() {
        @Override
        public void run() {
            mTimeNow += mDelay;
            convertDatesToMinutes(mStartTime, mTimeNow);
            if (mHandler != null) {
                mHandler.postDelayed(r, mDelay);
            }
        }
    };


    @SuppressLint("SetTextI18n")
    public void convertDatesToMinutes(long start, long end) {
        long secs = (end - start);
        if(secs < 0){
            secs *= -1;
        }
        long second = (secs / 1000) % 60;
        long minute = (secs / (1000 * 60)) % 60;
        long hour = (secs / (1000 * 60 * 60)) % 24;
        long days = (secs / (1000 * 60 * 60 * 24));

        if (minute >= 6 || hour > 0 || days > 0) {
            colorView.setBackgroundTintList(ColorStateList.valueOf(Globals.INACTIVE));
            AlertManager.setAlertStatus(mac, NodeState.Offline);
        } else {

            int color = Globals.NORMAL;
            for (AlertData alert : alerts) {
                if (alert.getNodeState().equals(NodeState.Alert)) {
                    color = Globals.ALERT;
                    break;
                } else if (alert.getNodeState().equals(NodeState.Warning)) {
                    color = Globals.WARNING;
                }
            }
            colorView.setBackgroundTintList(ColorStateList.valueOf(color));
        }

/*        if(days>0){
            String time = String.format(Locale.getDefault(), "%2d days", days);
            setText(time + mPart2);
        }else {
            String time = "";
            if(hour > 0){
                time += String.format(Locale.getDefault(), "%2dh ", hour);
            }
            if(minute > 0){
                time += String.format(Locale.getDefault(), " %2dm ", minute);
            }

            time += String.format(Locale.getDefault(), " %2ds", second);
            setText(mPart1 + time + mPart2);
        }*/
    }
}