package com.sustbus.driver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sustbus.driver.fragments.FinishedFragment;
import com.sustbus.driver.fragments.NextFragment;
import com.sustbus.driver.fragments.OnRoadFragment;
import com.sustbus.driver.util.CallBack;
import com.sustbus.driver.util.RecyclerViewAdapter;
import com.sustbus.driver.util.RouteDatabaseManager;
import com.sustbus.driver.util.RouteInformation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ScheduleActivity extends AppCompatActivity {

    RouteDatabaseManager routeDatabaseManager;
    private int currentMenuItemid;
    private List<RouteInformation> routeInformations,finished,next,onRoad;
    private Map<String,String> onRoadMap;
    private ChildEventListener childEventListener;
    private RecyclerViewAdapter finishedAdapter,nextAdapter,onadapter;
    private boolean initok=false;
    private BottomNavigationView bottomNav;
    private CountDownTimer countDownTimerForRefreshingLists;
    private TextView appBarTextView;
    private ImageButton refreshImagebutton;
    private Fragment currentFragment;
    private boolean forRideShare;
    private ProgressDialog currentProgressDialogue;
    private AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        bottomNav=findViewById(R.id.navigation);
        appBarTextView=findViewById(R.id.appbar_tv);
        refreshImagebutton=findViewById(R.id.appbar_ib);
        forRideShare=getIntent().getBooleanExtra("forRideShare",false);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItemClick(menuItem);
                return true;
            }
        });
        routeDatabaseManager=new RouteDatabaseManager(this);
        if(!routeDatabaseManager.isUpdated()) {
            dialog("Checking for Schedule update");
            routeDatabaseManager.checkForUpdate(new CallBack() {
                @Override
                public void ok() {
                    if(currentProgressDialogue!=null)currentProgressDialogue.dismiss();
                    if (forRideShare) init(R.id.menu_item_on_road);
                    else init(R.id.menu_item_next);
                }

                @Override
                public void notOk() {
                    if(currentProgressDialogue!=null)currentProgressDialogue.dismiss();
                    if (forRideShare){
                        refreshImagebutton.setClickable(false);
                        bottomNav.getMenu().close();
                        Snackbar.make(findViewById(R.id.frame_container), "Schedules needs update, Check Internet and try Again", Snackbar.LENGTH_INDEFINITE).show();
                    }
                    else{
                        Snackbar.make(findViewById(R.id.frame_container), "Schedules were not updated", Snackbar.LENGTH_LONG).show();
                        init(R.id.menu_item_next);
                    }
                }
            }, false);
        }
        else {
            if(forRideShare)init(R.id.menu_item_on_road);
            else init(R.id.menu_item_next);
        }
        refreshImagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRefreshButton();
            }
        });
    }

    public void init(int id){

        if(currentFragment!=null){
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
        }
        if(currentProgressDialogue!=null)currentProgressDialogue.dismiss();
        routeInformations=routeDatabaseManager.getAll();
        if(routeInformations.isEmpty()){
            Snackbar.make(findViewById(R.id.frame_container),"Please Check Your Internet Connection",Snackbar.LENGTH_LONG).show();
            return;
        }
        Collections.sort(routeInformations, new Comparator<RouteInformation>() {
            @Override
            public int compare(RouteInformation o1, RouteInformation o2) {
                return o1.comparableStartTime.compareTo(o2.comparableStartTime);
            }
        });

        onRoadMap=new HashMap<>();
        onRoad=new ArrayList<>();
        next=new ArrayList<>();
        finished=new ArrayList<>();

        next.addAll(routeInformations);
        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               try {

                    onRoadMap.put(dataSnapshot.getKey(),dataSnapshot.child("key").getValue(String.class));
               }
               catch (Exception e){}
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        onRoadMap.remove(dataSnapshot.getKey());
                    }catch (Exception e){}
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child("busesOnRoad").addChildEventListener(childEventListener);
        initok=true;
        finishedAdapter=new RecyclerViewAdapter(this,finished,null,RecyclerViewAdapter.FINISHED);
        nextAdapter=new RecyclerViewAdapter(this,next,null,RecyclerViewAdapter.NEXT);
        onadapter=new RecyclerViewAdapter(this, onRoad, new RecyclerViewAdapter.ClickEvent() {
            @Override
            public void click(int position, int from) {
                     onRoadClickEvent(position);
            }
        }, RecyclerViewAdapter.ON_ROAD);

        refresh();

        refreshImagebutton.setClickable(true);
        bottomNav.setSelectedItemId(id);
        countDownTimerForRefreshingLists=new CountDownTimer(10000000000l,2*1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                    refresh();
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimerForRefreshingLists.start();
    }

    public void menuItemClick(MenuItem menuItem){

        if(!initok)return;

        int id=menuItem.getItemId();

        if(id==currentMenuItemid)return;
        currentMenuItemid=id;

        if(id==R.id.menu_item_on_road){
            OnRoadFragment fragment=new OnRoadFragment();
            fragment.mAdapter=onadapter;
            fragment.mLayoutManager=new LinearLayoutManager(this);
            appBarTextView.setText("Buses on move");
            loadFragment(fragment);

        }
        else if(id==R.id.menu_item_next){
            NextFragment fragment=new NextFragment();
            fragment.mAdapter=nextAdapter;
            fragment.mLayoutManager=new LinearLayoutManager(this);
            appBarTextView.setText("Next buses to go");
            loadFragment(fragment);
        }
        else {
            FinishedFragment fragment=new FinishedFragment();
            fragment.mLayoutManager=new LinearLayoutManager(this);
            fragment.mAdapter=finishedAdapter;
            appBarTextView.setText("Completed Schedules");
            loadFragment(fragment);
        }

    }

    public void handleRefreshButton(){


        refreshImagebutton.setClickable(false);
        refreshImagebutton.animate().rotation(360).setDuration(100*700).setInterpolator(new CycleInterpolator(20)).start();
        initok=false;
        currentMenuItemid=0;
        if(finished!=null)finished.clear();
        if(onRoad!=null)onRoad.clear();
        if(next!=null)next.clear();

        if(countDownTimerForRefreshingLists!=null)countDownTimerForRefreshingLists.cancel();
        FirebaseDatabase.getInstance().getReference().child("busesOnRoad").removeEventListener(childEventListener);
        routeDatabaseManager.checkForUpdate(new CallBack() {
            @Override
            public void ok() {
                init(bottomNav.getSelectedItemId());
                refreshImagebutton.animate().cancel();
                refreshImagebutton.clearAnimation();
        }

            @Override
            public void notOk() {
                Snackbar.make(findViewById(R.id.frame_container),"Check Connection and Try Again",4000).show();
                init(bottomNav.getSelectedItemId());
                refreshImagebutton.animate().cancel();
                refreshImagebutton.clearAnimation();
            }
        },true);

    }

    public void refresh(){

        String  nowTime=getTime();
        for (int i = 0; i <onRoad.size() ; i++) {
               RouteInformation tmp=onRoad.get(i);
               if(tmp.comparableEndTime.compareTo(nowTime)<=0){
                   finished.add(0,tmp);
                   onRoad.remove(i);
                   i--;
               }
        }

      for(int i=0;i<next.size();i++){
          RouteInformation tmp=next.get(i);
         // Log.d("DEBMES",tmp.comparableStartTime+" "+tmp.comparableEndTime+" "+nowTime);
          if(tmp.comparableEndTime.compareTo(nowTime)<=0){
              finished.add(0,tmp);
              next.remove(i);
              i--;
          }
          else if(tmp.comparableStartTime.compareTo(nowTime)<=0){
              onRoad.add(0,tmp);
              next.remove(i);
              i--;
          }
      }

        for (RouteInformation tmp:onRoad) {
            if(onRoadMap.containsKey(tmp.getRouteId())){
                tmp.setMarkerId(onRoadMap.get(tmp.getRouteId()));
            }
            else tmp.setMarkerId(null);
        }


      nextAdapter.notifyDataSetChanged();
      finishedAdapter.notifyDataSetChanged();
      onadapter.notifyDataSetChanged();
    }

    public void onRoadClickEvent(int position){

        if(position>=onRoad.size()){
            Snackbar.make(findViewById(R.id.frame_container),"Something Went Wrong",Snackbar.LENGTH_LONG).show();
            return;
        }
        RouteInformation tmp = onRoad.get(position);


        if(forRideShare) {

             if (tmp.getMarkerId() != null) {
                 Snackbar.make(findViewById(R.id.frame_container), "Please Select the Available Schedules.(Red ones)", 4000).show();
                 return;
             }

             dialog("Checking online for Availability...");
             FirebaseDatabase.getInstance().getReference().child("busesOnRoad").addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     if (!dataSnapshot.hasChild(tmp.getRouteId())) {
                         giveResult(position);
                     } else {
                         Snackbar.make(findViewById(R.id.frame_container), "This Schedule is Already Running", 2000).show();
                     }

                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                     Snackbar.make(findViewById(R.id.frame_container), "Please Check your Internet Connection", 2000).show();
                 }
             });
         }
        else if(tmp.getMarkerId()!=null){

            AlertDialog.Builder builder=new AlertDialog.Builder(this);

           alertDialog= builder.setTitle("Confirmation")
                    .setMessage("Show the bus on map?")
                    .setPositiveButton("show", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(ScheduleActivity.this,MapsActivity.class)
                                    .putExtra("fromSchedule",true)
                                     .putExtra("markerToShow",tmp.getMarkerId()));

                        }
                    })
                    .setNegativeButton("cancel",null)
                     .create();

           alertDialog.show();

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentProgressDialogue!=null) {
            currentProgressDialogue.dismiss();
        }
        if(countDownTimerForRefreshingLists!=null)countDownTimerForRefreshingLists.cancel();
        if(childEventListener!=null)FirebaseDatabase.getInstance().getReference().child("busesOnRoad").removeEventListener(childEventListener);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.disallowAddToBackStack();
        transaction.commit();
        currentFragment=fragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private String getTime(){
        int hour= Calendar.getInstance().get(Calendar.HOUR);

        int min=Calendar.getInstance().get(Calendar.MINUTE);
        String ampm=Calendar.getInstance().get(Calendar.AM_PM)==Calendar.AM?"am":"pm";

        if(ampm.equals("pm")){
            hour+=12;
        }
        String nowTime=(hour<10?"0":"")+hour+":"+(min<10?"0":"")+min;
        return nowTime;
    }

    public void giveResult(int position){

        RouteInformation tmp=onRoad.get(position);

        Intent intent=new Intent();

        intent.putExtra("path",tmp.getPath());
        intent.putExtra("title",tmp.getTitle());
        intent.putExtra("routeId",tmp.getRouteId());

        setResult(100,intent);
        finish();
    }

    private void dialog(String msg){
          if(currentProgressDialogue!=null){
              currentProgressDialogue.dismiss();
          }
          else currentProgressDialogue=new ProgressDialog(this);

          currentProgressDialogue.setMessage(msg);
          currentProgressDialogue.show();

    }
}
