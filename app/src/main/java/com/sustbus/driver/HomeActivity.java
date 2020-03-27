package com.sustbus.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";


    private Button signOutBtn;
    private Button openMapBtn;
    private Button scheduleBtn;
    private Button shareRideBtn;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;
    UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        signOutBtn = findViewById(R.id.sign_out_btn);
        openMapBtn=findViewById(R.id.show_map);
        shareRideBtn=findViewById(R.id.share_ride);
        scheduleBtn=findViewById(R.id.schedule);

        databaseReference= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();

        signOutBtn.setOnClickListener(this);
        openMapBtn.setOnClickListener(this);
        scheduleBtn.setOnClickListener(this);
        shareRideBtn.setOnClickListener(this);


        if(mAuth.getCurrentUser()==null){
            Intent intent=new Intent(HomeActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else{
            userDatabaseReference=databaseReference.child("users").child(mAuth.getCurrentUser().getUid());


            userDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userInfo = UserInfo.getBuilder()
                            .setUserName(dataSnapshot.child("userName").getValue(String.class))
                            .setPassword(dataSnapshot.child("password").getValue(String.class))
                            .setEmail(dataSnapshot.child("email").getValue(String.class))
                            .setDriver(dataSnapshot.child("isDriver").getValue(Boolean.class))
                            .build();
                    Toast.makeText(HomeActivity.this, userInfo.getUserName() + " ",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onDataChange: " + userInfo.getUserName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.sign_out_btn){

            mAuth.signOut();
            Intent intent=new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();

        }
        else if(i==R.id.share_ride){
            handleRideShare();
        }
       else  if(i==R.id.show_map){

            Intent intent=new Intent(HomeActivity.this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    }

    public void handleRideShare(){

    }


}
