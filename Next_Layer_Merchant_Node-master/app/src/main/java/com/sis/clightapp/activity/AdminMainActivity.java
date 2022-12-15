package com.sis.clightapp.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;
import com.sis.clightapp.R;

import com.sis.clightapp.ViewPager.CustomViewPager;
import com.sis.clightapp.ViewPager.FragmentAdapter;
import com.sis.clightapp.fragments.admin.AdminFragment1;
import com.sis.clightapp.fragments.shared.ExitDialogFragment;
import com.sis.clightapp.session.MyLogOutService;


import java.util.ArrayList;
import java.util.List;


public class AdminMainActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private CustomViewPager customViewPager;
    int setwidht, setheight;
    ProgressBar progressBar;

    @Override
    public void onDestroy() {
        finish();
        super.onDestroy();
        Runtime.getRuntime().gc();
        System.gc();
        stopService(new Intent(this, MyLogOutService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main11);
        progressBar = findViewById(R.id.pb_home);
        initView();
        setViewPagerAdapter();
        configureNavigationDrawer();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbaradmin);
        ImageView navImg = (ImageView) toolbar.findViewById(R.id.imageView9);
        navImg.setOnClickListener(view -> {
            DrawerLayout drawerLayouttemp = (DrawerLayout) findViewById(R.id.admindrawer_layout);
            drawerLayouttemp.openDrawer(GravityCompat.START);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return true;
    }

    private void initView() {

        customViewPager = findViewById(R.id.custom_view_pager);
        drawerLayout = (DrawerLayout) findViewById(R.id.admindrawer_layout);
    }

    private void setViewPagerAdapter() {

        customViewPager.setPagingEnabled(false);
        FragmentAdapter pagerAdapter = new FragmentAdapter(getSupportFragmentManager(), 0, getFragment());

        customViewPager.setAdapter(pagerAdapter);
        customViewPager.setOffscreenPageLimit(5);
    }

    private List<Fragment> getFragment() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new AdminFragment1());
        return fragmentList;
    }

    private void configureNavigationDrawer() {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht = width * 45;
        setwidht = setwidht / 100;
        setheight = height / 2;
        drawerLayout = (DrawerLayout) findViewById(R.id.admindrawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.adminnavigation);
        navView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_1) {
                setFragment();
            }
            return false;
        });
    }

    private void setFragment() {
        drawerLayout.closeDrawers();
        customViewPager.setCurrentItem(0);
    }


    @Override
    public void onBackPressed() {
        new ExitDialogFragment(() -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("isFromLogin", true);
            startActivity(intent);
            finish();
            return null;
        }).show(getSupportFragmentManager(), null);
    }
}
