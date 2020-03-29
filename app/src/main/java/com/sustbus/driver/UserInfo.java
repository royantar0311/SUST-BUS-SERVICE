package com.sustbus.driver;

import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public  class UserInfo {
    String email;
    boolean isStudentPermitted;
    String userName;
    boolean isDriver;
    Double lat;
    Double lang;

    private static  UserInfo theInfo=new UserInfo();

    public static UserInfo getInstance(){
        return theInfo;
    }

    public UserInfo() {
    }

    private UserInfo(Builder builder) {
        theInfo.email=this.email = builder.email;
        theInfo.isStudentPermitted=this.isStudentPermitted = builder.isStudentPermitted;
        theInfo.userName=this.userName = builder.userName;
        theInfo.isDriver=this.isDriver = builder.isDriver;
        theInfo.lat=this.lat = builder.lat;
        theInfo.lang=this.lang = builder.lang;
    }

    public static Builder getBuilder(){
        return new Builder();
    }

    public static class Builder{
        String email;
        boolean isStudentPermitted;
        String userName;
        boolean isDriver;
        Double lat;
        Double lang;
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
        public UserInfo build(){
            return new UserInfo(this);
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
