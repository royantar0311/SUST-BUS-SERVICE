package com.sustbus.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private DatabaseReference databaseReference,userDatabaseReference,userLocationData;
    private FirebaseAuth mAuth;
    private ImageView rideShareIndicatorIV;
    private boolean isRideShareOn=false;
    private LocationManager locationManager;
    private LocationListener locationListener;

    UserInfo userInfo;
    Intent intent ;
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
            userDatabaseReference=databaseReference.child("users").child(mAuth.getCurrentUser().getUid());

            userLocationData=databaseReference.child("alive").child(mAuth.getCurrentUser().getUid());

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

    }



    public void turnOnRideShare(){

        isRideShareOn=true;
        rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.end_ride));
        userLocationData.child("destination").setValue("To Campus");

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                userLocationData.child("lat").setValue(location.getLatitude());
                userLocationData.child("lng").setValue(location.getLongitude());

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

    public void turnOffRideShare(){
        isRideShareOn=false;
        rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.start_ride));
        locationManager.removeUpdates(locationListener);
        locationListener=null;
        userLocationData.setValue(null);
    }




    @Override
    public void onClick(View view) {
        int i = view.getId();

        if(i==R.id.ride_on_cv){
            if(userInfo.isDriver && (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED || requestLocationPermission()) ) {
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

    private boolean requestLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This Permission is Needed Share Your Location")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_CODE);
                            shareRideTv.callOnClick();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_CODE);
        }
        if(ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isRideShareOn) {
            locationManager.removeUpdates(locationListener);
            locationListener=null;
            userLocationData.removeValue();
        }
    }
}
