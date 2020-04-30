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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

public class StudentsListFragment extends Fragment implements CheckChangedListener {
    private static final String TAG = "StudentsListFragment";

    View view;
    RecyclerView recyclerView;
    StudentsRecyclerAdapter recyclerAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_students_list,container,false);
        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.students_list_recycler_view);
        initRecyclerView(FirebaseAuth.getInstance().getCurrentUser());
    }

    private void initRecyclerView(FirebaseUser currentUser) {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("driver",false)
                .whereEqualTo("profileCompleted",true);;
        FirestoreRecyclerOptions<UserInfo> options = new FirestoreRecyclerOptions.Builder<UserInfo>()
                .setQuery(query,UserInfo.class)
                .build();
        recyclerAdapter = new StudentsRecyclerAdapter(options,this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        recyclerAdapter.startListening();
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
                //                Glide.with(getContext())
//                        .load(snapshot.getString("idUrl"))
//                        .apply(new RequestOptions().override(200,200))
//                        .into(userEv);

                String img=snapshot.getString("idUrl");
                byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
                userEv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
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
