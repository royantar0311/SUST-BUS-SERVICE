package com.sustbus.driver.adminPanel;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sustbus.driver.R;
import com.sustbus.driver.util.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomQueryFragment extends Fragment implements CheckChangedListener {
    private static final String TAG = "CustomQueryFragment";
    View view;
    Spinner driverSp,permissionSp,ascDescSp;
    RecyclerView recyclerView;
    CustomQueryRecyclerAdapter recyclerAdapter;
    Button searchBtn;
    String ascDesc,from,to;
    Query.Direction qd;
    boolean permission,driver;
    TextView fromTv,toTv;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cq,container,false);
        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        driverSp = view.findViewById(R.id.cq_driver_spinner);
        permissionSp = view.findViewById(R.id.cq_permission_spinner);
        ascDescSp = view.findViewById(R.id.cq_asc_desc_spinner);
        fromTv = view.findViewById(R.id.cq_from_tv);
        toTv = view.findViewById(R.id.cq_to_tv);
        searchBtn = view.findViewById(R.id.cq_search_btn);
        recyclerView = view.findViewById(R.id.cq_recycler_view);
        initDriverSp();
        initPermissionSp();
        initAscDescSp();
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: searchPressed");
                from = fromTv.getText().toString().trim();
                to = toTv.getText().toString().trim();
                while(from.length() < 10) from = from + '0';
                while(to.length() < 10) to = to + '9';
                initRecyclerView(FirebaseAuth.getInstance().getCurrentUser());
            }
        });

    }

    private void initRecyclerView(FirebaseUser currentUser) {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("regiNo",qd)
                .whereGreaterThanOrEqualTo("regiNo",from)
                .whereLessThanOrEqualTo("regiNo",to)
                .whereEqualTo("driver",driver)
                .whereEqualTo("permitted",permission)
                .whereEqualTo("profileCompleted",true);
        FirestoreRecyclerOptions<UserInfo> options = new FirestoreRecyclerOptions.Builder<UserInfo>()
                .setQuery(query,UserInfo.class)
                .build();
        recyclerAdapter = new CustomQueryRecyclerAdapter(options,this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        recyclerAdapter.startListening();
    }



    private void initAscDescSp() {
        List<String> ascDescSpElements = new ArrayList<>();
        ascDescSpElements.add(0,"Select");
        ascDescSpElements.add(1,"Ascending");
        ascDescSpElements.add(2,"Descending");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),android.R.layout.simple_spinner_item,ascDescSpElements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ascDescSp.setAdapter(adapter);
        ascDescSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0 ){
                    searchBtn.setEnabled(false);
                } else if(position == 1) {
                    searchBtn.setEnabled(true);
                    qd = Query.Direction.ASCENDING;
                }else if(position == 2){
                    searchBtn.setEnabled(true);
                    qd = Query.Direction.DESCENDING;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                searchBtn.setEnabled(false);
            }
        });
    }

    private void initPermissionSp() {
        List<String> permissionSpElements = new ArrayList<>();
        permissionSpElements.add(0,"Select");
        permissionSpElements.add(1,"True");
        permissionSpElements.add(2,"False");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),android.R.layout.simple_spinner_item,permissionSpElements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        permissionSp.setAdapter(adapter);
        permissionSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                if(position == 0 )searchBtn.setEnabled(false);
                else if(position == 1){
                    permission = true;
                    searchBtn.setEnabled(true);
                }
                else if(position == 2){
                    permission = false;
                    searchBtn.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                searchBtn.setEnabled(false);
            }
        });
    }

    private void initDriverSp() {
        List<String> driverSpElements = new ArrayList<>();
        driverSpElements.add(0,"Select");
        driverSpElements.add(1,"Driver");
        driverSpElements.add(2,"Student");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),android.R.layout.simple_spinner_item,driverSpElements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverSp.setAdapter(adapter);
        driverSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                if(position == 0 )searchBtn.setEnabled(false);
                else if(position == 1){
                    driver = true;
                    searchBtn.setEnabled(true);
                }
                else if(position == 2){
                    driver = false;
                    searchBtn.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                searchBtn.setEnabled(false);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(recyclerAdapter != null){
            recyclerAdapter.stopListening();
        }
    }
    @Override
    public void onSwitchStateChanged(boolean isChecked, DocumentSnapshot snapshot) {
        snapshot.getReference().update("permitted",isChecked);
    }
    @Override
    public void onItemClicked(String uId) {
        Log.d(TAG, "onItemClicked: " + uId);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.user_info_alertdialog,null);
        EditText emailEt,userNameEt,regiNoEt;
        TextView driverTv;
        ImageView userEv;
        Switch permittedSwitch,profileCompletedSwitch;
        emailEt = view.findViewById(R.id.ad_email_et);
        userNameEt = view.findViewById(R.id.ad_username_et);
        regiNoEt = view.findViewById(R.id.ad_regino_et);
        userEv = view.findViewById(R.id.ad_user_id_image_ev);
        driverTv = view.findViewById(R.id.ad_driver_tv);
        permittedSwitch = view.findViewById(R.id.ad_permitted_switch);
        profileCompletedSwitch = view.findViewById(R.id.ad_profile_completed_switch);
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users")
                .document(uId);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                emailEt.setText(snapshot.getString("email"));
                userNameEt.setText(snapshot.getString("userName"));
                regiNoEt.setText(snapshot.getString("regiNo"));
                permittedSwitch.setChecked(snapshot.getBoolean("permitted"));
                profileCompletedSwitch.setChecked(snapshot.getBoolean("profileCompleted"));
                driverTv.setText(snapshot.getBoolean("driver")?"Driver":"Student");
                try {
                    String img = snapshot.getString("idUrl");
                    byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
                    userEv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                }
                catch (NullPointerException e){
                }
                catch (IllegalArgumentException e){
                    Log.d(TAG, "onSuccess: " + e.getMessage());
                }
            }
        });
        new AlertDialog.Builder(getContext())
                .setTitle("Change or Validate")
                .setView(view)
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email,userName,regiNo;
                        email = emailEt.getText().toString().trim();
                        userName = userNameEt.getText().toString().trim();
                        regiNo = regiNoEt.getText().toString().trim();
                        Log.d(TAG, "onClick: " + email + " " + regiNo);
                        documentReference.update("email",email);
                        documentReference.update("userName",userName);
                        documentReference.update("regiNo",regiNo);
                        documentReference.update("permitted",permittedSwitch.isChecked());
                        documentReference.update("profileCompleted",profileCompletedSwitch.isChecked());
                    }
                })
                .setNegativeButton("cancel",null)
                .show();
    }
}
