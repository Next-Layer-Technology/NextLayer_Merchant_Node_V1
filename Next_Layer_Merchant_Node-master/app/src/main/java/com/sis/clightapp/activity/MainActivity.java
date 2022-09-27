package com.sis.clightapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sis.clightapp.Interface.ApiClientStartStop;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.NetworkManager;
import com.sis.clightapp.model.Data;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends BaseActivity {

    Button opAdmin, opMerchant, opCheckOut;
    final String SUCCESS = "success";
    final String IS_USER_LOGIN = "isuserlogin";
    String TAG = "CLighting App";
    String ip = "";
    int port = 0000;
    final String dflUserId = "checkoutuser";
    final String dflPsswd = "user123";
    final int CHECKOUT = 2000;
    TextView setTextWithSpan;
    int recallTime = 0;

    private WebSocketClient webSocketClient;
    int code = 0;

    //TODO: For All Status Checks!!!
    boolean isCheckOutConfirmed = false;
    boolean isMerchantConfirmed = false;
    boolean isAdminConfirmed = false;
    boolean isBitcoinConfirmed = false;
    boolean isThorConfirmed = false;
    boolean isLightningConfirmed = false;
    ImageView thorNodeStatusImg;
    //            lightningNodeStatusImg,bitcoinNodeStatusImg;
    ProgressDialog checkStatusPD;
    boolean isConfirmMerchant = false;
    MerchantData currentMerchantData;
    ProgressDialog startServerPD, stopServerPD, wait20SecPD;
    Bundle bundle;
    boolean isFromLogin = false;

    @Override
    public void onBackPressed() {
        //   super.onBackPressed();
        ask_exit();
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
//                sharedPreferences.clearAllPrefExceptOfSShkeyPassword(MainActivity.this);
//                Intent ii = new Intent(MainActivity.this, MainEntryActivity.class);
                Intent ii = new Intent(MainActivity.this, MainEntryActivityNew.class);
                startActivity(ii);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recallTime = 0;
        //checkLocationPermisiion();
        //getLatestLocation();
        checkStatusPD = new ProgressDialog(bContext);
        checkStatusPD.setMessage("Loading...");
        checkStatusPD.setCancelable(false);
        startServerPD = new ProgressDialog(bContext);
        startServerPD.setMessage("Loading...");
        startServerPD.setCancelable(false);
        stopServerPD = new ProgressDialog(bContext);
        stopServerPD.setMessage("Loading...");
        stopServerPD.setCancelable(false);
        wait20SecPD = new ProgressDialog(bContext);
        wait20SecPD.setMessage("Loading...");
        wait20SecPD.setCancelable(false);
        currentMerchantData = GlobalState.getInstance().getMerchantData();
        if (currentMerchantData != null) {
            isConfirmMerchant = true;
            isThorConfirmed = true;
            GlobalState.getInstance().setMerchantConfirm(true);
        } else {
            isThorConfirmed = false;
            isConfirmMerchant = false;
            GlobalState.getInstance().setMerchantConfirm(false);
        }
        dialog = new ProgressDialog(bContext);
        dialog.setMessage("Connecting...");
        setTextWithSpan = findViewById(R.id.imageView3);

        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);
        thorNodeStatusImg = findViewById(R.id.thor_status_main);
        updateStatusBox(0, true);

//        lightningNodeStatusImg = findViewById(R.id.lightning_status);
//        bitcoinNodeStatusImg = findViewById(R.id.bitcoin_status);
        opAdmin = findViewById(R.id.optionAdmin);
        opMerchant = findViewById(R.id.optionMerchant);
        opCheckOut = findViewById(R.id.optionCheckout);
        Intent iin = getIntent();
        bundle = iin.getExtras();

        if (bundle != null) {
            isFromLogin = bundle.getBoolean("isFromLogin");
        }

        opAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (isThorConfirmed) {
                    Log.e(TAG, "Admin Mode Selected");
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    i.putExtra("role", "admin");
                    startActivity(i);
                    finish();
//                } else {
//                    goAlertDialogwithOneBTn(1, "", "Your Sorvereign is Not Connect!!!", "OK", "");
//
//                }
            }
        });
        opMerchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //luqman comment
//                if (isThorConfirmed) {
                Log.e(TAG, "Merchant Mode Selected");
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra("role", "merchant");
                startActivity(i);
                finish();
//                } else {
//                    goAlertDialogwithOneBTn(1, "", "Your Sorvereign is Not Connect!!!", "OK", "");
//                }

            }
        });
        opCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (isThorConfirmed && isLightningConfirmed && isBitcoinConfirmed) {
                Log.e(TAG, "Selecting CheckOut Mode");
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra("role", "checkout");
                startActivity(i);
                finish();
//                } else {
//                    if (!isLightningConfirmed) {
//                        goAlertDialogwithOneBTn(1, "", "Lightning Node is Down", "OK", "");
//                    } else if (!isBitcoinConfirmed) {
//                        goAlertDialogwithOneBTn(1, "", "Bitcoin Node is Down", "OK", "");
//                    }
//                }
            }
        });

        if (!isFromLogin) {
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

                } else {
                    //  checkAppFlow();

                    updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this));
                    updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this));
                    updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this));

                }
            } else {
                // checkAppFlow();

                updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this));
                updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this));
                updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this));

            }
            // showToast("User Not Login");

        } else {
            updateStatusBox(1, sharedPreferences.getThorStatus(THORSTATUS, this));
            updateStatusBox(2, sharedPreferences.getLightningStatus(LIGHTNINGSTATUS, this));
            updateStatusBox(3, sharedPreferences.getBitcoinStatus(BITCOINSTATUS, this));
        }
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
                goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
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
    }

    private void goToLightningDownCase() {
        updateStatusBox(2, false);
        goAlertDialogwithOneBTn(1, "", "Please Restart Lightning Node Manually!", "OK", "");
    }

    //TODO: Update the Status Box
    private void updateStatusBox(int i, boolean b) {
//        switch (i) {
//            case 1:
//                if (b) {
//                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
//                    isThorConfirmed = true;
//                    sharedPreferences.setThorStatus(true, THORSTATUS, this);
//                    thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
//                    isThorConfirmed = false;
//                    sharedPreferences.setThorStatus(false, THORSTATUS, this);
//                    thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
//                }
//                break;
//            case 2:
//                if (b) {
//                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
//                    isLightningConfirmed = true;
//                    sharedPreferences.setLightningStatus(true, LIGHTNINGSTATUS, this);
////                    lightningNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
//                    isLightningConfirmed = false;
//                    sharedPreferences.setLightningStatus(false, LIGHTNINGSTATUS, this);
////                    lightningNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
//                }
//                break;
//            case 3:
//                if (b) {
//                    sharedPreferences.setBoolean(true, ISALLSERVERUP, this);
//                    isBitcoinConfirmed = true;
//                    sharedPreferences.setBitcoinStatus(true, BITCOINSTATUS, this);
////                    bitcoinNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, ISALLSERVERUP, this);
//                    isBitcoinConfirmed = false;
//                    sharedPreferences.setBitcoinStatus(false, BITCOINSTATUS, this);
////                    bitcoinNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));
//                }
//                break;
//        }

        if (sharedPreferences.getvalueofconnectedSocket("socketconnected", bContext).equals("")) {
            thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.redstatus));

        } else {
            thorNodeStatusImg.setImageDrawable(getDrawable(R.drawable.greenstatus));

        }
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
}
