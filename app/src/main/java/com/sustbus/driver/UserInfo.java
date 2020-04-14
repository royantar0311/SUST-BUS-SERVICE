package com.sustbus.driver;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {
    private String email;
    private boolean isStudentPermitted;
    private String userName;
    boolean isDriver;
    private Double lat;
    private Double lang;
    private DatabaseReference databaseReference;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference userLocationData;
    private FirebaseAuth mAuth;
    private String uId;
    private static UserInfo instance;

    public static UserInfo getInstance(){
        if(instance == null){
            instance = new UserInfo();
        }
        return instance;
    }

    public UserInfo() {
    }

    /**Private method needed for the builder
     * class to store them on the main userinfo
     * object after getting all the data*/

    UserInfo userInfo(Builder builder) {
        instance.email = builder.email;
        instance.isStudentPermitted = builder.isStudentPermitted;
        instance.userName = builder.userName;
        instance.isDriver = builder.isDriver;
        instance.lat = builder.lat;
        instance.lang = builder.lang;
        instance.uId = builder.uId;
        return this;
    }

    /**This method creates a object of the inner Builder class */

    public  Builder getBuilder(){
        return new Builder();
    }

    /**Builder pattern to build a Userinfo*/

    public class Builder{
        String email;
        boolean isStudentPermitted;
        String userName;
        boolean isDriver;
        Double lat;
        Double lang;
        String uId;

        Builder(){}

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setIsStudentPermitted(boolean isStudentPermitted) {
            this.isStudentPermitted = isStudentPermitted;
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
        public Builder setUId(String uId) {
            this.uId = uId;
            return this;
        }

        public UserInfo build(){
            return userInfo(this);
        }
    }

    public Map<String, Object> toMap(){
        Map<String, Object>map = new HashMap<>();
        map.put("email", email);
        map.put("isStudentPermitted",isStudentPermitted);
        map.put("userName", userName);
        map.put("isDriver", isDriver);
        map.put("lat",lat);
        map.put("lang",lang);
        return map;
    }

    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public String getEmail() {
        return email;
    }

    public boolean getIsStudentPermitted() {
        return isStudentPermitted;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isDriver() {
        return isDriver;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLang() {
        return lang;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsStudentPermitted(boolean isStudentPermitted) {
        this.isStudentPermitted =isStudentPermitted;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDriver(boolean driver) {
        isDriver = driver;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLang(Double lang) {
        this.lang = lang;
    }

    public void setLatLang(Double Lat, Double Lang){
        this.lat = lat;
        this.lang = lang;
    }

}
