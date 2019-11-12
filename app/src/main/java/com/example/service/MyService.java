package com.example.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.example.common.Constant;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {


    public static final String TAG = "MyService";
    private int[] dts = new int[]{35, 10, 10, 35, 10, 20};
    private int[] dtts = new int[]{35, 45, 55, 90, 100, 120};
    private String msStr = "08:00,08:50,09:00,09:45,09:55,10:40,10:50,11:35,13:30,14:10,14:20,15:00,15:10,15:50,16:00,16:40,16:50,17:30,17:50,18:30,18:40,19:20,19:30,20:10,20:20,21:00";
    private int[] ms = null;
    private MyBinder mBinder = new MyBinder();
    private boolean running = true;
    private ArrayList<Integer> times = null;
    private ArrayList<Integer> tens = null;
    private Vibrator vibrator = null;
    private int nextTime = 0;
    private int nextIndex = 0;
    private int index = 0;
    private Intent intent = new Intent(Constant.BORDERCAST_ACTION);
    private Intent intent1 = new Intent(Constant.BORDERCAST_ACTION1);
    private int ddt = 0;
    private Timer timer = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() executed");
        super.onCreate();

        String[] mss = msStr.split(",");
        ms = new int[mss.length];
        for (int i = 0; i < mss.length; i++) {
            String s = mss[i];
            String[] hm = s.split(":");
            int h = Integer.valueOf(hm[0]);
            int m = Integer.valueOf(hm[1]);
            ms[i] = h*60 + m;
        }

        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        //适配8.0service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(TAG, "诺秒贷", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), TAG).build();
            startForeground(1, notification);
            startForegroundService(new Intent(this, MyService.class));
        } else {
            Notification.Builder builder = new Notification.Builder
                    (this.getApplicationContext()); //获取一个Notification构造器
            Intent nfIntent = new Intent(this, MainActivity.class);

            builder.setContentIntent(PendingIntent.
                    getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                    .setContentTitle("标题") // 设置下拉列表里的标题
                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                    .setContentText("内容") // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

            Notification notification = builder.build(); // 获取构建好的Notification
            notification.defaults = Notification.DEFAULT_SOUND;
            startForeground(818, notification);
        }
        initNextTime();

    }

    /**
     * 初始化nextTime , 设置好nextTime时间
     */
    private void initNextTime() {

        int curTime = getTime();
        for (int i = 0; i < ms.length; i++) {
            int t = ms[i];
            if (t > curTime) {
                nextTime = t;
                nextIndex = i;
                break;
            }
        }
        if (nextIndex != 0) {
            ddt = ms[nextIndex] - ms[nextIndex - 1];
        }else {
            ddt = 0;
        }

        // 发送广播
        intent.putExtra(Constant.NEXT_TIME, toTime(nextTime));
        intent.putExtra(Constant.DT, ddt);
        sendBroadcast(intent);
    }

    private String toTime(int t) {
        return String.format("%02d : %02d", t / 60, t % 60);
    }

    private int getTime() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int minute = instance.get(Calendar.MINUTE);
        return hour * 60 + minute;
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

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() executed");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        running = false;
        return super.onUnbind(intent);
    }

    private long[] test = new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};

    public class MyBinder extends Binder {
        private PowerManager.WakeLock wakeLock = null;

        public void startDownload(int vTime, int vTimes, final boolean isAlert) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            assert pm != null;

            //保持唤醒
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire(12 * 60 * 60 * 1000);

            running = true;
            initNextTime();
            updateTest(vTime, vTimes);
            timer = new Timer();
            TimerTask task;
            task = new TimerTask() {
                @Override
                public void run() {
                    if (getTime() >= nextTime) {
                        nextIndex++;
                        if (nextIndex >= ms.length){
                            stop();
                            return;
                        }
                        nextTime = ms[nextIndex];
                        ddt = ms[nextIndex] - ms[nextIndex-1];

                        // 发送广播
                        intent.putExtra(Constant.NEXT_TIME, toTime(nextTime));
                        intent.putExtra(Constant.DT, ddt);
                        sendBroadcast(intent);
                        if (nextIndex % 2 != 0){
                            vibrator.vibrate(test, -1);
                        }else {
                            vibrator.vibrate(1000);
                        }
                        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone rt = RingtoneManager.getRingtone(getApplicationContext(), uri);
                        rt.play();
                        Log.d(TAG, String.format("index = %d, nextTime = %s, ddt = %d", nextIndex, nextTime, ddt));
                    }
                    sendBroadcast(intent1);
                }
            };
            timer.schedule(task, 2000, 1000);
            Log.d("TAG", "startDownload() executed");
        }

        public void stop() {
            if (wakeLock != null){
                wakeLock.release();
            }
            running = false;
            stopForeground(true);
            timer.cancel();
        }

        public boolean isRunning(){
            return running;
        }

        public void initText() {
            initNextTime();
        }
    }

    private void updateTest(int vTime, int vTimes) {
        long[] ints = new long[vTimes * 2];
        for (int i = 0; i < vTimes; i++) {
            ints[2 * i] = 1000;
            ints[2 * i + 1] = vTime;
        }
        test = ints;
    }
}
