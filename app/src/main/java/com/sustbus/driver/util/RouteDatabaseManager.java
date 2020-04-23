package com.sustbus.driver.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;

public class RouteDatabaseManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String PREF_ID="DRIVER_APP_SUST";
    private String  today;
    private Set<String> ids;
    private Context context;
    private String version;

    public RouteDatabaseManager(Context context){
        this.context=context;

        sharedPreferences=context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        Calendar calendar=Calendar.getInstance();
        int y=calendar.get(Calendar.YEAR),m=calendar.get(Calendar.MONTH),d=calendar.get(Calendar.DAY_OF_MONTH);
        today=""+y+(m>=10?"":"0")+m+(d>=10?"":"0")+d;
        version=sharedPreferences.getString("version","noExist");
    }
    public Boolean isUpdated(){
        return today.equals(version);
    }

    public void checkForUpdate(CallBack callBack, boolean forced){

        if(forced){
           update(callBack);
           return;
        }



       if(!today.equals(version)){
           update(callBack);

       }
       else{
           callBack.ok();
       }
    }

    private void update(CallBack callBack){
        ids=new HashSet<>();
        Log.d("DEB",today);


        FirebaseFirestore.getInstance().collection("routes").get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            editor.clear();
                            for (QueryDocumentSnapshot d:task.getResult()){

                                String key=d.getId();
                                    ids.add(key);
                                    editor.putString(key+"path",d.getString("path"));
                                    editor.putString(key+"time",d.getString("time"));
                                    editor.putString(key+"title",d.getString("title"));
                                    editor.putString(key+"show",d.getString("show"));

                            }

                            editor.putStringSet("id",ids);
                            editor.putString("version",today);
                            editor.commit();
             //               Log.d("DEB","firestore");
                            callBack.ok();


                        }
                        else{
                          callBack.notOk();
                        }
                    }
                });

    }

    public List<RouteInformation> getAll(){
        List<RouteInformation> lst=new ArrayList<>();
        ids=sharedPreferences.getStringSet("id",new HashSet<>());

        for(String key:ids){
            RouteInformation routeInformation=new RouteInformation();
            routeInformation.setRouteId(key);
            routeInformation.setPath(sharedPreferences.getString(key+"path","Campus-Amborkhan"));
            routeInformation.setTime(sharedPreferences.getString(key+"time","12:30 pm"));
            routeInformation.setTitle(sharedPreferences.getString(key+"title","SUST-SUST"));
            routeInformation.setShow(sharedPreferences.getString(key+"show","No Information"));
           // Log.d("DEB",routeInformation.getPath()+" "+routeInformation.getTime());
            lst.add(routeInformation);
        }

        return lst;
    }

}
