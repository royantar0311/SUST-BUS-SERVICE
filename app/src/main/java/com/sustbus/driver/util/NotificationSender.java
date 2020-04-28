package com.sustbus.driver.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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

    public void send(String topic,String mes){


        JSONObject notification=new JSONObject();
        JSONObject notificationBody=new JSONObject();
        JSONObject data=new JSONObject();

        try{
            data.put("markerKey",userId);
            notificationBody.put("title", "Bus Passed");
            notificationBody.put("body", mes);

            notification.put("to", "/topics/"+topic);
           // notification.put("notification", notificationBody);
            notification.put("data", data);


        }
        catch (Exception e){
            Log.d("DEBMES",e.getMessage());
            e.printStackTrace();
        }


        StringRequest req=new StringRequest(Request.Method.POST, FCM_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
           },null){


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
                Log.d("DEBMES","called");
                return params;
            }
        };

        requestQueue.add(req);

    }

    public void destroy(){
        requestQueue.stop();
    }
}