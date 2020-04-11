package com.sustbus.driver;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.res.Resources;
import android.os.CountDownTimer;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.GeoApiContext;
import com.google.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import androidx.annotation.NonNull;

public class MapUtil {

    public static final String CAMPUS="Campus";
    public static final String AMBORKHANA="Amborkhana";
    public static final String SUBID_BAZAR="Subid Bazar";
    public static final String MODINA_MARKET="Modina Market";
    public static final String RIKABI_BAZAR="Rikabi Bazar";
    public static final String  CHOWHATTA="chowhatta";
    public static final String NAIORPUL="Naiorpul";
    public static final String  KUMARPARA="Kumarpara";
    public static final String EIDGAH="Eidgah";
    public static final String TILAGOR="Tilagor";
    public static final String BALUCHAR="Baluchar";
    public static final String LAKKATURA="Lakkatura";


    public static Map<String, LatLng>latLngMap=new HashMap<>();

    private static GeoApiContext geoApiContext;

    static {

         latLngMap.put(CAMPUS,new LatLng(24.917326,91.831946));
         latLngMap.put(MODINA_MARKET,new LatLng(24.910599,91.848425));
         latLngMap.put(SUBID_BAZAR,new LatLng(24.907898,91.859542));
         latLngMap.put(AMBORKHANA,new LatLng(24.905023,91.869917));
         latLngMap.put(RIKABI_BAZAR,new LatLng(24.899122,91.862674));
         latLngMap.put(CHOWHATTA,new LatLng(24.899424,91.868818));
         latLngMap.put(NAIORPUL,new LatLng(24.894753,91.878688));
         latLngMap.put(KUMARPARA,new LatLng(24.899373,91.879114));
         latLngMap.put(EIDGAH,new LatLng(24.906465,91.880405));
         latLngMap.put(TILAGOR,new LatLng(24.896190,91.900370));
         latLngMap.put(BALUCHAR,new LatLng(24.903055,91.895983));
         latLngMap.put(LAKKATURA,new LatLng(24.923850,91.872001));

         String key="AIzaSyAJsyecW4eYPOQzuC5VonO9IyAJjNx2_XQ";

         geoApiContext=new GeoApiContext.Builder()
                 .apiKey(key)
                 .queryRateLimit(2)
                 .maxRetries(2)
                 .retryTimeout(2,TimeUnit.SECONDS)
                 .readTimeout(1, TimeUnit.SECONDS)
                 .writeTimeout(1,TimeUnit.SECONDS)
                 .build();

    }

    public void enableGPS(final Context context, final Activity activity, final int RequestCode){

        LocationRequest locationRequest=LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2*1000)
                .setFastestInterval(1000);


        LocationSettingsRequest locationSettingRequest=new LocationSettingsRequest.Builder()
                                                    .addLocationRequest(locationRequest)
                                                   .setAlwaysShow(true).build();


        final Task<LocationSettingsResponse> result= LocationServices.getSettingsClient(context)
                                               .checkLocationSettings(locationSettingRequest);

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                  try{
                      LocationSettingsResponse response=task.getResult(ApiException.class);
                  }
                  catch (ApiException e){
                      switch (e.getStatusCode()){
                          case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                              try {
                                  ResolvableApiException resolvableApiException=(ResolvableApiException)e;
                                  resolvableApiException.startResolutionForResult(activity,RequestCode);
                              }
                              catch (IntentSender.SendIntentException ie){
                              }
                              break;
                          case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                              break;

                      }


                  }


            }
        });

    }


    public static MapUtil mapUtil=new MapUtil();

    public static MapUtil getInstance(){
        return mapUtil;
    }

}
