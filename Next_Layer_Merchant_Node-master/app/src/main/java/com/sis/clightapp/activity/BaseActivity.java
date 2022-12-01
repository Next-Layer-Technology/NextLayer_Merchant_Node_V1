package com.sis.clightapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.MyApplication;
import com.sis.clightapp.session.MyLogOutService;

import java.io.File;

public class BaseActivity extends AppCompatActivity {
    Context bContext;
    Activity bActivity;
    final String ISMERCHANTLOGIN = "ismerchantlogin";
    final String MERCHANTID = "merchantid";
    final String ISSERVERLOGIN = "isserverlogin";
    final String SERVERURL = "serverurl";
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

    public BaseActivity() {
    }

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            return (returnVal == 0);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        startService(new Intent(bContext, MyLogOutService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(bContext, MyLogOutService.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        appInitialization();
    }

    private void appInitialization() {
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }


    private final Thread.UncaughtExceptionHandler _unCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(@NonNull Thread thread, Throwable ex) {
            ex.printStackTrace();
            startActivity(new Intent(bContext, MainEntryActivityNew.class));
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    private void initView() {
        bContext = this;
        bActivity = this;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(bContext);
        sharedPreferences = new CustomSharedPreferences();
    }


    public void showToast(String message) {
        Toast.makeText(bContext, message, Toast.LENGTH_SHORT).show();
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
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(bActivity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                lat = wayLatitude;
                                lon = wayLongitude;
                                Log.e(TAG, "Lattitude:" + wayLatitude + " Longitude: " + wayLongitude);
                                GlobalState.getInstance().setLattitude(String.valueOf(wayLatitude));
                                GlobalState.getInstance().setLongitude(String.valueOf(wayLongitude));
                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
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


    public void clodeSoftKeyBoard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
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
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 123);
                            }
                        });

                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(bActivity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            }
        }
    }

    boolean isSshKeyPassExist() {
        boolean isSshkeypassExist = false;
        if (sharedPreferences.getString("sshkeypass", bContext) != null) {
            isSshkeypassExist = !sharedPreferences.getString("sshkeypass", bContext).isEmpty();

        }
        return isSshkeypassExist;
    }

    boolean isSsKeyFileExist() {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        assert yourFile != null;
        return yourFile.exists();
    }
}
