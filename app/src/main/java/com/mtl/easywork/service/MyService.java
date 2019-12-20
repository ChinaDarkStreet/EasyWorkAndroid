package com.mtl.easywork.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.mtl.easywork.MainActivity;
import com.mtl.easywork.R;
import com.mtl.easywork.common.Constant;
import com.mtl.easywork.common.TimeUtil;
import com.mtl.easywork.recevier.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;

public class MyService extends Service {


    public static final String TAG = "MyService";
    private String msStr = "07:20,08:00,08:10,08:50,09:00,09:45,09:55,10:40,10:50,11:35,12:00,12:40,12:50,13:20,13:30,14:10,14:20,15:00,15:10,15:50,16:00,16:40,16:50,17:30,17:50,18:30,18:40,19:20,19:30,20:10,20:20,21:00,21:20,22:00";
    //private String msStr = "09:00,09:45,09:55,10:40,10:50,11:35,13:30,14:10,14:20,15:00,15:10,15:50,16:00,16:40,16:50,17:30,17:50,18:30,18:40,19:20,19:30,20:10";
    private int[] ms = null;
    private MyBinder mBinder = new MyBinder();
    private boolean running = false;
    private Vibrator vibrator = null;
    private int nextTime = 0;
    private int nextIndex = 0;
    private Intent intent = new Intent(Constant.BORDERCAST_ACTION);
    private int ddt = 0;
    private AlarmManager manager;
    private PendingIntent pi;
    private Notification.Builder builder;
    private Intent mainActivityIntent;
    private MediaPlayer mediaPlayer;

    /**
     * 循环做活动
     */
    public void doWhile() {

        // 获取下个时间点的间隔时间
        int curSecondTime = getTime();
        if (curSecondTime >= nextTime * 60) {
            nextIndex++;
            if (nextIndex > ms.length - 1) {
                notification("结束", "恭喜!");
                running = false;
                mBinder.stop(4);
                sendBroadcast(new Intent(Constant.SERVICE_STATUS));
                return;
            }
            nextTime = ms[nextIndex];
            ddt = ms[nextIndex] - ms[nextIndex - 1];

            notification(toTime(nextTime), ddt + " 分钟");
            intent.putExtra(Constant.NEXT_TIME, toTime(nextTime));
            intent.putExtra(Constant.DT, ddt);
            sendBroadcast(intent);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG", "打印时间: " + new Date().
                            toString());
                    vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }).start();
        }
        int five = (nextTime * 60 - curSecondTime) * 1000;
        Log.i(TAG, "five = " + five);

        manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long triggerAtTime = SystemClock.elapsedRealtime() + five;
        pi = PendingIntent.getBroadcast(this, 0, mainActivityIntent, 0);
        manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() executed");
        //初始化振动器
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mainActivityIntent = new Intent(this, AlarmReceiver.class);

        initMs();
        // 初始化nextTime
        if (initNextTime()){
            intent.putExtra(Constant.NEXT_TIME, TimeUtil.toTime(nextTime));
            intent.putExtra(Constant.DT, ddt);
            sendBroadcast(intent);
        }else {
            intent.putExtra(Constant.NEXT_TIME, "- -:- -");
            intent.putExtra(Constant.DT, -1);
            sendBroadcast(intent);
        }

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() executed");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind() executed");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");

        // 如果运行中, 则doWhile做循环, 否则进行初始化运行
        if (!running) {// 开始运行, 可以初始化一些东西
            running = true;
            initForegroundService();
        }
        doWhile();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }

    //private long[] test = new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};

    public class MyBinder extends Binder {

        public void stop() {
            running = false;
            manager.cancel(pi);
            stopForeground(true);
            Log.d(TAG, "com.mtl.easywork.service.MyService.MyBinder.stop");
        }

        public void stop(final int time) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(time * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    running = false;
                    manager.cancel(pi);
                    stopForeground(true);
                    Log.d(TAG, "com.mtl.easywork.service.MyService.MyBinder.stop");
                }
            }).start();
        }

        /**
         * 判断当前状态是否可以运行
         * @return
         */
        public boolean canRun(){
            return initNextTime();
        }

        public void testNotication() {
            notification(toTime(nextTime), ddt + " 分钟");
        }

        public boolean isRunning() {
            return running;
        }
    }

    private void notification(String title, String content) {
        builder.setContentTitle(title)
                .setContentText(content);
        startForeground(818, builder.build());

    }

    private void initForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //设定的通知渠道名称
            String channelName = getString(R.string.app_name);
            //设置通知的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            //构建通知渠道
            NotificationChannel channel = new NotificationChannel(Constant.CHANNEL_ID, channelName, importance);
            channel.setDescription(Constant.CHANNEL_DESCRIPTION);
            Log.i(TAG, "声音: " + Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm));
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm), null);
            //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            // 在API11之后构建Notification的方式
            builder = new Notification.Builder(this, Constant.CHANNEL_ID);
            Intent intent = new Intent(this, MainActivity.class);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(toTime(nextTime))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(ddt + " 分钟")
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(true);
            Notification notification = builder.build();
            startForeground(818, notification);
        } else {
            // 在API11之后构建Notification的方式
            Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
            Intent intent = new Intent(this, MainActivity.class);
            builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle("标题1")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("内容1")
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(true);
            Notification notification = builder.build();
            notification.defaults = Notification.DEFAULT_SOUND;
            startForeground(818, notification);

        }

    }

    /**
     * 初始化ms
     */
    private void initMs() {
        String[] mss = msStr.split(",");
        ms = new int[mss.length];
        for (int i = 0; i < mss.length; i++) {
            String s = mss[i];
            String[] hm = s.split(":");
            int h = Integer.valueOf(hm[0]);
            int m = Integer.valueOf(hm[1]);
            ms[i] = h * 60 + m;
        }
    }

    /**
     * 初始化nextTime , 设置好nextTime时间
     */
    private boolean initNextTime() {

        int curTime = getTime();
        boolean canRun = false;

        for (int i = 0; i < ms.length; i++) {
            int t = ms[i];
            if (t*60 > curTime) {
                nextTime = t;
                nextIndex = i;
                canRun = true;
                break;
            }
        }
        if (!canRun) {
            return false;
        }
        if (nextIndex != 0) {
            ddt = ms[nextIndex] - ms[nextIndex - 1];
        } else {
            ddt = 0;
        }

        // 发送广播
        intent.putExtra(Constant.NEXT_TIME, toTime(nextTime));
        intent.putExtra(Constant.DT, ddt);
        sendBroadcast(intent);
        return true;
    }

    private String toTime(int t) {
        return String.format("%02d : %02d", t / 60, t % 60);
    }

    /**
     * 获取秒值
     *
     * @return
     */
    private int getTime() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int minute = instance.get(Calendar.MINUTE);
        int second = instance.get(Calendar.SECOND);
        return hour * 60 * 60 + minute * 60 + second;
    }
}
