package com.sis.clightapp.activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.sis.clightapp.R;
import com.sis.clightapp.ViewPager.CustomViewPager;
import com.sis.clightapp.ViewPager.FragmentAdapter;
import com.sis.clightapp.fragments.checkout.CheckOutFragment1;
import com.sis.clightapp.fragments.checkout.CheckOutsFragment2;
import com.sis.clightapp.fragments.checkout.CheckOutsFragment3;
import com.sis.clightapp.session.MyLogOutService;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CheckOutMain11 extends BaseActivity {
    private DrawerLayout drawerLayout;
    private CustomViewPager customViewPager;
//    ImageView logoIcon,cartIcon;
    int setwidht,setheight;
    ProgressBar progressBar;
    public ActionBar actionbar;
    int width ;
    int height;
    private  boolean staus=true;

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
        setContentView(R.layout.activity_check_out_main11);
         width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Log.e(TAG,"Mode:Login As UserMode/CheckOut");
        initView();
        //configureToolbar(R.drawable.ic_menu, "",0);
        setViewPagerAdapter();
        configureNavigationDrawer();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarcheckout);
        ImageView navImg=(ImageView) toolbar.findViewById(R.id.imageView9);
        RelativeLayout cartIconImg=(RelativeLayout) toolbar.findViewById(R.id.imageView8);
        cartIconImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customViewPager.setCurrentItem(2);
            }
        });

        navImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawerLayouttemp = (DrawerLayout) findViewById(R.id.checkoutdrawer_layout);
                NavigationView navView = (NavigationView) findViewById(R.id.checkoutnavigation);
                drawerLayouttemp.openDrawer(GravityCompat.START);

            }
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

//        logoIcon=findViewById(R.id.logoicon);
//        cartIcon=findViewById(R.id.carticon);

        progressBar=findViewById(R.id.pb_home);
        customViewPager = findViewById(R.id.custom_view_pager);
        drawerLayout = (DrawerLayout) findViewById(R.id.checkoutdrawer_layout);
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
                        CheckOutFragment1 firstFragment = (CheckOutFragment1) getSupportFragmentManager().getFragments().get(0);
                    //    firstFragment.hideCheckBox();
                        firstFragment.refreshAdapter(true);
                        break;
                    case 1:
                         CheckOutsFragment2 secondFragment = (CheckOutsFragment2) getSupportFragmentManager().getFragments().get(1);
                           secondFragment.refreshList();
                        break;
                    case 2:
                        CheckOutsFragment3 thirdFragment = (CheckOutsFragment3) getSupportFragmentManager().getFragments().get(2);
                        thirdFragment.refreshAdapter();
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
        fragmentList.add(new CheckOutFragment1().getInstance());
        fragmentList.add(new CheckOutsFragment2().getInstance());
        fragmentList.add(new CheckOutsFragment3().getInstance());
        return fragmentList;
    }
    private void configureNavigationDrawer() {
        int width  = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht=width*45;
        setwidht=setwidht/100;
        setheight=height/2;

        drawerLayout = (DrawerLayout) findViewById(R.id.checkoutdrawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.checkoutnavigation);
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
                }
                else if (itemId == R.id.menu_3) {
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
    public void swipeToCheckOutFragment3(int fragmentPosition) {
        customViewPager.setCurrentItem(fragmentPosition);
    }
    // Creating exit dialogue
    public void ask_exit(){
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog=new Dialog(getApplicationContext());
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
        Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        goAlertDialogwithOneBTnDialog.setCancelable(false);
        final TextView alertTitle_tv=goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
        final TextView alertMessage_tv=goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
        final Button yesbtn=goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
        final Button nobtn=goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
        yesbtn.setText("Yes");
        nobtn.setText("No");
        alertTitle_tv.setText(getString(R.string.exit_title));
        alertMessage_tv.setText(getString(R.string.exit_subtitle));
        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.clearAllPrefExceptOfSShkeyPassword(getApplicationContext());
                Intent ii=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(ii);
                finish();
            }
        });
        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAlertDialogwithOneBTnDialog.dismiss();
            }
        });
        goAlertDialogwithOneBTnDialog.show();
    }
    public void  clearcache() {
        sharedPreferences.clearAllPrefExceptOfSShkeyPassword(getApplicationContext());
    }
    @Override
    public void onBackPressed() {
        int position=  customViewPager.getCurrentItem();
        switch (position) {
            case 0:
                CheckOutFragment1 firstFragment = (CheckOutFragment1) getSupportFragmentManager().getFragments().get(0);
                firstFragment.onBackPressed();
                break;
            case 1:
                CheckOutsFragment2 secondFragment = (CheckOutsFragment2) getSupportFragmentManager().getFragments().get(1);
                secondFragment.onBackPressed();
                break;
            case 2:
                CheckOutsFragment3 thirdFragment = (CheckOutsFragment3) getSupportFragmentManager().getFragments().get(2);
                thirdFragment.onBackPressed();
                break;
            default:
                return;
        }
    }
    public void updateCartIcon(int count) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarcheckout);
        TextView  actionbar_notifcation_textview=toolbar.findViewById(R.id.textView8);
        if(count==0) {
            actionbar_notifcation_textview.setBackground(getDrawable(R.drawable.before));
            actionbar_notifcation_textview.setText("");
        }
        else {
            actionbar_notifcation_textview.setBackground(getDrawable(R.drawable.after));
            actionbar_notifcation_textview.setText(String.valueOf(count));
        }
    }
    }