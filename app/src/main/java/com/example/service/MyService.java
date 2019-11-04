package com.example.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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
    private MyBinder mBinder = new MyBinder();
    private boolean running = true;
    private ArrayList<Integer> times = null;
    private ArrayList<Integer> tens = null;
    private Vibrator vibrator = null;
    private int nextTime = 0;
    private int index = 0;
    private Intent intent = new Intent(Constant.BORDERCAST_ACTION);
    private Intent intent1 = new Intent(Constant.BORDERCAST_ACTION1);
    private int ddt = 0;
    private Timer timer = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() executed");
        super.onCreate();

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

        // 初始化 nextTime
        int time = getTime();
        int abTime = time - 450;
        //获取整除后的值
        int dt = abTime % 120;
        int base = time - dt;
        if (dt < dtts[0]) {
            nextTime = base + dtts[0];
            index = 0;
        } else if (dt < dtts[1]) {
            nextTime = base + dtts[1];
            index = 1;
        } else if (dt < dtts[2]) {
            nextTime = base + dtts[2];
            index = 2;
        } else if (dt < dtts[3]) {
            nextTime = base + dtts[3];
            index = 3;
        } else if (dt < dtts[4]) {
            nextTime = base + dtts[4];
            index = 4;
        } else {
            nextTime = base + dtts[5];
            index = 5;
        }

        // 发送广播
        intent.putExtra(Constant.NEXT_TIME, toTime(setDdtGetShowTime(index)));
        intent.putExtra(Constant.DT, ddt);
        sendBroadcast(intent);

    }

    private int setDdtGetShowTime(int index) {
        if (index == 2) {
            ddt = 10;
        } else if (index == 5) {
            ddt = 20;
        } else {
            ddt = 45;
            if (index == 0 || index == 3) {
                return nextTime + 10;
            }
        }
        return nextTime;
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
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        running = false;
        return super.onUnbind(intent);
    }

    private long[] test = new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};

    public class MyBinder extends Binder {
        public void startDownload(int vTime, int vTimes) {
//
//
//            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//            int five = 5000; // 这是5s
//            long triggerAtTime = SystemClock.elapsedRealtime() + five;
//            Intent i = new Intent(this, MainActivity.MsgReceiver.class);
//            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

            timer = new Timer();
            TimerTask task;
            task = new TimerTask() {
                @Override
                public void run() {
                    if (getTime() >= nextTime) {
                        index = ++index % 6;
                        nextTime += dts[index];

                        // 发送广播
                        intent.putExtra(Constant.NEXT_TIME, toTime(setDdtGetShowTime(index)));
                        intent.putExtra(Constant.DT, ddt);
                        sendBroadcast(intent);

                        if (index == 1 || index == 4) {
                            vibrator.vibrate(1000);
                        } else {
                            vibrator.vibrate(test, -1);
                        }
                        Log.d(TAG, String.format("index = %d, nextTime = %s, ddt = %d", index, nextTime, ddt));
                    }
                    sendBroadcast(intent1);
                }
            };
            timer.schedule(task, 2000, 1000);
//            timer.cancel();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (running) {
//
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
            Log.d("TAG", "startDownload() executed");
            // 执行具体的下载任务
        }

        public void startDownload(int vTime, int vTimes, final boolean isAlert) {
            updateTest(vTime, vTimes);
            timer = new Timer();
            TimerTask task;
            task = new TimerTask() {
                @Override
                public void run() {
                    if (getTime() >= nextTime) {
                        index = ++index % 6;
                        nextTime += dts[index];

                        // 发送广播
                        intent.putExtra(Constant.NEXT_TIME, toTime(setDdtGetShowTime(index)));
                        intent.putExtra(Constant.DT, ddt);
                        sendBroadcast(intent);

                        if (index == 1 || index == 4) {
                            if (isAlert) {
                                vibrator.vibrate(1000);
                            }
                        } else {
                            vibrator.vibrate(test, -1);
                        }
                        Log.d(TAG, String.format("index = %d, nextTime = %s, ddt = %d", index, nextTime, ddt));
                    }
                    sendBroadcast(intent1);
                }
            };
            timer.schedule(task, 2000, 1000);
            Log.d("TAG", "startDownload() executed");
        }

        public void stop() {
            timer.cancel();
            running = false;
            stopForeground(true);
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
