package com.sustbus.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginBtn;
    private TextView signUpTv;
    private TextFieldBoxes emailEt;
    private TextFieldBoxes passwordEt;
    boolean emailOk = false;
    private String email;

    private String password;
    boolean passwordOk = false;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.username_tf);
        passwordEt = findViewById(R.id.password_tf);
        loginBtn = findViewById(R.id.login_btn);
        signUpTv = findViewById(R.id.sign_up_tv);

        progressDialog=new ProgressDialog(this);

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

    @Override
    public void onClick(View view) {
        int i=view.getId();
        if(i == R.id.sign_up_tv){

            Intent intent=new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        }
        else if(i == R.id.login_btn){

            if( emailOk && passwordOk) {

                FirebaseAuth mAuth=FirebaseAuth.getInstance();
                progressDialog.setMessage("Logging in");
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            progressDialog.hide();
                            Intent intent=new Intent(LoginActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(intent);
                            finish();
                        }
                        else{
                            progressDialog.hide();
                            passwordEt.setHelperText("Wrong Email id or Password");
                            passwordEt.setHelperTextColor(ContextCompat.getColor(com.sustbus.driver.LoginActivity.this, R.color.A400red));
                        }
                    }
                });

            }
            else {
                Toast.makeText(LoginActivity.this, "please enter the fields correctly", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void emailIdValidator(String theNewText) {
        if (Patterns.EMAIL_ADDRESS.matcher(theNewText).matches()) {
            emailEt.setHelperText(" ");
            emailEt.setCounterTextColor(ContextCompat.getColor(this, R.color.sust));
            emailEt.setPrimaryColor(ContextCompat.getColor(this, R.color.sust));
            email = theNewText;
            emailOk = true;

        } else {
            emailEt.setHelperText("please enter a valid email adress");
            emailEt.setHelperTextColor(ContextCompat.getColor(this, R.color.A400red));
            emailEt.setCounterTextColor(ContextCompat.getColor(this, R.color.A400red));
            emailEt.setPrimaryColor(ContextCompat.getColor(this, R.color.A400red));
            emailOk = false;
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

}
