package com.sustbus.driver.adminPanel;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sustbus.driver.R;
import com.sustbus.driver.util.NotificationSender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class AdminPanelActivity extends AppCompatActivity {
    private static final String TAG = "AdminPanelActivity";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private FragmentManager fragmentManager;
    private String SERVER_KEY;
    private NotificationSender notificationSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        tabLayout = findViewById(R.id.admin_tab_layout);
        viewPager = findViewById(R.id.admin_view_pager);

        fragmentManager = getSupportFragmentManager();
        viewPagerAdapter = new ViewPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerAdapter.addFragment(new TypeListFragment("student"), "Student list");
        viewPagerAdapter.addFragment(new TypeListFragment("driver"), "Driver list");
        viewPagerAdapter.addFragment(new TypeListFragment("staff"), "Staff list");
        viewPagerAdapter.addFragment(new TypeListFragment("teacher"), "Teacher list");
        viewPagerAdapter.addFragment(new CustomQueryFragment(), "Custom Query");
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        FirebaseFirestore.getInstance().collection("key").document("key").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    SERVER_KEY = task.getResult().getString("SERVER");
                    notificationSender = new NotificationSender(AdminPanelActivity.this.getApplicationContext(),
                            SERVER_KEY);
                }
            }
        });

    }

    protected void notifyUser(String uId, boolean state) {
        notificationSender.notifyUser(uId, state);
    }
}
