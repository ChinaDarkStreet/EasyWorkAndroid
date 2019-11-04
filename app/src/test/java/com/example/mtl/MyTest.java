package com.example.mtl;

import android.app.Service;
import android.os.Vibrator;
import android.util.Log;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class MyTest {
    private int nextTime = 0;
    private int index = 0;
    @Test
    public void test(){

        // 初始化 nextTime
        int time = getTime();
        int abTime = time - 450;
        //获取整除后的值
        int dt = abTime % 120;
        int base = time - dt;
        if (dt < 35) {
            nextTime = base + 35;
            index = 0;
        } else if (dt < 45) {
            nextTime = base + 45;
            index = 1;
        } else if (dt < 55) {
            nextTime = base + 55;
            index = 2;
        } else if (dt < 90) {
            nextTime = base + 90;
            index = 3;
        } else if (dt < 100) {
            nextTime = base + 100;
            index = 4;
        } else {
            nextTime = base + 120;
            index = 5;
        }


        int ddt = 0;
        if (index == 2){
            ddt = 10;
        }else if(index == 5){
            ddt = 20;
        }else{
            ddt = 45;
        }

        System.out.println(ddt);
        System.out.println(toTime(nextTime));
    }

    private String toTime(int i) {
        return i / 60 + " : " + i % 60;
    }

    @Test
    public void test1(){
        int t = 1325;
        System.out.println(String.format("%02d:%02d", t / 60, t%60));
    }

    private int getTime() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int minute = instance.get(Calendar.MINUTE);
        return hour * 60 + minute;
    }
}
