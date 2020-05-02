package com.sustbus.driver.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sustbus.driver.R;
import com.sustbus.driver.util.RecyclerViewAdapter2;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ShowNotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter2 mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private CountDownTimer countDownTimer;
    private TextView textView;
    private View root;

    public ShowNotificationsFragment(RecyclerView.LayoutManager layoutManager, RecyclerViewAdapter2 mAdapter) {
        this.layoutManager = layoutManager;
        this.mAdapter = mAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DEBMES", "oncreat");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root != null) return root;
        root = inflater.inflate(R.layout.fragment_show_notifications, container, false);
        textView = root.findViewById(R.id.show_notification_tv);
        recyclerView = root.findViewById(R.id.show_notification_recycleView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        countDownTimer = new CountDownTimer(1000000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mAdapter.getItemCount() == 0) textView.setVisibility(View.VISIBLE);
                else textView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimer.start();

        return root;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DEBMES", "destroy");
        countDownTimer.cancel();
        layoutManager = null;
        recyclerView.setLayoutManager(null);
        recyclerView.setAdapter(null);
    }
}
