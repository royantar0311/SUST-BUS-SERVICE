package com.sustbus.driver;

import android.os.Bundle;

import com.sustbus.driver.util.NotificationSender;

import androidx.appcompat.app.AppCompatActivity;


public class NotificationSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        NotificationSender n=new NotificationSender(this,"sdfssdf");
        n.send("test","testing");
    }
}
