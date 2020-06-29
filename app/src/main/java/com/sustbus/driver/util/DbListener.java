package com.sustbus.driver.util;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class DbListener implements Runnable {
    private static final String TAG = "getFromDB";
    UserInfo userInfo =  UserInfo.getInstance();
    CallBack callBack;
    public DbListener(CallBack callBack){
        this.callBack = callBack;
    }
    @Override
    public void run() {
        Log.d(TAG, "run: " + Thread.currentThread().getName());
        Log.d(TAG, "run: " + userInfo.getuId());
        FirebaseFirestore.getInstance().collection("users").document(userInfo.getuId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        if (snapshot != null || snapshot.exists()) {
                            UserInfo.setInstance(snapshot.toObject(UserInfo.class));
                            userInfo = UserInfo.getInstance();
                            callBack.ok();
                            //Log.d(TAG, userInfo.toString());
                            Log.d(TAG, "onEvent: " + userInfo.toString());
                            //if (!userInfo.isProfileCompleted() || !userInfo.isPermitted()) {
//                                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                startActivity(intent);
//                                finish();
                            //}
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });
    }
}