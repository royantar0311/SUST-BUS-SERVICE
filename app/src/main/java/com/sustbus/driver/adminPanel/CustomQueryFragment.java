package com.sustbus.driver.adminPanel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.Toast;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class CustomQueryFragment extends Fragment implements CheckChangedListener {
    private static final String TAG = "CustomQueryFragment";
    private View view;
    private Spinner driverSp, permissionSp, ascDescSp;
    private RecyclerView recyclerView;
    private TypesRecyclerAdapter recyclerAdapter;
    private Button searchBtn;
    private String from, to;
    private Query.Direction qd;
    private boolean permission;
    private String type;
    private TextView fromTv, toTv,errorText;
    private double lat,lng;
    private boolean state,typeOk,permissionOk,orderOk;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cq, container, false);
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
        errorText = view.findViewById(R.id.cq_error_text);
        initDriverSp();
        initPermissionSp();
        initAscDescSp();
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: searchPressed");
                if(typeOk && permissionOk && orderOk){
                   errorText.setText("");
                }
                else {
                    errorText.setText("please select all fields");
                    errorText.setTextColor(ContextCompat.getColor(getRootActivity(), R.color.A400red));
                    return;
                }
                from = fromTv.getText().toString().trim();
                to = toTv.getText().toString().trim();
                while (from.length() < 10) from = from + '0';
                while (to.length() < 10) to = to + '9';
                initRecyclerView(FirebaseAuth.getInstance().getCurrentUser());
            }
        });

    }

    private void initRecyclerView(FirebaseUser currentUser) {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("regiNo", qd)
                .whereGreaterThanOrEqualTo("regiNo", from)
                .whereLessThanOrEqualTo("regiNo", to)
                .whereEqualTo(type, true)
                .whereEqualTo("permitted", permission)
                .whereEqualTo("profileCompleted", true);
        FirestoreRecyclerOptions<UserInfo> options = new FirestoreRecyclerOptions.Builder<UserInfo>()
                .setQuery(query, UserInfo.class)
                .build();
        recyclerAdapter = new TypesRecyclerAdapter(options, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerAdapter.startListening();
    }


    private void initAscDescSp() {
        List<String> ascDescSpElements = new ArrayList<>();
        ascDescSpElements.add(0, "Select");
        ascDescSpElements.add(1, "Ascending");
        ascDescSpElements.add(2, "Descending");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item, ascDescSpElements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ascDescSp.setAdapter(adapter);
        ascDescSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if (position == 1) {
                    searchBtn.setEnabled(true);
                    qd = Query.Direction.ASCENDING;
                } else if (position == 2) {
                    qd = Query.Direction.DESCENDING;
                }
               orderOk = position > 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                searchBtn.setEnabled(false);
            }
        });
    }

    private void initPermissionSp() {
        List<String> permissionSpElements = new ArrayList<>();
        permissionSpElements.add(0, "Select");
        permissionSpElements.add(1, "True");
        permissionSpElements.add(2, "False");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item, permissionSpElements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        permissionSp.setAdapter(adapter);
        permissionSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                if (position == 1) {
                    permission = true;
                } else if (position == 2) {
                    permission = false;
                }
                permissionOk = position > 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                searchBtn.setEnabled(false);
            }
        });
    }

    private void initDriverSp() {
        List<String> driverSpElements = new ArrayList<>();
        driverSpElements.add(0, "Select");
        driverSpElements.add(1, "Driver");
        driverSpElements.add(2, "Student");
        driverSpElements.add(3, "Stuff");
        driverSpElements.add(4, "Teacher");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item, driverSpElements);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverSp.setAdapter(adapter);
        driverSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                type = "";
                if (position == 1) {
                    type = "driver";
                } else if (position == 2) {
                    type = "student";
                } else if (position == 3 ){
                    type ="staff";
                }
                else if (position == 4 ){
                    type ="teacher";
                }
                typeOk = position>0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                searchBtn.setEnabled(false);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (recyclerAdapter != null) {
            recyclerAdapter.startListening();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recyclerAdapter != null) {
            recyclerAdapter.stopListening();
        }
    }

    @Override
    public void onSwitchStateChanged(boolean isChecked, DocumentSnapshot snapshot) {
        snapshot.getReference().update("permitted", isChecked);
    }

    @Override
    public void onItemClicked(String uId) {
        Log.d(TAG, "onItemClicked: " + uId);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.user_info_alertdialog, null);
        EditText emailEt, userNameEt, regiNoEt;
        TextView driverTv;
        ImageView userEv;
        Switch permittedSwitch, profileCompletedSwitch;
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
                state = snapshot.getBoolean("permitted");
                emailEt.setText(snapshot.getString("email"));
                userNameEt.setText(snapshot.getString("userName"));
                regiNoEt.setText(snapshot.getString("regiNo"));
                permittedSwitch.setChecked(state);
                profileCompletedSwitch.setChecked(snapshot.getBoolean("profileCompleted"));
                driverTv.setText(snapshot.getBoolean("driver") ? "Driver" : "Student");
                lat = snapshot.getDouble("lat")==null?0.00:snapshot.getDouble("lat");
                lng = snapshot.getDouble("lng")==null?0.00: snapshot.getDouble("lng");
                try {
                    String img = snapshot.getString("idUrl");
                    byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
                    userEv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                } catch (NullPointerException e) {
                } catch (IllegalArgumentException e) {
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
                        String email, userName, regiNo;
                        email = emailEt.getText().toString().trim();
                        userName = userNameEt.getText().toString().trim();
                        regiNo = regiNoEt.getText().toString().trim();
                        Log.d(TAG, "onClick: " + email + " " + regiNo);
                        documentReference.update("email", email);
                        documentReference.update("userName", userName);
                        documentReference.update("regiNo", regiNo);
                        documentReference.update("permitted", permittedSwitch.isChecked());
                        if(state!=permittedSwitch.isChecked())notifyUsingActivity(uId,permittedSwitch.isChecked());
                        documentReference.update("profileCompleted", profileCompletedSwitch.isChecked());
                    }
                })
                .setNegativeButton("cancel", null)
                .setNeutralButton("Get Location", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: "+ lat +","+ lng);
                        startGoogleMaps(lat, lng);
                    }
                })
                .show();
    }


    void startGoogleMaps(double latitude, double longitude){
        if(latitude==0.00 && longitude==0.00){
            Toast.makeText(getActivity(),"No Location Found",Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmIntentUri = Uri.parse("geo:"+latitude+ ","+ longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }
    private void notifyUsingActivity(String uId, boolean state){
        ((AdminPanelActivity)this.getActivity()).notifyUser(uId,state);
    }
    private Activity getRootActivity(){
        return  ((AdminPanelActivity)this.getActivity());
    }
}
