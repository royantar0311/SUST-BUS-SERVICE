package com.sustbus.driver.adminPanel;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class DriversListFragment extends Fragment implements DriversRecyclerAdapter.CheckChangedListener {
    private static final String TAG = "DriversListFragment";
    View view;
    RecyclerView recyclerView;
    DriversRecyclerAdapter recyclerAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_drivers_list,container,false);
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
                .whereEqualTo("driver",true)
                .whereEqualTo("profileCompleted",true);
        FirestoreRecyclerOptions<UserInfo> options = new FirestoreRecyclerOptions.Builder<UserInfo>()
                .setQuery(query,UserInfo.class)
                .build();
        recyclerAdapter = new DriversRecyclerAdapter(options,this);
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
    public void onItemClicked(DocumentSnapshot snapshot) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.forgot_password_alertdialog,null);
        new AlertDialog.Builder(getContext())
                .setTitle("Change Password")
                .setView(view)
                .show();
    }
}
