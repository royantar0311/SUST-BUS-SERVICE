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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    public static final int MIN_TIME = 1000;
    public static final int MIN_DIST = 5;
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private FloatingActionButton locateMeBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener,pathChangeListner;
    private Map<String,Marker> markerMap;
    private MapUtil mapUtil;
    private RoutingEngine routingEngine;
    private Map<String,String> pathInformationMap;
    private boolean ok = false;
    private Polyline currentPolylineOnMap=null;
    private List<Waypoint> waypoints=null;
    private List<GeoCoordinates> geoCoordinatesOfPolyLine=null;
    private RoutingEngine.CarOptions carOptions;
    private TextView informationTv;
    private boolean freeLocateMeButton=true;
    private Marker myLocationMarker;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locateMeBtn = findViewById(R.id.locate_me_btn);

        locateMeBtn.setOnClickListener(this);
        informationTv=findViewById(R.id.information_tv);

        databaseReference= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();


        if(mAuth.getCurrentUser()==null){
            Intent intent=new Intent(MapsActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else userUid=mAuth.getCurrentUser().getUid();

        mapUtil=MapUtil.getInstance();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLngBounds latLngBounds=new LatLngBounds.Builder().include(new LatLng(24.910837,91.888013))
                                                         .include(new LatLng(24.861436,91.825502)).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(),13f),100,null);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        markerMap=new HashMap<>();


        try {
            routingEngine=new RoutingEngine();

        }
        catch (InstantiationErrorException e){
            new RuntimeException(e.error.name());

        }

        carOptions=new RoutingEngine.CarOptions();
        carOptions.routeOptions=new RouteOptions.Builder().setAlternatives(0).setOptimizationMode(OptimizationMode.SHORTEST).build();
        carOptions.restrictions=new RouteRestrictions();
        carOptions.restrictions.avoidAreas=mapUtil.restrictionList;
        if(!mapUtil.rideShareStatus)showUserLocation();
        else{
            new CountDownTimer(60000*5,2000){
                @Override
                public void onFinish() {

                }

                @Override
                public void onTick(long millisUntilFinished) {
                    if(markerMap.containsKey(userUid)){
                        markerMap.get(userUid).
                        setIcon(bitmapDescriptorFromVector(R.drawable.ic_blue_bus_for_user));
                        this.cancel();
                    }

                }
            }.start();
        }

         childEventListener =new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                LatLng pos=null;
                String key=null,title=null;

                Marker tmpMarker;
                try{
                     pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                     key = dataSnapshot.getKey();
                     title=dataSnapshot.child("title").getValue(String.class);
                }

                catch (Exception e){

                }

                if(pos!=null && key !=null){
                    tmpMarker= addMark(pos,title);
                    tmpMarker.showInfoWindow();
                    tmpMarker.setTag(key);
                    tmpMarker.setFlat(true);
                    markerMap.put(key,tmpMarker);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                LatLng pos=null;
                Marker tmpMarker;

                String key=dataSnapshot.getKey(),title=null;
                 if(markerMap.containsKey(key)){
                         pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                         markerMap.get(key).setPosition(pos);
                 }

                 else {

                     try{
                         pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                         key = dataSnapshot.getKey();
                         title=dataSnapshot.child("title").getValue(String.class);
                     }
                     catch (Exception e){

                     }

                     if(pos!=null && key !=null){

                         tmpMarker= addMark(pos,title);
                         tmpMarker.showInfoWindow();
                         tmpMarker.setTag(key);
                         tmpMarker.setFlat(true);
                         markerMap.put(key,tmpMarker);
                     }

                 }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key=dataSnapshot.getKey();
                if(markerMap.containsKey(key)){
                    markerMap.get(key).remove();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

         pathInformationMap=new HashMap<>();

         pathChangeListner=new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                         pathInformationMap.put( dataSnapshot.getKey(), dataSnapshot.child("path").getValue(String.class));
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 String key=dataSnapshot.getKey();
                 pathInformationMap.remove(key);
                 pathInformationMap.put(key,dataSnapshot.child("path").getValue(String.class));
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

        if(marker.equals(myLocationMarker)){
            informationTv.setText("This is you");
            return false ;
        }


        String path=pathInformationMap.get((String)marker.getTag());

        if(path==null || path.equals((String)"NA;")){
            Snackbar.make(findViewById(R.id.maps_activity),"Sorry, Currently Route is not availavle for this bus",Snackbar.LENGTH_LONG).show();
            return false;
        }

        MapUtil.PathInformation pathInformation=mapUtil.stringToPath(path);

        Waypoint starWaypoint=new Waypoint(new GeoCoordinates(marker.getPosition().latitude,marker.getPosition().longitude));

        waypoints=pathInformation.getWayPoints();

        waypoints.add(0,starWaypoint);

        routingEngine.calculateRoute(waypoints, carOptions, new CalculateRouteCallback() {
           @Override
           public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> list) {

               if(routingError==null){
                   Route route=list.get(0);
                   geoCoordinatesOfPolyLine=showRoute(route,R.color.blue2);
               }
               else{
                   Snackbar.make(findViewById(R.id.maps_activity),routingError.toString(),Snackbar.LENGTH_LONG);
               }

           }
       });

        return false;

    }

    public List<GeoCoordinates> showRoute(Route route,int color){

        if (currentPolylineOnMap!=null)currentPolylineOnMap.remove();

        List<GeoCoordinates> tmpList=route.getPolyline();
        PolylineOptions polylineOptions=new PolylineOptions();

        for(GeoCoordinates g:tmpList){
            LatLng tmp=new LatLng(g.latitude,g.longitude);
            polylineOptions.add(tmp);
        }
        polylineOptions.width(14);
        polylineOptions.color(ContextCompat.getColor(this,color));

        long time=route.getDurationInSeconds()+route.getTrafficDelayInSeconds();

        int hour= (int) (time/3600);
        int minute= (int)(time%3600)/60;
        polylineOptions.endCap(new ButtCap());


        currentPolylineOnMap=mMap.addPolyline(polylineOptions);
        informationTv.setText("Estimated time: "+hour+" hour "+minute+" minutes");
        return tmpList;
    }

    public void getEstimatedTime(){

        if(geoCoordinatesOfPolyLine==null || waypoints==null){
            Snackbar.make(findViewById(R.id.maps_activity),"Please Select a Bus First",Snackbar.LENGTH_LONG).show();
            freeLocateMeButton=true;
            return;
        }

        if(mapUtil.rideShareStatus){
            Snackbar.make(findViewById(R.id.maps_activity),"You are on this bus",Snackbar.LENGTH_LONG);
            if(markerMap.containsKey(userUid))mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerMap.get(userUid).getPosition(),20));
            freeLocateMeButton=true;
            return;
        }

        if(myLocationMarker==null){
            Snackbar.make(findViewById(R.id.maps_activity),"Please check your Location Setting!",Snackbar.LENGTH_LONG).show();
            freeLocateMeButton=true;
            return;
        }

        GeoCoordinates myPos=new GeoCoordinates(myLocationMarker.getPosition().latitude,myLocationMarker.getPosition().longitude);
        int lim=0;
        for(int i=0;i+1<geoCoordinatesOfPolyLine.size();i++){
            Double distv1v2=geoCoordinatesOfPolyLine.get(i).distanceTo(geoCoordinatesOfPolyLine.get(i+1));

            double distv1pos=myPos.distanceTo(geoCoordinatesOfPolyLine.get(i));
            double distv2pos=myPos.distanceTo(geoCoordinatesOfPolyLine.get(i+1));

            if(distv2pos+distv1pos-distv1v2<=10){
                lim=i+1;
                break;
            }
        }

        if(lim==0){
            Snackbar.make(findViewById(R.id.maps_activity),"You are not on the route of the bus",Snackbar.LENGTH_LONG).show();
            freeLocateMeButton=true;
            return;
        }

        List<Waypoint> waypointsForUserRoute=new ArrayList<>();

        for(int j=0;j<waypoints.size();j++) {
            for (int i = 0; i < lim; i++) {
                double dist=waypoints.get(j).coordinates.distanceTo(geoCoordinatesOfPolyLine.get(i));
                //Log.d("DEB",waypoints.get(j).coordinates+" "+geoCoordinatesOfPolyLine.get(i)+" "+dist);
                if (dist <= 30) {
                    waypointsForUserRoute.add(waypoints.get(j));
                    break;
                }

            }
        }

        waypointsForUserRoute.add(0,new Waypoint(geoCoordinatesOfPolyLine.get(0)));
        waypointsForUserRoute.add(new Waypoint(myPos));

        routingEngine.calculateRoute(waypointsForUserRoute, carOptions, new CalculateRouteCallback() {
            @Override
            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> list) {
                if(routingError==null){
                    showRoute(list.get(0),R.color.orange4);
                }
                else{
                    Snackbar.make(findViewById(R.id.maps_activity),"Please try Again",Snackbar.LENGTH_LONG).show();
                }
                freeLocateMeButton=true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.locate_me_btn && freeLocateMeButton){
            freeLocateMeButton=false;
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


    locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
                if(myLocationMarker==null)myLocationMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude()))
                                                                                             .icon(bitmapDescriptorFromVector(R.drawable.ic_radio_button))
                                                                                             .flat(true));
                else myLocationMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            mapUtil.enableGPS(getApplicationContext(), getActivity(), 101);
        }
    };


    try {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
    } catch (SecurityException e) {
        Snackbar.make(findViewById(R.id.home_scrollview), e.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101){
            if(requestCode==RESULT_OK)showUserLocation();
        }
    }

    public Activity getActivity(){
        return (Activity)this;
  }

    public Marker addMark(LatLng cur,String title){

        Marker marker=mMap.addMarker(new MarkerOptions().position(cur).title(title)
                .icon(bitmapDescriptorFromVector(R.drawable.ic_directions_bus_black_24dp)));
        return marker;

    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(this,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Toast.makeText(this, "press back again to exit", Toast.LENGTH_SHORT).show();
            if(ok){

                databaseReference.child("alive").removeEventListener(childEventListener);
                databaseReference.child("destinations").removeEventListener(pathChangeListner);
                databaseReference=null;
                if(locationManager!=null)locationManager.removeUpdates(locationListener);
                locationManager=null;
                locationListener=null;
                markerMap.clear();
                pathInformationMap.clear();
                finish();
            }
            ok = true;
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
