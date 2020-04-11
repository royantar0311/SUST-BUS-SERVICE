package com.sustbus.driver;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    public static final int MIN_TIME = 1000;
    public static final int MIN_DIST = 5;
    private ImageButton locateMeBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    private Map<String,Marker> markerMap;
    private MapUtil mapUtil;
    UserInfo userInfo;
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


        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(24.9192, 91.8319)));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(24.9192, 91.8319), 18.0f));
        mMap.setTrafficEnabled(true);

        mMap.setBuildingsEnabled(true);
        markerMap=new HashMap<>();

         childEventListener =new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                LatLng pos=null;
                String key=null,path=null;
                Marker tmpMarker;
                try{
                     pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                     key = dataSnapshot.getKey();
                     path = dataSnapshot.child("destination").getValue(String.class);

                }

                catch (Exception e){

                }

                if(pos!=null && key !=null && path!=null){
                    MapUtil.PathInformation pathInformation=mapUtil.stringToPath(path);
                    tmpMarker= addMark(pos, pathInformation.getDestText());
                    tmpMarker.showInfoWindow();
                    tmpMarker.setTag(pathInformation);
                    markerMap.put(key,tmpMarker);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                LatLng pos=null;
                Marker tmpMarker;

                String key=dataSnapshot.getKey(),path=null;
                 if(markerMap.containsKey(key)){

                         pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                         markerMap.get(key).setPosition(pos);
                 }

                 else {

                     try{
                         pos = new LatLng(dataSnapshot.child("lat").getValue(Double.class), dataSnapshot.child("lng").getValue(Double.class));
                         key = dataSnapshot.getKey();
                         path= dataSnapshot.child("destination").getValue(String.class);

                     }
                     catch (Exception e){

                     }

                     if(pos!=null && key !=null && path!=null){
                         MapUtil.PathInformation pathInformation=mapUtil.stringToPath(path);
                         tmpMarker= addMark(pos, pathInformation.getDestText());
                         tmpMarker.showInfoWindow();
                         tmpMarker.setTag(pathInformation);
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

        databaseReference.child("alive").addChildEventListener(childEventListener);

       mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        MapUtil.PathInformation pathInformation=(MapUtil.PathInformation)marker.getTag();
        Log.d("PATH:" ,""+pathInformation.getSrc());
        DirectionsResult result;

        Toast.makeText(this,"markerClicked",Toast.LENGTH_SHORT).show();

        try{
            assert pathInformation != null;
            result=DirectionsApi.newRequest(mapUtil.getGeoApiContext())
                    .mode(TravelMode.TRANSIT)
                    .origin(pathInformation.getSrc())
                    .destination(pathInformation.getDest())
                    .alternatives(false)
                    .await();


            List<com.google.maps.model.LatLng> decodedPath=result.routes[0].overviewPolyline.decodePath();

            PolylineOptions polylineOptions=new PolylineOptions();

            for (com.google.maps.model.LatLng x:decodedPath){
                LatLng tmp=new LatLng(x.lat,x.lng);
                polylineOptions.add(tmp);
            }

            mMap.addPolyline(polylineOptions);

        }
        catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            Log.d("marker: ",e.getMessage());

            StackTraceElement arr[]=new StackTraceElement[e.getStackTrace().length];
            arr=e.getStackTrace();
            for (int i=0;i<arr.length;i++) {

                Log.d("error: ", arr[i].getClassName()+"\n"+arr[i].getMethodName()+"\n"+arr[i].getLineNumber());
            }
        }











        return false;
    }

    public Marker addMark(LatLng cur, String title){


        Marker marker=mMap.addMarker(new MarkerOptions().position(cur)
                .title(title)
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
        if(i == R.id.locate_me_btn){
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(cur));
        }
    }

    private boolean ok = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Toast.makeText(this, "press back again to exit", Toast.LENGTH_SHORT).show();
            if(ok){

                databaseReference.child("alive").removeEventListener(childEventListener);
                databaseReference=null;
                userDatabaseReference = null;
                markerMap.clear();
                finish();
            }
            ok = true;
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
