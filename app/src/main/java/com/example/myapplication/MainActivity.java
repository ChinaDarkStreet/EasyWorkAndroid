package com.example.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.common.Constant;
import com.example.common.TimeUtil;
import com.example.service.MyRun;
import com.example.service.MyService;

import java.sql.Time;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private Handler handler = null;
    private MyRun runnable = null;
    private Button button = null;
    private Vibrator vibrator = null;
    private TextView text1 = null;
    private TextView text2 = null;
    private MyService.MyBinder myBinder;
    private MsgReceiver myBroadcastReceiver;
    private PowerManager.WakeLock wakeLock;
    private Intent bindIntent;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.start_service);
        text1 = findViewById(R.id.word);
        text2 = findViewById(R.id.time);

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
        unregisterReceiver(myBroadcastReceiver);
        unbindService(connection);
        handler.removeCallbacks(runnable);
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

        @Override
        public void onReceive(Context context, Intent intent) {
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
                text2.setText(TimeUtil.getHMS());
            }
        }

    }
}
