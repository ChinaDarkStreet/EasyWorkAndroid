package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.common.Constant;
import com.example.common.TimeUtil;
import com.example.service.MyService;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private Button button = null;
    private Vibrator vibrator = null;
    private TextView text1 = null;
    private TextView text2 = null;
    private MyService.MyBinder myBinder;
    private MsgReceiver myBroadcastReceiver;
    private PowerManager.WakeLock wakeLock;
    private Intent bindIntent;
    private boolean serviceInitButton;
    private LinearLayout main;
    private Random random = new Random();

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
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        main = findViewById(R.id.main);
        button = findViewById(R.id.start_service);
        text1 = findViewById(R.id.word);
        text2 = findViewById(R.id.time);

        // service 初始化按钮标志
        serviceInitButton = false;

        button.setOnClickListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BORDERCAST_ACTION);
        intentFilter.addAction(Constant.BORDERCAST_ACTION1);
        myBroadcastReceiver = new MsgReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);

        bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        unregisterReceiver(myBroadcastReceiver);
        unbindService(connection);
        super.onDestroy();
    }
    
    
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyService.MyBinder) service;
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_service:
                Button button = (Button) view;
                String text = (String) button.getText();
                if (text.equals("开始")){
                    int vTime = Integer.parseInt(((EditText)findViewById(R.id.vTime)).getText().toString());
                    int vTimes = Integer.parseInt(((EditText)findViewById(R.id.vTimes)).getText().toString());
                    boolean isAlert = ((Switch)findViewById(R.id.switch1)).isChecked();

                    PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                    assert pm != null;
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
                    wakeLock.acquire(12 * 60 * 60 * 1000);

                    myBinder.startDownload(vTime, vTimes, isAlert);
                    button.setText("结束");
                }else{
                    myBinder.stop();
                    wakeLock.release();
                    button.setText("开始");
                }
                break;
            default:
                break;
        }
    }

    public class MsgReceiver extends BroadcastReceiver {

        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!serviceInitButton){
                if(myBinder != null){
                    myBinder.initText();
                    button.setText("结束");
                    serviceInitButton = true;
                }
            }

            if (intent.getAction().equals(Constant.BORDERCAST_ACTION)){
                //拿到进度，更新UI
                String nextTime = intent.getStringExtra(Constant.NEXT_TIME);
                int dt = intent.getIntExtra(Constant.DT, 0);
                text1.setText(String.format("阶段时间:\n" +
                        "%d分钟\n" +
                        "下个时间点:\n" +
                        "%s", dt, nextTime));
                Log.d(TAG, String.format("nextTime = %s, dt = %d", nextTime, dt));
            }else {
                String hms = TimeUtil.getHms();
                if (hms.endsWith("00")){
                    main.setPadding(0, random.nextInt(400) + 80, 0, 0);
                }
                text2.setText(hms);
            }
        }

    }
}
