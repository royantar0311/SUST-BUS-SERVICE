package com.sustbus.driver.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;

public class RouteDatabaseManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String PREF_ID="DRIVER_APP_SUST";
    private String  today;
    private Set<String> ids;
    private Context context;

    public RouteDatabaseManager(Context context){
        this.context=context;

        sharedPreferences=context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        Calendar calendar=Calendar.getInstance();
        int y=calendar.get(Calendar.YEAR),m=calendar.get(Calendar.MONTH),d=calendar.get(Calendar.DAY_OF_MONTH);
        today=""+y+(m>=10?"":"0")+m+(d>=10?"":"0")+d;

    }

    public ListenerRegistration checkForUpdate(CallBack callBack, boolean forced){

        if(forced){
           return update(callBack);
        }

       String version=sharedPreferences.getString("version","noExist");

       if(!today.equals(version)){
           return update(callBack);

       }
       else{
           callBack.ok();
           return null;
       }
    }

    private ListenerRegistration update(CallBack callBack){
        ids=sharedPreferences.getStringSet("id",new HashSet<>());
        Log.d("DEB",today);
        ListenerRegistration listenerRegistration=FirebaseFirestore.getInstance().collection("routes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e==null){
                    for (DocumentChange qd:queryDocumentSnapshots.getDocumentChanges()){
                        QueryDocumentSnapshot d=qd.getDocument();
                        String key=d.getId();

                        if(qd.getNewIndex()==-1){//document removed
                            ids.remove(key);
                            editor.remove(key+"path");
                            editor.remove(key+"title");
                            editor.remove(key+"time");
                            editor.remove(key+"show");
                        }
                        else if(qd.getOldIndex()==-1){//document added
                            ids.add(key);
                            editor.putString(key+"path",d.getString("path"));
                            editor.putString(key+"time",d.getString("time"));
                            editor.putString(key+"title",d.getString("title"));
                            editor.putString(key+"show",d.getString("show"));
                        }
                        else{//document changed
                            editor.putString(key+"path",d.getString("path"));
                            editor.putString(key+"time",d.getString("time"));
                            editor.putString(key+"title",d.getString("title"));
                            editor.putString(key+"show",d.getString("show"));

                        }
                    }
                    editor.remove("id");
                    editor.putStringSet("id",ids);
                    editor.putString("version",today);
                    editor.apply();
                   // Log.d("DEB","firestore");
                    callBack.ok();
                }
                else {
                    //Log.d("DEB","firestoreeror "+e.getMessage());
                    callBack.notOk();

                }

            }

        });


      return listenerRegistration;
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
            lst.add(routeInformation);
        }

        return lst;
    }

}
