package com.sustbus.driver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserInfo userInfo;

    private ProgressDialog progressDialog;

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

        progressDialog=new ProgressDialog(this);

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

        userInfo = UserInfo.getInstance();
    }

    /**
     * end of on create
     **/

    protected void onStart(){
        super.onStart();
        db = FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.sign_in_tv) {

            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();



        }
        else if (i == R.id.signup_btn) {


            if (emailOk && userNameOk && passwordOk) {

                progressDialog.setMessage("Register in progress");
                progressDialog.setCancelable(false);
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {
                            progressDialog.hide();
                            UserInfo.getBuilder()
                                    .setDriver(false)
                                    .setIsStudentPermitted(UserInfo.STUDENT_NOT_PERMITTED)
                                    .setEmail(email)
                                    .setuId(mAuth.getCurrentUser().getUid())
                                    .setUserName(userName)
                                    .build();
                            db.collection("users")
                                    .document(userInfo.getuId())
                                    .set(userInfo.toMap());
                            Intent intent=new Intent(MainActivity.this,HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            progressDialog.hide();
                            String error=task.getException().getMessage();

                            if(error.contains("email")){
                                emailEt.setHelperText(error);
                            }
                            else if(error.contains("password")){
                                passwordEt.setHelperText(error);
                            }
                            else{
                                Toast.makeText(MainActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });

            } else {
                Snackbar.make(findViewById(R.id.main_relative_layout),"Please enter all the fields correctly",Snackbar.LENGTH_SHORT).show();
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