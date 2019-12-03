package com.mtl.easywork.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    private static DateFormat hms = new SimpleDateFormat("hh:mm:ss");
    private static DateFormat Hms = new SimpleDateFormat("HH:mm:ss");

    public static String gethms(){
        return hms.format(new Date());
    }
    public static String getHms(){
        return Hms.format(new Date());
    }

    public static String toTime(int t) {
        return String.format("%02d : %02d", t / 60, t % 60);
    }

    public static int getTime() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int minute = instance.get(Calendar.MINUTE);
        return hour * 60 + minute;
    }
}
