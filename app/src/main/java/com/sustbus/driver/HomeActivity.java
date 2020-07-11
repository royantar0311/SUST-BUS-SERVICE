/*
 * Copyright (C) 2019-2020 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
package com.sustbus.driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sustbus.driver.adminPanel.AdminPanelActivity;
import com.sustbus.driver.adminPanel.RouteManager;
import com.sustbus.driver.util.CallBack;
import com.sustbus.driver.util.DbListener;
import com.sustbus.driver.util.MapUtil;
import com.sustbus.driver.util.PermissionsRequestor;
import com.sustbus.driver.util.ResultListener;
import com.sustbus.driver.util.UserInfo;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, FirebaseAuth.AuthStateListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "HomeActivity";
    private static final int LOCATION_PERMISSION_CODE = 1;
    UserInfo userInfo = null;

    private TextView userNameTv;
    private TextView driverOrStudent;
    private CardView shareRideTv;
    private CardView adminPanelCv;
    private ImageView dpEv;
    private FirebaseAuth mAuth;
    private ImageView rideShareIndicatorIV;
    private boolean isRideShareOn = false;
    private LocationManager locationManager;
    private String userUid;
    private MapUtil mapUtil;
    private PermissionsRequestor permissionsRequestor;
    private CardView routeUploaderCv;
    private String SERVER_KEY = "hello";
    private LocationUploaderService locationUploaderService;
    private SharedPreferences observer;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUploaderService.LocalBinder binder = (LocationUploaderService.LocalBinder) service;
            locationUploaderService = binder.getSerVice();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("DEB","disconnected");
            locationUploaderService = null;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        rideShareIndicatorIV = findViewById(R.id.ride_share_iv);

        CardView openMapBtn = findViewById(R.id.track_buses_cv);
        adminPanelCv = findViewById(R.id.admin_panel_cv);
        routeUploaderCv = findViewById(R.id.route_uploader);
        shareRideTv = findViewById(R.id.ride_on_cv);
        CardView scheduleBtn = findViewById(R.id.bus_schedule_cv);
        userNameTv = findViewById(R.id.row_item_user_name_tv);
        driverOrStudent = findViewById(R.id.driver_or_student_tv);
        CardView profileCv = findViewById(R.id.profile_cv);
        CardView signOut = findViewById(R.id.logout_cv);
        dpEv = findViewById(R.id.home_user_image_ev);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        findViewById(R.id.home_notifications_tv).setOnClickListener(this);
        openMapBtn.setOnClickListener(this);
        scheduleBtn.setOnClickListener(this);
        shareRideTv.setOnClickListener(this);
        profileCv.setOnClickListener(this);
        signOut.setOnClickListener(this);
        routeUploaderCv.setOnClickListener(this);
        FirebaseAuth.getInstance().addAuthStateListener(this);
        adminPanelCv.setOnClickListener(this);

        userInfo = UserInfo.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mapUtil = MapUtil.getInstance();
        observer=getSharedPreferences("observer",MODE_PRIVATE);
    }

    private void loadImage() {
        Log.d(TAG, "loadImage: called");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String img = userInfo.getUrl();
                    byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dpEv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                        }
                    });
                } catch (Exception ignored) {
                }
            }
        });
        thread.start();
    }

    private void initDatabase() {
        /**
         * Database initialization
         * */
        Log.d(TAG, "updateDatabase: called");
        FirebaseFirestore.getInstance().collection("key").document("key").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    SERVER_KEY = task.getResult().getString("SERVER");
                }
            }
        });
    }

    private void dashboardSetup() {
        Log.d(TAG, "dashboardSetup: called");
        /**
         * setting up dashboard for user (driver/student)
         * */
        userNameTv.setText(userInfo.getUserName());

        if(userInfo.isTeacher()) {
            driverOrStudent.setText("Teacher");
        } else if(userInfo.isStaff()) {
            driverOrStudent.setText("Staff");
        } else if(userInfo.isStudent()) {
            driverOrStudent.setText("Student");
        }else if(userInfo.isDriver()) {
            driverOrStudent.setText("Driver");
        }



        if (getIntent().getStringExtra("markerKey") != null) {

            Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
            intent.putExtra("fromSchedule", true);
            intent.putExtra("markerToShow", getIntent().getStringExtra("markerKey"));
            startActivity(intent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        permissionsRequestor = new PermissionsRequestor(this);
        permissionsRequestor.request(new ResultListener() {
            @Override
            public void permissionsGranted() {
            }

            @Override
            public void permissionsDenied() {
                Snackbar.make(findViewById(R.id.home_scrollview), "Please grant all Permissions", Snackbar.LENGTH_LONG).show();
            }
        });
        if(observer.getBoolean("running",false)){
            Log.d("DEB","observer");
            rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.end_ride));
            isRideShareOn=true;
            mapUtil.rideShareStatus=true;
            bindService(new Intent(HomeActivity.this,LocationUploaderService.class),mServiceConnection,BIND_AUTO_CREATE);
            observer.registerOnSharedPreferenceChangeListener(this);
        }
        else{
            rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.start_ride));
            isRideShareOn=false;
            mapUtil.rideShareStatus=false;
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");

        if(isRideShareOn){
            unbindService(mServiceConnection);
        }

        observer.unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.d(TAG, "onAuthStateChanged: fired");
        if (firebaseAuth.getCurrentUser() == null) {
            Log.d(TAG, "onAuthStateChanged: " + "mAuth gets null");
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {

            userUid = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "onAuthStateChanged: " + userUid);
            FirebaseMessaging.getInstance().subscribeToTopic(userUid);
            SharedPreferences.Editor ed=getSharedPreferences("userInfo",MODE_PRIVATE).edit();
            ed.putString("uId",userUid);
            ed.commit();
            userInfo.setuId(userUid);
            DbListener dbListener = new DbListener(new CallBack(){
                @Override
                public void ok() {
                    userInfo = UserInfo.getInstance();
                    userInfo.setuId(userUid);
                    Log.d("DEB",userInfo.toString());
                    dashboardSetup();
                    loadImage();
                    FirebaseFirestore.getInstance().collection("admin")
                            .document(firebaseAuth.getCurrentUser().getUid())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult().exists() && task.getResult().getBoolean("active")) {
                                adminPanelCv.setVisibility(View.VISIBLE);
                                routeUploaderCv.setVisibility(View.VISIBLE);
                                userInfo.setAdmin(true);
                                driverOrStudent.append(" (admin)");
                                Log.d("admin", "ok: "+userInfo.toString());

                            } else {
                                Log.d("admin", "notok: "+userInfo.toString());
                                routeUploaderCv.setVisibility(View.GONE);
                                adminPanelCv.setVisibility(View.GONE);
                            }
                        }
                    });
                }

                @Override
                public void notOk() {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            initDatabase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }


    public void initializePath() {
        startActivityForResult(new Intent(this, ScheduleActivity.class).putExtra("forRideShare", true), 100);
    }

    public Activity getActivity() {
        return this;
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.ride_on_cv) {
                if (!isRideShareOn) {
                    if (!userInfo.isDriver()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Warning!")
                                .setMessage("Since you are not a Driver, if you misuse this feature legal actions will be taken against you")
                                .setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        gotoSchedule();
                                    }
                                })
                                .setNegativeButton("cancel", null)
                                .show();
                    }
                    else gotoSchedule();

                }
                else {
                    Log.d("DEB","stopped");
                    locationUploaderService.forcestop();

                    //turnOffRideShare(false);
                }

        }
        else if (i == R.id.logout_cv) {
            userInfo.reset();
            SharedPreferences.Editor ed=getSharedPreferences("settings",MODE_PRIVATE).edit();
            ed.clear();
            ed.commit();

            ed = getSharedPreferences("userInfo",MODE_PRIVATE).edit();
            FirebaseMessaging.getInstance().unsubscribeFromTopic(userUid);
            ed.clear();
            ed.commit();

            SharedPreferences pref = getSharedPreferences("NOTIFICATIONS", MODE_PRIVATE);
            Set<String> st = pref.getStringSet("tokenSet", new HashSet<>());
            for (String tmp : st) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(tmp);
            }

            ed=pref.edit();
            ed.clear();
            ed.commit();

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            finish();

        }
        else if (i == R.id.track_buses_cv) {
            startActivity(new Intent(HomeActivity.this, MapsActivity.class));
        }
        else if (i == R.id.profile_cv && userInfo != null) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        }
        else if (i == R.id.bus_schedule_cv) {
            startActivity(new Intent(HomeActivity.this, ScheduleActivity.class));
        }
        else if (i == R.id.home_notifications_tv) {
            startActivity(new Intent(HomeActivity.this, NotificationSettings.class));
        }
        else if (i == R.id.route_uploader) {
            startActivity(new Intent(HomeActivity.this, RouteManager.class));
        }
        else if(i==R.id.admin_panel_cv){
            Intent intent = new Intent(HomeActivity.this, AdminPanelActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void gotoSchedule() {
        boolean isGps = false;
        try {
            isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        if (!isGps) {
            mapUtil.enableGPS(getApplicationContext(), this, 101);
        } else initializePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) initializePath();
        }
        else if (requestCode == 100 && data != null) {

            String path = data.getStringExtra("path");
            String routeId = data.getStringExtra("routeId");
            String title = data.getStringExtra("title");
            if (path == null || routeId == null || title == null) return;


            getSharedPreferences("observer",MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
            Intent intent=new Intent(HomeActivity.this,LocationUploaderService.class);
            bindService(new Intent(HomeActivity.this,LocationUploaderService.class),mServiceConnection, IntentService.BIND_AUTO_CREATE);

            intent.putExtra("path",path);
            intent.putExtra("routeId",routeId);
            intent.putExtra("title",title);
            intent.putExtra("userUid",userUid);
            intent.putExtra("SERVER_KEY",SERVER_KEY);
            intent.putExtra("for",data.getStringExtra("for"));
            //Start service:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Log.d("DEB","Started");

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestor.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        //if (listener != null) listener.remove();
        locationManager = null;
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("DEB","preference changed: "+key);
        Log.d("DEB",""+sharedPreferences.getBoolean(key,false));
        if(sharedPreferences.getBoolean(key,false)){
            MapUtil.rideShareStatus = true;
            isRideShareOn=true;
            rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.end_ride));
        }
        else {
            MapUtil.rideShareStatus = false;
            isRideShareOn=false;
            rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.start_ride));
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            unbindService(mServiceConnection);
            stopService(new Intent(this,LocationUploaderService.class));

        }
    }
}
