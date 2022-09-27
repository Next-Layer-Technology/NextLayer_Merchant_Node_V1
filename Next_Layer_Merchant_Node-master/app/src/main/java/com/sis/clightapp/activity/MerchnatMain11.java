package com.sis.clightapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.sis.clightapp.Network.CheckNetwork;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.NetworkManager;
import com.sis.clightapp.ViewPager.CustomViewPager;
import com.sis.clightapp.ViewPager.FragmentAdapter;
import com.sis.clightapp.fragments.checkout.CheckOutFragment1;
import com.sis.clightapp.fragments.checkout.CheckOutsFragment2;
import com.sis.clightapp.fragments.checkout.CheckOutsFragment3;
import com.sis.clightapp.fragments.merchant.MerchantFragment1;
import com.sis.clightapp.fragments.merchant.MerchantFragment2;
import com.sis.clightapp.fragments.merchant.MerchantFragment3;
import com.sis.clightapp.session.MyLogOutService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MerchnatMain11 extends BaseActivity {
    private DrawerLayout drawerLayout;
    private CustomViewPager customViewPager;
    int setwidht,setheight;
    ProgressBar progressBar;
    public ActionBar actionbar;
    private  boolean staus=true;
    private Handler handler;
    Runnable my_runnable;
    @Override
    public void onDestroy() {
        finish();
        super.onDestroy();
        Runtime.getRuntime().gc();
        System.gc();
        stopService(new Intent(bContext, MyLogOutService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchnat_main11);
        progressBar=findViewById(R.id.pb_home);
        initView();
       // configureToolbar(R.drawable.ic_menu, "");
        setViewPagerAdapter();
        configureNavigationDrawer();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarmerchant);
        ImageView navImg=(ImageView) toolbar.findViewById(R.id.imageView9);
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

        customViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                // showToast(String.valueOf(position));

                switch (position) {
                    case 0:
                        //do nothing
                        MerchantFragment1 merchantFragment11 = (MerchantFragment1) getSupportFragmentManager().getFragments().get(0);
                        merchantFragment11.isInAppMerchant1=true;
                        break;
                    case 1:
                        MerchantFragment1 merchantFragment1 = (MerchantFragment1) getSupportFragmentManager().getFragments().get(0);
                        merchantFragment1.isInAppMerchant1=false;
                        MerchantFragment2 merchantFragment2 = (MerchantFragment2) getSupportFragmentManager().getFragments().get(1);
                        merchantFragment2.reLoadItemsInList();
                        break;
                    case 2:
                        //do nothing
                        MerchantFragment3 merchantFragment3 = (MerchantFragment3) getSupportFragmentManager().getFragments().get(2);
                        merchantFragment3.checkAppFlow();
                        break;
                    default:
                        return;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    private List<Fragment> getFragment() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new MerchantFragment1().getInstance());
        fragmentList.add(new MerchantFragment2().getInstance());
        fragmentList.add(new MerchantFragment3().getInstance());
        return fragmentList;
    }
    private void configureNavigationDrawer() {

        int width  = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht=width*45;
        setwidht=setwidht/100;
        setheight=height/2;
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

    public void  clearcache()
    {
        sharedPreferences.clearAllPrefExceptOfSShkeyPassword(getApplicationContext());
    }
    @Override
    public void onBackPressed() {
       int position=  customViewPager.getCurrentItem();
        switch (position) {
            case 0:
                MerchantFragment1 firstFragment = (MerchantFragment1) getSupportFragmentManager().getFragments().get(0);
                firstFragment.onBackPressed();
                break;
            case 1:
                MerchantFragment2 secondFragment = (MerchantFragment2) getSupportFragmentManager().getFragments().get(1);
                secondFragment.onBackPressed();
                break;
            case 2:
                MerchantFragment3 thirdFragment = (MerchantFragment3) getSupportFragmentManager().getFragments().get(2);
                thirdFragment.onBackPressed();
                break;
            default:
                return;
        }
        }
}
