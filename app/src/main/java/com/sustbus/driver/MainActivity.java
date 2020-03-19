package com.sustbus.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private TextView signInTv;
    private Button signUpBtn;
    private TextFieldBoxes userNameEt;
    private TextFieldBoxes emailEt;
    private TextFieldBoxes passwordEt;
    private String userName;
    private String email;
    private String password;
    boolean userNameOk = false;
    boolean emailOk = false;
    boolean passwordOk = false;
    private Intent intent;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN
                );
        setContentView(R.layout.activity_main);


        userNameEt = findViewById(R.id.username_tf);
        emailEt = findViewById(R.id.email_tf);
        passwordEt = findViewById(R.id.password_tf);
        signUpBtn = findViewById(R.id.signup_btn);
        signInTv = findViewById(R.id.sign_in_tv);

        bundle = new Bundle();

        emailEt.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                emailIdValidator(theNewText);
            }
        });
        userNameEt.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                userNameValidator(theNewText);
            }
        });
        passwordEt.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                passwordValidator(theNewText);
            }
        });
        signUpBtn.setOnClickListener(this);
        signInTv.setOnClickListener(this);
    }

    /**
     * end of on create
    **/


    private void emailIdValidator(String theNewText) {
        if (Patterns.EMAIL_ADDRESS.matcher(theNewText).matches()) {
            emailEt.setHelperText(" ");
            emailEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.sust));
            emailEt.setPrimaryColor(ContextCompat.getColor( MainActivity.this,R.color.sust));
            email = theNewText;
            emailOk = true;

        } else {
            emailEt.setHelperText("please enter a valid email adress");
            emailEt.setHelperTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            emailEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            emailEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            emailOk = false;
        }
    }
    private void userNameValidator(String theNewText){
        if (theNewText.length() < 4 ) {

            userNameEt.setHelperText("user name must contain 6 letters at least");
            userNameEt.setHelperTextColor(ContextCompat.getColor(com.sustbus.driver.MainActivity.this, R.color.A400red));
            userNameEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            userNameEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            userNameOk = false;
        }
        else if (theNewText.contains(".") ||
                theNewText.contains("$") ||
                theNewText.contains("[") ||
                theNewText.contains("]") ||
                theNewText.contains("#") ||
                theNewText.contains("/")) {

            userNameEt.setHelperText("\".$[]#/\" are not allowed");
            userNameEt.setHelperTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            userNameEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            userNameEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            userNameOk = false;

        } else {
            userNameEt.setHelperText(" ");
            userNameEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.sust));
            userNameEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.sust));
            userName = theNewText;
            userNameOk = true;
        }
    }
    private void passwordValidator(String theNewText){
        if(theNewText.length()<6 ){
            passwordEt.setHelperText("user name must contain 6 letters at least");
            passwordEt.setHelperTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            passwordEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            passwordEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            passwordOk = false;
        } else {
            passwordEt.setHelperText(" ");
            passwordEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.sust));
            passwordEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.sust));
            password = theNewText;
            passwordOk = true;
        }
    }

    @Override
    public void onClick(View view) {
        int i=view.getId();
        if(i == R.id.sign_in_tv){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else if(i == R.id.signup_btn){


            if(emailOk && userNameOk && passwordOk) {
                finish();
                intent = new Intent(MainActivity.this, HomeActivity.class);
                bundle.putString("email", email);
                bundle.putString("username", userName);
                bundle.putString("password", password);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            else {
                Toast.makeText(MainActivity.this, "please enter the fields correctly", Toast.LENGTH_SHORT).show();
            }
        }
    }
}