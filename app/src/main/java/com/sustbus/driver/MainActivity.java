package com.sustbus.driver;

import androidx.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_main);


        userNameEt = findViewById(R.id.username_tf);
        emailEt = findViewById(R.id.email_tf);
        passwordEt = findViewById(R.id.password_tf);
        signUpBtn = findViewById(R.id.signup_btn);
        signInTv = findViewById(R.id.sign_in_tv);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("users");
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


        mAuth=FirebaseAuth.getInstance();

             if(mAuth.getCurrentUser()!=null) {
                 startActivity(new Intent(MainActivity.this, HomeActivity.class));
                 finish();
             }
    }

    /**
     * end of on create
     **/

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.sign_in_tv) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else if (i == R.id.signup_btn) {


            if (emailOk && userNameOk && passwordOk) {


                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {

                            String uid=mAuth.getCurrentUser().getUid();

                            DatabaseReference childDb=databaseReference.child(uid);
                            childDb.child("name").setValue(userName);
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(MainActivity.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            } else {
                Toast.makeText(MainActivity.this, "please enter the fields correctly", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void emailIdValidator(String theNewText) {
        if (Patterns.EMAIL_ADDRESS.matcher(theNewText).matches()) {
            emailEt.setHelperText(" ");
            emailEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.sust));
            emailEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.sust));
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

    private void userNameValidator(String theNewText) {
        if (theNewText.length() < 4) {

            userNameEt.setHelperText("user name must contain 6 letters at least");
            userNameEt.setHelperTextColor(ContextCompat.getColor(com.sustbus.driver.MainActivity.this, R.color.A400red));
            userNameEt.setCounterTextColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            userNameEt.setPrimaryColor(ContextCompat.getColor(MainActivity.this, R.color.A400red));
            userNameOk = false;
        } else if (theNewText.contains(".") ||
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

    private void passwordValidator(String theNewText) {
        if (theNewText.length() < 6) {
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


}