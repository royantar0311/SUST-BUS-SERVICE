package com.sustbus.driver.adminPanel;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sustbus.driver.R;
import com.sustbus.driver.util.UserInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;


public class DriversListFragment extends Fragment implements CheckChangedListener {
    private static final String TAG = "DriversListFragment";
    View view;
    RecyclerView recyclerView;
    DriversRecyclerAdapter recyclerAdapter;
    double lat,lng;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_drivers_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        recyclerView = view.findViewById(R.id.drivers_list_recycler_view);
        initRecyclerView(FirebaseAuth.getInstance().getCurrentUser());
    }

    private void initRecyclerView(FirebaseUser currentUser) {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("driver", true)
                .whereEqualTo("profileCompleted", true);
        FirestoreRecyclerOptions<UserInfo> options = new FirestoreRecyclerOptions.Builder<UserInfo>()
                .setQuery(query, UserInfo.class)
                .build();
        recyclerAdapter = new DriversRecyclerAdapter(options, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerAdapter.startListening();
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
                emailEt.setText(snapshot.getString("email"));
                userNameEt.setText(snapshot.getString("userName"));
                regiNoEt.setText(snapshot.getString("regiNo"));
                permittedSwitch.setChecked(snapshot.getBoolean("permitted"));
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
                        documentReference.update("profileCompleted", profileCompletedSwitch.isChecked());
                    }
                })
                .setNegativeButton("cancel", null)
                .setNeutralButton("Get Location", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGoogleMaps(lat,lng);
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
        startActivity(mapIntent);
    }
}
