package com.sustbus.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginBtn;
    private TextView signUpTv;
    private TextFieldBoxes userNameEt;
    private TextFieldBoxes passwordEt;
    boolean userNameOk = false;
    private String userName;
    private String password;
    boolean passwordOk = false;
    private Intent intent;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEt = findViewById(R.id.username_tf);
        passwordEt = findViewById(R.id.password_tf);
        loginBtn = findViewById(R.id.login_btn);
        signUpTv = findViewById(R.id.sign_up_tv);



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

        signUpTv.setOnClickListener(this);
        loginBtn.setOnClickListener(this);


    }
    private void userNameValidator(String theNewText){
        if (theNewText.length() < 4 ) {

            userNameEt.setHelperText("user name must contain 6 letters at least");
            userNameEt.setHelperTextColor(ContextCompat.getColor(com.sustbus.driver.LoginActivity.this, R.color.A400red));
            userNameEt.setCounterTextColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            userNameEt.setPrimaryColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            userNameOk = false;
        }
        else if (theNewText.contains(".") ||
                theNewText.contains("$") ||
                theNewText.contains("[") ||
                theNewText.contains("]") ||
                theNewText.contains("#") ||
                theNewText.contains("/")) {

            userNameEt.setHelperText("\".$[]#/\" are not allowed");
            userNameEt.setHelperTextColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            userNameEt.setCounterTextColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            userNameEt.setPrimaryColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            userNameOk = false;

        } else {
            userNameEt.setHelperText(" ");
            userNameEt.setCounterTextColor(ContextCompat.getColor(LoginActivity.this, R.color.sust));
            userNameEt.setPrimaryColor(ContextCompat.getColor(LoginActivity.this, R.color.sust));
            userName = theNewText;
            userNameOk = true;
        }
    }

    private void passwordValidator(String theNewText){
        if(theNewText.length()<6 ){
            passwordEt.setHelperText("user name must contain 6 letters at least");
            passwordEt.setHelperTextColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            passwordEt.setCounterTextColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            passwordEt.setPrimaryColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            passwordOk = false;
        } else {
            passwordEt.setHelperText(" ");
            passwordEt.setCounterTextColor(ContextCompat.getColor(LoginActivity.this, R.color.sust));
            passwordEt.setPrimaryColor(ContextCompat.getColor(LoginActivity.this, R.color.sust));
            password = theNewText;
            passwordOk = true;
        }
    }

    @Override
    public void onClick(View view) {
        int i=view.getId();
        if(i == R.id.sign_up_tv){
            finish();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
        else if(i == R.id.login_btn){
            finish();
            if( userNameOk && passwordOk) {
                finish();
                bundle = new Bundle();
                intent = new Intent(LoginActivity.this, HomeActivity.class);
                bundle.putString("username", userName);
                bundle.putString("password", password);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(LoginActivity.this, "please enter the fields correctly", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
