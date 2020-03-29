package com.sustbus.driver;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    public static final int MIN_TIME = 1000;
    public static final int MIN_DIST = 5;
    private ImageButton locateMeBtn;
    private  LatLng cur;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference databaseReference;
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
           // Toast.makeText(this,userInfo.getUserName(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        LatLng sustGolChattor = new LatLng(24.919141, 91.831745);
        mMap.addMarker(new MarkerOptions()
                .position(sustGolChattor)
                .title("SUST GolChattor"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sustGolChattor,19));


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.clear();
                cur = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(cur)
                        .title("Marker in Sydney")
                        .icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_directions_bus_black_24dp)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(cur));

                userDatabaseReference.child("lat").setValue(cur.latitude);
                userDatabaseReference.child("lng").setValue(cur.longitude);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIST,locationListener );
        }
        catch (SecurityException e){
            Snackbar.make(findViewById(R.id.maps_activity),e.getMessage(),Snackbar.LENGTH_SHORT).show();
        }

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
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cur));
        }
    }

    private boolean ok = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Toast.makeText(this, "press back again to exit", Toast.LENGTH_SHORT).show();
            if(ok){
                locationManager.removeUpdates(locationListener);
                locationManager = null;
                databaseReference = null;
                userDatabaseReference = null;
                finish();
            }
            ok = true;
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
