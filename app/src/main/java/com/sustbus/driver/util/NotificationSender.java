package com.sustbus.driver.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotificationSender {
    public String FCM_API = "https://fcm.googleapis.com/fcm/send";
    public String serverKey = "AAAARdR9Avs:APA91bHJPAkbsTVnULi3VlSmz7rQ3n2vdgZSpbpeVEeDQT5b--CD6yAbqt4bZlsuRPwkDkV5J6Vm35s8x-95eGW69MUA0RbCj__YfCtCq0aULuBrItKrpBAvaYYgIa-kYPRgWmbPH1qV";
    public String contentType = "application/json";
    private Context context;
    private String userId;
    private RequestQueue requestQueue;

    public NotificationSender(Context ctx,String userId){
        context=ctx;
        this.userId=userId;
        requestQueue= Volley.newRequestQueue(context.getApplicationContext());
    }

    public void send(String passingThrough,String awayOrTowards) {


        String token1=MapUtil.removeSpace(passingThrough),token2=MapUtil.removeSpace(passingThrough);

        String body = "Hello";
        if (awayOrTowards.equals("away")) {
            body = "Going away from Campus";
            token1+=".away.";
            token2+=".away.";
        } else{
            token1+=".towards.";
            token2+=".towards.";
            body = "Coming towards Campus";
        }


        token1+="00_00";
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if(hour>=6 && hour<=9)token2+="06_09";
        else if(hour>=9 && hour<=12)token2+="09_12";
        else if(hour>=12 && hour<=15)token2+="12_15";
        else if(hour>=15 && hour<=18)token2+="15_18";
        else if(hour>=18 && hour<=22)token2+="18_22";

         sendTo(passingThrough,body,token1);
         sendTo(passingThrough,body,token2);
    }
    private void sendTo(String title ,String body,String token){

        JSONObject notification = new JSONObject();
        JSONObject data = new JSONObject();

        try{
            data.put("markerKey",userId);
            data.put("title", "Bus Passed "+title);
            data.put("token", token);
            data.put("when", String.valueOf(DateTimeUtils.currentTimeMillis()));
            data.put("body", body+", Hurry Up!");

            notification.put("to", "/topics/"+token);
            notification.put("time_to_live", 1200);
            notification.put("priority", "high");
            notification.put("data", data);

        }
        catch (Exception e){
            Log.d("DEBMES",e.getMessage());
            e.printStackTrace();
        }


        StringRequest req=new StringRequest(Request.Method.POST, FCM_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("DEBMES",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DEBMES", error.getMessage());
            }
        }){


            @Override
            public byte[] getBody() throws AuthFailureError {
                try{
                    return notification.toString().getBytes(StandardCharsets.UTF_8);
                }
                catch (Exception e){
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key="+serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };

        requestQueue.add(req);

    }

    public void destroy(){
        requestQueue.stop();
    }
}