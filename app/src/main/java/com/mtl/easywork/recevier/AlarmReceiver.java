package com.mtl.easywork.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mtl.easywork.service.MyService;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MyService.class);
        context.startService(i);
    }
}