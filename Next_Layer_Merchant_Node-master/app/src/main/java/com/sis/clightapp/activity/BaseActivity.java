package com.sis.clightapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sis.clightapp.session.MyLogOutService;
import com.sis.clightapp.util.CustomSharedPreferences;
import com.sis.clightapp.util.GlobalState;

public class BaseActivity extends AppCompatActivity {
    final String IS_USER_LOGIN = "isuserlogin";
    final String LASTDATE = "lastdate";
    final String THORSTATUS = "thorstatus";
    final String LIGHTNINGSTATUS = "lightningstatus";
    final String BITCOINSTATUS = "bitcoinstatus";
    final String ISALLSERVERUP = "isallserverup";
    CustomSharedPreferences sharedPreferences;
    ProgressDialog dialog, loginDialog;
    String TAG = "CLighting App";
    double lat, lon;
    private FusedLocationProviderClient mFusedLocationClient;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    ProgressDialog loginLodingProgressDialog;

    @Override
    public void onStart() {
        super.onStart();
        startService(new Intent(this, MyLogOutService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MyLogOutService.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    private void initView() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sharedPreferences = new CustomSharedPreferences();
    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    void setTextWithSpan(TextView textView, String text, String spanText, StyleSpan style) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        int start = text.indexOf(spanText);
        int end = start + spanText.length();
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(sb);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            wayLatitude = location.getLatitude();
                            wayLongitude = location.getLongitude();
                            lat = wayLatitude;
                            lon = wayLongitude;
                            Log.e(TAG, "Latitude:" + wayLatitude + " Longitude: " + wayLongitude);
                            GlobalState.getInstance().setLattitude(String.valueOf(wayLatitude));
                            GlobalState.getInstance().setLongitude(String.valueOf(wayLongitude));
                        } else {
                            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        }
                    });
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 123: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForPermission();
    }

    public void checkForPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission necessary");
                alertBuilder.setMessage("Write Storage permission is necessary for using this App!!!");
                alertBuilder.setPositiveButton("Yes",
                        (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, 123);
                        });

                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            }
        }
    }
}
