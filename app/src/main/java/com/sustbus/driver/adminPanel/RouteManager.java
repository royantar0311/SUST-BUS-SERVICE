package com.sustbus.driver.adminPanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.sustbus.driver.R;
import com.sustbus.driver.fragments.NextFragment;
import com.sustbus.driver.util.CallBack;
import com.sustbus.driver.util.RecyclerViewAdapter;
import com.sustbus.driver.util.RouteInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RouteManager extends AppCompatActivity {
    public TextView appbarTv;
    public FloatingActionButton addButton;
    private ProgressDialog dialog;
    private RecyclerViewAdapter mAdapter;
    private List<RouteInformation> routeList;
    private NextFragment viewFragment;
    private RouteCreatorFragment routeCreatorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_manager);
        addButton = findViewById(R.id.rm_add_button);
        appbarTv = findViewById(R.id.rm_appbar_tv);
        dialog = new ProgressDialog(this);

        dialog.setMessage("Fetching Routes..");
        dialog.setCancelable(false);
        dialog.show();
        FirebaseFirestore.getInstance().collection("routes").get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    routeList=new ArrayList<>();

                    for (QueryDocumentSnapshot d : task.getResult()) {
                        RouteInformation routeInformation=new RouteInformation();
                        routeInformation.setTime(d.getString("time"));
                        routeInformation.setPath(d.getString("path"));
                        routeInformation.setShow(d.getString("show"));
                        routeInformation.setTitle(d.getString("title"));
                        routeInformation.setRouteId(d.getId());

                        routeList.add(routeInformation);
                    }
                    init();
                }
                else{
                    dialog.dismiss();
                    //Snackbar.make(findViewById(R.id.rm_frame),"Check Connection and try again.", BaseTransientBottomBar.LENGTH_INDEFINITE).show();
                    }
                }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {

                routeCreatorFragment=new RouteCreatorFragment();
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(routeCreatorFragment==null){
                            routeCreatorFragment=new RouteCreatorFragment();
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.rm_frame,routeCreatorFragment)
                                .addToBackStack(null)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commit();
                        v.setVisibility(View.INVISIBLE);
                    }
                });
                addButton.setVisibility(View.VISIBLE);
            }
        }).start();

    }

    void init(){

        Collections.sort(routeList, new Comparator<RouteInformation>() {
            @Override
            public int compare(RouteInformation o1, RouteInformation o2) {
                return o1.comparableStartTime.compareTo(o2.comparableStartTime);
            }
        });

        mAdapter=new RecyclerViewAdapter(this, routeList, new RecyclerViewAdapter.ClickEvent() {
            @Override
            public void click(int position, int from) {
                handleCallback(position);
            }
        },100);


        viewFragment=new NextFragment();
        viewFragment.mLayoutManager=new LinearLayoutManager(this);
        viewFragment.mAdapter=mAdapter;

        getSupportFragmentManager().beginTransaction().replace(R.id.rm_frame,viewFragment).disallowAddToBackStack().commit();

        dialog.dismiss();


    }

    void handleCallback(int pos){





    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        addButton.setVisibility(View.VISIBLE);
    }
}
