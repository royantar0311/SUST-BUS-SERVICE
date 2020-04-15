/*
 * Copyright (C) 2019-2020 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.sustbus.driver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.here.sdk.routing.Waypoint;
import com.here.sdk.routing.WaypointType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    public static final String CAMPUS_GATE="Gate";


    public static Map<String, GeoCoordinates>GeoCoordinatesMap=new HashMap<>();
    public static List<GeoBox> restrictionList=new ArrayList<>();

    static {
         GeoCoordinatesMap.put(CAMPUS_GATE,new GeoCoordinates(24.911127,91.832222));
         GeoCoordinatesMap.put(CAMPUS,new GeoCoordinates(24.920856,91.832484));
         GeoCoordinatesMap.put(MODINA_MARKET,new GeoCoordinates(24.910599,91.848425));
         GeoCoordinatesMap.put(SUBID_BAZAR,new GeoCoordinates(24.907898,91.859542));
         GeoCoordinatesMap.put(AMBORKHANA,new GeoCoordinates(24.905023,91.869917));
         GeoCoordinatesMap.put(RIKABI_BAZAR,new GeoCoordinates(24.899122,91.862674));
         GeoCoordinatesMap.put(CHOWHATTA,new GeoCoordinates(24.899424,91.868818));
         GeoCoordinatesMap.put(NAIORPUL,new GeoCoordinates(24.894753,91.878688));
         GeoCoordinatesMap.put(KUMARPARA,new GeoCoordinates(24.899373,91.879114));
         GeoCoordinatesMap.put(EIDGAH,new GeoCoordinates(24.906465,91.880405));
         GeoCoordinatesMap.put(TILAGOR,new GeoCoordinates(24.896190,91.900370));
         GeoCoordinatesMap.put(BALUCHAR,new GeoCoordinates(24.903055,91.895983));
         GeoCoordinatesMap.put(LAKKATURA,new GeoCoordinates(24.923850,91.872001));

         restrictionList.add(new GeoBox(new GeoCoordinates(24.921759,91.82511),new GeoCoordinates(24.925740,91.83950)));


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


    public PathInformation stringToPath(String path){

        PathInformation pathInformation=new PathInformation();

        if(path==null){
            pathInformation.isRouteAvailable=false;
            return pathInformation;
        }
        String latlng=null;

        for (int i=0,j=0; j<path.length() ;i++) {


            for (int k = j; j < path.length(); j++) {
                if (path.charAt(j) == ';') {
                     latlng=path.substring(k,j);
                     j++;
                    break;
                }
            }

            //Log.d("MAPUTIL:",GeoCoordinates+";");

            if (i == 0){
                if(latlng=="NA")pathInformation.isRouteAvailable=false;
                else pathInformation.addWayPoint(GeoCoordinatesMap.get(latlng));
            }
            else if(j==path.length()){
                pathInformation.setDest(GeoCoordinatesMap.get(latlng));
            }
            else {
                pathInformation.addWayPoint(GeoCoordinatesMap.get(latlng));
            }
        }



        return pathInformation;
    }

    public static MapUtil mapUtil=new MapUtil();

    public static MapUtil getInstance(){
        return mapUtil;
    }


    class PathInformation{

        private Waypoint dest;
        public boolean isRouteAvailable=true;
        

        public void setDest(GeoCoordinates gdest) {
            dest=new Waypoint(gdest);
            dest.type=WaypointType.STOPOVER;
            wayPoints.add(dest);
        }

        public Waypoint getDest() {
            return dest;
        }

        private List<Waypoint>wayPoints=new ArrayList<>();
        public void addWayPoint(GeoCoordinates geoCoordinates){
            Waypoint tmp=new Waypoint(geoCoordinates);
            tmp.type= WaypointType.STOPOVER;
            wayPoints.add(tmp);
        }

        public List<Waypoint> getWayPoints() {
            return wayPoints;
        }

        public void kill(){wayPoints.clear();}

    }


}

class PermissionsRequestor {

    private static final int PERMISSIONS_REQUEST_CODE = 42;
    private ResultListener resultListener;
    private final Activity activity;

    public PermissionsRequestor(Activity activity) {
        this.activity = activity;
    }

    public interface ResultListener {
        void permissionsGranted();
        void permissionsDenied();
    }

    public void request(ResultListener resultListener) {
        this.resultListener = resultListener;

        String[] missingPermissions = getPermissionsToRequest();
        if (missingPermissions.length == 0) {
            resultListener.permissionsGranted();
        } else {
            ActivityCompat.requestPermissions(activity, missingPermissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    private String[] getPermissionsToRequest() {
        ArrayList<String> permissionList = new ArrayList<>();
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    if (ContextCompat.checkSelfPermission(
                            activity, permission) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M &&
                                permission.equals(Manifest.permission.CHANGE_NETWORK_STATE)) {
                            // Exclude CHANGE_NETWORK_STATE as it does not require explicit user approval.
                            // This workaround is needed for devices running Android 6.0.0,
                            // see https://issuetracker.google.com/issues/37067994
                            continue;
                        }
                        permissionList.add(permission);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return permissionList.toArray(new String[0]);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (resultListener == null) {
            return;
        }

        if (grantResults.length == 0) {
            // Request was cancelled.
            return;
        }

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                allGranted &= result == PackageManager.PERMISSION_GRANTED;
            }

            if (allGranted) {
                resultListener.permissionsGranted();
            } else {
                resultListener.permissionsDenied();
            }
        }
    }
}
