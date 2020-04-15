package com.sustbus.driver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import static com.sustbus.driver.MapsActivity.MIN_DIST;
import static com.sustbus.driver.MapsActivity.MIN_TIME;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private static final int LOCATION_PERMISSION_CODE = 1;
    private TextView userNameTv;
    private TextView driverOrStudent;
    private CardView openMapBtn;
    private CardView scheduleBtn;
    private CardView shareRideTv;
    private CardView profileCv;
    private CardView signOut;
    private DatabaseReference databaseReference,userDatabaseReference,userLocationData;
    private FirebaseFirestore firestoreDb;
    private FirebaseAuth mAuth;
    private ImageView rideShareIndicatorIV;
    private boolean isRideShareOn=false,quit=false;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String userUid;
    private MapUtil mapUtil;
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
        profileCv = findViewById(R.id.profile_cv);
        signOut = findViewById(R.id.help_center_cv);


        openMapBtn.setOnClickListener(this);
        scheduleBtn.setOnClickListener(this);
        shareRideTv.setOnClickListener(this);
        profileCv.setOnClickListener(this);
        signOut.setOnClickListener(this);

        shareRideTv.setEnabled(false);

        mAuth=FirebaseAuth.getInstance();



        /**
         * Database initialization
         * */

        if(mAuth.getCurrentUser()==null){
            Intent intent=new Intent(HomeActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else{
            databaseReference= FirebaseDatabase.getInstance().getReference();
            userUid=mAuth.getCurrentUser().getUid();
            firestoreDb = FirebaseFirestore.getInstance();
            userInfo = UserInfo.getInstance();

            /**
             * getting data from cloud firestore
             * */
            firestoreDb.collection("users").document(userUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {

                        userInfo = documentSnapshot.toObject(UserInfo.class);


                        Log.d(TAG, "Current data: " + documentSnapshot.getData());
                        Log.d(TAG, "userInfo new datas"
                                    + "\nisDriver " + userInfo.isDriver()
                                    + "\nuid " + userInfo.getuId()
                                    + "\nispermitted " + userInfo.getIsStudentPermitted()
                                    + "\nemail " + userInfo.getEmail()
                        );
                        /**
                         * setting up dashboard for user (driver/student)
                         * */
                        userNameTv.setText(userInfo.getUserName());
                        if(userInfo.isDriver()){
                            Log.d(TAG, "onEvent: " + " ashena?");
                            driverOrStudent.setText("Driver");
                            shareRideTv.setEnabled(true);
                        }
                        else {
                            driverOrStudent.setText("Student");
                            shareRideTv.setEnabled(false);
                        }
                    }
                    else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
            userLocationData=databaseReference.child("alive");

            /**
             * previously firebase realtime-database was used;
             * */
//            userDatabaseReference=databaseReference.child("users").child(userUid);
//            userDatabaseReference.addValueEventListener(new ValueEventListener() {
//
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    userInfo.getBuilder()
//                            .setIsStudentPermitted(dataSnapshot.child("isStudentPermitted").getValue(Boolean.class))
//                            .setEmail(dataSnapshot.child("email").getValue(String.class))
//                            .setDriver(dataSnapshot.child("isDriver").getValue(Boolean.class))
//                            .build();
//
//                    userNameTv.setText(userInfo.getUserName());
//                    if(userInfo.isDriver){
//                        driverOrStudent.setText("Driver");
//                        shareRideTv.setEnabled(true);
//                    }
//                    else {
//                        driverOrStudent.setText("Student");
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
        }


        mapUtil=MapUtil.getInstance();
    }

    public void turnOnRideShare(){

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        /**
         * checking if gps is enabled or not
         * */
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

        /**
         * gps is enabled and location updates will be shown on the map and pushed to database
         * */

        isRideShareOn=true;
        rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.end_ride));

        userLocationData.child(userUid).child("destination").setValue(mapUtil.CAMPUS+";"+mapUtil.AMBORKHANA+";");

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                userLocationData.child(userUid).child("lat").setValue(location.getLatitude());
                userLocationData.child(userUid).child("lng").setValue(location.getLongitude());
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
            Snackbar.make(findViewById(R.id.maps_activity),e.getMessage(),Snackbar.LENGTH_SHORT).show();
        }

    }

    public Activity getActivity(){
        return (Activity)this;
    }

    public void turnOffRideShare(){

        isRideShareOn=false;
        rideShareIndicatorIV.setImageDrawable(getDrawable(R.drawable.start_ride));
        locationManager.removeUpdates(locationListener);
        locationListener=null;
        userLocationData.child(userUid).setValue(null);
    }




    @Override
    public void onClick(View view) {
        int i = view.getId();

        if(i==R.id.ride_on_cv){

            /**
             * check for permission to use location service
             * */
            if(userInfo.isDriver() && (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED || requestLocationPermission()) ) {
                if(!isRideShareOn)turnOnRideShare();
                else turnOffRideShare();
            }
            else if(!userInfo.isDriver()){
                Snackbar.make(findViewById(R.id.home_scrollview), "You're not a Driver!", Snackbar.LENGTH_SHORT).show();
            }
        }
        else if(i == R.id.help_center_cv){
            FirebaseAuth.getInstance().signOut();
            Intent intent=new Intent(HomeActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();

        }
        else  if(i==R.id.track_buses_cv){
            startActivity(new Intent(HomeActivity.this,MapsActivity.class));
        }
        else if(i==R.id.profile_cv){
            startActivity(new Intent(HomeActivity.this,ProfileActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101){
            if(resultCode==Activity.RESULT_OK)turnOnRideShare();
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
            userLocationData.child(userUid).removeValue();
        }
    }
}
