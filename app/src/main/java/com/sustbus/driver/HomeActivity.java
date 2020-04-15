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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.*;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Waypoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sustbus.driver.MapsActivity.MIN_DIST;
import static com.sustbus.driver.MapsActivity.MIN_TIME;
import static com.sustbus.driver.UserInfo.*;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private static final int LOCATION_PERMISSION_CODE = 1;
    private TextView userNameTv;
    private TextView driverOrStudent;
    private CardView openMapBtn;
    private CardView scheduleBtn;
    private CardView shareRideTv;
    private DatabaseReference databaseReference,userDatabaseReference,userLocationData,userPathReference;
    private FirebaseAuth mAuth;
    private ImageView rideShareIndicatorIV;
    private boolean isRideShareOn=false,quit=false;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String userUid;
    private MapUtil mapUtil;
    private PermissionsRequestor permissionsRequestor;
    private List<String> pathString;
    private boolean pathOk;
    private int determineCallCount;
    private GeoCoordinates previousPosition,currentPosition;
    
    UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        rideShareIndicatorIV=findViewById(R.id.ride_share_iv);

        openMapBtn = findViewById(R.id.track_buses_cv);
        shareRideTv = findViewById(R.id.ride_on_cv);
        scheduleBtn = findViewById(R.id.bus_schedule_cv);
        userNameTv =  findViewById(R.id.user_name_tv);
        driverOrStudent = findViewById(R.id.driver_or_student_tv);

        openMapBtn.setOnClickListener(this);
        scheduleBtn.setOnClickListener(this);
        shareRideTv.setOnClickListener(this);

        shareRideTv.setEnabled(false);

        databaseReference= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();


        if(mAuth.getCurrentUser()==null){
            Intent intent=new Intent(HomeActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else{
            userUid=mAuth.getCurrentUser().getUid();
            userDatabaseReference=databaseReference.child("users").child(userUid);

            userLocationData=databaseReference.child("alive").child(userUid);
            userPathReference=databaseReference.child("destinations").child(userUid).child("path");
            userDatabaseReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userInfo = UserInfo.getBuilder()
                            .setUserName(dataSnapshot.child("userName").getValue(String.class))
                            .setIsStudentPermitted(dataSnapshot.child("isStudentPermitted").getValue(Boolean.class))
                            .setEmail(dataSnapshot.child("email").getValue(String.class))
                            .setDriver(dataSnapshot.child("isDriver").getValue(Boolean.class))
                            .build();

                    userNameTv.setText(userInfo.getUserName());
                    if(userInfo.isDriver){
                        driverOrStudent.setText("Driver");
                        shareRideTv.setEnabled(true);
                    }
                    else {
                        driverOrStudent.setText("Student");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        mapUtil=MapUtil.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionsRequestor=new PermissionsRequestor(this);
        permissionsRequestor.request(new PermissionsRequestor.ResultListener() {
            @Override
            public void permissionsGranted() {

            }

            @Override
            public void permissionsDenied() {
                Snackbar.make(findViewById(R.id.home_scrollview),"Please grant all Permissions",Snackbar.LENGTH_LONG).show();
            }
        });

    }

    public void turnOnRideShare(){


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGps=false;
        try {
            isGps=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception e){

        }

        if(!isGps){
            mapUtil.enableGPS(getApplicationContext(),this,101);
            locationManager=null;
            return;
        }




        isRideShareOn=true;
        rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.end_ride));

        initializePath();

        userLocationData.child("title").setValue("Campus-Tilagor-Campus");

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                userLocationData.child("lat").setValue(location.getLatitude());
                userLocationData.child("lng").setValue(location.getLongitude());
                handlePath(new GeoCoordinates(location.getLatitude(),location.getLongitude()));

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                mapUtil.enableGPS(getApplicationContext(),getActivity(),101);
                turnOffRideShare();
            }
        };


        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIST,locationListener );
        }
        catch (SecurityException e){
            Snackbar.make(findViewById(R.id.home_scrollview),e.getMessage(),Snackbar.LENGTH_SHORT).show();
        }

    }
    
    public void determineCurrentLocation(GeoCoordinates latLng){
        
        if(determineCallCount==0)previousPosition=latLng;
        else if(latLng.distanceTo(previousPosition)>=5){
            currentPosition=latLng;
            int rem=0;
            for (int i=0;i+1<pathString.size();i++){
                GeoCoordinates co1=mapUtil.GeoCoordinatesMap.get(pathString.get(i));
                GeoCoordinates co2=mapUtil.GeoCoordinatesMap.get(pathString.get(i+1));

                if(Math.abs(currentPosition.distanceTo(co2)+previousPosition.distanceTo(co2)-previousPosition.distanceTo(currentPosition))<=1){

                    if(co1.distanceTo(previousPosition)<co1.distanceTo(currentPosition)){
                        rem=i+2;
                        break;
                    }
                    
                }
                else if(Math.abs(currentPosition.distanceTo(co1)+previousPosition.distanceTo(co1)-previousPosition.distanceTo(currentPosition))<=1){

                    if(co2.distanceTo(previousPosition)>co2.distanceTo(currentPosition)){
                        rem=i+1;
                        break;
                    }

                }
                else if(co1.distanceTo(previousPosition)<co1.distanceTo(currentPosition) && co2.distanceTo(previousPosition)>co2.distanceTo(currentPosition)){
                    rem=i+1;
                    break;

                }

                
            }

            if(rem==0){
               determineCallCount=0;
               return;
            }
            else{
                for (int i=0;i<rem;i++)pathString.remove(0);
                pathOk=true;
                updatePath();
            }
        }
        
        determineCallCount=1;
        
    }
    
    

    public void handlePath(GeoCoordinates newLatLng){

        if(pathString.size()==1)return;
        
        if(!pathOk){
            determineCurrentLocation(newLatLng);
            return;
        }
        
        
        GeoCoordinates toCheck=mapUtil.GeoCoordinatesMap.get(pathString.get(0));

        if(newLatLng.distanceTo(toCheck)<=100){
            pathString.remove(0);
            updatePath();
        }


    }

    public void initializePath(){

        pathString=new ArrayList<>(Arrays.asList(mapUtil.CAMPUS,mapUtil.CAMPUS_GATE,mapUtil.MODINA_MARKET,mapUtil.SUBID_BAZAR,mapUtil.AMBORKHANA,mapUtil.EIDGAH,mapUtil.KUMARPARA,mapUtil.TILAGOR,mapUtil.BALUCHAR, mapUtil.CAMPUS));
        userPathReference.setValue("NA;");
        pathOk=false;
        determineCallCount=0;
        
    }

  public  void updatePath(){
      String path=new String();
      for (String s:pathString)path+=(s+";");
      userPathReference.setValue(path);
  }
    public Activity getActivity(){
        return (Activity)this;
    }

    public void turnOffRideShare(){

        isRideShareOn=false;
        rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.start_ride));
        locationManager.removeUpdates(locationListener);
        locationListener=null;
        userLocationData.setValue(null);
        userPathReference.setValue(null);
    }



    @Override
    public void onClick(View view) {
        int i = view.getId();

        if(i==R.id.ride_on_cv){
            if(userInfo.isDriver) {
                if(!isRideShareOn)turnOnRideShare();
                else turnOffRideShare();
            }
            else {
                Snackbar.make(findViewById(R.id.home_scrollview), "You're not a Driver!", Snackbar.LENGTH_SHORT).show();
            }
        }
        else  if(i==R.id.track_buses_cv){
            startActivity(new Intent(HomeActivity.this,MapsActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101){
            if(resultCode==Activity.RESULT_OK)turnOnRideShare();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestor.onRequestPermissionsResult(requestCode,grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isRideShareOn) {
            locationManager.removeUpdates(locationListener);
            locationListener=null;
            userLocationData.removeValue();
            userPathReference.setValue(null);

        }
    }
}
