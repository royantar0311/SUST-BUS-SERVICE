package com.sustbus.driver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.AlarmClock;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sustbus.driver.util.UserInfo;

import org.joda.time.DateTimeUtils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, remoteMessage.getData().toString());
        if (remoteMessage.getNotification() != null || !remoteMessage.getData().get("token").contains(".")) {
            Log.d(TAG, "onMessageReceived: " + "congratulate");
            showNotification(remoteMessage);
        } else sendNotification(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        UserInfo userInfo = UserInfo.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic("broadcast");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
            if (userInfo.isDriver()) FirebaseMessaging.getInstance().subscribeToTopic("driver");
            if (userInfo.isStaff()) FirebaseMessaging.getInstance().subscribeToTopic("staff");
            if (userInfo.isStudent()) FirebaseMessaging.getInstance().subscribeToTopic("student");
            if (userInfo.isTeacher()) FirebaseMessaging.getInstance().subscribeToTopic("teacher");
        }

        FirebaseMessaging.getInstance().subscribeToTopic("broadcast");
        SharedPreferences pref = getSharedPreferences("NOTIFICATIONS", MODE_PRIVATE);
        Set<String> st = pref.getStringSet("tokenSet", new HashSet<>());
        for (String tmp : st) {
            FirebaseMessaging.getInstance().subscribeToTopic(tmp);
        }
    }


    private void showNotification(RemoteMessage remoteMessage) {
        String body,title,channelID,channelName;
        int id;
        /**TODO: this portion will be removed after the development of backend server
         *  getNotification sends the notification to system tray if the app is in background
         *  we must provide data key-value pair not notification in the payload
         *  sending from the firebase console will trigger default notification
         * */
        if(remoteMessage.getNotification()!=null){
            id = 1;
            body = remoteMessage.getNotification().getBody();
            title = remoteMessage.getNotification().getTitle();
            channelID = "broadcast";
            channelName = "Broadcast Channel";
            Log.d(TAG, "showNotification: " + "here");
        }
        else {
            id = 2;
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            channelID = "SUST_Bus_target";
            channelName = "Permission Alert";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channelID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
                .setColor(ContextCompat.getColor(this, R.color.A400red))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(3)
                .setShowWhen(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(channelID, channelName,
                            NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(id, builder.build());
    }


    private void sendNotification(RemoteMessage message) {

        String token = message.getData().get("token");
        SharedPreferences pref = getSharedPreferences("NOTIFICATIONS", MODE_PRIVATE);
        Boolean giveAlarm = pref.getBoolean(token, false);
        long currentTime = DateTimeUtils.currentTimeMillis();
        long lastAlarmTime = pref.getLong("lastAlarmTime", currentTime - 10 * 60000);
        if (giveAlarm && currentTime - lastAlarmTime >= 5 * 60000) {

            Intent alarm = new Intent(AlarmClock.ACTION_SET_ALARM);
            alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            int min = Calendar.getInstance().get(Calendar.MINUTE);
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            min++;
            if (min == 60) {
                min = 0;
                hour++;
            }
            alarm.putExtra(AlarmClock.EXTRA_HOUR, hour);
            alarm.putExtra(AlarmClock.EXTRA_MINUTES, min);
            alarm.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            alarm.putExtra(AlarmClock.EXTRA_MESSAGE, (String) message.getData().get("title") + ", Hurry up!");
            startActivity(alarm);
            SharedPreferences.Editor editor = pref.edit();

            editor.remove("lastAlarmTime");
            editor.putLong("lastAlarmTime", DateTimeUtils.currentTimeMillis());
            editor.commit();

        }

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("markerKey", message.getData().get("markerKey"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "Bus Alert!";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
                        .setContentTitle(message.getData().get("title"))
                        .setContentText(message.getData().get("body"))
                        .setColor(ContextCompat.getColor(this, R.color.A400red))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setWhen(Long.parseLong(message.getData().get("when")))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(2)
                        .setShowWhen(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

}
