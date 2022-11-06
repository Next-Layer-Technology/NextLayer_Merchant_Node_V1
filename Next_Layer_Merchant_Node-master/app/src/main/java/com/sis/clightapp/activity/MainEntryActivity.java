package com.sis.clightapp.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sis.clightapp.Interface.ApiClientStartStop;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.NetworkManager;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp;
import com.sis.clightapp.model.server.ServerData;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainEntryActivity extends BaseActivity {
    int count1 = 0;
    Handler handler;
    Runnable r;
    Button connect, scanQR;
    EditText serverlink;
    String serverURL = null;
    static boolean connectserverstatus = false;
    String TAG = "CLighting App";
    //private static final String TAG = MainActivity.class.getSimpleName();
    //private GoogleApiClient apiClient;
    private Location currentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isContinue = false;
    private boolean isGPS = false;
    private boolean isLocFetch = false;
    private StringBuilder stringBuilder;
    //qr code scanner object
    private IntentIntegrator qrScan;
    //MerchantLoginConfir
    EditText et_merchantid;
    Button confirm_btn;
    boolean isConfirmMerchant = false;
    boolean isConfirmThorActive = false;
    ProgressDialog confirmingProgressDialog;
    TextView setTextWithSpan;
    Button startNodeBtn, stopNodeBtn;
    MerchantData currentMerchantData;
    ProgressDialog startServerPD, stopServerPD, wait20SecPD;
    TextView result_Node;
    Dialog getAndConfirmSshKeyDialog;

    boolean isBitcoinConfirmed = false;
    boolean isThorConfirmed = false;
    boolean isLightningConfirmed = false;
    ProgressDialog checkStatusPD;


    int recallTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Here, thisActivity is the current activity
        setContentView(R.layout.activity_main_entry2);
        //  checkForPermission();
        sharedPreferences = new CustomSharedPreferences();
        setTextWithSpan = findViewById(R.id.imageView3);
        startNodeBtn = findViewById(R.id.startNodeBtn);
        stopNodeBtn = findViewById(R.id.stopNodeBtn);
        result_Node = findViewById(R.id.result_Node);
        isConfirmThorActive = false;
        startServerPD = new ProgressDialog(MainEntryActivity.this);
        startServerPD.setMessage("Connecting...");
        startServerPD.setCancelable(false);
        stopServerPD = new ProgressDialog(MainEntryActivity.this);
        stopServerPD.setMessage("Connecting...");
        stopServerPD.setCancelable(false);
        checkStatusPD = new ProgressDialog(bContext);
        checkStatusPD.setMessage("Loading...");
        checkStatusPD.setCancelable(false);
        wait20SecPD = new ProgressDialog(bContext);
        wait20SecPD.setMessage("Loading...");
        wait20SecPD.setCancelable(false);

        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);
        confirmingProgressDialog = new ProgressDialog(MainEntryActivity.this);
        confirmingProgressDialog.setMessage("Confirming...");
        confirmingProgressDialog.setCancelable(false);
        //Confirm MerchantID
        et_merchantid = findViewById(R.id.merchantid_et);
        confirm_btn = findViewById(R.id.confirm);
        //intializing scan object
        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        String prompt = getResources().getString(R.string.enter_Sovereign_key_via_qr);
        qrScan.setPrompt(prompt);
        //View objects
        serverlink = findViewById(R.id.et_email);
        dialog = new ProgressDialog(MainEntryActivity.this);
        dialog.setMessage("Connecting...");
        Log.e(TAG, "App Start");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //   checkcache();    TODO//:old version will use it after testing
        connect = findViewById(R.id.btn_connect);
        scanQR = findViewById(R.id.btn_scanQr);


        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clodeSoftKeyBoard();
                String merchantId = et_merchantid.getText().toString();
                if (merchantId != null) {
                    if (!merchantId.isEmpty()) {
//                        findMerchant(merchantId);
                    } else {
                        showToast("Please Enter Merchant Id");
                    }
                } else {
                    showToast("Please Enter Merchant Id1");
                }
            }
        });
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConfirmMerchant) {
                    if (isConfirmThorActive) {
                        qrScan.initiateScan();
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Sovereign is Not Connect!!!", "OK", "");
                    }

                } else {
                    goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                }
            }

        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConfirmMerchant) {
//                    isConfirmThorActive=true;  // For test only
                    if (isConfirmThorActive) {
                        connectServerMethod(serverlink.getText().toString());
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Sovereign is Not Connect!!!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                }
            }
        });
        startNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConfirmMerchant) {
                    if (currentMerchantData != null) {
                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                            String type = "start";
                            String sshIp = currentMerchantData.getSsh_ip_port();
                            if (sshIp != null) {
                                if (!sshIp.isEmpty()) {
                                    if (sshIp.contains(":")) {
                                        String[] sh = sshIp.split(":");
                                        String host = sh[0];
                                        String port = sh[1];  //khuwajaid
                                        String sshPass = currentMerchantData.getSsh_password();
                                        String sshUsername = currentMerchantData.getSsh_username();
                                        startNodeServer(type, host, port, sshUsername, sshPass);
                                    } else {
                                        //TODO
                                        goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                                    }
                                } else {
                                    //TODO
                                    goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                }
            }
        });
        stopNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConfirmMerchant) {
                    if (currentMerchantData != null) {
                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                            String type = "stop";
                            String sshIp = currentMerchantData.getSsh_ip_port();
                            if (sshIp != null) {
                                if (!sshIp.isEmpty()) {
                                    if (sshIp.contains(":")) {
                                        String[] sh = sshIp.split(":");
                                        String host = sh[0];
                                        String port = sh[1];
                                        String sshPass = currentMerchantData.getSsh_password();
                                        String sshUsername = currentMerchantData.getSsh_username();
                                        stopNodeServer(type, host, port, sshUsername, sshPass);
                                    } else {
                                        //TODO
                                        goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                                    }
                                } else {
                                    //TODO
                                    goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                }
            }
        });
        checkAppCache();
    }

    private void goAlertDialogwithOneBTn(int i, String alertTitleMessage, String alertMessage, String alertBtn1Message, String alertBtn2Message) {
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog = new Dialog(bContext);
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
        Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        goAlertDialogwithOneBTnDialog.setCancelable(false);
        final TextView alertTitle_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
        final TextView alertMessage_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
        final Button yesbtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
        final Button nobtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
        yesbtn.setText(alertBtn1Message);
        nobtn.setText(alertBtn2Message);
        alertTitle_tv.setText(alertTitleMessage);
        alertMessage_tv.setText(alertMessage);
        if (i == 1) {
            nobtn.setVisibility(View.GONE);
            alertTitle_tv.setVisibility(View.GONE);
        } else {

        }
        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAlertDialogwithOneBTnDialog.dismiss();
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

    //TODO:Getting the scan results
    //Check PErmisiion
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                serverURL = result.getContents();
                // showToast(serverURL);
                connectServerMethod(serverURL);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void connectServerMethod(String baseServerUrl) {
        //  startActivity(new Intent(MainEntryActivity.this, ScannedBarcodeActivity.class));
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        String serverlinktext = baseServerUrl;
        if (serverlinktext.isEmpty()) {
            dialog.dismiss();
            Log.e(TAG, "Sorvereign Link Empty");
            goAlertDialogwithOneBTn(1, "", "Empty Sorvereign Link", "Retry", "");
        } else {
            if (serverlinktext.contains(":")) {
                String ip = "www.google.com";
                String portstring = "443";
                String url = serverlinktext;
                String[] tokUrl = url.split(":");
                ip = tokUrl[0];
                portstring = tokUrl[1];
                int port;
                if (isStringInt(portstring)) {
                    port = Integer.valueOf(portstring);
                    //Log.e(TAG,"ConnectingWithServerLink : "+serverlinktext);
                    Log.e(TAG, "ConnectingWithServerLink2 : " + ip + ":" + port);
                    //:TODO make asyn or do in background thread
                    ServerData serverData = new ServerData();
                    serverData.setServerUrl(serverlinktext);
                    serverData.setIp(ip);
                    serverData.setPort(portstring);
                    GlobalState.getInstance().setServerData(serverData);
                    dialog.dismiss();
                    if (isOnline()) {
                        ConnectToThorServer connectToThorServer = new ConnectToThorServer(bActivity);
                        if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                            connectToThorServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String(ip), new String(portstring)});
                        } else {
                            connectToThorServer.execute(new String[]{new String(ip), new String(portstring)});
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid Sorvereign Link", "OK", "");
                    }
                } else {
                    port = 0;
                    goAlertDialogwithOneBTn(1, "", "Invalid Sorvereign Link", "OK", "");
                    dialog.dismiss();
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Invalid Sorvereign Link", "OK", "");
                dialog.dismiss();
            }
        }
    }

    private void checkAppCache() {
        Map<String, ?> allPrefs = sharedPreferences.getAll(this); //your sharedPreference
        Set<String> set = allPrefs.keySet();
        for (String s : set) {
            Log.d("AllPref", s + "<" + allPrefs.get(s).getClass().getSimpleName() + "> =  "
                    + allPrefs.get(s).toString());
        }
        Date date = new Date(System.currentTimeMillis());
        long lastDatTime = sharedPreferences.getsession(LASTDATE, getApplicationContext());
        Calendar lastdatecalendar = Calendar.getInstance();
        lastdatecalendar.setTimeInMillis(lastDatTime);
        Date lastdateD = lastdatecalendar.getTime();
        long currentDateTime = date.getTime();
        long diff = currentDateTime - lastDatTime;
        // Calculate difference in minutes
        long diffMinutes = diff / (60 * 1000);
        Log.e(TAG, "App Cache Checking");
        Log.e("IsMerchantLogin:", "=" + sharedPreferences.getBoolean(ISMERCHANTLOGIN, getApplicationContext()));
        Log.e("IsThorUserLogin:", "=" + sharedPreferences.getBoolean(IS_USER_LOGIN, getApplicationContext()));
        Log.e("LastDate:", "=" + new Date(sharedPreferences.getsession(LASTDATE, getApplicationContext())).toString());
        Log.e("CurrentDate:", "=" + new Date(System.currentTimeMillis()).toString());
        Log.d("Diffrence Time:", String.valueOf(diffMinutes));
        Log.e("ThorStatus:", "=" + sharedPreferences.getBoolean(THORSTATUS, getApplicationContext()));
        Log.e("LightningStatus:", "=" + sharedPreferences.getBoolean(LIGHTNINGSTATUS, getApplicationContext()));
        Log.e("BitconStatus:", "=" + sharedPreferences.getBoolean(BITCOINSTATUS, getApplicationContext()));

        if (diffMinutes < 240) {
            if (sharedPreferences.getBoolean(THORSTATUS, this)) {
                updateResultNodeStatus(1, "ACTIVE");
            } else {
                updateResultNodeStatus(2, "INACTIVE");
            }
            if (sharedPreferences.getBoolean(ISMERCHANTLOGIN, this)) {
                String xx = sharedPreferences.getString(SERVERURL, this);
//                findMerchant(sharedPreferences.getString(MERCHANTID, this));
            } else {
                sharedPreferences.clearAllPrefExceptOfSShkeyPassword(this);
            }
        } else {
            updateResultNodeStatus(2, "INACTIVE");
            sharedPreferences.clearAllPrefExceptOfSShkeyPassword(this);
        }
    }

    //TODO: Confirm Merchant Id
//    private void findMerchant(final String id) {
//        confirmingProgressDialog.show();
//        confirmingProgressDialog.setCancelable(false);
//        Call<MerchantLoginResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).merchant_Loging(id);
//        call.enqueue(new Callback<MerchantLoginResp>() {
//            @Override
//            public void onResponse(Call<MerchantLoginResp> call, Response<MerchantLoginResp> response) {
//                confirmingProgressDialog.dismiss();
//                if (response.isSuccessful()) {
//                    if (response.body() != null) {
//                        if (response.body().getMessage().equals("successfully done")) {
//                            MerchantData merchantData = new MerchantData();
//                            merchantData = response.body().getMerchantData();
//                            GlobalState.getInstance().setLattitude(merchantData.getLatitude());
//                            GlobalState.getInstance().setLongitude(merchantData.getLongitude());
//                            GlobalState.getInstance().setMerchantData(merchantData);
//                            currentMerchantData = merchantData;
//                            Log.e("2fapass", currentMerchantData.getPassword());
//                            GlobalState.getInstance().setMerchant_id(id);
//                            //gotoTestCase(merchantData);
//                            if (sharedPreferences.getBoolean(ISMERCHANTLOGIN, MainEntryActivity.this) && sharedPreferences.getBoolean(ISSERVERLOGIN, MainEntryActivity.this)) {
//                                connectServerMethod(sharedPreferences.getString(SERVERURL, MainEntryActivity.this));
//                            } else {
//                                if (sharedPreferences.getBoolean(ISMERCHANTLOGIN, MainEntryActivity.this)) {
//                                    isConfirmMerchant = true;
//                                    sharedPreferences.setBoolean(true, ISMERCHANTLOGIN, MainEntryActivity.this);
//                                    sharedPreferences.setString(merchantData.getMerchant_name(), MERCHANTID, MainEntryActivity.this);
//                                    goToCallStartThorServerAutomaticlly();
//                                } else {
//                                    goTo2FaPasswordDialog(merchantData);
//                                }
//
//                            }
//
//
//                        } else {
//                            isConfirmMerchant = false;
//                            GlobalState.getInstance().setMerchantConfirm(false);
//                            goAlertDialogwithOneBTn(1, "", "Invalid Merchant ID!", "OK", "");
//                        }
//                    } else {
//                        isConfirmMerchant = false;
//                        Log.e("Error:", response.toString());
////                    showToast(response.toString());
//                        goAlertDialogwithOneBTn(1, "", "Server Error", "OK", "");
//
//                    }
//                } else {
//                    isConfirmMerchant = false;
//                    Log.e("Error:", response.toString());
////                    showToast(response.toString());
//                    goAlertDialogwithOneBTn(1, "", "Server Error", "OK", "");
//
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<MerchantLoginResp> call, Throwable t) {
//                isConfirmMerchant = false;
//                GlobalState.getInstance().setMerchantConfirm(false);
//                confirmingProgressDialog.dismiss();
//                //showToast("Network Error");
//                goAlertDialogwithOneBTn(1, "", "Network Error", "OK", "");
//
//            }
//        });
//
//    }

    private void goTo2FaPasswordDialog(MerchantData merchantData) {
        final MerchantData merchantDatafinal = merchantData;
        final Dialog enter2FaPassDialog;
        enter2FaPassDialog = new Dialog(bContext);
        enter2FaPassDialog.setContentView(R.layout.merchat_twofa_pass_lay);
        Objects.requireNonNull(enter2FaPassDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        enter2FaPassDialog.setCancelable(false);
        final EditText et_2Fa_pass = enter2FaPassDialog.findViewById(R.id.taskEditText);
        final Button btn_confirm = enter2FaPassDialog.findViewById(R.id.btn_confirm);
        final Button btn_cancel = enter2FaPassDialog.findViewById(R.id.btn_cancel);
        final ImageView iv_back = enter2FaPassDialog.findViewById(R.id.iv_back_invoice);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enter2FaPassDialog.dismiss();

            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clodeSoftKeyBoard();
                String task = String.valueOf(et_2Fa_pass.getText());
                if (task.isEmpty()) {
                    goAlertDialogwithOneBTn(1, "", "Enter 2FA Password", "OK", "");
                } else {
                    if (task.equals(merchantDatafinal.getPassword())) {

                        enter2FaPassDialog.dismiss();
                        GlobalState.getInstance().setMerchantConfirm(true);
                        final Dialog goAlertDialogwithOneBTnDialog;
                        goAlertDialogwithOneBTnDialog = new Dialog(bContext);
                        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
                        Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        goAlertDialogwithOneBTnDialog.setCancelable(false);
                        final TextView alertTitle_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
                        final TextView alertMessage_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
                        final Button yesbtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
                        final Button nobtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
                        yesbtn.setText("Next");
                        nobtn.setText("");
                        nobtn.setVisibility(View.GONE);
                        alertTitle_tv.setText("");
                        alertTitle_tv.setVisibility(View.GONE);
                        alertMessage_tv.setText("Merchant Id Confirmed");
                        yesbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goAlertDialogwithOneBTnDialog.dismiss();
                                if (isSsKeyFileExist() && isSshKeyPassExist()) {
                                    isConfirmMerchant = true;
                                    sharedPreferences.setBoolean(true, ISMERCHANTLOGIN, MainEntryActivity.this);
                                    sharedPreferences.setString(merchantDatafinal.getMerchant_name(), MERCHANTID, MainEntryActivity.this);
                                    goToCallStartThorServerAutomaticlly();
                                } else {
                                    goToGetAndConfirmSshKey();
                                }
                            }
                        });
                        nobtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goAlertDialogwithOneBTnDialog.dismiss();
                            }
                        });
                        goAlertDialogwithOneBTnDialog.show();

                    } else {
                        goAlertDialogwithOneBTn(1, "", "Incorrect Password", "Retry", "");

                    }
                }

            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enter2FaPassDialog.dismiss();
            }
        });
        enter2FaPassDialog.show();
    }

    private void goToGetAndConfirmSshKey() {
        getAndConfirmSshKeyDialog = new Dialog(bContext);
        getAndConfirmSshKeyDialog.setContentView(R.layout.getandconfirmsshkeydialog_layout);
        Objects.requireNonNull(getAndConfirmSshKeyDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getAndConfirmSshKeyDialog.setCancelable(false);
        final Button btn_confirm = getAndConfirmSshKeyDialog.findViewById(R.id.btn_confirm);
        final Button btn_cancel = getAndConfirmSshKeyDialog.findViewById(R.id.btn_cancel);
        final ImageView iv_back = getAndConfirmSshKeyDialog.findViewById(R.id.iv_back_invoice);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndConfirmSshKeyDialog.dismiss();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clodeSoftKeyBoard();
                final Dialog goAlertDialogwithOneBTnDialog;
                goAlertDialogwithOneBTnDialog = new Dialog(bContext);
                goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
                Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                goAlertDialogwithOneBTnDialog.setCancelable(false);
                final TextView alertTitle_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
                final TextView alertMessage_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
                final Button yesbtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
                final Button nobtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
                yesbtn.setText("Next");
                nobtn.setText("BACK");
                alertTitle_tv.setText("Private key Password Confirmation!");
                alertMessage_tv.setText("You will now encrypt your private key with a unique password! You do not need to remember this password as it will be stored securely. In the case that you lose access to these authentication credentials locally, you will need to contact Next Layer customer service to reset access from this device.");
                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goAlertDialogwithOneBTnDialog.dismiss();
                        goTOGetsshkeyPass();
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
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndConfirmSshKeyDialog.dismiss();
            }
        });
        getAndConfirmSshKeyDialog.show();
    }

    private void goTOGetsshkeyPass() {
        final Dialog entergoTOGetsshkeyPass;
        entergoTOGetsshkeyPass = new Dialog(bContext);
        entergoTOGetsshkeyPass.setContentView(R.layout.sshkeyprpaslay);
        Objects.requireNonNull(entergoTOGetsshkeyPass.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        entergoTOGetsshkeyPass.setCancelable(false);
        final EditText et_2Fa_pass = entergoTOGetsshkeyPass.findViewById(R.id.taskEditText);
        final Button btn_confirm = entergoTOGetsshkeyPass.findViewById(R.id.btn_confirm);
        final Button btn_cancel = entergoTOGetsshkeyPass.findViewById(R.id.btn_cancel);
        final ImageView iv_back = entergoTOGetsshkeyPass.findViewById(R.id.iv_back_invoice);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entergoTOGetsshkeyPass.dismiss();

            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = et_2Fa_pass.getText().toString();
                if (pass.isEmpty()) {
                    showToast("Please Enter Password");
                } else {
                    if (pass.length() < 5) {
                        clodeSoftKeyBoard();
                        goAlertDialogwithOneBTn(1, "", "Required minimum 5 Digit", "OK", "");
                    } else {
                        sharedPreferences.setString(pass, "sshkeypass", bContext);
                        clodeSoftKeyBoard();
                        entergoTOGetsshkeyPass.dismiss();
                        goTOCallGetSshDowload("yes", pass);
                    }

                }

            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entergoTOGetsshkeyPass.dismiss();
            }
        });
        entergoTOGetsshkeyPass.show();
    }


    private void goTOCallGetSshDowload(String pwCase, String sshkeyPass) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            goToCallStartThorServerAutomaticlly();
            sharedPreferences.setBoolean(true, ISMERCHANTLOGIN, MainEntryActivity.this);
            sharedPreferences.setString(GlobalState.getInstance().getMerchantData().getMerchant_name(), MERCHANTID, MainEntryActivity.this);
            getAndConfirmSshKeyDialog.dismiss();
        } else {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                    String type = "start";
                    String sshIp = currentMerchantData.getSsh_ip_port();
                    if (sshIp != null) {
                        if (!sshIp.isEmpty()) {
                            if (sshIp.contains(":")) {
                                String[] sh = sshIp.split(":");
                                String host = sh[0];
                                String port = sh[1];  //khuwajaid
                                String sshPass = currentMerchantData.getSsh_password();
                                String sshUsername = currentMerchantData.getSsh_username();
                                // startNodeServer(type,host,port,sshUsername,sshPass);
                                String url = "";
                                if (pwCase.equals("yes")) {
                                    url = "http://boostterminal.stepinnsolution.com/ssh-files/key-add.php?type=start&host=" + host + "&port=" + port + "&username=" + sshUsername + "&password=" + sshPass + "&sshkeypw=" + sshkeyPass;

                                } else {
                                    url = "http://boostterminal.stepinnsolution.com/ssh-files/key-add.php?type=start&host=" + host + "&port=" + port + "&username=" + sshUsername + "&password=" + sshPass + "&sshkeypw=" + sshkeyPass;
                                }
                                DownloadFileFromServer downloadFileFromServer = new DownloadFileFromServer(MainEntryActivity.this);
                                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                                    downloadFileFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String(url)});
                                } else {
                                    downloadFileFromServer.execute(new String[]{new String(url)});
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");

//                                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainEntryActivity.this);
//                                    builder.setMessage("Invalid SSH IP!")
//                                            .setCancelable(false)
//                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                public void onClick(final DialogInterface dialog, final int id) {
//                                                    dialog.cancel();
//                                                }
//                                            }).show();
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");

//                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainEntryActivity.this);
//                                builder.setMessage("Empty SSH IP!")
//                                        .setCancelable(false)
//                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                            public void onClick(final DialogInterface dialog, final int id) {
//                                                dialog.cancel();
//                                            }
//                                        }).show();
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }

                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing!", "OK", "");

                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");

//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainEntryActivity.this);
//                    builder.setMessage("Enter Merchant ID")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
            }
        }

    }

    private class DownloadFileFromServer extends AsyncTask<String, Integer, String> {
        // Constant for identifying the dialog
        private static final int LOADING_DIALOG = 100;
        private Activity parent;
        ProgressDialog exitingFromServerPD;

        public DownloadFileFromServer(Activity parent) {
            // record the calling activity, to use in showing/hiding dialogs
            this.parent = parent;
            exitingFromServerPD = new ProgressDialog(parent);
            exitingFromServerPD.setMessage("Downloading...");
        }

        protected void onPreExecute() {
            // called on UI thread
            // parent.showDialog(LOADING_DIALOG);
            exitingFromServerPD.show();
            exitingFromServerPD.setCancelable(false);
            exitingFromServerPD.setCanceledOnTouchOutside(false);
        }

        protected String doInBackground(String... urls) {
            // called on the background thread
            int count2 = urls.length;
            String url2 = urls[0];
            ContextWrapper contextWrapper = new ContextWrapper(bContext);
            File directory = contextWrapper.getDir(bActivity.getFilesDir().getName(), Context.MODE_PRIVATE);
            File file = new File(directory, "sshkey");

            int count;
            try {
                URL url = new URL(url2);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/merhantapp");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    //  publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return "false";
            }


            return "true";
        }

        protected void onProgressUpdate(Integer... progress) {
            // called on the UI thread
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            exitingFromServerPD.dismiss();
            if (result.equals("true")) {
                showToast("FileSaved");
                isConfirmMerchant = true;
                goToCallStartThorServerAutomaticlly();
                getAndConfirmSshKeyDialog.dismiss();

            } else {
                showToast("Retry...");
                isConfirmMerchant = false;
            }
            Log.e(TAG, "Connection Status Result:" + result);
        }
    }

    private void goToCallStartThorServerAutomaticlly() {
        sharedPreferences.setBoolean(true, ISMERCHANTLOGIN, MainEntryActivity.this);
        sharedPreferences.setString(GlobalState.getInstance().getMerchantData().getMerchant_name(), MERCHANTID, MainEntryActivity.this);
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                    String type = "start";
                    String sshIp = currentMerchantData.getSsh_ip_port();
                    if (sshIp != null) {
                        if (!sshIp.isEmpty()) {
                            if (sshIp.contains(":")) {
                                String[] sh = sshIp.split(":");
                                String host = sh[0];
                                String port = sh[1];  //khuwajaid
                                String sshPass = currentMerchantData.getSsh_password();
                                String sshUsername = currentMerchantData.getSsh_username();
                                Log.e(TAG, "App Cache Checking");
                                if (sharedPreferences.getBoolean(IS_USER_LOGIN, getApplicationContext())) {
                                    Date date = new Date(System.currentTimeMillis());
                                    long lastDatTime = sharedPreferences.getsession(LASTDATE, getApplicationContext());
                                    Calendar lastdatecalendar = Calendar.getInstance();
                                    lastdatecalendar.setTimeInMillis(lastDatTime);
                                    Date lastdateD = lastdatecalendar.getTime();
                                    long currentDateTime = date.getTime();
                                    long diff = currentDateTime - lastDatTime;
                                    // Calculate difference in minutes
                                    long diffMinutes = diff / (60 * 1000);
                                    if (diffMinutes < 240) {
                                        if (sharedPreferences.getThorStatus(THORSTATUS, this)) {
                                            updateResultNodeStatus(1, "ACTIVE");
                                        } else {
                                            updateResultNodeStatus(2, "INACTIVE");
                                        }
                                    } else {
                                        startNodeServer(type, host, port, sshUsername, sshPass);
                                    }
                                } else {
                                    // showToast("User Not Login");
                                    startNodeServer(type, host, port, sshUsername, sshPass);
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }
    }

    //TODO:START AND STOP Node SERVER APIs
    private void startNodeServer(String type, String host, String port, String sshUsername, String sshPass) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            startServerPD.show();
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startThorStopNodeServer3(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                sharedPreferences.setBoolean(true, THORSTATUS, MainEntryActivity.this);
                                updateResultNodeStatus(1, "ACTIVE");
                                isConfirmThorActive = true;
                            } else {
                                isConfirmThorActive = false;
                                sharedPreferences.setBoolean(false, THORSTATUS, MainEntryActivity.this);
                                updateResultNodeStatus(2, "INACTIVE");
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                            }
                        } else {
                            isConfirmThorActive = false;
                            sharedPreferences.setBoolean(false, THORSTATUS, MainEntryActivity.this);
                            updateResultNodeStatus(2, "INACTIVE");
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                        }


                    } else {
                        isConfirmThorActive = false;
                        sharedPreferences.setBoolean(false, THORSTATUS, MainEntryActivity.this);
                        updateResultNodeStatus(2, "INACTIVE");
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                    }
                    startServerPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    sharedPreferences.setBoolean(false, THORSTATUS, MainEntryActivity.this);
                    updateResultNodeStatus(2, "INACTIVE");
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue", "OK", "");
                    startServerPD.dismiss();
                    isConfirmThorActive = false;
                }
            });
        } else {
            sharedPreferences.setBoolean(false, THORSTATUS, MainEntryActivity.this);
            updateResultNodeStatus(2, "INACTIVE");
            goAlertDialogwithOneBTn(1, "", "SSH Key is Missing", "OK", "");
            isConfirmMerchant = false;
        }


    }



    private void stopNodeServer(String type, String host, String port, String sshUsername, String sshPass) {
        startServerPD.show();
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);

            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startThorStopNodeServer3(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                updateResultNodeStatus(2, "INACTIVE");
                                isConfirmThorActive = false;
                            } else {
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");

//                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainEntryActivity.this);
//                                builder.setMessage("Invalid SSH Info!")
//                                        .setCancelable(false)
//                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                            public void onClick(final DialogInterface dialog, final int id) {
//                                                dialog.cancel();
//                                            }
//                                        }).show();
                            }

                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");

//                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainEntryActivity.this);
//                            builder.setMessage("Invalid SSH Info!")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                        public void onClick(final DialogInterface dialog, final int id) {
//                                            dialog.cancel();
//                                        }
//                                    }).show();
                        }


                    } else {
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");

//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainEntryActivity.this);
//                        builder.setMessage("Invalid SSH Info!")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
                    }

                    startServerPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {

                    goAlertDialogwithOneBTn(1, "", "Server Side Issue", "OK", "");

//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainEntryActivity.this);
//                    builder.setMessage("Server Side Issue!")
//                            .setCancelable(false)
//                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
                    startServerPD.dismiss();
                }
            });

        } else {
            showToast("SSH Key is Missing");
            isConfirmMerchant = false;
            startServerPD.dismiss();
        }


    }

    //TODO:Update the Result on View
    private void updateResultNodeStatus(int type, String s) {
        switch (type) {
            case 1:
                isConfirmThorActive = true;
                result_Node.setText(s);
                result_Node.setTextColor(ContextCompat.getColor(bContext, R.color.active_text_colour));
                break;
            case 2:
                isConfirmThorActive = false;
                result_Node.setText(s);
                result_Node.setTextColor(ContextCompat.getColor(bContext, R.color.inactive_text_colour));
                break;
        }
        result_Node.setText(s);
    }

    //TODO: RPC Call For Connect Thor Server:
    private class ConnectToThorServer extends AsyncTask<String, Integer, Boolean> {
        // Constant for identifying the dialog
        private static final int LOADING_DIALOG = 100;
        private Activity parent;
        private ProgressDialog dialog2;
        public ConnectToThorServer asyncObject;
        private String ipport = "";

        public ConnectToThorServer(Activity parent) {
            // record the calling activity, to use in showing/hiding dialogs
            this.parent = parent;
            dialog2 = new ProgressDialog(parent);
            dialog2.setMessage("Connecting...");
        }

        protected void onPreExecute() {
            dialog2.show();
            dialog2.setCancelable(false);
            dialog2.setCanceledOnTouchOutside(false);
            asyncObject = this;
            new CountDownTimer(7000, 7000) {
                public void onTick(long millisUntilFinished) {
                    // You can monitor the progress here as well by changing the onTick() time
                }

                public void onFinish() {
                    // stop async task if not in progress
                    if (asyncObject.getStatus() == AsyncTask.Status.RUNNING) {
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(bContext);
                        builder.setMessage("Sorvereign Link Not Exist")
                                .setCancelable(false)
                                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                        asyncObject.cancel(false);
                                        try {
                                            if (dialog2 != null && dialog2.isShowing()) {

                                                dialog2.dismiss();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).show();
                        asyncObject.cancel(false);
                        try {
                            if (dialog2 != null && dialog2.isShowing()) {

                                dialog2.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Add any specific task you wish to do as your extended class variable works here as well.
                    }
                }
            }.start();

            // called on UI thread
            // parent.showDialog(LOADING_DIALOG);
        }

        protected Boolean doInBackground(String... urls) {
            // called on the background thread
            int count = urls.length;

            String ip = urls[0];
            int port = Integer.valueOf(urls[1]);
            ipport = ip + ":" + port;
            Boolean status = Boolean.valueOf(NetworkManager.getInstance().connectClient(ip, port));

            return status;
        }

        protected void onProgressUpdate(Integer... progress) {
            // called on the UI thread
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Boolean result) {
            // this method is called back on the UI thread, so it's safe to
            //  make UI calls (like dismissing a dialog) here
            //  parent.dismissDialog(LOADING_DIALOG);
            try {
                if (dialog2 != null && dialog2.isShowing()) {

                    dialog2.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean resultstatus = toBooleanDefaultIfNull(result);
            if (resultstatus) {
                sharedPreferences.setBoolean(true, ISSERVERLOGIN, parent);
                sharedPreferences.setString(ipport, SERVERURL, parent);
                isConfirmMerchant=true;
                gotoNextScreen();
                //exitServer();
            } else {
                goAlertDialogwithOneBTn(1, "", "Sorvereign Link Not Exist", "Retry", "");
            }
            Log.e(TAG, "ConnectToServer Result:" + resultstatus);
        }
    }

    private void gotoNextScreen() {
        Date date = new Date(System.currentTimeMillis());
        long lastDatTime = sharedPreferences.getsession(LASTDATE, getApplicationContext());
        Calendar lastdatecalendar = Calendar.getInstance();
        lastdatecalendar.setTimeInMillis(lastDatTime);
        Date lastdateD = lastdatecalendar.getTime();
        Log.e("Before:", lastdateD.toString());
        long currentDateTime = date.getTime();
        Log.e("Current:", date.toString());
        long diff = currentDateTime - lastDatTime;
        // Calculate difference in minutes
        long diffMinutes = diff / (60 * 1000);
        if (diffMinutes < 240) {
            if (sharedPreferences.getBoolean(ISALLSERVERUP, this)) {
                updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this));
                updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this));
                updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this));
                goToSelectionModeScreen();
            } else {
                checkAppFlow();
            }
        }else {
            checkAppFlow();
        }

    }

    private void goToSelectionModeScreen() {
        Intent toLoginintent = new Intent(MainEntryActivity.this, MainActivity.class);
        startActivity(toLoginintent);
    }

    //TODO:Point No 6
    private void checkAppFlow() {
        //TODO:Set thor button display green colour
//        thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
        updateStatusBox(1, true);
        updateStatusBox(2, false);
        updateStatusBox(3, false);
        //TODO:check status of bitcoin node
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null && currentMerchantData.getRpc_username() != null && currentMerchantData.getRpc_password() != null) {
                    String type = "status";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                if (currentMerchantData.isIs_own_bitcoin()) {
                                    //TODO:When  Own BTC
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    String rpcUserName = currentMerchantData.getRpc_username();
                                    String rpcPassword = currentMerchantData.getRpc_password();
                                    callBitcoinNodeStatusCheck(type, host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
                                } else {
                                    //TODO:When Not Own BTC
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    String rpcUserName = currentMerchantData.getRpc_username();
                                    String rpcPassword = currentMerchantData.getRpc_password();
                                    goTOtheNotOwnBTC(host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "Retry", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }
    }

    //TODO:    :Main Case :   Case to Not Own Bitcoin
    private void goTOtheNotOwnBTC(String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
        //Step 1) App assumes proper remote bitcoin node is up and display green color (DGC).
        updateStatusBox(3, true);
        //Step 2) A restart is performed on the lightning node and then a status check.
        String type = "start";
        reStartLightningStep(type, host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
    }

    private void reStartLightningStep(final String type, final String host, final String port, final String sshUsername, final String sshPass, final String rpcUserName, final String rpcPassword) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);

            RequestBody rpcUserName2 = RequestBody.create(MediaType.parse("text/plain"), rpcUserName);
            RequestBody rpcPassword2 = RequestBody.create(MediaType.parse("text/plain"), rpcPassword);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);

            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {

                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                final String repsMessag = resp.getMessage();
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        startServerPD.dismiss();
                                        reCheckLightningSatus(type, host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
                                    }
                                }, AppConstants.TIMEFORWAITLN2);

                            } else {
                                updateStatusBox(3, false);
                                updateStatusBox(2, false);
                                startServerPD.dismiss();
                            }

                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                            startServerPD.dismiss();
                        }


                    } else {
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                        startServerPD.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {

                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSH is Missing");
        }


    }

    private void reCheckLightningSatus(String type, String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
        checkStatusPD.show();
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            RequestBody rpcUserName2 = RequestBody.create(MediaType.parse("text/plain"), rpcUserName);
            RequestBody rpcPassword2 = RequestBody.create(MediaType.parse("text/plain"), rpcPassword);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);

            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).checkLightningNodeServerStatus2(sshkeypass, host2, port2, sshUsername2, itemImageFileMPBody, rpcUserName2, rpcPassword2);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200 && resp.getMessage().equals("up")) {
                                updateStatusBox(2, true);
                                updateStatusBox(3, true);
                                goToSelectionModeScreen();
                                checkStatusPD.dismiss();
                            } else if (resp.getCode() == 200 && resp.getMessage().equals("down")) {
                                updateStatusBox(2, false);
                                updateStatusBox(3, false);
                                checkStatusPD.dismiss();
                                goAlertDialogwithOneBTn(1, "", "Please check the status of your remote BTC node and manually restart your Lightning Node.", "OK", "");
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");
                            checkStatusPD.dismiss();
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");
                        checkStatusPD.dismiss();
                    }

                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    checkStatusPD.dismiss();
                }
            });
        } else {
            showToast("SSH is Missing");
            checkStatusPD.dismiss();
        }
    }

    //TODO:    :Main Case :   Case To OWn Bitcoin
    private void callBitcoinNodeStatusCheck(String type, String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
        checkStatusPD.show();

        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            RequestBody rpcUserName2 = RequestBody.create(MediaType.parse("text/plain"), rpcUserName);
            RequestBody rpcPassword2 = RequestBody.create(MediaType.parse("text/plain"), rpcPassword);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).checkBitcoinNodeServerStatus2(sshkeypass, host2, port2, sshUsername2, itemImageFileMPBody, rpcUserName2, rpcPassword2);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200 && resp.getMessage().equals("up")) {
                                goTOBitcoinUpCase();
                            } else if (resp.getCode() == 200 && resp.getMessage().equals("down")) {
                                goTOBitcoinDownCase();
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");
                        }

                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");
                    }
                    checkStatusPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    checkStatusPD.dismiss();
                }
            });
        } else {
            showToast("SSH Is Misiing");
        }


    }

    //TODO: GO TO THE BITCOIN DOWN CASE
    private void goTOBitcoinDownCase() {
        gotoRestartBitcoinApi();
    }

    private void gotoRestartBitcoinApi() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                    String type = "start";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                if (currentMerchantData.isIs_own_bitcoin()) {
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    startBitcoinServer(type, host, port, sshUsername, sshPass);
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "");
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }
    }

    //TODO:START AND STOP Bitcoin SERVER APIs
    private void startBitcoinServer(String type, String host, String port, String sshUsername, String sshPass) {
        startServerPD.show();

        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startBitcoinServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {

                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                // updateResultBitcoinStatus("Bitcoin: "+resp.getMessage());
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        startServerPD.dismiss();
                                        goToForStartBitcoinDone();
                                    }
                                }, AppConstants.TIMEFORWAITLN2);

                            } else {
                                goToBitcoinNodeNotDone();
                                startServerPD.dismiss();
                                //  updateResultBitcoinStatus(resp.getMessage());
                            }

                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                            startServerPD.dismiss();
                        }


                    } else {
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                        startServerPD.dismiss();
                    }


                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSH Is Misiing");
        }
    }

    private void goToForStartBitcoinDone() {
        //TODO: AFter @0 seconds wait!!
        wait20SecPD.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wait20SecPD.dismiss();
                goToStartLightningNode();
            }

        }, AppConstants.TIMEFORWAITLN);
    }

    private void goToStartLightningNode() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null && currentMerchantData.getRpc_username() != null && currentMerchantData.getRpc_password() != null) {
                    String type = "start";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                String sshPass = currentMerchantData.getSsh_password();
                                String sshUsername = currentMerchantData.getSsh_username();
                                startLightningServer2(type, host, port, sshUsername, sshPass);
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }

    }

    //TODO:START  Lightnning SERVER API
    private void startLightningServer2(String type, String host, String port, String sshUsername, String sshPass) {
        startServerPD.show();
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                updateStatusBox(2, true);
                                goToSelectionModeScreen();
                            } else {
                                updateStatusBox(2, false);
                                goToRestartLighntningNodefor9Cycles();
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                        }
                    } else {
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                    }
                    startServerPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSH Is Misiing");
        }
    }

    private void goToRestartLighntningNodefor9Cycles() {
        //TODO: AFter @0 seconds wait!!
        wait20SecPD.show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                wait20SecPD.dismiss();
                goRestartLightningApi9();
            }

        }, AppConstants.TIMEFORWAITLN);
    }

    private void goRestartLightningApi9() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null && currentMerchantData.getRpc_username() != null && currentMerchantData.getRpc_password() != null) {
                    String type = "status";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                if (currentMerchantData.isIs_own_bitcoin()) {
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    String rpcUserName = currentMerchantData.getRpc_username();
                                    String rpcPassword = currentMerchantData.getRpc_password();
                                    startLightningNode9(type, host, port, sshUsername, sshPass);
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "");
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }
    }

    private void startLightningNode9(String type, String host, String port, String sshUsername, String sshPass) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        updateStatusBox(2, true);
                                        startServerPD.dismiss();
                                        checkTheLightningNodeStatus9();
                                    }
                                }, AppConstants.TIMEFORWAITLN2);   //40 seconds seconds

//                            updateResultLightningStatus("Lightnning: "+resp.getMessage());
                            } else {
                                updateStatusBox(2, false);
//                            updateResultLightningStatus(resp.getMessage());
                                startServerPD.dismiss();
                            }

                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                            startServerPD.dismiss();
                        }
                    } else {
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                        startServerPD.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSH Is Misiing");
        }
    }

    private void checkTheLightningNodeStatus9() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null && currentMerchantData.getRpc_username() != null && currentMerchantData.getRpc_password() != null) {
                    String type = "status";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                if (currentMerchantData.isIs_own_bitcoin()) {
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    String rpcUserName = currentMerchantData.getRpc_username();
                                    String rpcPassword = currentMerchantData.getRpc_password();
                                    callLightningNodeStatusCheck9(type, host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "");
                                }
                            } else {
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }
    }

    private void callLightningNodeStatusCheck9(String type, String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
        checkStatusPD.show();

        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            RequestBody rpcUserName2 = RequestBody.create(MediaType.parse("text/plain"), rpcUserName);
            RequestBody rpcPassword2 = RequestBody.create(MediaType.parse("text/plain"), rpcPassword);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);

            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).checkLightningNodeServerStatus2(sshkeypass, host2, port2, sshUsername2, itemImageFileMPBody, rpcUserName2, rpcPassword2);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200 && resp.getMessage().equals("up")) {
                                updateStatusBox(2, true);
                                goToSelectionModeScreen();
                            } else if (resp.getCode() == 200 && resp.getMessage().equals("down")) {
                                updateStatusBox(2, false);
                                goTowaitandRecall();
                            }

                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");

                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");


                    }
                    checkStatusPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    checkStatusPD.dismiss();
                }
            });
        } else {
            showToast("SSH Is Misiing");
        }
    }

    private void goTowaitandRecall() {
        if (recallTime > 8) {
            updateStatusBox(2,false);
            goAlertDialogwithOneBTn(1, "", "Please Restart Lightning Node Manually!", "OK", "");
        } else {
            recallTime = recallTime + 1;
            wait20SecPD.show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    wait20SecPD.dismiss();
                    goRestartLightningApi9();
                }

            }, AppConstants.TIMEFORWAITLN);
        }

    }

    private void goToBitcoinNodeNotDone() {
        goAlertDialogwithOneBTn(1, "", "Bitcoin Is Not Start Succefully!!!", "OK", "");
    }

    //TODO: GO TO THE BITCOIN UP CASE
    private void goTOBitcoinUpCase() {
        updateStatusBox(3, true);
        callRestartLightningNodeAPI();
    }

    private void callRestartLightningNodeAPI() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null && currentMerchantData.getRpc_username() != null && currentMerchantData.getRpc_password() != null) {
                    String type = "start";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                String sshPass = currentMerchantData.getSsh_password();
                                String sshUsername = currentMerchantData.getSsh_username();
                                startLightningServer(type, host, port, sshUsername, sshPass);
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");
                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }

                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }
    }

    //TODO:START  Lightnning SERVER APIs
    private void startLightningServer(String type, String host, String port, String sshUsername, String sshPass) {
        startServerPD.show();
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);

            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {

                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        updateStatusBox(2, true);
                                        startServerPD.dismiss();
                                        checkTheLightningNodeStatus();
                                    }
                                }, AppConstants.TIMEFORWAITLN2);   //40 seconds seconds

//                            updateResultLightningStatus("Lightnning: "+resp.getMessage());
                            } else {
                                startServerPD.dismiss();
                                updateStatusBox(2, false);
                                goAlertDialogwithOneBTn(1, "", "Please Restart Lightning Node Manually!", "OK", "");
                            }
                        } else {
                            startServerPD.dismiss();
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                        }
                    } else {
                        startServerPD.dismiss();
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "OK", "");
                    }


                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSH Is Misiing");
        }
    }

    private void checkTheLightningNodeStatus() {
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null && currentMerchantData.getRpc_username() != null && currentMerchantData.getRpc_password() != null) {
                    String type = "status";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                if (currentMerchantData.isIs_own_bitcoin()) {
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    String rpcUserName = currentMerchantData.getRpc_username();
                                    String rpcPassword = currentMerchantData.getRpc_password();
                                    callLightningNodeStatusCheck(type, host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
                                } else {
                                    goAlertDialogwithOneBTn(1, "", "No Own Bitcoin Node!!", "OK", "");
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "OK", "");
                            }
                        } else {
                            //TODO
                            goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "OK", "");


                        }
                    } else {
                        //TODO
                        goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Merchant Info Missing", "OK", "");
                }
            } else {
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
            }
        } else {
            goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
        }

    }

    //TODO: Lightning Status Check API
    private void callLightningNodeStatusCheck(String type, String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
        checkStatusPD.show();
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            String sshkeypasval = sharedPreferences.getString("sshkeypass", bContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            RequestBody rpcUserName2 = RequestBody.create(MediaType.parse("text/plain"), rpcUserName);
            RequestBody rpcPassword2 = RequestBody.create(MediaType.parse("text/plain"), rpcPassword);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).checkLightningNodeServerStatus2(sshkeypass, host2, port2, sshUsername2, itemImageFileMPBody, rpcUserName2, rpcPassword2);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200 && resp.getMessage().equals("up")) {
                                goToLightningUpCase();
                            } else if (resp.getCode() == 200 && resp.getMessage().equals("down")) {
                                goToLightningDownCase();
                            }

                        } else {
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info", "OK", "");
                    }
                    checkStatusPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", "Server Side Issue!!", "OK", "");
                    checkStatusPD.dismiss();
                }
            });
        } else {
            showToast("SSH Is Misiing");
        }

    }

    private void goToLightningUpCase() {
        updateStatusBox(2, true);
        goToSelectionModeScreen();
    }

    private void goToLightningDownCase() {
        updateStatusBox(2, false);
        goAlertDialogwithOneBTn(1, "", "Please Restart Lightning Node Manually!", "OK", "");
    }

    //TODO: Update the Status Box
    private void updateStatusBox(int i, boolean b) {
        switch (i) {
            case 1:
                if (b) {
                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
                    isThorConfirmed = true;
                    sharedPreferences.setThorStatus(true, THORSTATUS, this);
                    //thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
                } else {
                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
                    isThorConfirmed = false;
                    sharedPreferences.setThorStatus(false, THORSTATUS, this);
                    //thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
                }
                break;
            case 2:
                if (b) {
                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
                    isLightningConfirmed = true;
                    sharedPreferences.setLightningStatus(true, LIGHTNINGSTATUS, this);
                    //lightningNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
                } else {
                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
                    isLightningConfirmed = false;
                    sharedPreferences.setLightningStatus(false, LIGHTNINGSTATUS, this);
                    //lightningNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
                }
                break;
            case 3:
                if (b) {
                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
                    isBitcoinConfirmed = true;
                    sharedPreferences.setBitcoinStatus(true, BITCOINSTATUS, this);
                    //  bitcoinNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
                } else {
                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
                    isBitcoinConfirmed = false;
                    sharedPreferences.setBitcoinStatus(false, BITCOINSTATUS, this);
                    //bitcoinNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
                }
                break;
        }
    }

    private static void downloadFile(String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            return; // swallow a 404
        } catch (IOException e) {
            return; // swallow a 404
        }
    }

    @Override
    public void onBackPressed() {
        //   super.onBackPressed();
        ask_exit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void ask_exit() {
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog = new Dialog(bContext);
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
                goAlertDialogwithOneBTnDialog.dismiss();
                finishAffinity();
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
}
