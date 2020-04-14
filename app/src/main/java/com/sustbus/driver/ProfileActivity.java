package com.sustbus.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.dd.CircularProgressButton;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private UserInfo userInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userInfo = UserInfo.getInstance();

    }

    @Override
    public void onClick(View view) {

    }
}
