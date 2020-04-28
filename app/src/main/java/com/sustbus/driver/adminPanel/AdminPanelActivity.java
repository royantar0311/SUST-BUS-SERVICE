package com.sustbus.driver.adminPanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.sustbus.driver.R;

public class AdminPanelActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        tabLayout = findViewById(R.id.admin_tab_layout);
        viewPager = findViewById(R.id.admin_view_pager);

        fragmentManager = getSupportFragmentManager();
        viewPagerAdapter = new ViewPagerAdapter(fragmentManager,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerAdapter.addFragment(new StudentsListFragment(),"Students List");
        viewPagerAdapter.addFragment(new DriversListFragment(),"Drivers List");
        viewPagerAdapter.addFragment(new CustomQueryFragment(),"Custom Query");
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
