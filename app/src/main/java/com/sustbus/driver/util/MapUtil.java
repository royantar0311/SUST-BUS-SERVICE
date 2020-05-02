package com.sustbus.driver.util;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class MapUtil {

    public static final String CAMPUS = "Campus";
    public static final String AMBORKHANA = "Amborkhana";
    public static final String SUBID_BAZAR = "Subid Bazar";
    public static final String MODINA_MARKET = "Modina Market";
    public static final String RIKABI_BAZAR = "Rikabi Bazar";
    public static final String CHOWHATTA = "Chowhatta";
    public static final String NAIORPUL = "Naiorpul";
    public static final String KUMARPARA = "Kumarpara";
    public static final String EIDGAH = "Eidgah";
    public static final String TILAGOR = "Tilagor";
    public static final String BALUCHAR = "Baluchar";
    public static final String LAKKATURA = "Lakkatura";
    public static final String CAMPUS_GATE = "Gate";



    public static boolean rideShareStatus = false;

    public static Map<String, GeoCoordinates> GeoCoordinatesMap = new HashMap<>();
    public static List<GeoBox> restrictionList = new ArrayList<>();
    public static MapUtil mapUtil = new MapUtil();
    public static List<String> placeList;

    static {
        GeoCoordinatesMap.put(CAMPUS_GATE, new GeoCoordinates(24.911103, 91.832213));
        GeoCoordinatesMap.put(CAMPUS, new GeoCoordinates(24.920856, 91.832484));
        GeoCoordinatesMap.put(MODINA_MARKET, new GeoCoordinates(24.910353, 91.847973));
        GeoCoordinatesMap.put(SUBID_BAZAR, new GeoCoordinates(24.907373, 91.860607));
        GeoCoordinatesMap.put(AMBORKHANA, new GeoCoordinates(24.905059, 91.869903));
        GeoCoordinatesMap.put(RIKABI_BAZAR, new GeoCoordinates(24.899174, 91.862412));
        GeoCoordinatesMap.put(CHOWHATTA, new GeoCoordinates(24.899423, 91.868813));
        GeoCoordinatesMap.put(NAIORPUL, new GeoCoordinates(24.894782, 91.878659));
        GeoCoordinatesMap.put(KUMARPARA, new GeoCoordinates(24.899368, 91.879104));
        GeoCoordinatesMap.put(EIDGAH, new GeoCoordinates(24.906645, 91.879974));
        GeoCoordinatesMap.put(TILAGOR, new GeoCoordinates(24.896180, 91.900212));
        GeoCoordinatesMap.put(BALUCHAR, new GeoCoordinates(24.903014, 91.895963));
        GeoCoordinatesMap.put(LAKKATURA, new GeoCoordinates(24.925066, 91.871258));

        restrictionList.add(new GeoBox(new GeoCoordinates(24.921759, 91.82511), new GeoCoordinates(24.925740, 91.83950)));
        placeList= Arrays.asList(CAMPUS_GATE,AMBORKHANA,MODINA_MARKET,SUBID_BAZAR,RIKABI_BAZAR,EIDGAH,KUMARPARA,TILAGOR,NAIORPUL,CHOWHATTA,LAKKATURA
                                ,BALUCHAR,CAMPUS);
    }

    public static String removeSpace(String s){
        String z= "";
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)!=' ')z+=s.charAt(i);
            else z+='_';
        }
        return z;
    }

    public static MapUtil getInstance() {
        return mapUtil;
    }

    public void enableGPS(final Context context, final Activity activity, final int RequestCode) {


        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2 * 1000)
                .setFastestInterval(1000);


        LocationSettingsRequest locationSettingRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true).build();


        final Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
                .checkLocationSettings(locationSettingRequest);

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(activity, RequestCode);
                            } catch (IntentSender.SendIntentException ie) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;

                    }
                }
            }
        });
    }

    public PathInformation stringToPath(String path) {

        PathInformation pathInformation = new PathInformation();

        if (path == null) {
            pathInformation.isRouteAvailable = false;
            return pathInformation;
        }
        String latlng = null;

        for (int i = 0, j = 0; j < path.length(); i++) {


            for (int k = j; j < path.length(); j++) {
                if (path.charAt(j) == ';') {
                    latlng = path.substring(k, j);
                    j++;
                    break;
                }
            }

            //Log.d("MAPUTIL:",GeoCoordinates+";");

            if (i == 0) {
                if (latlng == "NA") pathInformation.isRouteAvailable = false;
                else pathInformation.addWayPoint(GeoCoordinatesMap.get(latlng));
            } else if (j == path.length()) {
                pathInformation.setDest(GeoCoordinatesMap.get(latlng));
            } else {
                pathInformation.addWayPoint(GeoCoordinatesMap.get(latlng));
            }
        }


        return pathInformation;
    }
}
