package com.sustbus.driver;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class ScheduleActivity extends AppCompatActivity {
   RouteDatabaseManager routeDatabaseManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shcedule);

        routeDatabaseManager=new RouteDatabaseManager();
        routeDatabaseManager.checkForUpdate(this, new OnUpdateCheckFinish() {
            @Override
            public void ok() {
                Log.d("DEB","finish");
                show();
            }

            @Override
            public void notOk() {
               Log.d("DEB","notFinish");
            }
        });
    }

    public void show(){
        List<RouteInformation> lst=routeDatabaseManager.getAll();

        for (RouteInformation ri:lst){
            Log.d("DEB",ri.getPath()+ri.getRouteId()+ri.getTitle()+"");
        }
    }
}
