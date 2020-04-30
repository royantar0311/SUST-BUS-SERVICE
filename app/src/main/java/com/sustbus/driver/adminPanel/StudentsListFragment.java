package com.sustbus.driver.adminPanel;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sustbus.driver.R;
import com.sustbus.driver.util.UserInfo;

public class StudentsListFragment extends Fragment implements StudentsRecyclerAdapter.CheckChangedListener {
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
}
