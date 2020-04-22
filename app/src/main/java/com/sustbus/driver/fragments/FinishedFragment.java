package com.sustbus.driver.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sustbus.driver.R;
import com.sustbus.driver.util.RecyclerViewAdapter;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class FinishedFragment extends Fragment {

    public RecyclerView.LayoutManager mLayoutManager;
    public RecyclerViewAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View root =inflater.inflate(R.layout.fragment_finished, container, false);
        recyclerView=root.findViewById(R.id.finished_recycler_view);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLayoutManager=null;
        recyclerView.setAdapter(null);
    }



}
