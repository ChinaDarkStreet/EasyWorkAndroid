package com.mtl.easywork;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mtl.easywork.common.Constant;
import com.mtl.easywork.common.TimeUtil;
import com.mtl.easywork.service.MyService;
import com.mtl.easywork.service.TimeService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Intent bindIntent;
    private MsgReceiver myBroadcastReceiver;
    private TextView curTimeTV;
    private TextView nextTimeTV;
    private TextView dtTV;
    private Intent timeService;
    private Switch theSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定按键
        theSwitch = findViewById(R.id.theSwitch);
        theSwitch.setOnClickListener(this);
        Button testB = findViewById(R.id.testB);
        testB.setOnClickListener(this);

        curTimeTV = findViewById(R.id.curTimeTV);
        nextTimeTV = findViewById(R.id.nextTimeTV);
        dtTV = findViewById(R.id.dtTV);

        // 注册接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BORDERCAST_ACTION);
        intentFilter.addAction(Constant.CUR_TIME_REFLASH_ACTION);
        intentFilter.addAction(Constant.SERVICE_STATUS);
        myBroadcastReceiver = new MsgReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);

        //绑定service
        bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);

        // 启动时间模块
        timeService = new Intent(this, TimeService.class);
        startService(timeService);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.theSwitch:
                Switch sw = (Switch) view;
                if (sw.isChecked()){

                    //myBinder.start();
                    if(myBinder.canRun()){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            startForegroundService(bindIntent);
                        }
                        else{
                            startService(bindIntent);
                        }
                        sw.setChecked(true);
                        sw.setText("运行中");
                    }else {
                        sw.setChecked(false);
                        Toast.makeText(this,"没有下一个时间点",Toast.LENGTH_LONG).show();
                    }
                }else{
                    myBinder.stop();
                    sw.setChecked(false);
                    sw.setText("未运行");
                }
                break;
            case R.id.testB:
                MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
                mediaPlayer.start();
                break;
            default:
                break;
        }
    }

    public class MsgReceiver extends BroadcastReceiver {

        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Constant.BORDERCAST_ACTION)) {
                String nextTime = intent.getStringExtra(Constant.NEXT_TIME);
                int dt = intent.getIntExtra(Constant.DT, 0);
                Log.d(TAG, String.format("nextTime = %s, dt = %d", nextTime, dt));
                nextTimeTV.setText(nextTime);
                dtTV.setText(String.format("%d 分钟", dt));
            } else if (intent.getAction().equals(Constant.SERVICE_STATUS)){
                checkStatus();
            } else{
                curTimeTV.setText(TimeUtil.getHms());
            }
        }

    }

    private MyService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyService.MyBinder) service;
            checkStatus();
        }
    };

    /**
     * 更新按钮状态
     */
    private void checkStatus() {

        if (myBinder == null || !myBinder.isRunning()){
            theSwitch.setText("未运行");
            theSwitch.setChecked(false);
        }else {
            theSwitch.setText("运行中");
            theSwitch.setChecked(true);
        };
    }


    private static final String TAG = "MainActivity";
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart()");

        checkStatus();

        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        // 解绑接收器
        unregisterReceiver(myBroadcastReceiver);
        // 停止服务
        stopService(timeService);
        // unbindService
        unbindService(connection);

        super.onDestroy();
    }
}
