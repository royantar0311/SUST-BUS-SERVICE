package com.sustbus.driver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.here.sdk.core.GeoCoordinates;
import com.sustbus.driver.util.MapUtil;
import com.sustbus.driver.util.NotificationSender;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class LocationUploaderService extends Service {

    private IBinder binderToReturn=new LocalBinder();
    private SharedPreferences.Editor edit;
    private boolean isBinded=false;
    private Uploader mUploader;
    private final String notificationChannel="1234";

    public void out(String s){
        Log.d("DEB",s);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBinded=true;
        out("Binded");
        return binderToReturn;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        out("unbinded");
        isBinded=false;
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent) {
        isBinded=true;
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        edit=getSharedPreferences("observer",MODE_PRIVATE).edit();
        out("service Oncreate");
        // Android O requires a Notification Channel.
        NotificationManager mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(notificationChannel, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }


    }
    @Override
    public void onDestroy() {

        out("service Dstroy");
        edit.putBoolean("running",false);
        edit.commit();
        stopForeground(true);
        super.onDestroy();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getBooleanExtra("stop",false)){
            out("got flag");
            forcestop();
            return START_NOT_STICKY;
        }

        edit=getSharedPreferences("observer",MODE_PRIVATE).edit();

        edit.putBoolean("running",true);
        edit.commit();
        startForeground(1,getNotification());

        mUploader=new Uploader(intent);
        mUploader.start();

        out("After run");
        return START_NOT_STICKY;
    }




    public void forcestop(){
        if(mUploader!=null && mUploader.isAlive() )mUploader.turnOff();
    }
    private void stop(){
        out("InStop()");


        edit.putBoolean("running", false);
        edit.commit();

        if(!isBinded) {

            out("here");
            stopForeground(true);
            stopSelf();
        }
    }


    public class LocalBinder extends Binder {
         LocationUploaderService getSerVice(){return LocationUploaderService.this;}

    }

    private Notification getNotification() {
        Intent intent=new Intent(this,LocationUploaderService.class);
        intent.putExtra("stop",true);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0 /* Request code */, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Ride share on")
                //.setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_directions_bus_black_24dp)
                .setColor(ContextCompat.getColor(getApplicationContext(),R.color.A400red))
                .addAction(0,"TAP TO STOP",pendingIntent)
                .setWhen(System.currentTimeMillis());


        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setChannelId(notificationChannel);
        }

        return builder.build();
    }


    private class Uploader extends Thread{

        private  String  TAG="onlocationThread";
        private volatile boolean stop=false;
       private boolean taskEnded=false;
       private List<String> pathString;
       private String userUid,routeIdCurrentlySharing;
       private DatabaseReference databaseReference,userLocationData,userPathReference;
       private boolean pathOk;
       private int determineCallCount;
       private  String SERVER_KEY="hello";
       private NotificationSender notificationSender;
       private LocationCallback locationCallback;
       private Location ridersPreviousLocation;
       private FusedLocationProviderClient fusedLocationProviderClient;
       private LocationRequest locationRequest;
       private MapUtil mapUtil;
       private GeoCoordinates previousPosition;
       private String  For;

       Uploader(Intent data){

           String path = data.getStringExtra("path");
           String routeId = data.getStringExtra("routeId");
           String title = data.getStringExtra("title");
           userUid=data.getStringExtra("userUid");
           SERVER_KEY=data.getStringExtra("SERVER_KEY");
           For=data.getStringExtra("for");

           databaseReference=FirebaseDatabase.getInstance().getReference();
           userLocationData = FirebaseDatabase.getInstance().getReference().child("alive").child(userUid);
           userPathReference = FirebaseDatabase.getInstance().getReference().child("destinations").child(userUid).child("path");
           userLocationData.onDisconnect().setValue(null);
           userPathReference.onDisconnect().setValue(null);
           mapUtil=MapUtil.getInstance();

           pathString = new ArrayList<>();
           int last = 0;
           for (int i = 0; i < path.length(); i++) {
               if (path.charAt(i) == ';') {
                   pathString.add(path.substring(last, i));
                   //Log.d("DEB", path.substring(last, i));
                   last = i + 1;
               }
           }



           fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocationUploaderService.this.getApplicationContext());
           locationRequest = new LocationRequest();
           locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
           locationRequest.setInterval(4500);
           locationRequest.setFastestInterval(3000);

           Log.d("DEB", "thread created " + pathString.toString());
           userLocationData.child("title").setValue(title);
           databaseReference.child("busesOnRoad").child(routeId).onDisconnect().setValue(null);
           databaseReference.child("busesOnRoad").child(routeId).child("key").setValue(userUid);
           databaseReference.child("busesOnRoad").child(routeId).child("for").setValue(For);
           routeIdCurrentlySharing = routeId;
           userPathReference.setValue("NA;");
           pathOk = false;
           determineCallCount = 0;

       }
       @Override
        public void run() {
            super.run();


           notificationSender = new NotificationSender(LocationUploaderService.this.getApplicationContext(), userUid, SERVER_KEY,For);

           locationCallback=new LocationCallback(){

               @Override
               public void onLocationResult(LocationResult location) {
                   super.onLocationResult(location);
                   float rotation = 0;

                   if (ridersPreviousLocation != null) {
                       rotation = location.getLastLocation().bearingTo(ridersPreviousLocation);
                       if(Math.abs(ridersPreviousLocation.getLatitude()-location.getLastLocation().getLatitude())<1e-5  &&
                               Math.abs(ridersPreviousLocation.getLongitude()-location.getLastLocation().getLongitude())<1e-5
                       )return;
                   }

                   Log.d("DEB", "loc: "+location.getLastLocation().getLatitude() +"  "+ location.getLastLocation().getLongitude());
                   //userInfo.setLatLng(location.getLastLocation().getLatitude(),location.getLastLocation().getLongitude());
                   userLocationData.child("lat").setValue(location.getLastLocation().getLatitude());
                   userLocationData.child("lng").setValue(location.getLastLocation().getLongitude());

                   ridersPreviousLocation = location.getLastLocation();
                   userLocationData.child("rotation").setValue(rotation);
                   handlePath(new GeoCoordinates(location.getLastLocation().getLatitude(), location.getLastLocation().getLongitude()));
               }
           };

           fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,getMainLooper());



            while(!taskEnded && !stop) {
            }
            LocationUploaderService.this.stop();
        }

        public void determineCurrentLocation(GeoCoordinates latLng) {
            Log.d(TAG, "determineCurrentLocation: called");
            if (determineCallCount == 0) previousPosition = latLng;
            else if (latLng.distanceTo(previousPosition) >= 5) {
                int rem = 0;
                for (int i = 0; i + 1 < pathString.size(); i++) {
                    GeoCoordinates co1 = MapUtil.GeoCoordinatesMap.get(pathString.get(i));
                    GeoCoordinates co2 = MapUtil.GeoCoordinatesMap.get(pathString.get(i + 1));

                    if (Math.abs(latLng.distanceTo(co2) + previousPosition.distanceTo(co2) - previousPosition.distanceTo(latLng)) <= 1) {

                        if (co1.distanceTo(previousPosition) < co1.distanceTo(latLng)) {
                            rem = i + 2;
                            break;
                        }

                    } else if (Math.abs(latLng.distanceTo(co1) + previousPosition.distanceTo(co1) - previousPosition.distanceTo(latLng)) <= 1) {

                        if (co2.distanceTo(previousPosition) > co2.distanceTo(latLng)) {
                            rem = i + 1;
                            break;
                        }

                    } else if (co1.distanceTo(previousPosition) < co1.distanceTo(latLng) && co2.distanceTo(previousPosition) > co2.distanceTo(latLng)) {
                        rem = i + 1;
                        break;

                    }


                }

                if (rem == 0) {
                    determineCallCount = 0;
                    return;
                } else {
                    for (int i = 0; i < rem; i++) pathString.remove(0);
                    pathOk = true;
                    updatePath();
                }
            }


            determineCallCount = 1;

        }

        public void handlePath(GeoCoordinates newLatLng) {

            if (!pathOk) {
                determineCurrentLocation(newLatLng);
                return;
            }
            Log.d(TAG, "handlePath: " + pathString.toString());
            GeoCoordinates toCheck = null;
            try {
                toCheck = MapUtil.GeoCoordinatesMap.get(pathString.get(0));
            }
            catch (IndexOutOfBoundsException e){
                turnOff();
            }
            double distance = newLatLng.distanceTo(toCheck);
            if ( distance <= 50) {

                String toNotify = pathString.get(0);

                if (pathString.contains(mapUtil.CAMPUS)) {
                    if (pathString.contains(toNotify)) {
                        notificationSender.send(toNotify, "away");
                    } else {
                        notificationSender.send(toNotify, "towards");
                    }
                } else notificationSender.send(toNotify, "away");

                pathString.remove(0);
                updatePath();

                if(pathString.size() == 0)turnOff();

            }


        }


        public void updatePath() {
            String path = "";
            for (String s : pathString) {
                path += (s + ";");
            }

            if (path.isEmpty()) path = "NA;";
            userPathReference.setValue(path);
        }


        public  void turnOff(){
            notificationSender.destroy();
            notificationSender = null;
            userLocationData.setValue(null);
            userPathReference.setValue(null);
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            databaseReference.child("busesOnRoad").child(routeIdCurrentlySharing).setValue(null);
            stop=true;
        }

    }
}
