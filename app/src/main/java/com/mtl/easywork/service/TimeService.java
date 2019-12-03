package com.mtl.easywork.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mtl.easywork.common.Constant;

import java.util.Timer;
import java.util.TimerTask;

public class TimeService extends Service {
    public static final String TAG = "TimeService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() executed");

        Timer timer = new Timer();
        TimerTask task;
        task = new TimerTask() {
            @Override
            public void run() {
                sendBroadcast(new Intent(Constant.CUR_TIME_REFLASH_ACTION));
            }
        };
        timer.schedule(task, 0, 1000);

        super.onCreate();

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }
}
