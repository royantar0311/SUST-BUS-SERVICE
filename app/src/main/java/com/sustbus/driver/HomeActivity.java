package com.sustbus.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";


    private TextView userNameTv;
    private TextView passwordTv;
    private Button signOutBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        userNameTv = findViewById(R.id.username_tv);
        passwordTv = findViewById(R.id.password_tv);
        signOutBtn = findViewById(R.id.sign_out_btn);


        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            userNameTv.setText("username :" + bundle.get("username"));
            passwordTv.setText("password :" + bundle.get("password"));
        }
        signOutBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.sign_out_btn){
           // Toast.makeText(HomeActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        }
    }
}
