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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.OptimizationMode;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RouteOptions;
import com.here.sdk.routing.RouteRestrictions;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Waypoint;
import com.sustbus.driver.util.MapUtil;
import com.sustbus.driver.util.PathInformation;
import com.sustbus.driver.util.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MapsActivity";
    public static final int MIN_TIME = 2000;
    public static final int MIN_DIST = 5;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private FloatingActionButton locateMeBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener, pathChangeListner;
    private Map<String, Marker> markerMap;
    private MapUtil mapUtil;
    private RoutingEngine routingEngine;
    private Map<String, String> pathInformationMap;
    private Map<String,String> ForMap;
    private boolean ok = false;
    private Polyline currentPolylineOnMap = null;
    private List<Waypoint> waypoints = null;
    private List<GeoCoordinates> geoCoordinatesOfPolyLine = null;
    private RoutingEngine.CarOptions carOptions;
    private TextView informationTv;
    private boolean freeLocateMeButton = true;
    private Marker myLocationMarker;
    private LocationManager locationManager;
    private String userUid;
    private boolean isCalculatingBusRout = false;
    private CardView informationCard;
    private UserInfo user=UserInfo.getInstance();
    private boolean showStudent,showStaff,showTeacher;
    private Switch studentSw,teacherSw,staffSw;
    private SharedPreferences settings;
    private boolean staffChecked,studentChecked,teacherChecked;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        settings=getSharedPreferences("settings",MODE_PRIVATE);
        showStudent=settings.getBoolean("map_showStudent",true);
        showTeacher=settings.getBoolean("map_showTeacher",true);
        showStaff=settings.getBoolean("map_showStaff",true);
        locateMeBtn = findViewById(R.id.locate_me_btn);
        informationCard = findViewById(R.id.information_tv_cardview);
        locateMeBtn.setOnClickListener(this);
        informationTv = findViewById(R.id.information_tv);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);


        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else userUid = mAuth.getCurrentUser().getUid();

        mapUtil = MapUtil.getInstance();

        if(user.isStudent()&& !user.isAdmin()){
            findViewById(R.id.map_filter_fab).setVisibility(View.GONE);
            showTeacher=false;
            showStaff=false;
        }
        else{
            findViewById(R.id.map_filter_fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectorPopUp();
                }
            });
        }

        if(user.isStaff() && !user.isAdmin()){
            showTeacher=false;
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLngBounds latLngBounds = new LatLngBounds.Builder().include(new LatLng(24.910837, 91.888013))
                .include(new LatLng(24.861436, 91.825502)).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 13f), 100, null);

        markerMap = new HashMap<>();
        ForMap=new HashMap<>();


        try {
            routingEngine = new RoutingEngine();

        } catch (InstantiationErrorException e) {
            new RuntimeException(e.error.name());

        }

        carOptions = new RoutingEngine.CarOptions();
        carOptions.routeOptions = new RouteOptions.Builder().setAlternatives(0).setOptimizationMode(OptimizationMode.SHORTEST).build();
        carOptions.restrictions = new RouteRestrictions();
        carOptions.restrictions.avoidAreas = MapUtil.restrictionList;
        if (!MapUtil.rideShareStatus) showUserLocation();
        else {
            new CountDownTimer(60000 * 5, 2000) {
                @Override
                public void onFinish() {

                }

                @Override
                public void onTick(long millisUntilFinished) {
                    if (markerMap.containsKey(userUid)) {
                        markerMap.get(userUid).
                                setIcon(bitmapDescriptorFromVector(R.drawable.ic_blue_bus_for_user));
                        this.cancel();
                    }

                }
            }.start();
        }

        if (getIntent().getBooleanExtra("fromSchedule", false)) {

            new CountDownTimer(60000, 1000) {
                @Override
                public void onFinish() {

                }

                @Override
                public void onTick(long millisUntilFinished) {
                    if (markerMap.containsKey(getIntent().getStringExtra("markerToShow"))) {
                        String markerKey = getIntent().getStringExtra("markerToShow");
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerMap.get(markerKey).getPosition(), 16f));
                        this.cancel();
                    }
                }
            }.start();

        }

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                LatLng pos = null;
                String key = null, title = null,For="s";


                Marker tmpMarker;
                try {
                    pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                    key = dataSnapshot.getKey();
                    title = dataSnapshot.child("title").getValue(String.class);
                    For=dataSnapshot.child("for").getValue(String.class);
                } catch (Exception e) {

                }

                if (pos != null && key != null) {

                    tmpMarker = addMark(pos, title,For);
                    tmpMarker.showInfoWindow();
                    tmpMarker.setTag(key);
                    tmpMarker.setFlat(true);
                    markerMap.put(key, tmpMarker);
                    ForMap.put(key,For);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                LatLng pos = null;
                Marker tmpMarker;

                String key = dataSnapshot.getKey(), title = null,For="s";
                if (markerMap.containsKey(key)) {
                    pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                    markerMap.get(key).setPosition(pos);
                    markerMap.get(key).setRotation(dataSnapshot.child("rotation").getValue(Float.class));
                }
                else {

                    try {
                        pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                        key = dataSnapshot.getKey();
                        title = dataSnapshot.child("title").getValue(String.class);
                        For=dataSnapshot.child("for").getValue(String.class);
                    } catch (Exception e) {

                    }

                    if (pos != null && key != null) {

                        tmpMarker = addMark(pos, title,For);
                        tmpMarker.showInfoWindow();
                        tmpMarker.setTag(key);
                        tmpMarker.setFlat(true);
                        markerMap.put(key, tmpMarker);
                        ForMap.put(key,For);
                    }

                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                if (markerMap.containsKey(key)) {
                    markerMap.get(key).remove();
                    markerMap.remove(key);
                    ForMap.remove(key);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        pathInformationMap = new HashMap<>();

        pathChangeListner = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                pathInformationMap.put(dataSnapshot.getKey(), dataSnapshot.child("path").getValue(String.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                pathInformationMap.remove(key);
                pathInformationMap.put(key, dataSnapshot.child("path").getValue(String.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                pathInformationMap.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        databaseReference.child("alive").addChildEventListener(childEventListener);
        databaseReference.child("destinations").addChildEventListener(pathChangeListner);

        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (marker.equals(myLocationMarker)) {
            greenSignal("This is you");
            return false;
        }


        String path = pathInformationMap.get(marker.getTag());

        if (path == null || path.equals("NA;")) {
            blinkRed("Sorry, Currently Route is not availavle for this bus");
            return false;
        }

        if (isCalculatingBusRout) {
            blinkRed(null);
            return false;
        }
        isCalculatingBusRout = true;
        informationTv.setText("Please wait, Getting bus route...");

        PathInformation pathInformation = mapUtil.stringToPath(path);

        Waypoint starWaypoint = new Waypoint(new GeoCoordinates(marker.getPosition().latitude, marker.getPosition().longitude));

        waypoints = pathInformation.getWayPoints();

        waypoints.add(0, starWaypoint);

        routingEngine.calculateRoute(waypoints, carOptions, new CalculateRouteCallback() {
            @Override
            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> list) {

                if (routingError == null) {
                    Route route = list.get(0);
                    geoCoordinatesOfPolyLine = showRoute(route, R.color.black);
                } else {
                    blinkRed("Try Again");
                }
                isCalculatingBusRout = false;

            }
        });

        return false;

    }

    public List<GeoCoordinates> showRoute(Route route, int color) {

        if (currentPolylineOnMap != null) currentPolylineOnMap.remove();

        List<GeoCoordinates> tmpList = route.getPolyline();
        PolylineOptions polylineOptions = new PolylineOptions();

        for (GeoCoordinates g : tmpList) {
            LatLng tmp = new LatLng(g.latitude, g.longitude);
            polylineOptions.add(tmp);
        }
        polylineOptions.width(14);
        polylineOptions.color(ContextCompat.getColor(this, color));

        long time = route.getDurationInSeconds() + route.getTrafficDelayInSeconds();

        int hour = (int) (time / 3600);
        int minute = (int) (time % 3600) / 60;
        int second = (int) (time % (3600)) % 60;
        polylineOptions.endCap(new ButtCap());

        String tmp = "";
        if (hour != 0) tmp = hour + " hour ";
        if (minute != 0) tmp += minute + " minute ";
        if (second != 0) tmp += second + " seconds";

        currentPolylineOnMap = mMap.addPolyline(polylineOptions);
        greenSignal("Estimated time: " + tmp);
        return tmpList;
    }

    public void getEstimatedTime() {

        if (geoCoordinatesOfPolyLine == null || waypoints == null) {
            blinkRed("Please Select a Bus First");
            freeLocateMeButton = true;
            return;
        }

        if (MapUtil.rideShareStatus) {
            greenSignal("You are on this bus");
            if (markerMap.containsKey(userUid))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerMap.get(userUid).getPosition(), 15));
            freeLocateMeButton = true;
            return;
        }

        if (myLocationMarker == null) {
            blinkRed("Please check your Location Settings!");
            freeLocateMeButton = true;
            return;
        }

        GeoCoordinates myPos = new GeoCoordinates(myLocationMarker.getPosition().latitude, myLocationMarker.getPosition().longitude);
        int lim = 0;
        for (int i = 0; i + 1 < geoCoordinatesOfPolyLine.size(); i++) {
            Double distv1v2 = geoCoordinatesOfPolyLine.get(i).distanceTo(geoCoordinatesOfPolyLine.get(i + 1));

            double distv1pos = myPos.distanceTo(geoCoordinatesOfPolyLine.get(i));
            double distv2pos = myPos.distanceTo(geoCoordinatesOfPolyLine.get(i + 1));

            if (distv2pos + distv1pos - distv1v2 <= 100) {
                lim = i + 1;
                break;
            }
        }

        if (lim == 0) {
            blinkRed("You are not on the route of the bus");
            freeLocateMeButton = true;
            return;
        }
        informationTv.setText("Getting route and time for you...");

        List<Waypoint> waypointsForUserRoute = new ArrayList<>();

        for (int j = 0; j < waypoints.size(); j++) {
            for (int i = 0; i < lim; i++) {
                double dist = waypoints.get(j).coordinates.distanceTo(geoCoordinatesOfPolyLine.get(i));
                //Log.d("DEB",waypoints.get(j).coordinates+" "+geoCoordinatesOfPolyLine.get(i)+" "+dist);
                if (dist <= 30) {
                    waypointsForUserRoute.add(waypoints.get(j));
                    break;
                }

            }
        }

        waypointsForUserRoute.add(0, new Waypoint(geoCoordinatesOfPolyLine.get(0)));
        waypointsForUserRoute.add(new Waypoint(myPos));
        routingEngine.calculateRoute(waypointsForUserRoute, carOptions, new CalculateRouteCallback() {
            @Override
            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> list) {
                if (routingError == null) {
                    showRoute(list.get(0), R.color.orange4);
                } else {
                    blinkRed("Please try Again");
                }
                freeLocateMeButton = true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.locate_me_btn) {
            if (!freeLocateMeButton) {
                blinkRed("Please wait, already getting a route for you");
                return;
            }

            freeLocateMeButton = false;
            getEstimatedTime();
        }
    }

    public void showUserLocation() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean isGps = false;
        try {
            isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {

        }

        if (!isGps) {
            mapUtil.enableGPS(getApplicationContext(), this, 101);
            locationManager = null;
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult location) {
                super.onLocationResult(location);
                Log.d(TAG, "onLocationResult:  asche");
                if (myLocationMarker == null)
                    myLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLastLocation().getLatitude(), location.getLastLocation().getLongitude()))
                            .icon(bitmapDescriptorFromVector(R.drawable.ic_radio_button))
                            .flat(true));
                else
                    myLocationMarker.setPosition(new LatLng(location.getLastLocation().getLatitude(), location.getLastLocation().getLongitude()));
            }
        },getMainLooper());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) showUserLocation();
            else finish();
        }
    }

    public Activity getActivity() {
        return this;
    }

    public Marker addMark(LatLng cur, String title,String For) {
       int id=R.drawable.ic_directions_bus_black_24dp;
       if(For.equals("t"))id=R.drawable.ic_bus_for_teacher;
       if(For.equals("sf"))id=R.drawable.ic_bus_for_staff;
        boolean setVisible=false;

        if(For.equals("s") && showStudent)setVisible=true;
        else if(For.equals("t") && showTeacher)setVisible=true;
        else if(For.equals("sf") && showStaff)setVisible=true;


        Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(cur)
                .title(title)
                .anchor(.5f, .5f)
                .icon(bitmapDescriptorFromVector(id))
                .visible(setVisible)
                );
        return marker;

    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void blinkRed(String mes) {
        if (mes != null) {
            informationTv.setText(mes);
        }
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.RESTART);
        informationCard.setAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                informationCard.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cpb_error_state_selector));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                informationCard.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void greenSignal(String mes) {

        informationTv.setText(mes);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(450);
        alphaAnimation.setRepeatCount(0);
        informationCard.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                informationCard.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.greenSignal));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                informationCard.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(!ok)Toast.makeText(this, "press back again to exit", Toast.LENGTH_SHORT).show();
            if (ok) {

                databaseReference.child("alive").removeEventListener(childEventListener);
                databaseReference.child("destinations").removeEventListener(pathChangeListner);
                databaseReference = null;
                locationManager = null;markerMap.clear();
                pathInformationMap.clear();
                finish();
            }
            ok = true;
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void selectorPopUp(){

        View v=getLayoutInflater().inflate(R.layout.category_selector_popup,null);
        staffSw=v.findViewById(R.id.staff_switch);
        teacherSw=v.findViewById(R.id.teacher_switch);
        studentSw=v.findViewById(R.id.student_switch);

        staffChecked=showStaff;
        studentChecked=showStudent;
        teacherChecked=showTeacher;

        staffSw.setChecked(staffChecked);
        studentSw.setChecked(studentChecked);
        teacherSw.setChecked(teacherChecked);

        staffSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                staffChecked=isChecked;
            }
        });
        studentSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                studentChecked=isChecked;
            }
        });
        if(user.isStaff() && !user.isAdmin()){
            teacherSw.setVisibility(View.GONE);
            v.findViewById(R.id.teacher_sw_tv).setVisibility(View.GONE);
        }
        else{
            teacherSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    teacherChecked=isChecked;
                }
            });
        }

        AlertDialog alertDialog=new AlertDialog.Builder(this)
                .setView(v)
                .create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                if(staffChecked!=showStaff || teacherChecked!=showTeacher || studentChecked!=showStudent){
                    showStaff=staffChecked;
                    showTeacher=teacherChecked;
                    showStudent=studentChecked;
                    handleShow();
                }
            }
        });

    }

    public void  handleShow(){

        for(Map.Entry en:ForMap.entrySet()){

            if(en.getValue().equals("s") ) {
                if (showStudent) markerMap.get(en.getKey()).setVisible(true);
                else markerMap.get(en.getKey()).setVisible(false);
            }
            if(en.getValue().equals("t")) {
                if (showTeacher) markerMap.get(en.getKey()).setVisible(true);
                else markerMap.get(en.getKey()).setVisible(false);
            }

            if(en.getValue().equals("sf")) {
                if(showStaff)markerMap.get(en.getKey()).setVisible(true);
                else markerMap.get(en.getKey()).setVisible(false);
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor ed=settings.edit();
        ed.putBoolean("map_showStudent",showStudent);
        ed.putBoolean("map_showTeacher",showTeacher);
        ed.putBoolean("map_showStaff",showStaff);
        ed.commit();
    }
}
