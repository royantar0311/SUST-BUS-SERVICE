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

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //Log.d("DEBMES",remoteMessage.getData().toString());
        sendNotification(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        //Log.d("DEBMES","New Token");
        SharedPreferences pref = getSharedPreferences("NOTIFICATIONS", MODE_PRIVATE);
        Set<String> st = pref.getStringSet("tokenSet", new HashSet<>());
        for (String tmp : st) {
            FirebaseMessaging.getInstance().subscribeToTopic(tmp);
        }
    }

    private void sendNotification(RemoteMessage message) {

        String token = message.getData().get("token");

        Boolean giveAlarm = getSharedPreferences("NOTIFICATIONS", MODE_PRIVATE).getBoolean(token, false);

        if (giveAlarm) {

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

        }

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("markerKey", message.getData().get("markerKey"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "sust";
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
