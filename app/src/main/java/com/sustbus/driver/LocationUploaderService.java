package com.sustbus.driver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class LocationUploaderService extends Service {

    private IBinder binderToReturn=new LocalBinder();
    private SharedPreferences.Editor edit;
    private boolean isBinded=false;
    private Uploader mUploader;
    private final String notificationChannel="1234";

    public void out(String s){
        Log.d("DEB",s);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBinded=true;
        out("Binded");
        return binderToReturn;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        out("unbinded");
        isBinded=false;
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent) {
        isBinded=true;
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(notificationChannel, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }
    @Override
    public void onDestroy() {

        out("service Dstroy");
        stopForeground(true);
        super.onDestroy();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        edit=getSharedPreferences("observer",MODE_PRIVATE).edit();

        edit.putBoolean("running",true);
        edit.commit();

        out("OnstartCommand");
        mUploader=new Uploader();
        out("Run Started");
        mUploader.start();
        out("After run");
        startForeground(1,getNotification());

        return START_NOT_STICKY;
    }




    public void forcestop(){
        if(mUploader!=null && mUploader.isAlive() )mUploader.turnOff();
    }
    private void stop(){
        out("InStop()");


        edit.putBoolean("running", false);
        edit.commit();

        if(!isBinded) {

            out("here");
            stopForeground(true);
            stopSelf();
        }
    }


    public class LocalBinder extends Binder {
         LocationUploaderService getSerVice(){return LocationUploaderService.this;}

    }
    private Notification getNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText("Ride share is on")
                //.setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setChannelId(notificationChannel);
        }

        return builder.build();
    }
    private class Uploader extends Thread{
       private volatile boolean stop=false;
       private boolean taskEnded=false;
        @Override
        public void run() {
            super.run();
            int i=20;
            while(!taskEnded && !stop){

                try {
                    this.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("DEB","i is "+i);

                i--;
                if(i==0)taskEnded=true;

            }


            LocationUploaderService.this.stop();
        }

        public  void turnOff(){
            stop=true;
        }

    }
}
