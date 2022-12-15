package com.sis.clightapp.activity;

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

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.sis.clightapp.R;
import com.sis.clightapp.ViewPager.CustomViewPager;
import com.sis.clightapp.ViewPager.FragmentAdapter;
import com.sis.clightapp.fragments.checkout.CheckOutFragment1;
import com.sis.clightapp.fragments.checkout.CheckOutsFragment2;
import com.sis.clightapp.fragments.checkout.CheckOutsFragment3;
import com.sis.clightapp.fragments.shared.ExitDialogFragment;
import com.sis.clightapp.session.MyLogOutService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CheckOutMainActivity extends BaseActivity {
    private DrawerLayout drawerLayout;
    private CustomViewPager customViewPager;
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
        setContentView(R.layout.activity_check_out_main11);
        Log.e(TAG, "Mode:Login As UserMode/CheckOut");
        initView();
        setViewPagerAdapter();
        configureNavigationDrawer();
        Toolbar toolbar = findViewById(R.id.toolbarcheckout);
        ImageView navImg = toolbar.findViewById(R.id.imageView9);
        RelativeLayout cartIconImg = toolbar.findViewById(R.id.imageView8);
        cartIconImg.setOnClickListener(view -> customViewPager.setCurrentItem(2));

        navImg.setOnClickListener(view -> {
            DrawerLayout drawer = findViewById(R.id.checkoutdrawer_layout);
            drawer.openDrawer(GravityCompat.START);
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
        progressBar = findViewById(R.id.pb_home);
        customViewPager = findViewById(R.id.custom_view_pager);
        drawerLayout = findViewById(R.id.checkoutdrawer_layout);
    }

    private void setViewPagerAdapter() {
        customViewPager.setPagingEnabled(false);
        FragmentAdapter pagerAdapter = new FragmentAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,getFragment());
        customViewPager.setAdapter(pagerAdapter);
        customViewPager.setOffscreenPageLimit(5);
        customViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        CheckOutFragment1 firstFragment = (CheckOutFragment1) getSupportFragmentManager().getFragments().get(0);
                        firstFragment.setAdapter();
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
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private List<Fragment> getFragment() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new CheckOutFragment1());
        fragmentList.add(new CheckOutsFragment2());
        fragmentList.add(new CheckOutsFragment3());
        return fragmentList;
    }

    private void configureNavigationDrawer() {
        drawerLayout = findViewById(R.id.checkoutdrawer_layout);
        NavigationView navView = findViewById(R.id.checkoutnavigation);

        navView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_1) {
                setFragment(0);
            } else if (itemId == R.id.menu_2) {
                setFragment(1);
            } else if (itemId == R.id.menu_3) {
                setFragment(2);
            }
            return false;
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
    public void ask_exit() {
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog = new Dialog(getApplicationContext());
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
        Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        goAlertDialogwithOneBTnDialog.setCancelable(false);
        final TextView alertTitle_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
        final TextView alertMessage_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
        final Button yesbtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
        final Button nobtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
        yesbtn.setText("Yes");
        nobtn.setText("No");
        alertTitle_tv.setText(getString(R.string.exit_title));
        alertMessage_tv.setText(getString(R.string.exit_subtitle));
        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.clearAllPrefExceptOfSShkeyPassword(getApplicationContext());
                Intent ii = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(ii);
                finish();
            }
        });
        nobtn.setOnClickListener(v -> goAlertDialogwithOneBTnDialog.dismiss());
        goAlertDialogwithOneBTnDialog.show();
    }

    @Override
    public void onBackPressed() {
        new ExitDialogFragment(() -> {
            stopService(new Intent(this, MyLogOutService.class));
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return null;
        }).show(getSupportFragmentManager(), null);
    }

    public void updateCartIcon(int count) {
        Toolbar toolbar = findViewById(R.id.toolbarcheckout);
        TextView actionbar_notifcation_textview = toolbar.findViewById(R.id.textView8);
        if (count == 0) {
            actionbar_notifcation_textview.setBackground(getDrawable(R.drawable.before));
            actionbar_notifcation_textview.setText("");
        } else {
            actionbar_notifcation_textview.setBackground(getDrawable(R.drawable.after));
            actionbar_notifcation_textview.setText(String.valueOf(count));
        }
    }
}