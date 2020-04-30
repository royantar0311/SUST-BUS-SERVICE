package com.sustbus.driver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sustbus.driver.util.UserInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    boolean emailOk = false;
    boolean passwordOk = false;
    private Button loginBtn;
    private TextView forgotPasswordTv;
    private TextView signUpTv;
    private TextFieldBoxes emailEt;
    private TextFieldBoxes passwordEt;
    private String email;
    private String password;
    private ProgressDialog progressDialog;
    private String message;
    UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.username_tf);
        passwordEt = findViewById(R.id.password_tf);
        loginBtn = findViewById(R.id.login_btn);
        signUpTv = findViewById(R.id.sign_up_tv);
        forgotPasswordTv = findViewById(R.id.forgot_password_tv);


        progressDialog = new ProgressDialog(this);
        userInfo = UserInfo.getInstance();
        userInfo.reset();
        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initForgotPasswordAD();
            }
        });

        emailEt.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                emailIdValidator(theNewText);
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

    private void initForgotPasswordAD() {
        EditText editText = new EditText(getApplicationContext());
        editText.setSingleLine(true);
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Reset Password")
                .setMessage("enter your email")
                .setView(editText)
                .setPositiveButton("send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = editText.getText().toString().trim();
                        message = "please enter a valid email address";
                        if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                            return;
                        }
                            Log.d(TAG, "onClick: " + email);
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "onComplete: here ");
                                            message = "please check your E-mail";
                                            if (!task.isSuccessful()) {
                                                message = task.getException().getMessage();
                                            }
                                            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                                        }
                                    });
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.sign_up_tv) {

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        } else if (i == R.id.login_btn) {

            if (emailOk && passwordOk) {

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                progressDialog.setMessage("Logging in");
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userInfo.setuId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            FirebaseFirestore.getInstance().collection("users").document(userInfo.getuId())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    progressDialog.hide();
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    if(task.isSuccessful() && task.getResult()!=null && task.getResult().exists()){
                                        UserInfo.setInstance(Objects.requireNonNull(task.getResult().toObject(UserInfo.class)));
                                    }
                                    startActivity(intent);
                                    finish();

                                }
                            });
                        } else {

                            progressDialog.hide();
                            passwordEt.setHelperText(task.getException().getMessage());
                            passwordEt.setHelperTextColor(ContextCompat.getColor(com.sustbus.driver.LoginActivity.this, R.color.A400red));
                        }
                    }
                });

            } else {
                Toast.makeText(LoginActivity.this, "please enter the fields correctly", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void emailIdValidator(String theNewText) {
        if (Patterns.EMAIL_ADDRESS.matcher(theNewText).matches()) {
            emailEt.setHelperText(" ");
            emailEt.setPrimaryColor(ContextCompat.getColor(this, R.color.sust));
            email = theNewText;
            emailOk = true;

        } else {
            emailEt.setHelperText("please enter a valid email adress");
            emailEt.setHelperTextColor(ContextCompat.getColor(this, R.color.A400red));
            emailEt.setPrimaryColor(ContextCompat.getColor(this, R.color.A400red));
            emailOk = false;
        }
    }
    private void passwordValidator(String theNewText) {
        if (theNewText.length() < 6) {
            passwordEt.setHelperText("user name must contain 6 letters at least");
            passwordEt.setHelperTextColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            passwordEt.setPrimaryColor(ContextCompat.getColor(LoginActivity.this, R.color.A400red));
            passwordOk = false;
        } else {
            passwordEt.setHelperText(" ");
            passwordEt.setPrimaryColor(ContextCompat.getColor(LoginActivity.this, R.color.sust));
            password = theNewText;
            passwordOk = true;
        }
    }

}
