package com.sis.clightapp.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.sis.clightapp.R;
import com.sis.clightapp.ViewPager.CustomViewPager;
import com.sis.clightapp.ViewPager.FragmentAdapter;
import com.sis.clightapp.fragments.merchant.MerchantFragment1;
import com.sis.clightapp.fragments.merchant.MerchantFragment2;
import com.sis.clightapp.fragments.merchant.MerchantFragment3;
import com.sis.clightapp.fragments.shared.ExitDialogFragment;
import com.sis.clightapp.session.MyLogOutService;
import com.sis.clightapp.util.GlobalState;

import java.util.ArrayList;
import java.util.List;

public class MerchantMainActivity extends BaseActivity {
    private DrawerLayout drawerLayout;
    private CustomViewPager customViewPager;
    int setwidht, setheight;
    ProgressBar progressBar;
    public ActionBar actionbar;
    private boolean staus = true;
    private Handler handler;
    Runnable my_runnable;

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
        setContentView(R.layout.activity_merchnat_main11);
        progressBar = findViewById(R.id.pb_home);
        initView();
        // configureToolbar(R.drawable.ic_menu, "");
        setViewPagerAdapter();
        configureNavigationDrawer();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarmerchant);
        ImageView navImg = (ImageView) toolbar.findViewById(R.id.imageView9);
        navImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawerLayouttemp = (DrawerLayout) findViewById(R.id.merchantdrawer_layout);
                NavigationView navView = (NavigationView) findViewById(R.id.merchantnavigation);
                drawerLayouttemp.openDrawer(GravityCompat.START);
//                showToast("Clciked");
            }
        });


//        final Handler ha=new Handler();
//        ha.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                //call function
//
//                if(CheckNetwork.isInternetAvailable(MerchnatMain11.this))
//                {
//
//                    // getHeartBeat();
//                }
//                else {
//
//                }
//                ha.postDelayed(this, 180000);
//            }
//        }, 180000);

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
        drawerLayout = (DrawerLayout) findViewById(R.id.merchantdrawer_layout);
    }

    private void setViewPagerAdapter() {

        customViewPager.setPagingEnabled(false);
        FragmentAdapter pagerAdapter = new FragmentAdapter(getSupportFragmentManager(), 0, getFragment());

        customViewPager.setAdapter(pagerAdapter);
        customViewPager.setOffscreenPageLimit(5);
    }

    private List<Fragment> getFragment() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new MerchantFragment1());
        fragmentList.add(new MerchantFragment2());
        fragmentList.add(new MerchantFragment3());
        return fragmentList;
    }

    private void configureNavigationDrawer() {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht = width * 45;
        setwidht = setwidht / 100;
        setheight = height / 2;
        drawerLayout = (DrawerLayout) findViewById(R.id.merchantdrawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.merchantnavigation);
        View headerView = navView.getHeaderView(0);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

                //showToast("drwaer open");
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menu_1) {
                    setFragment(0);
                } else if (itemId == R.id.menu_2) {
                    setFragment(1);
                } else if (itemId == R.id.menu_3) {
                    setFragment(2);
                }
                return false;
            }
        });
    }

    private void setFragment(int fragmentPosition) {
        drawerLayout.closeDrawers();
        customViewPager.setCurrentItem(fragmentPosition);
    }

    public void clearcache() {
        sharedPreferences.clearAllPrefExceptOfSShkeyPassword(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        new ExitDialogFragment(() -> {
            clearAndGoBack();
            return null;
        }).show(getSupportFragmentManager(), null);
    }

    public void clearAndGoBack() {
        this.stopService(new Intent(this, MyLogOutService.class));
        Intent ii = new Intent(this, HomeActivity.class);
        startActivity(ii);
    }
}
