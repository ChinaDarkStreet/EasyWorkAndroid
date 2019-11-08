package com.example.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
}
