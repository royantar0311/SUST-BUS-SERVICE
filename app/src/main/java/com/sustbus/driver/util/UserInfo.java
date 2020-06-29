package com.sustbus.driver.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    public static final boolean PERMITTED = true;
    public static final boolean NOT_PERMITTED = false;
    public static final int PERMISSION_PENDING = -1;
    private static final String TAG = "UserInfo";
    public static Builder builder = new Builder();
    private static UserInfo instance;
    private String email;
    private boolean permitted;
    private String userName;
    private boolean driver;
    private boolean profileCompleted;
    private Double lat;
    private Double lang;
    private String uId;
    private String regiNo;
    private String url;
    private String idUrl;
    public UserInfo() {
    }
    public static UserInfo getInstance() {
        if(instance == null){
            instance = new UserInfo();
        }
        return instance;
    }

    public static void setInstance(UserInfo i) {
        instance = i;
        Log.d(TAG, "setInstance: " + i.toString());
    }
    public void updateToDbase(CallBack callBack)  {
        Log.d(TAG, "updateToDbase: " + Thread.currentThread().getName());
        Thread t1 = new Thread(() ->{
            Log.d(TAG, "updateToDbase: " + Thread.currentThread().getName());
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(this.uId)
                    .update(this.toMap())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            callBack.ok();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callBack.notOk();
                        }
                    });
        });
        t1.start();
    }


    /**
     * Private method needed for the builder
     * class to store them on the main userinfo
     * object after getting all the data
     */

    static UserInfo userInfo(Builder builder) {
        instance.email = builder.email;
        instance.permitted = builder.permitted;
        instance.userName = builder.userName;
        instance.driver = builder.isDriver;
        instance.lat = builder.lat;
        instance.lang = builder.lang;
        instance.uId = builder.uId;
        return instance;
    }

    /**
     * This method creates a object of the inner Builder class
     */

    public static Builder getBuilder() {
        return builder;
    }

    /**
     * Creating a map of all the data to push to the database
     */

    public void reset() {
        email = null;
        userName = null;
        uId = null;
        regiNo = null;
        profileCompleted = false;
        idUrl = null;
        permitted = false;
        driver = false;
        url = null;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("permitted", permitted);
        map.put("profileCompleted", profileCompleted);
        map.put("userName", userName);
        map.put("driver", driver);
        map.put("uId", uId);
        map.put("regiNo", regiNo);
        map.put("url", url);
        map.put("idUrl", idUrl);
        return map;
    }

    public String toString() {
        return "userInfo new datas"
                + "\niDurl " + instance.getIdUrl()
                + "\nisDriver " + instance.isDriver()
                + "\nuid " + instance.getuId()
                + "\nisPermitted " + instance.isPermitted()
                + "\nisProfileCompleted " + instance.isProfileCompleted()
                + "\nemail " + instance.getEmail()
                + "\nurl " + instance.getUrl()
                + "\nregiNO " + instance.regiNo
                + "\nuserName " + instance.userName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRegiNo() {
        return regiNo;
    }

    public void setRegiNo(String regiNo) {
        this.regiNo = regiNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPermitted() {
        return permitted;
    }

    public void setPermitted(boolean permitted) {
        this.permitted = permitted;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isDriver() {
        return driver;
    }

    public void setDriver(boolean driver) {
        this.driver = driver;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setLatLng(double lat, double lang) {
        this.lat = lat;
        this.lang = lang;
    }

    public String getIdUrl() {
        return idUrl;
    }

    public void setIdUrl(String idUrl) {
        this.idUrl = idUrl;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public LatLng getLatLang() {
        return new LatLng(lat, lang);
    }

    /**
     * Builder pattern to build a Userinfo
     */

    public static class Builder {
        String email;
        boolean permitted;
        String userName;
        boolean isDriver;
        Double lat;
        Double lang;
        String uId;
        boolean profileCompleted;

        Builder() {
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPermitted(boolean permitted) {
            this.permitted = permitted;
            return this;
        }

        public Builder setProfileCompleted(boolean profileCompleted) {
            this.profileCompleted = profileCompleted;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setDriver(boolean driver) {
            isDriver = driver;
            return this;
        }

        public Builder setLat(Double lat) {
            this.lat = lat;
            return this;
        }

        public Builder setLang(Double lang) {
            this.lang = lang;
            return this;
        }

        public Builder setuId(String uId) {
            this.uId = uId;
            return this;
        }

        public UserInfo build() {
            return userInfo(builder);
        }
    }

}

