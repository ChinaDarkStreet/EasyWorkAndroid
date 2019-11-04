package com.example.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    private static DateFormat hms = new SimpleDateFormat("hh:mm:ss");

    public static String getHMS(){
        return hms.format(new Date());
    }
}
