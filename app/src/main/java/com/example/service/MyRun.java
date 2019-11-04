package com.example.service;

import android.os.Handler;
import android.os.Vibrator;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class MyRun implements Runnable {
    private long[] test = new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};
    private Handler handler = null;
    private Vibrator vibrator = null;
    private int[] times = new int[]{20, 35, 45, 55, 75, 90, 100, 120};
    private TextView text = null;

    public MyRun(Vibrator vibrator, TextView text, Handler handler) {
        this.vibrator = vibrator;
        this.text = text;
        this.handler = handler;
    }

    @Override
    public void run() {
        Date date = new Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int minute = instance.get(Calendar.MINUTE);
        int start = 450;
        int i1 = hour * 60 + minute - start;
        int i3 = i1 % 120;
        System.out.println("i3 = " + i3);
        int ti = 0;
        if (i3 < 45) {
            if (i3 < 35){
                ti = 35 - i3;
            }else{
                ti = 45 - i3;
            }
            text.setText(String.format("上课时间:\n45分钟\n下个时间点:\n%s", toTime(i1 + 45 - i3 + start)));
        } else if (i3 < 55) {
            ti = 55 - i3;
            text.setText(String.format("下课时间:\n10分钟\n下个时间点:\n%s", toTime(i1 + ti + start)));
        } else if (i3 < 100) {
            if (i3 < 90){
                ti = 90 - i3;
            }else{
                ti = 100 - i3;
            }
            text.setText(String.format("上课时间:\n45分钟\n下个时间点:\n%s", toTime(i1 + 100 - i3 + start)));
        } else {
            ti = 120 - i3;
            text.setText(String.format("下课时间:\n20分钟\n下个时间点:\n%s", toTime(i1 + ti + start)));
        }
        if(i3 == 0 || i3 == 45 || i3 == 55 || i3 == 100){
            vibrator.vibrate(test, -1);
        }else if (i3 == 35 || i3 == 90){
            vibrator.vibrate(1000);
        }else{
            vibrator.vibrate(500);
        }

        handler.postDelayed(this, ti * 60 * 1000);
    }

    private String toTime(int i) {
        return i / 60 + " : " + i % 60;
    }

    public void stopRunning() {

    }
}
