package com.sustbus.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";


    private TextView userNameTv;
    private TextView passwordTv;
    private Button signOutBtn;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        userNameTv = findViewById(R.id.username_tv);
        passwordTv = findViewById(R.id.password_tv);
        signOutBtn = findViewById(R.id.sign_out_btn);

        databaseReference= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        userDatabaseReference=databaseReference.child("users").child(mAuth.getCurrentUser().getUid());

        (userDatabaseReference.child("name")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName=dataSnapshot.getValue(String.class);
                Toast.makeText(HomeActivity.this, "welcome "+userName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        signOutBtn.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.sign_out_btn){

            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }
}
