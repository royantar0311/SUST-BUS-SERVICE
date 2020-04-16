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
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.OptimizationMode;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RouteOptions;
import com.here.sdk.routing.RouteRestrictions;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Waypoint;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    public static final int MIN_TIME = 1000;
    public static final int MIN_DIST = 5;
    private static final String TAG = "MapsActivity";
    UserInfo userInfo;
    private GoogleMap mMap;
    private ImageButton locateMeBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener,pathChangeListner;
    private Map<String,Marker> markerMap;
    private MapUtil mapUtil;
    private RoutingEngine routingEngine;
    private Map<String,String> pathInformationMap;
    private boolean ok = false;
    private Polyline polyline=null;
    private List<Waypoint> waypoints=null;
    private List<GeoCoordinates> geoCoordinatesOfPolyLine=null;
    private RoutingEngine.CarOptions carOptions;
    private boolean routeSelected=false;
    private TextView informationTv;
    private boolean freeLocateMeButton=true;
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
        else{
            userInfo=UserInfo.getInstance();
            //
            //
            // userDatabaseReference=databaseReference.child("alive").child(mAuth.getCurrentUser().getUid());
           // Toast.makeText(this,userInfo.getUserName(),Toast.LENGTH_SHORT).show();
        }
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


        markerMap=new HashMap<>();

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
                   showRoute(route,R.color.blue2);
                   routeSelected=true;

               }
               else{
                   Snackbar.make(findViewById(R.id.maps_activity),routingError.toString(),Snackbar.LENGTH_LONG);
               }

           }
       });



        return false;

    }

    public void showRoute(Route route,int color){

        if (polyline!=null)polyline.remove();

        geoCoordinatesOfPolyLine=route.getPolyline();
        PolylineOptions polylineOptions=new PolylineOptions();

        for(GeoCoordinates g:geoCoordinatesOfPolyLine){
            LatLng tmp=new LatLng(g.latitude,g.longitude);
            polylineOptions.add(tmp);
        }
        polylineOptions.width(14);
        polylineOptions.color(ContextCompat.getColor(this,color));

        long time=route.getDurationInSeconds();

        int hour= (int) (time/3600);
        int minute= (int)(time%3600)/60;
        polylineOptions.endCap(new SquareCap());


        polyline=mMap.addPolyline(polylineOptions);
        informationTv.setText("Estimated time: "+hour+" hour "+minute+" minutes");
    }

    public void getEstimatedTime(Location location){

        if(!routeSelected){
            Snackbar.make(findViewById(R.id.maps_activity),"Please Select a Bus First",Snackbar.LENGTH_LONG).show();
            return;
        }



        if(location==null){
            Snackbar.make(findViewById(R.id.maps_activity),"Something Went Wrong!",Snackbar.LENGTH_LONG).show();
            return;
        }

        GeoCoordinates myPos=new GeoCoordinates(location.getLatitude(),location.getLongitude());
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
            return;
        }

        List<Waypoint> waypointsForUserRoute=new ArrayList<>();

        for (int i=0,j=0;i<lim;i++){

            if(geoCoordinatesOfPolyLine.get(i).equals(waypoints.get(j).coordinates)){
                waypointsForUserRoute.add(waypoints.get(j));
                j++;
            }
        }

        waypoints.add(0,new Waypoint(geoCoordinatesOfPolyLine.get(0)));
        waypoints.add(new Waypoint(myPos));

        routingEngine.calculateRoute(waypointsForUserRoute, carOptions, new CalculateRouteCallback() {
            @Override
            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> list) {
                if(routingError==null){
                    showRoute(list.get(0),R.color.orange4);
                }
                else{
                    Snackbar.make(findViewById(R.id.maps_activity),"Please try Again",Snackbar.LENGTH_LONG).show();
                }
            }
        });


    }

    public Marker addMark(LatLng cur,String title){
        Marker marker=mMap.addMarker(new MarkerOptions().position(cur).title(title)
                .icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_directions_bus_black_24dp)));
        return marker;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(MapsActivity context, int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.locate_me_btn && freeLocateMeButton){
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                      if(location!=null)getEstimatedTime(location);
                      freeLocateMeButton=true;
                }
            });
            freeLocateMeButton=false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Toast.makeText(this, "press back again to exit", Toast.LENGTH_SHORT).show();
            if(ok){

                databaseReference.child("alive").removeEventListener(childEventListener);
                databaseReference.child("destinations").removeEventListener(pathChangeListner);
                databaseReference=null;
                userDatabaseReference = null;
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
