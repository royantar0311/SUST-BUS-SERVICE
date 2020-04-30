package com.sustbus.driver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sustbus.driver.fragments.RegisterNotification;
import com.sustbus.driver.fragments.ShowNotificationsFragment;
import com.sustbus.driver.util.CallBack;
import com.sustbus.driver.util.RecyclerViewAdapter2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;


public class NotificationSettings extends AppCompatActivity {

    public TextView appBartv;
    public FloatingActionButton fab;
    List<String> tokenList;
    RecyclerViewAdapter2 recyclerViewAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        appBartv=findViewById(R.id.notification_setting_appbar_tv);
        fab=findViewById(R.id.notification_setting_fab);
        tokenList=new ArrayList<>();

        init();

    }

    public void init(){
        SharedPreferences sharedPreferences=getSharedPreferences("NOTIFICATIONS", Context.MODE_PRIVATE);
        Set<String> keySet=sharedPreferences.getStringSet("tokenSet",new HashSet<>());

        for(String s:keySet) {
            tokenList.add(s);
            Log.d("DEBMES", s);
        }
        recyclerViewAdapter2=new RecyclerViewAdapter2(this,tokenList);

        getSupportFragmentManager().beginTransaction().replace(R.id.notification_fragment_container,
                new ShowNotificationsFragment(new LinearLayoutManager(this),recyclerViewAdapter2))
                .commit();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.INVISIBLE);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.notification_fragment_container, new RegisterNotification(new CallBack() {
                    @Override
                    public void ok() {
                        reCheck();
                    }

                    @Override
                    public void notOk() {

                    }
                }));
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    public void reCheck(){
        SharedPreferences sharedPreferences=getSharedPreferences("NOTIFICATIONS", Context.MODE_PRIVATE);
        Set<String> keySet=sharedPreferences.getStringSet("tokenSet",new HashSet<>());

        for(String s:keySet) {
            if(!tokenList.contains(s))tokenList.add(s);
        }
        recyclerViewAdapter2.notifyDataSetChanged();
    }
}


