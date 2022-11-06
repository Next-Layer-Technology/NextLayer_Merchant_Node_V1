package com.sis.clightapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sis.clightapp.Network.CheckNetwork;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.GpsUtils;
import com.sis.clightapp.Utills.LocationHelper;
import com.sis.clightapp.Utills.MyApplication;
import com.sis.clightapp.session.MyLogOutService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BaseActivity extends AppCompatActivity {
    protected MyApplication mApp = new MyApplication();
    Context bContext;
    Activity bActivity;
    Long time_captured, time_ended;
    private static final String ACTION_FINISH = "action_finish";
    int INTENT_AUTHENTICATE = 1234;
    //    ApiPaths bApiPaths;
//    Functions functions;
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
    final boolean ACTIVE = true;
    DisplayMetrics displayMetrics;
    CustomSharedPreferences sharedPreferences;
    public ActionBar actionbar;
    ProgressDialog dialog, loginDialog;
    String TAG = "CLighting App";
    double lat, lon;
    private boolean isLocFetch = false;
    private Location currentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    // private boolean isContinue = false;
    private boolean isGPS = false;
    ProgressDialog loginLodingProgressDialog;
    int iii = 0;
    //    private boolean isLocFetch=false;
    private StringBuilder stringBuilder;
    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,

    };
    private Thread.UncaughtExceptionHandler defaultUEH;

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal == 0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public boolean isInt(Object obj) {

        boolean type = false;
        if (obj instanceof Integer) {
            type = true;
        } else {
            type = false;
        }
        return type;
    }

    public boolean isString(Object obj) {

        boolean type = false;
        if (obj instanceof String) {
            type = true;
        } else {
            type = false;
        }
        return type;
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

        // showToast("start");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(bContext, MyLogOutService.class));
        // showToast("stop");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        appInitialization();
    }

    private void appInitialization() {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }


    // handler listener
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            // TODO handle exception here
            showToast("GTH");
            Toast.makeText(bContext, "asasasas", Toast.LENGTH_LONG).show();
            startActivity(new Intent(bContext, MainEntryActivity.class));
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
//        functions = new Functions();
//        bApiPaths = functions.retrofitBuilder();
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

    public boolean isEmailValid(CharSequence email) {
        return !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean toBooleanDefaultIfNull(Boolean bool) {
        if (bool == null) return false;
        return bool.booleanValue();
    }

    public void checkLocationPermisiion() {
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                //call function

                if (CheckNetwork.isInternetAvailable(bContext)) {
                    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                    boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!enabled) {
                        showToast("Turn On GPS");
                        new GpsUtils(bContext).turnGPSOn(new GpsUtils.onGpsListener() {
                            @Override
                            public void gpsStatus(boolean isGPSEnable) {
                                // turn on GPS
                                isGPS = isGPSEnable;
                            }
                        });
                    }
                    if (ActivityCompat.checkSelfPermission(bContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(bContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || !enabled) {
                        ActivityCompat.requestPermissions(bActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                AppConstants.LOCATION_REQUEST);
                    }
                } else {
                    showToast("Location Not Found");
                }
                ha.postDelayed(this, 180000);
            }
        }, 180000);
    }

    public void getDeviceLocation() {
        LocationHelper locationHelper = new LocationHelper(bContext);
        locationHelper.startListeningUserLocation(new LocationHelper.MyLocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                // Here you got user location :)
                GlobalState.getInstance().setLongitude(String.valueOf(location.getLongitude()));
                GlobalState.getInstance().setLattitude(String.valueOf(location.getLatitude()));
                isLocFetch = true;
                Log.e("Location", String.valueOf(location.getLatitude()) + "_" + String.valueOf(location.getLongitude()));

            }


        });
    }

    public void getLatestLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        lat = wayLatitude;
                        lon = wayLongitude;
                        // isLocFetch=true;
                        setLocationFetch(true);
                        Log.e(TAG, "Lattitude:" + wayLatitude + " Longitude: " + wayLongitude);
                        GlobalState.getInstance().setLattitude(String.valueOf(wayLatitude));
                        GlobalState.getInstance().setLongitude(String.valueOf(wayLongitude));
//                        if (!isContinue) {

                        //showToast("CallBack:"+String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                        //  txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
//                        } else {
//                            stringBuilder=new StringBuilder();
//                            stringBuilder.append(wayLatitude);
//                            stringBuilder.append("-");
//                            stringBuilder.append(wayLongitude);
//                            stringBuilder.append("\n\n");
//                            //  txtContinueLocation.setText(stringBuilder.toString());
//                            showToast("Continous Location:"+stringBuilder.toString());
//                        }
                        if (mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };


        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            setLocationFetch(false);
            return;
        }
        // isContinue = false;
        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(bContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(bContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(bActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
//            if (isContinue) {
//                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
//            } else {

            mFusedLocationClient.getLastLocation().addOnSuccessListener(bActivity, new OnSuccessListener<Location>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        lat = wayLatitude;
                        lon = wayLongitude;
                        //isLocFetch=true;
                        setLocationFetch(true);
                        Log.e(TAG, "Lattitude:" + wayLatitude + " Longitude: " + wayLongitude);
                        GlobalState.getInstance().setLattitude(String.valueOf(wayLatitude));
                        GlobalState.getInstance().setLongitude(String.valueOf(wayLongitude));
                        //showToast(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                        //  txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                    } else {
                        //isLocFetch=false;
                        setLocationFetch(false);
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                }
            });
//            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
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
                                setLocationFetch(true);
                                //isLocFetch=true;
                                Log.e(TAG, "Lattitude:" + wayLatitude + " Longitude: " + wayLongitude);
                                GlobalState.getInstance().setLattitude(String.valueOf(wayLatitude));
                                GlobalState.getInstance().setLongitude(String.valueOf(wayLongitude));
                                // showToast(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                                //txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                            } else {
                                //isLocFetch=false;
                                setLocationFetch(false);
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        }
                    });
//                    }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

    public boolean isLocationFetch() {
        return isLocFetch;
    }

    public void setLocationFetch(boolean state) {
        isLocFetch = state;
    }

    public String getLatLong() {

        if (GlobalState.getInstance().getLattitude() != null || GlobalState.getInstance().getLongitude() != null) {
            return GlobalState.getInstance().getLattitude() + "_" + GlobalState.getInstance().getLongitude();

        } else {
            // getDeviceLocation();
            return "0.0" + "_" + "0.0";
        }

    }

    public String excatFigure(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));
        return d.toPlainString();
    }

    public void clodeSoftKeyBoard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void clearBackStack() {
        sendBroadcast(new Intent(ACTION_FINISH));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForPermission();
    }

    public Boolean checkForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

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
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode) {
//            case 123: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    if ((ContextCompat.checkSelfPermission(this,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ) == PackageManager.PERMISSION_GRANTED)
//                    ) {
//                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//            }
//        }
//    }

    boolean isSshKeyPassExist() {
        boolean isSshkeypassExist = false;
        if (sharedPreferences.getString("sshkeypass", bContext) != null) {
            if (sharedPreferences.getString("sshkeypass", bContext).isEmpty()) {
                isSshkeypassExist = false;
            } else {
                isSshkeypassExist = true;
            }

        } else {
            isSshkeypassExist = false;
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
        return yourFile.exists();
    }

//    @Override
//    public void onUserInteraction() {
//        super.onUserInteraction();
//        showToast("chal gya g");
//        new CustomSharedPreferences().setcountertime(System.currentTimeMillis(), "countertime", bContext);
//        time_captured = new CustomSharedPreferences().getcountertime("countertime", bContext);
//        time_ended = time_captured + 30000;
//    }

}
