package com.sustbus.driver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sustbus.driver.util.UserInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.opencensus.stats.Measure;
import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";
    private static final int REQUESTING_DP = 1001;
    private static final int REQUESTING_ID = 1002;

    private UserInfo userInfo;
    private Button updateProfileBtn;
    private Button dpChooserBtn;
    private Button idChooserBtn;
    private EditText userNameEt;
    private EditText regiNoEt;
    private TextView idAvailibilityTv;
    private StorageReference storageReference;
    private CircleImageView dpEv;
    private TextFieldBoxes userNameTf;
    private TextFieldBoxes regiNoTf;
    private String userName = null, regiNo = null;
    private boolean userNameOk;
    private boolean regiNoOk;
    private DocumentReference db;
    private byte[] dpBytes,idBytes;
    private Uri dpFilePath = null, idFilePath = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_v2);

        updateProfileBtn = findViewById(R.id.update_profile_btn);
        userNameTf = findViewById(R.id.profile_username_tf);
        regiNoTf = findViewById(R.id.profile_regino_tf);
        userNameEt = findViewById(R.id.profile_username_et);
        regiNoEt = findViewById(R.id.profile_regino_et);
        dpChooserBtn = findViewById(R.id.dp_chooser_button);
        dpEv = findViewById(R.id.profile_user_image_ev);
        idChooserBtn = findViewById(R.id.profile_id_chooser_btn);
        idAvailibilityTv = findViewById(R.id.profile_id_availability);

        userInfo = UserInfo.getInstance();
        db = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userInfo.getuId());
        storageReference = FirebaseStorage.getInstance()
                .getReference()
                .child("users images")
                .child(userInfo.getuId());

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

        progressDialog = new ProgressDialog(this);
        loadImage();
    }

    private void loadImage() {
        if (userInfo != null && userInfo.getUrl() != null) {
            Glide.with(ProfileActivity.this)
                    .load(userInfo.getUrl())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .apply(new RequestOptions().placeholder(R.drawable.loading))
                    .into(dpEv);
        }
    }


    private void userNameValidator(String theNewText) {
        userName = theNewText;
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
        }
    }

    private void regiNoValidator(String theNewText) {
        regiNo = theNewText;
        if (theNewText.length() != 10) {
            regiNoTf.setHelperText("registration number must contain 10 numbers");
            regiNoTf.setHelperTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            regiNoTf.setCounterTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            regiNoTf.setPrimaryColor(ContextCompat.getColor(ProfileActivity.this, R.color.A400red));
            regiNoOk = false;
        } else {
            regiNoTf.setHelperText("");
            regiNoTf.setCounterTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.sust));
            regiNoTf.setPrimaryColor(ContextCompat.getColor(ProfileActivity.this, R.color.sust));
            regiNoOk = true;
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.update_profile_btn) {
            Log.d(TAG, "onClick: " + "update button clicked ");
            progressDialog.setMessage("Updating Profile");
            progressDialog.setCancelable(false);
            progressDialog.show();
            if (userNameOk && regiNoOk &&
                    (idFilePath != null) == (userInfo.getIsStudentPermitted() == UserInfo.STUDENT_NOT_PERMITTED)) {

                if (dpFilePath != null) {
                    Log.d(TAG, "onClick: filepath not null, file upload initializing");
                    BackgroundImageUpload backgroundImageUpload = new BackgroundImageUpload();
                    backgroundImageUpload.execute(dpFilePath);
                    Log.d(TAG, "onClick: here");
                }
                if (idFilePath != null) {
                    storageReference.child("id.jpg").putFile(idFilePath);
                    storageReference.child("id.jpg").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "onSuccess: id upload successful");
                                }
                            });
                }
                updateRestOfTheData();
            } else {
                progressDialog.hide();
                Toast.makeText(ProfileActivity.this, "enter all fields correctly", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateRestOfTheData() {
        if (userInfo.getIsStudentPermitted() == UserInfo.STUDENT_NOT_PERMITTED) {
            Log.d(TAG, "onClick: update_profile_button -> asking for permission");
            userInfo.setIsStudentPermitted(UserInfo.PERMISSION_PENDING);
        } else {
            Log.d(TAG, "onClick: update_profile_button -> updating profile");
        }
        userInfo.setUserName(userName);
        userInfo.setRegiNo(regiNo);
        Log.d(TAG, userInfo.toString());
        db.update(userInfo.toMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: " + "profile updated");
                        progressDialog.hide();
                        if (userInfo.getIsStudentPermitted() == UserInfo.PERMISSION_PENDING) {
                            permissionPending();
                        } else if (userInfo.getIsStudentPermitted() == UserInfo.STUDENT_PERMITTED) {
                            permitted();
                        }
                        Toast.makeText(ProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTING_DP && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult: " + data + "dp fetched");
            dpFilePath = data.getData();
            Glide.with(this)
                    .load(dpFilePath)
                    .into(dpEv);
        } else if (requestCode == REQUESTING_ID && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult: " + data + "id fetched");
            idFilePath = data.getData();
        }
        userNameEt.setText(userName);
        regiNoEt.setText(regiNo);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (userInfo.getIsStudentPermitted() == UserInfo.STUDENT_NOT_PERMITTED) {
            updateProfileBtn.setEnabled(true);
            updateProfileBtn.setText("Request Permission");
            updateProfileBtn.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.custom_button));

        } else if (userInfo.getIsStudentPermitted() == UserInfo.PERMISSION_PENDING) {
            permissionPending();
        } else if (userInfo.getIsStudentPermitted() == UserInfo.STUDENT_PERMITTED) {
            permitted();
        }
        userName = userInfo.getUserName();
        regiNo = userInfo.getRegiNo();
        userNameEt.setText(userName);
        regiNoEt.setText(regiNo);
    }

    private void permitted() {
        updateProfileBtn.setEnabled(true);
        updateProfileBtn.setText("Update Profile");
        idAvailibilityTv.setText("ID Confirmed");
        idChooserBtn.setVisibility(View.GONE);
        updateProfileBtn.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.custom_button));
    }

    private void permissionPending() {
        updateProfileBtn.setEnabled(false);
        idChooserBtn.setVisibility(View.GONE);
        idAvailibilityTv.setText(" ID will be checked manually");
        updateProfileBtn.setText("your request is being proceed...");
        updateProfileBtn.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.custom_button));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backButtonPressed(null);
    }
    public void backButtonPressed(View view) {
        finish();
    }

    public void idChooserButtonPressed(View view) {
        Log.d(TAG, "onClick: " + "id chooser button clicked");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select ID"), REQUESTING_ID);
    }

    public void dpChooserButtonPressed(View view) {
        Log.d(TAG, "dpChooserButtonPressed: ");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Photo"), REQUESTING_DP);
    }
    public class BackgroundImageUpload extends AsyncTask<Uri, Integer, byte[]>{
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute: ");
        }



        @Override
        protected byte[] doInBackground(Uri... uris) {
            Log.d(TAG, "doInBackground: started");

            if(bitmap == null){
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(ProfileActivity.this.getContentResolver(),uris[0]);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                
            }
            byte[] bytes;
            bytes = getBytesFromBitmap(bitmap,20);
            return bytes;
        }
        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            Log.d(TAG, "onPostExecute: done " + bytes.length);
            UploadTask uploadTask = storageReference.child("dp.jpg").putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: dp compressed and uploaded");
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UserInfo.downNeeded=true;
                            userInfo.setUrl(uri.toString());
                        }
                    });
                }
            });
        }
    }
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality,stream);
        return stream.toByteArray();
    }
}
