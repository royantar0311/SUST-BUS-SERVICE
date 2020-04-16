package com.sustbus.driver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";
    private static final int REQUESTING_DP = 1001;

    private Bitmap dpBitmap;
    private UserInfo userInfo;
    private Button updateProfileBtn;
    private Button dpChooserBtn;
    private Button idChooserBtn;
    private EditText userNameEt;
    private EditText regiNoEt;
    private CircleImageView dpEv;
    private TextFieldBoxes userNameTf;
    private TextFieldBoxes regiNoTf;
    private String userName=null,regiNo=null;
    private boolean userNameOk;
    private boolean regiNoOk;
    private FirebaseFirestore db;
    private Uri filePath = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        updateProfileBtn = findViewById(R.id.update_profile_btn);
        userNameTf = findViewById(R.id.profile_username_tf);
        regiNoTf = findViewById(R.id.profile_regino_tf);
        userNameEt = findViewById(R.id.profile_username_et);
        regiNoEt = findViewById(R.id.profile_regino_et);
        dpChooserBtn = findViewById(R.id.dp_chooser_button);
        dpEv = findViewById(R.id.profile_user_image_ev);

        userInfo = UserInfo.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameTf.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                userNameValidator(theNewText);
            }
        });
        regiNoTf.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                regiNoValidator(theNewText);
            }
        });

        updateProfileBtn.setOnClickListener(this);
        dpChooserBtn.setOnClickListener(this);

        db.collection("users")
                .document(userInfo.getuId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Log.d(TAG, "Current data on Changed: on ProfileActivty" + documentSnapshot.getData());

                            UserInfo.setInstance(documentSnapshot.toObject(UserInfo.class));
                            userInfo=UserInfo.getInstance();

                            update();

                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });
        if(userInfo.getUrl() != null){
            Glide.with(ProfileActivity.this)
                    .load(userInfo.getUrl())
                    .into(dpEv);
        }
    }

    private void update() {
        if(userInfo.getIsStudentPermitted() == UserInfo.STUDENT_NOT_PERMITTED){
            updateProfileBtn.setEnabled(true);
            updateProfileBtn.setText("Request Permission");
            updateProfileBtn.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.custom_button));

        }
        else if(userInfo.getIsStudentPermitted() == UserInfo.PERMISSION_PENDING){
            updateProfileBtn.setEnabled(false);
            updateProfileBtn.setText("your request is being proceed...");
            updateProfileBtn.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.custom_button));
        }
        else {
            updateProfileBtn.setEnabled(true);
            updateProfileBtn.setText("Update Profile");
            updateProfileBtn.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.custom_button));

        }
        userNameEt.setText(userInfo.getUserName());
        regiNoEt.setText(userInfo.getRegiNo());
    }

    private void userNameValidator(String theNewText)   {
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

            if(filePath != null){
                StorageReference storageReference =FirebaseStorage.getInstance()
                        .getReference()
                        .child("users images")
                        .child(userInfo.getuId())
                        .child("dp.jpg");
                        storageReference.putFile(filePath);
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                userInfo.setUrl(uri.toString());
                                Log.d(TAG, "onSuccess: " + uri.toString());
                            }
                        });

            }

            if(userNameOk && regiNoOk) {
                if (userInfo.getIsStudentPermitted() == UserInfo.STUDENT_NOT_PERMITTED) {
                    Log.d(TAG, "onClick: update_profile_button -> asking for permission");
                    userInfo.setIsStudentPermitted(UserInfo.PERMISSION_PENDING);
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
        else if(i == R.id.dp_chooser_button){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Seclect Profile Photo"),REQUESTING_DP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUESTING_DP && resultCode==RESULT_OK &&
                data != null && data.getData() != null){
            Log.d(TAG, "onActivityResult: " + data);
            filePath = data.getData();
            try {
                dpBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                dpBitmap = Bitmap.createScaledBitmap(dpBitmap,125, 125, true);
                Glide.with(this)
                        .asBitmap()
                        .load(filePath)
                        .apply(new RequestOptions()
                                .centerCrop()
                                .error(R.drawable.user_not_available_image)
                                .fallback(R.drawable.user_not_available_image)
                                .error(R.drawable.user_not_available_image))
                        .into(dpEv);
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
