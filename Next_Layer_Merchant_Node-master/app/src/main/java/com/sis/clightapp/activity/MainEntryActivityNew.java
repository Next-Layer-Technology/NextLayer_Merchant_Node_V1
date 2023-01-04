package com.sis.clightapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sis.clightapp.Interface.ApiClient2;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiFCM;
import com.sis.clightapp.Interface.Webservice;
import com.sis.clightapp.Interface.ApiPaths2;
import com.sis.clightapp.R;
import com.sis.clightapp.fragments.printing.PrintDialogFragment;
import com.sis.clightapp.fragments.shared.Auth2FaFragment;
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.util.CustomSharedPreferences;
import com.sis.clightapp.util.GlobalState;
import com.sis.clightapp.model.FCMResponse;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp;
import com.sis.clightapp.model.REST.get_session_response;
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse;
import com.sis.clightapp.model.WebsocketResponse.WebSocketResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MainEntryActivityNew extends BaseActivity {
    TextView register_btn, cancel_action, register_action;
    ProgressDialog confirmingProgressDialog;
    MerchantData currentMerchantData;
    boolean isConfirmMerchant = false;
    boolean isLoginMerchant = false;
    int code = 0;
    String code1 = "";
    private WebSocketClient webSocketClient;
    KeyguardManager keyguardManager;

    private static final String TAG = HomeActivity.class.getName();

    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_entry_new);

        confirmingProgressDialog = new ProgressDialog(MainEntryActivityNew.this);
        confirmingProgressDialog.setMessage("Confirming...");
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setCanceledOnTouchOutside(false);
        register_btn = findViewById(R.id.register_btn);
//        Button button = findViewById(R.id.print);
//        button.setVisibility(View.VISIBLE);
//        button.setOnClickListener(v -> {
//            Invoice invoice = new Invoice();
//            invoice.setLabel("Label");
//            invoice.setAmount_msat("60000");
//            invoice.setAmount_received_msat("0.0000001");
//            invoice.setDescription("description");
//            invoice.setBolt11("lnbc662n1p3cd6m2sp5r6ulc2tgry7c9smm9ndrmxptg0mwmkyhk3sa6em2c8yqq4s8ntlqpp5juxqr05v30l4vuqf6g067xwl4q8xw69nwsttrncwx49lp8f9ve2sdq8v3jhxccxqzfvcqpjrzjqflfuth6uaxmx7pvaj304s4p9qzkm2gj0qhhg34k2h8w882fdsupgzadygqqvxqqqyqqqqqqqqqqqqqqyg9qyysgq8cpttudvl6z7zgclccfl36kqdsjcjsz60qg5zrhsdlz0y5w3l7xj0pystnuyu6s927da0hwqq5vycsuwys400qe3dungn9q5pn0gxyqqsz45sd");
//            invoice.setPayment_hash("970c01be8c8bff567009d21faf19dfa80e6768b37416b1cf0e354bf09d256655");
//            invoice.setExpires_at(System.currentTimeMillis());
//            invoice.setMsatoshi(60000);
//            invoice.setPaid_at(System.currentTimeMillis());
//            invoice.setPay_index(1);
//            invoice.setStatus("complete");
//            new PrintDialogFragment(invoice, null, new ArrayList()).show(getSupportFragmentManager(), null);
//        });
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        getExpireToken();
        if (new CustomSharedPreferences().getvalueofMerchantId("merchant_id", this) != 0) {
            findMerchant(new CustomSharedPreferences().getvalueofMerchantname("merchant_name", this), new CustomSharedPreferences().getvalueofMerchantpassword("merchant_pass", this));
        }
        if (!keyguardManager.isKeyguardSecure()) {
            dialog_LockCheck();
        }
        register_btn.setOnClickListener(v -> {
            if (sharedPreferences.getislogin("registered", this)) {
                showToast("You are registered already");
            } else {
                if (sharedPreferences.getissavecredential("credential", this)) {
                    dialogB();
                } else {
                    dialogA();
                }
                isLoginMerchant = false;
                GlobalState.getInstance().setLogin(isLoginMerchant);
            }
        });
        findViewById(R.id.signin_btn).setOnClickListener(v -> {
            if (sharedPreferences.getislogin("registered", this)) {
                isLoginMerchant = true;
                GlobalState.getInstance().setLogin(isLoginMerchant);
                Intent i = keyguardManager.createConfirmDeviceCredentialIntent("Authentication required", "password");
                startActivityForResult(i, 241);
            } else {
                loginPressed();
            }
        });

        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        String prompt = getResources().getString(R.string.scanqrfornewmembertoken);
        qrScan.setPrompt(prompt);
    }

    public void setToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("tFCM", "Fetching FCM registration token failed", task.getException());
                return;
            }
            String token = task.getResult();
            sendRegistrationToServer(token);
            Log.d("tes2Fcm", token);
        });
    }

    private void sendRegistrationToServer(String token) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nextlayer.live/testfcm/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiFCM apiInterface = retrofit.create(ApiFCM.class);
        try {
            JsonObject paramObject = new JsonObject();
            paramObject.addProperty("fcmRegToken", token);
            JsonObject paramObject1 = new JsonObject();
            paramObject1.addProperty("pwsUpdate", "New Token");
            paramObject.add("payload", paramObject1);

            Call<Object> call = apiInterface.FcmHitForToken(paramObject);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    Log.e("TAG", "onResponse: " + response.body().toString());

                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.e("TAG", "onResponse: " + t.getMessage().toString());

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 241) {
            if (resultCode == RESULT_OK) {
                if (code == 724) {
                    dialogC();
                } else {
                    createWebSocketClient();
                }
                Toast.makeText(this, "Success: Verified user's identity", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failure: Unable to verify user's identity", Toast.LENGTH_SHORT).show();
            }
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    showToast("Result Not Found");

                } else {
                    String memberToken = result.getContents();
                    if (et_email != null) {
                        et_email.setText(memberToken);
                    }

                    String ip_Address = et_ipaddress.getText().toString();
                    if (!ip_Address.isEmpty()) {
                        sharedPreferences.setvalueofipaddress(ip_Address, "ip", this);
                    }


                    if (sharedPreferences.getvalueofRefresh("refreshToken", this).equals("")) {
                        if (memberToken.isEmpty()) {
                            showToast("Enter refresh Token");
                        } else if (sharedPreferences.getvalueofipaddress("ip", this).equals("")) {
                            showToast("Enter Ip Adress");
                        } else {
                            try {
                                getOTP(memberToken);
                                dialogBBuilder.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    } else {
                        if (sharedPreferences.getvalueofipaddress("ip", this).equals("")) {
                            showToast("Enter Ip Adress");
                        } else {
                            try {
                                getOTP(sharedPreferences.getvalueofRefresh("refreshToken", this));
                                dialogBBuilder.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void getOTP(final String refresh) throws JSONException {
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", refresh);

        Call<WebSocketResponse> call = ApiClient2.getRetrofit().create(ApiPaths2.class).getotp(jsonObject1);
        call.enqueue(new Callback<WebSocketResponse>() {
            @Override
            public void onResponse(@NonNull Call<WebSocketResponse> call, @NonNull Response<WebSocketResponse> response) {
                if (response.body() != null) {

                    WebSocketResponse webSocketResponse = response.body();

                    if (webSocketResponse.getCode() == 700) {
                        sharedPreferences.setvalueofOtpSecret(webSocketResponse.getToken(), "otpsecret", MainEntryActivityNew.this);
                        sharedPreferences.setvalueofRefresh(refresh, "refreshToken", MainEntryActivityNew.this);
                        if (!webSocketResponse.getToken().isEmpty()) {
                            sharedPreferences.setvalueofRefresh(refresh, "refreshToken", MainEntryActivityNew.this);
                            dialog_Otp_Code(webSocketResponse.getToken());
                        }

                    } else if (webSocketResponse.getCode() == 701) {
                        sharedPreferences.setvalueofRefresh(refresh, "refreshToken", MainEntryActivityNew.this);
                        dialogC();
                        showToast(webSocketResponse.getMessage());
                    } else if (webSocketResponse.getCode() == 702) {
                        showToast(webSocketResponse.getMessage());
                    } else if (webSocketResponse.getCode() == 703) {
                        showToast(webSocketResponse.getMessage());
                    } else if (webSocketResponse.getCode() == 704) {

                        showToast(webSocketResponse.getMessage());
                    } else if (webSocketResponse.getCode() == 711) {
                        showToast(webSocketResponse.getMessage());

                    } else if (webSocketResponse.getCode() == 716) {
                        showToast(webSocketResponse.getMessage());

                    } else if (webSocketResponse.getCode() == 721) {
                        showToast(webSocketResponse.getMessage());

                    } else if (webSocketResponse.getCode() == 722) {
                        showToast(webSocketResponse.getMessage());

                    } else if (webSocketResponse.getCode() == 723) {
                        showToast(webSocketResponse.getMessage());

                    } else if (webSocketResponse.getCode() == 724) {
                        goTo2FaPasswordDialog(refresh);
                        showToast(webSocketResponse.getMessage());
                    } else if (webSocketResponse.getCode() == 725) {
                        showToast(webSocketResponse.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WebSocketResponse> call, @NonNull Throwable t) {
                dialog_GetInfo(1, t.getMessage());
                Log.e("get-funding-nodes:", t.getMessage());
                showToast("get-funding-nodes:" + t.getMessage());
            }
        });
    }

    private void getToken(String refresh, String twofactor_key) {
        int time = new CustomSharedPreferences().getvalueofExpierTime(this);
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", refresh);
        jsonObject1.addProperty("twoFactor", twofactor_key);
        jsonObject1.addProperty("time", time);

        Call<WebSocketOTPresponse> call = ApiClient2.getRetrofit().create(ApiPaths2.class).gettoken(jsonObject1);
        call.enqueue(new Callback<WebSocketOTPresponse>() {
            @Override
            public void onResponse(@NonNull Call<WebSocketOTPresponse> call, @NonNull Response<WebSocketOTPresponse> response) {
                confirmingProgressDialog.dismiss();
                if (response.body() != null) {
                    WebSocketOTPresponse webSocketOTPresponse = response.body();

                    if (webSocketOTPresponse.getCode() == 700) {
                        code = 0;
                        sharedPreferences.setislogin(true, "registered", MainEntryActivityNew.this);
                        if (webSocketOTPresponse.getToken().equals("")) {
                        } else {
                            sharedPreferences.setvalueofaccestoken(webSocketOTPresponse.getToken(), "accessToken", MainEntryActivityNew.this);
                            createWebSocketClient();
                            String isTokenSet = new CustomSharedPreferences().getvalue("IsTokenSet", MainEntryActivityNew.this);
                            if (isTokenSet.equals("1")) {
                                String token = new CustomSharedPreferences().getString("FcmToken", MainEntryActivityNew.this);
                                if (token != null) {
                                    setFCMToken(token, refresh);
                                }
                            }
                        }

                    } else if (webSocketOTPresponse.getCode() == 701) {
                        dialogC();
                        showToast("Missing 2FA code when requesting an access token");
                    } else if (webSocketOTPresponse.getCode() == 702) {
                        dialogC();
                        showToast("2FA code is incorrect / has timed out (30s window)");
                    } else if (webSocketOTPresponse.getCode() == 703) {
                        showToast("refresh token missing when requesting access code");
                    } else if (webSocketOTPresponse.getCode() == 704) {

                        showToast("refresh token missing when requesting access code");
                    } else if (webSocketOTPresponse.getCode() == 711) {
                        showToast("error -> attempting to initialize 2FA with the admin refresh code in a client system");

                    } else if (webSocketOTPresponse.getCode() == 716) {
                        showToast("Refresh token has expired (6 months), a new one is being mailed to the user");

                    } else if (webSocketOTPresponse.getCode() == 721) {
                        showToast("SendCommands is missing a \"commands\" field");

                    } else if (webSocketOTPresponse.getCode() == 722) {
                        showToast("SendCommands is missing a \"token\" with the access token");

                    } else if (webSocketOTPresponse.getCode() == 723) {

                        showToast("SendCommands received a refresh token instead of an access token");
                    } else if (webSocketOTPresponse.getCode() == 724) {
                        showToast("Access token has expired (at this point request 2FA code and get a new access token from /Refresh");
                        goTo2FaPasswordDialog(refresh);
                    } else if (webSocketOTPresponse.getCode() == 725) {
                        showToast("Misc websocket error, \"message\" field will include more data");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WebSocketOTPresponse> call, @NonNull Throwable t) {
                confirmingProgressDialog.dismiss();
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });
    }

    private void setFCMToken(String tokenFCM, String refreshToken) {
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", refreshToken);
        jsonObject1.addProperty("fcmRegToken", tokenFCM);

        Call<FCMResponse> call = ApiClient2.getRetrofit().create(ApiPaths2.class).setFcmToken(jsonObject1);
        call.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {
                assert response.body() != null;
                Log.d(TAG, "onResponse: " + response.body());
                if (response.body() != null) {
                    FCMResponse fcmResponse = response.body();
                    if (fcmResponse.getCode() == 700) {
                        new CustomSharedPreferences().setvalue("0", "IsTokenSet", getApplicationContext());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FCMResponse> call, @NonNull Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });

    }

    public void loginPressed() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialoglayoutpresslogin, null);
        dialogBuilder.setView(dialogView);
        cancel_action = dialogView.findViewById(R.id.cancel_action);
        register_action = dialogView.findViewById(R.id.register_action);
        cancel_action.setOnClickListener(v -> dialogBuilder.dismiss());
        register_action.setOnClickListener(v -> {
            if (sharedPreferences.getissavecredential("credential", this)) {
                dialogB();
            } else {
                dialogA();
            }

            dialogBuilder.dismiss();
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void dialogA() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_a, null);
        dialogBuilder.setView(dialogView);

        final EditText merchantIdEt = dialogView.findViewById(R.id.merchantid_et_register);
        final EditText merchantPassEt = dialogView.findViewById(R.id.merchantpass_et_register);

        Button submit = dialogView.findViewById(R.id.confirm);

        submit.setOnClickListener(v -> {
            String merchantId = merchantIdEt.getText().toString();
            String merchantPass = merchantPassEt.getText().toString();
            if (merchantId.equals("")) {
                showToast("please add user Id first!");
            } else if (merchantPass.equals("")) {
                showToast("please add user Password first!");
            } else {
                findMerchant(merchantId, merchantPass);
            }
            dialogBuilder.dismiss();
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    EditText et_email;
    EditText et_ipaddress;
    android.app.AlertDialog dialogBBuilder;

    public void dialogB() {
        dialogBBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.registerpopup_b, null);
        et_email = dialogView.findViewById(R.id.et_email2);
        et_ipaddress = dialogView.findViewById(R.id.ip_address);
        dialogBBuilder.setCanceledOnTouchOutside(false);
        et_email.setVisibility(View.VISIBLE);
        if (sharedPreferences.getvalueofipaddress("ip", this).equals("")) {
            et_ipaddress.setVisibility(View.VISIBLE);
        } else {
            et_ipaddress.setVisibility(View.GONE);
        }
        dialogBBuilder.setView(dialogView);
        Button confirm = dialogView.findViewById(R.id.confirmlink);
        Button scanQRCode = dialogView.findViewById(R.id.btn_scanQR);

        scanQRCode.setOnClickListener(v -> qrScan.initiateScan());

        confirm.setOnClickListener(v -> {
            String refresh = et_email.getText().toString();
            String ip_Address = et_ipaddress.getText().toString();
            if (!ip_Address.isEmpty()) {
                sharedPreferences.setvalueofipaddress(ip_Address, "ip", this);
            }
            if (sharedPreferences.getvalueofRefresh("refreshToken", this).equals("")) {
                if (refresh.isEmpty()) {
                    showToast("Enter refresh Token");
                } else if (sharedPreferences.getvalueofipaddress("ip", this).equals("")) {
                    showToast("Enter Ip Adress");
                } else {
                    try {
                        getOTP(refresh);
                        dialogBBuilder.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (sharedPreferences.getvalueofipaddress("ip", this).equals("")) {
                    showToast("Enter Ip Adress");
                } else {
                    try {
                        getOTP(sharedPreferences.getvalueofRefresh("refreshToken", this));
                        dialogBBuilder.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        dialogBBuilder.setView(dialogView);
        dialogBBuilder.show();
    }

    public void dialogC() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_c, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setCanceledOnTouchOutside(false);

        final EditText codef2 = dialogView.findViewById(R.id.code2fa);
        Button confirm = dialogView.findViewById(R.id.confirm2fa);
        confirm.setOnClickListener(v -> {
            String code2faConfirm = codef2.getText().toString();
            getToken(sharedPreferences.getvalueofRefresh("refreshToken", this), code2faConfirm);
            dialogBuilder.dismiss();
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void dialog_Otp_Code(String otp) {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_otp_code, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCanceledOnTouchOutside(false);
        TextView otpcode = dialogView.findViewById(R.id.otpcode);
        otpcode.setText(otp);
        TextView next = dialogView.findViewById(R.id.register_action_next);
        next.setOnClickListener(v -> {
            dialogC();
            dialogBuilder.dismiss();
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void dialog_LockCheck() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_lockcheck, null);
        dialogBuilder.setView(dialogView);
        TextView action_ok = dialogView.findViewById(R.id.action_ok);
        action_ok.setOnClickListener(v -> finish());
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.show();
    }

    @SuppressLint("SetTextI18n")
    public void dialog_GetInfo(final int val, String message) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.getinfo_popup, null);
        dialogBuilder.setView(dialogView);
        TextView next = dialogView.findViewById(R.id.getinfo_action);
        TextView viewText = dialogView.findViewById(R.id.visual_text);
        if (val == 1) {
            next.setText("Close");
        }
        if (val == 3) {
            next.setText("Reconnect");
        }
        if (val == 2) {
            next.setText("Close");
        } else {
            next.setText("Next");
        }
        viewText.setText(message);

        next.setOnClickListener(v -> {
            if (val == 1) {
                dialogBuilder.dismiss();
            } else if (val == 2) {
                dialogBuilder.dismiss();
            } else if (val == 3) {
                createWebSocketClient();
                dialogBuilder.dismiss();
            } else {
                createWebSocketClient1();
                dialogBuilder.dismiss();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void findMerchant(final String id, final String pass) {
        confirmingProgressDialog.show();
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setCanceledOnTouchOutside(false);
        JsonObject paramObject = new JsonObject();
        paramObject.addProperty("user_id", id);
        paramObject.addProperty("password", pass);
        Call<MerchantLoginResp> call = ApiClientBoost.getRetrofit().create(Webservice.class).merchant_Loging(paramObject);
        call.enqueue(new Callback<MerchantLoginResp>() {
            @Override
            public void onResponse(@NonNull Call<MerchantLoginResp> call, @NonNull Response<MerchantLoginResp> response) {
                confirmingProgressDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getMessage().equals("successfully login")) {
                            MerchantData merchantData = new MerchantData();
                            merchantData = response.body().getMerchantData();
                            MerchantData myObject = response.body().getMerchantData();
                            Gson gson = new Gson();
                            String json = gson.toJson(myObject);
                            new CustomSharedPreferences().setvalueofMerchantData(json, "data", MainEntryActivityNew.this);
                            GlobalState.getInstance().setLattitude(merchantData.getLatitude());
                            GlobalState.getInstance().setLongitude(merchantData.getLongitude());
                            GlobalState.getInstance().setMerchantData(merchantData);
                            currentMerchantData = merchantData;

                            GlobalState.getInstance().setMerchant_id(id);
                            sharedPreferences.setString(currentMerchantData.getSsh_password(), "sshkeypass", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalueofMerchantname(id, "merchant_name", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalueofMerchantpassword(pass, "merchant_pass", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalueofMerchantId(merchantData.getId(), "merchant_id", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalueofContainerAddress(merchantData.getContainer_address(), "container_address", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalueofLightningPort(merchantData.getLightning_port(), "lightning_port", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalueofPWSPort(merchantData.getPws_port(), "pws_port", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalueofMWSPort(merchantData.getMws_port(), "mws_port", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalue(merchantData.getAccessToken(), "accessTokenLogin", MainEntryActivityNew.this);
                            new CustomSharedPreferences().setvalue(merchantData.getRefreshToken(), "refreshTokenLogin", MainEntryActivityNew.this);

                            String mwsCommad = "ws://" + merchantData.getContainer_address() + ":" + merchantData.getMws_port() + "/SendCommands";
                            new CustomSharedPreferences().setvalueofMWSCommand(mwsCommad, "mws_command", MainEntryActivityNew.this);
                            sharedPreferences.setvalueofipaddress(merchantData.getContainer_address() + ":" + merchantData.getMws_port(), "ip", MainEntryActivityNew.this);

                            //private final String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";

                            //gotoTestCase(merchantData);
                            if (sharedPreferences.getislogin("registered", MainEntryActivityNew.this)) {

                            } else {
                                if (isLoginMerchant) {
                                    if (sharedPreferences.getvalueofSocketCode("socketcode", MainEntryActivityNew.this) == 724) {
                                        dialogC();
                                    } else if (sharedPreferences.getvalueofSocketCode("socketcode", MainEntryActivityNew.this) == 722) {
                                        dialogC();
                                    } else {
                                        createWebSocketClient();
                                    }
                                } else {
                                    dialogB();
                                }
                            }
                        } else {
                            isConfirmMerchant = false;
                            goAlertDialogwithOneBTn("Invalid Merchant ID!");
                        }
                    } else {
                        isConfirmMerchant = false;
                        Log.e("Error:", response.toString());
                        goAlertDialogwithOneBTn("Server Error");

                    }
                } else {
                    isConfirmMerchant = false;
                    Log.e("Error:", response.toString());
                    goAlertDialogwithOneBTn("Server Error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<MerchantLoginResp> call, @NonNull Throwable t) {
                isConfirmMerchant = false;
                confirmingProgressDialog.dismiss();
                goAlertDialogwithOneBTn("Network Error");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void goAlertDialogwithOneBTn(final String alertMessage) {
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog = new Dialog(this);
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
        Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        goAlertDialogwithOneBTnDialog.setCancelable(false);
        final TextView alertTitle_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
        final TextView alertMessage_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
        final Button yesbtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
        final Button nobtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
        yesbtn.setText("OK");
        nobtn.setText("");
        alertTitle_tv.setText("");
        alertMessage_tv.setText(alertMessage);
        nobtn.setVisibility(View.GONE);
        alertTitle_tv.setVisibility(View.GONE);
        yesbtn.setOnClickListener(v -> {
            goAlertDialogwithOneBTnDialog.dismiss();
            if (alertMessage.equals("Invalid Merchant ID!")) {
                if (!sharedPreferences.getissavecredential("credential", this)) {
                    dialogA();
                }
            }
        });
        nobtn.setOnClickListener(v -> goAlertDialogwithOneBTnDialog.dismiss());
        goAlertDialogwithOneBTnDialog.show();

    }

    private void createWebSocketClient() {
        Log.v(TAG, "createWebSocketClient: ");
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://" + sharedPreferences.getvalueofipaddress("ip", this) + "/SendCommands");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Log.v(TAG, "createWebSocketClient: " + uri);
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", MainEntryActivityNew.this);
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"ls\", \"ls -l\"] }";
                try {
                    JSONObject obj = new JSONObject(json);
                    Log.d("My App", obj.toString());
                    webSocketClient.send(String.valueOf(obj));
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }
                Log.i("WebSocket", "Session is starting");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                sharedPreferences.setvalueofSocketCode(0, "socketcode", MainEntryActivityNew.this);

                if (s.equals("{\"code\":724,\"message\":\"Access token has expired, please request a new token\"}")) {
                    try {
                        Log.v(TAG, "onTextReceived: " + s);
                        JSONObject jsonObject = new JSONObject(s);
                        code = jsonObject.getInt("code");
                        sharedPreferences.setvalueofSocketCode(code, "socketcode", MainEntryActivityNew.this);
                        if (code == 724) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialogC();
                                }
                            });

                            webSocketClient.close();
                        } else if (code == 700) {
                            webSocketClient.close();
                        }

                    } catch (JSONException err) {
                        Log.e(TAG, err.toString());
                    }

                } else if (s.equals("{\"code\":723,\"message\":\"Access token is invalid\"}")) {
                    Log.v(TAG, "onTextReceived: " + s);
                    runOnUiThread(() -> goTo2FaPasswordDialog(sharedPreferences.getvalueofRefresh("refreshToken", MainEntryActivityNew.this)));

                } else {
                    if (GlobalState.getInstance().getLogin()) {
                        runOnUiThread(() -> createWebSocketClient1());
                    } else {
                        runOnUiThread(() -> dialog_GetInfo(0, "Your node is now registered. The next time you log in you may do so using device based two-factor authentication."));
                    }

                }


            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(final Exception e) {
                System.out.println(e.getMessage());
                runOnUiThread(() -> {
                    if (e.getMessage().equals("Attempt to invoke virtual method 'boolean java.lang.Boolean.booleanValue()' on a null object reference")) {
                        dialog_GetInfo(0, "Your node is now registered. The next time you log in you may do so using device based two-factor authentication.");
                    } else {
                        dialog_GetInfo(2, e.getMessage());
                    }

                });
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(100000);
        webSocketClient.setReadTimeout(600000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    private void createWebSocketClient1() {
        URI uri;
        try {
            uri = new URI("ws://" + sharedPreferences.getvalueofipaddress("ip", this) + "/SendCommands");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", MainEntryActivityNew.this);
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli getinfo\"] }";
                try {
                    JSONObject obj = new JSONObject(json);
                    Log.d("My App", obj.toString());
                    webSocketClient.send(String.valueOf(obj));
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }
                Log.i("WebSocket", "Session is starting");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    code1 = jsonObject.getString("id");
                    if (code1.equals("")) {
                        sharedPreferences.setvalueofconnectedSocket("", "socketconnected", MainEntryActivityNew.this);
                    } else {
                        sharedPreferences.setvalueofconnectedSocket(code1, "socketconnected", MainEntryActivityNew.this);
                        runOnUiThread(() -> {
                            Intent i = new Intent(MainEntryActivityNew.this, HomeActivity.class);
                            startActivity(i);
                        });
                    }
                    if (code == 724) {
                        sharedPreferences.setvalueofSocketCode(code, "socketcode", MainEntryActivityNew.this);
                        webSocketClient.close();
                    }

                } catch (JSONException err) {
                    Log.d("Error", err.toString());
                }

            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(final Exception e) {
                System.out.println(e.getMessage());
                runOnUiThread(() -> {
                    dialog_GetInfo(2, e.getMessage());
                });
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(100000);
        webSocketClient.setReadTimeout(600000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }


    private void getExpireToken() {
        Call<get_session_response> call = ApiClientBoost.getRetrofit().create(Webservice.class).get_session("merchant", "haiww82uuw92iiwu292isk");
        call.enqueue(new Callback<get_session_response>() {
            @Override
            public void onResponse(@NonNull Call<get_session_response> call, @NonNull Response<get_session_response> response) {
                if (response.body() != null) {
                    get_session_response loginresponse = response.body();
                    if (loginresponse.getSession_token() != null) {
                        new CustomSharedPreferences().setvalueofExpierTime(Integer.parseInt(loginresponse.getSession_token()), MainEntryActivityNew.this);
                    } else {
                        showToast("Response empty");
                        new CustomSharedPreferences().setvalueofExpierTime(300, MainEntryActivityNew.this);
                    }
                } else {
                    new CustomSharedPreferences().setvalueofExpierTime(300, MainEntryActivityNew.this);
                }
            }

            @Override
            public void onFailure(@NonNull Call<get_session_response> call, @NonNull Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
                new CustomSharedPreferences().setvalueofExpierTime(300, MainEntryActivityNew.this);
            }
        });


    }


    private void goTo2FaPasswordDialog(String accessToken) {
        final Dialog enter2FaPassDialog;
        enter2FaPassDialog = new Dialog(this);
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
                String task = String.valueOf(et_2Fa_pass.getText());
                if (task.isEmpty()) {
                    showToast("Enter 2FA Password");
                } else {
                    enter2FaPassDialog.dismiss();
                    confirmingProgressDialog.show();
                    confirmingProgressDialog.setCancelable(false);
                    confirmingProgressDialog.setCanceledOnTouchOutside(false);

                    getSessionToken(accessToken, task);
                }

            }
        });
        btn_cancel.setOnClickListener(v -> enter2FaPassDialog.dismiss());
        enter2FaPassDialog.show();
    }

    private void getSessionToken(String accessToken, String twoFaCode) {
        Call<get_session_response> call = ApiClientBoost.getRetrofit().create(Webservice.class).get_session("merchant", "haiww82uuw92iiwu292isk");
        call.enqueue(new Callback<get_session_response>() {
            @Override
            public void onResponse(@NonNull Call<get_session_response> call, @NonNull Response<get_session_response> response) {
                if (response.body() != null) {
                    get_session_response loginresponse = response.body();
                    if (Integer.parseInt(loginresponse.getSession_token()) != -1) {
                        new CustomSharedPreferences().setvalueofExpierTime(Integer.parseInt(loginresponse.getSession_token()), MainEntryActivityNew.this);
                        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", MainEntryActivityNew.this);
                        getToken(RefToken, twoFaCode);
                    } else {
                        confirmingProgressDialog.dismiss();
                        showToast("Response empty");
                    }
                } else {
                    confirmingProgressDialog.dismiss();
                    try {
                        if (response.errorBody() != null) {
                            showToast(response.errorBody().string());
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<get_session_response> call, @NonNull Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
                confirmingProgressDialog.dismiss();
                showToast(t.getMessage());
            }
        });
    }
}