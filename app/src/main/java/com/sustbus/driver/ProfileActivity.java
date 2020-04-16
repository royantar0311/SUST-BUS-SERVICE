package com.sustbus.driver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";
    
    private UserInfo userInfo;
    private Button updateProfileBtn;
    private TextFieldBoxes userNameTf;
    private TextFieldBoxes regiNoTf;
    private String userName=null,regiNo=null;
    private boolean userNameOk;
    private boolean regiNoOk;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        updateProfileBtn = findViewById(R.id.update_profile_btn);
        userNameTf = findViewById(R.id.profile_username_tf);
        regiNoTf = findViewById(R.id.profile_regino_tf);

        userInfo = UserInfo.getInstance();
        Log.d(TAG, "userInfo new datas"
                + "\nisDriver " + userInfo.isDriver()
                + "\nuid " + userInfo.getuId()
                + "\nispermitted " + userInfo.getIsStudentPermitted()
                + "\nemail " + userInfo.getEmail()
        );

        userNameTf.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                updateProfileBtn.setEnabled(true);
                userNameValidator(theNewText);
            }
        });
        regiNoTf.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                updateProfileBtn.setEnabled(true);
                regiNoValidator(theNewText);
            }
        });
        updateProfileBtn.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();



    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userInfo.getuId()+"/isStudentPermitted")
        if(userInfo.getIsStudentPermitted() == UserInfo.STUDENT_NOT_PERMITTED){
            updateProfileBtn.setText("Request Permission");
        }
        else if(userInfo.getIsStudentPermitted() == UserInfo.PERMISSION_PENDING){
            updateProfileBtn.setEnabled(false);
            updateProfileBtn.setText("your request is being proceed");
        }
        else {
            updateProfileBtn.setText("Update Profile");
        }
        updateProfileBtn.setEnabled(false);

    }

    private void userNameValidator(String theNewText) {
        if (theNewText.length() < 4) {
            userNameTf.setHelperText("must contain 6 letters at least");
            userNameTf.setHelperTextColor(ContextCompat.getColor(com.sustbus.driver.ProfileActivity.this, R.color.A400red));
            userNameTf.setCounterTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            userNameTf.setPrimaryColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            userNameOk = false;
        } else {
            userNameTf.setHelperText(" ");
            userNameTf.setCounterTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.sust));
            userNameTf.setPrimaryColor(ContextCompat.getColor(ProfileActivity.this, R.color.sust));
            userNameOk = true;
            userName = theNewText;
        }
    }
    private void regiNoValidator(String theNewText) {
        if (theNewText.length() < 10) {
            regiNoTf.setHelperText("registration number must contain 10 numbers");
            regiNoTf.setHelperTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            regiNoTf.setCounterTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            regiNoTf.setPrimaryColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            regiNoOk = false;
        } else {
            regiNoTf.setHelperText("");
            regiNoTf.setCounterTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.sust));
            regiNoTf.setPrimaryColor(ContextCompat.getColor(ProfileActivity.this, R.color.sust));
            regiNo = theNewText;
            regiNoOk = true;
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.update_profile_btn){
            if(userNameOk && regiNoOk) {
                if (userInfo.getIsStudentPermitted() == UserInfo.STUDENT_NOT_PERMITTED) {
                    Log.d(TAG, "onClick: update_profile_button -> asking for permission");
                    userInfo.setIsStudentPermitted(UserInfo.PERMISSION_PENDING);
                    updateProfileBtn.setEnabled(false);
                    updateProfileBtn.setBackgroundColor();
                } else {
                    Log.d(TAG, "onClick: update_profile_button -> updating profile");
                }

                userInfo.setUserName(userName);
                userInfo.setRegiNo(regiNo);

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userInfo.getuId())
                        .update(userInfo.toMap());
            }
            else {
                Toast.makeText(ProfileActivity.this, "enter all fields correctly", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
