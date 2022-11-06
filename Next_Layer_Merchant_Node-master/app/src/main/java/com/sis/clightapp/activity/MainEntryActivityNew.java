package com.sis.clightapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sis.clightapp.Interface.ApiClient2;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiFCM;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Interface.ApiPaths2;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.model.Channel_BTCResponseData;
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
import java.util.Objects;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MainEntryActivityNew extends BaseActivity {
    TextView register_btn, cancel_action, register_action;
    ImageView signin_btn;
    ProgressDialog confirmingProgressDialog;
    MerchantData currentMerchantData;
    boolean isConfirmMerchant = false;
    boolean isLoginMerchant = false;
    boolean isValidAcessToken = false;
    int code = 0;
    boolean checkstaus = false;
    String code1 = "";
    private WebSocketClient webSocketClient;
    //Biometric
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    //key
    KeyguardManager keyguardManager;
    //volly
    private static final String TAG = MainActivity.class.getName();
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private JsonObjectRequest jsonObject;
    private String url = "http://98.226.215.246:8095";

    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_entry_new);

        //        MobileBiometric();
        confirmingProgressDialog = new ProgressDialog(MainEntryActivityNew.this);
        confirmingProgressDialog.setMessage("Confirming...");
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setCanceledOnTouchOutside(false);
        register_btn = findViewById(R.id.register_btn);
        signin_btn = findViewById(R.id.signin_btn);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        GetexpireToken();
        //SubscrieChannel();
        if (new CustomSharedPreferences().getvalueofMerchantId("merchant_id", this) == 0) {

        } else {
            findMerchant(new CustomSharedPreferences().getvalueofMerchantname("merchant_name", this), new CustomSharedPreferences().getvalueofMerchantpassword("merchant_pass", this));
        }
        if (keyguardManager.isKeyguardSecure()) {

        } else {
            dialog_LockCheck();
        }
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getislogin("registered", bContext)) {
                    showToast("You are registered already");
                } else {
                    if (sharedPreferences.getissavecredential("credential", bContext)) {
                        dialogB();
                    } else {
                        dialogA();
                    }
                    isLoginMerchant = false;
                    GlobalState.getInstance().setLogin(isLoginMerchant);
                }
            }
        });
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getislogin("registered", bContext)) {
                    //biometricPrompt.authenticate(promptInfo);
                    isLoginMerchant = true;
                    GlobalState.getInstance().setLogin(isLoginMerchant);
                    Intent i = keyguardManager.createConfirmDeviceCredentialIntent("Authentication required", "password");
                    startActivityForResult(i, 241);
                } else {
                    presslogin();
                }
            }
        });

        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        String prompt = getResources().getString(R.string.scanqrfornewmembertoken);
        qrScan.setPrompt(prompt);
        //setToken();

//        throw new RuntimeException("Test Crash"); // Force a crash
    }

    public void setToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("tFCM", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                // Get new FCM registration token
                String token = task.getResult();
                sendRegistrationToServer(token);
                // Log and toast
                String msg = token;
                Log.d("tes2Fcm", msg);
                // Toast.makeText(ActivitySplash.this, msg, Toast.LENGTH_SHORT).show();
            }
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
                    // JSONObject object=new JSONObject(new Gson().toJson(response.body()));
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
        // TODO: Implement this method to send token to your app server.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 241) {
            if (resultCode == RESULT_OK) {
                if (sharedPreferences.getvalueofaccestoken("accessToken", bContext).equals("")) {

                } else if (code == 724) {
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
                //if qrcode has nothing in it
                if (result.getContents() == null) {
                    showToast("Result Not Found");

                } else {
                    String memberToken = result.getContents();
                    if (et_email != null) {
                        et_email.setText(memberToken);
                    }

                    String refresh = memberToken;
                    String ip_Address = et_ipaddress.getText().toString();
                    if (ip_Address.equals("")) {
                    } else {
                        sharedPreferences.setvalueofipaddress(ip_Address, "ip", bContext);
                    }


                    if (sharedPreferences.getvalueofRefresh("refreshToken", bContext).equals("")) {
                        if (refresh.isEmpty()) {
                            showToast("Enter refresh Token");
                        } else if (sharedPreferences.getvalueofipaddress("ip", bContext).equals("")) {
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
                        if (sharedPreferences.getvalueofipaddress("ip", bContext).equals("")) {
                            showToast("Enter Ip Adress");
                        } else {
                            try {
                                getOTP(sharedPreferences.getvalueofRefresh("refreshToken", bContext));
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
            public void onResponse(Call<WebSocketResponse> call, Response<WebSocketResponse> response) {
                if (response.body() != null) {

                    WebSocketResponse webSocketResponse = response.body();

                    if (webSocketResponse != null) {
                        if (webSocketResponse.getCode() == 700) {
                            sharedPreferences.setvalueofOtpSecret(webSocketResponse.getToken(), "otpsecret", bContext);
                            sharedPreferences.setvalueofRefresh(refresh, "refreshToken", bContext);
                            if (webSocketResponse.getToken().equalsIgnoreCase("")) {

                            } else {
                                sharedPreferences.setvalueofRefresh(refresh, "refreshToken", bContext);
                                dialog_Otp_Code(webSocketResponse.getToken());
                            }

                        } else if (webSocketResponse.getCode() == 701) {
                            sharedPreferences.setvalueofRefresh(refresh, "refreshToken", bContext);
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
            }

            @Override
            public void onFailure(Call<WebSocketResponse> call, Throwable t) {
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

        Call<WebSocketOTPresponse> call = (Call<WebSocketOTPresponse>) ApiClient2.getRetrofit().create(ApiPaths2.class).gettoken(jsonObject1);
        call.enqueue(new Callback<WebSocketOTPresponse>() {
            @Override
            public void onResponse(Call<WebSocketOTPresponse> call, Response<WebSocketOTPresponse> response) {
                confirmingProgressDialog.dismiss();
                if (response.body() != null) {
                    WebSocketOTPresponse webSocketOTPresponse = response.body();

                    if (webSocketOTPresponse != null) {

                        if (webSocketOTPresponse.getCode() == 700) {
                            code = 0;
                            sharedPreferences.setislogin(true, "registered", bContext);
//                            showToast(webSocketOTPresponse.getToken());
                            if (webSocketOTPresponse.getToken().equals("")) {
                            } else {
                                sharedPreferences.setvalueofaccestoken(webSocketOTPresponse.getToken(), "accessToken", bContext);
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
            }

            @Override
            public void onFailure(Call<WebSocketOTPresponse> call, Throwable t) {
                confirmingProgressDialog.dismiss();
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });
    }

    private void setFCMToken(String tokenFCM, String refreshToken) {
        ///admin/invoice-to-client
        /*Authorization: Bearer {ACCESS_TOKEN_HERE}
        {"client_id": "C1640282683975726","invoice": "asdfjalksdjflaksjdf","store_name": "Some big store"}*/
        String accessToken = new CustomSharedPreferences().getvalue("accessTokenLogin", getApplicationContext());
        String RefToken = new CustomSharedPreferences().getvalueofaccestoken("accessToken", getApplicationContext());
        String token = "Bearer" + " " + accessToken;
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", refreshToken);
        jsonObject1.addProperty("fcmRegToken", tokenFCM);

        Call<FCMResponse> call = (Call<FCMResponse>) ApiClient2.getRetrofit().create(ApiPaths2.class).setFcmToken( jsonObject1);
        call.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
                if (response.body() != null) {
                    FCMResponse fcmResponse = response.body();
                    if (fcmResponse.getCode() == 700) {
                        new CustomSharedPreferences().setvalue("0", "IsTokenSet", getApplicationContext());
                    }
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });

    }

    public void presslogin() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialoglayoutpresslogin, null);
        dialogBuilder.setView(dialogView);
        cancel_action = dialogView.findViewById(R.id.cancel_action);
        register_action = dialogView.findViewById(R.id.register_action);
        cancel_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });
        register_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getissavecredential("credential", bContext)) {
                    dialogB();
                } else {
                    dialogA();
                }

                dialogBuilder.dismiss();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void dialogA() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_a, null);
        dialogBuilder.setView(dialogView);

        final EditText merchantid_et = dialogView.findViewById(R.id.merchantid_et_register);
        final EditText merchantpass_et = dialogView.findViewById(R.id.merchantpass_et_register);

        Button submit = dialogView.findViewById(R.id.confirm);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String merchantId = merchantid_et.getText().toString();
                String merchantPass = merchantpass_et.getText().toString();
                if (merchantId.equals("")) {
                    showToast("please add user Id first!");
                } else if (merchantPass.equals("")) {
                    showToast("please add user Password first!");
                } else {
                    findMerchant(merchantId, merchantPass);
                }
                dialogBuilder.dismiss();
            }
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

//        if (sharedPreferences.getvalueofRefresh("refreshToken", bContext).equals("")) {
        et_email.setVisibility(View.VISIBLE);
//        } else {
//            et_email.setVisibility(View.GONE);
//        }
        if (sharedPreferences.getvalueofipaddress("ip", bContext).equals("")) {
            et_ipaddress.setVisibility(View.VISIBLE);
        } else {
            et_ipaddress.setVisibility(View.GONE);
        }
        dialogBBuilder.setView(dialogView);
        Button confirm = dialogView.findViewById(R.id.confirmlink);
        Button scanQRCode = dialogView.findViewById(R.id.btn_scanQR);

        scanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.initiateScan();

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String refresh = et_email.getText().toString();
                String ip_Address = et_ipaddress.getText().toString();
                if (ip_Address.equals("")) {
                } else {
                    sharedPreferences.setvalueofipaddress(ip_Address, "ip", bContext);
                }


                if (sharedPreferences.getvalueofRefresh("refreshToken", bContext).equals("")) {
                    if (refresh.isEmpty()) {
                        showToast("Enter refresh Token");
                    } else if (sharedPreferences.getvalueofipaddress("ip", bContext).equals("")) {
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
                    if (sharedPreferences.getvalueofipaddress("ip", bContext).equals("")) {
                        showToast("Enter Ip Adress");
                    } else {
                        try {
                            getOTP(sharedPreferences.getvalueofRefresh("refreshToken", bContext));
                            dialogBBuilder.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        dialogBBuilder.setView(dialogView);
        dialogBBuilder.show();
    }

    public void dialogC() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(bContext, R.style.AlertDialog).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_c, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setCanceledOnTouchOutside(false);

        final EditText codef2 = dialogView.findViewById(R.id.code2fa);
        Button confirm = dialogView.findViewById(R.id.confirm2fa);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codef2Confirm = codef2.getText().toString();
                getToken(sharedPreferences.getvalueofRefresh("refreshToken", bContext), codef2Confirm);
                dialogBuilder.dismiss();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void dialog_Otp_Code(String otp) {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_otp_code, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCanceledOnTouchOutside(false);
        TextView otpcode = dialogView.findViewById(R.id.otpcode);
        otpcode.setText(otp);
        TextView next = dialogView.findViewById(R.id.register_action_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogC();
                dialogBuilder.dismiss();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void dialog_LockCheck() {
        final android.app.AlertDialog dialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialog).create();
        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registerpopup_lockcheck, null);
        dialogBuilder.setView(dialogView);
        TextView action_ok = dialogView.findViewById(R.id.action_ok);
        action_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.show();
    }

    public void dialog_GetInfo(final int val, String message) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(bContext, R.style.AlertDialog).create();
        // ...Irrelevant code for customizing the buttons and title
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

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (checkstaus) {
//                    Intent i = new Intent(MainEntryActivityNew.this, MainActivity.class);
//                    startActivity(i);
//                    dialogBuilder.dismiss();
//                } else
                if (val == 1) {
                    dialogBuilder.dismiss();
                } else if (val == 2) {
                    dialogBuilder.dismiss();
                } else if (val == 3) {
                    createWebSocketClient();
                    dialogBuilder.dismiss();
                } else {
                    checkstaus = false;
                    createWebSocketClient1();
                    dialogBuilder.dismiss();
                }
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
        Call<MerchantLoginResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).merchant_Loging(paramObject);
        //Call<MerchantLoginResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).merchant_Loging(id,pass);
        call.enqueue(new Callback<MerchantLoginResp>() {
            @Override
            public void onResponse(Call<MerchantLoginResp> call, Response<MerchantLoginResp> response) {
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
                            //Log.e("2fapass", currentMerchantData.getPassword());

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
                            sharedPreferences.setvalueofipaddress(merchantData.getContainer_address() + ":" + merchantData.getMws_port(), "ip", bContext);

                            //private final String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";

                            //gotoTestCase(merchantData);
                            if (sharedPreferences.getislogin("registered", bContext)) {

                            } else {
                                if (isLoginMerchant) {
                                    if (sharedPreferences.getvalueofSocketCode("socketcode", bContext) == 724) {
                                        dialogC();
                                    } else if (sharedPreferences.getvalueofSocketCode("socketcode", bContext) == 722) {
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
                            GlobalState.getInstance().setMerchantConfirm(false);
                            goAlertDialogwithOneBTn(1, "", "Invalid Merchant ID!", "OK", "");
                        }
                    } else {
                        isConfirmMerchant = false;
                        Log.e("Error:", response.toString());
//                    showToast(response.toString());
                        goAlertDialogwithOneBTn(1, "", "Server Error", "OK", "");

                    }
                } else {
                    isConfirmMerchant = false;
                    Log.e("Error:", response.toString());
//                    showToast(response.toString());
                    goAlertDialogwithOneBTn(1, "", "Server Error", "OK", "");
                }
            }

            @Override
            public void onFailure(Call<MerchantLoginResp> call, Throwable t) {
                isConfirmMerchant = false;
                GlobalState.getInstance().setMerchantConfirm(false);
                confirmingProgressDialog.dismiss();
                //showToast("Network Error");
                goAlertDialogwithOneBTn(1, "", "Network Error", "OK", "");
            }
        });
    }

    private void goAlertDialogwithOneBTn(final int i, String alertTitleMessage, final String alertMessage, String alertBtn1Message, String alertBtn2Message) {
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
                if (alertMessage.equals("Invalid Merchant ID!")) {
                    if (sharedPreferences.getissavecredential("credential", bContext)) {

                    } else {
                        dialogA();
                    }
                } else if (alertMessage.equals("Incorrect Password")) {

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

    }

    private void createWebSocketClient() {
        Log.v(TAG, "createWebSocketClient: ");
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://" + sharedPreferences.getvalueofipaddress("ip", bContext) + "/SendCommands");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Log.v(TAG, "createWebSocketClient: " + uri);
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", bContext);

                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"ls\", \"ls -l\"] }";

                try {

                    JSONObject obj = new JSONObject(json);
                    Log.d("My App", obj.toString());
                    webSocketClient.send(String.valueOf(obj));
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

                Log.i("WebSocket", "Session is starting");
//                Toast.makeText(getApplicationContext(), "opend", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                sharedPreferences.setvalueofSocketCode(0, "socketcode", bContext);

                if (s.equals("")) {

                } else if (s.equals("{\"code\":724,\"message\":\"Access token has expired, please request a new token\"}")) {
                    try {
                        Log.v(TAG, "onTextReceived: " + s);
                        JSONObject jsonObject = new JSONObject(s);
                        code = jsonObject.getInt("code");
                        sharedPreferences.setvalueofSocketCode(code, "socketcode", bContext);
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
                        } else {

                        }

                    } catch (JSONException err) {

                    }

                } else if (s.equals("{\"code\":723,\"message\":\"Access token is invalid\"}")) {
                    Log.v(TAG, "onTextReceived: " + s);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            goTo2FaPasswordDialog(sharedPreferences.getvalueofRefresh("refreshToken", bContext));
                        }
                    });

                } else {
                    if (GlobalState.getInstance().getLogin()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createWebSocketClient1();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog_GetInfo(0, "Your node is now registered. The next time you log in you may do so using device based two-factor authentication.");
                            }
                        });
                    }

                }


            }

            @Override
            public void onBinaryReceived(byte[] data) {
//                showToast("binary" + data.toString());
            }

            @Override
            public void onPingReceived(byte[] data) {
//                showToast("ping" + data.toString());
            }

            @Override
            public void onPongReceived(byte[] data) {
//                showToast("ping2" + data.toString());
            }

            @Override
            public void onException(final Exception e) {
                System.out.println(e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e.getMessage().equals("Attempt to invoke virtual method 'boolean java.lang.Boolean.booleanValue()' on a null object reference")) {
//                            dialog_GetInfo(3, "ERROR!\n" + "Something went wrong");
                            dialog_GetInfo(0, "Your node is now registered. The next time you log in you may do so using device based two-factor authentication.");
                        } else {
                            checkstaus = true;
                            dialog_GetInfo(2, e.getMessage());
                        }

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
            // Connect to local host
            uri = new URI("ws://" + sharedPreferences.getvalueofipaddress("ip", bContext) + "/SendCommands");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", bContext);

                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli getinfo\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocketClient.send(String.valueOf(obj));


                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

                Log.i("WebSocket", "Session is starting");
//                Toast.makeText(getApplicationContext(), "opend", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                System.out.println(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    code1 = jsonObject.getString("id");
                    if (code1.equals("")) {
                        sharedPreferences.setvalueofconnectedSocket("", "socketconnected", bContext);
                    } else {
                        sharedPreferences.setvalueofconnectedSocket(code1, "socketconnected", bContext);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                checkstaus = true;
//                                dialog_GetInfo(0, "Your status is connected\npress Next for Mode selection screen");
//                            }
//                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(MainEntryActivityNew.this, MainActivity.class);
                                startActivity(i);
                            }
                        });
                    }
                    if (code == 724) {
                        sharedPreferences.setvalueofSocketCode(code, "socketcode", bContext);
                        webSocketClient.close();
                    } else if (code == 724) {
                        sharedPreferences.setvalueofSocketCode(code, "socketcode", bContext);
                        webSocketClient.close();
                    } else {

                    }

                } catch (JSONException err) {
                    Log.d("Error", err.toString());
                }

            }

            @Override
            public void onBinaryReceived(byte[] data) {
//                showToast("binary" + data.toString());
            }

            @Override
            public void onPingReceived(byte[] data) {
//                showToast("ping" + data.toString());
            }

            @Override
            public void onPongReceived(byte[] data) {
//                showToast("ping2" + data.toString());
            }

            @Override
            public void onException(final Exception e) {
                System.out.println(e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkstaus = false;
                        dialog_GetInfo(2, e.getMessage());
                    }
                });
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void MobileBiometric() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainEntryActivityNew.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                if (sharedPreferences.getvalueofaccestoken("accessToken", bContext).equals("")) {

                } else if (code == 724) {
                    dialogC();
                } else {
                    createWebSocketClient();
                }

                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();
    }

    private void GetexpireToken() {

        Call<get_session_response> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).get_session("merchant", "haiww82uuw92iiwu292isk");
        call.enqueue(new Callback<get_session_response>() {
            @Override
            public void onResponse(Call<get_session_response> call, Response<get_session_response> response) {
                if (response.body() != null) {
                    get_session_response loginresponse = response.body();

//                    if (loginresponse.getMessage().equals("successfully done")) {
                    if (loginresponse.getSession_token() != null) {
//                        showToast(loginresponse.getSession_token());
                        new CustomSharedPreferences().setvalueofExpierTime(Integer.parseInt(loginresponse.getSession_token()), MainEntryActivityNew.this);

                    } else {
                        showToast("Response empty");
                        new CustomSharedPreferences().setvalueofExpierTime(300, MainEntryActivityNew.this);
                    }

//                    }
                } else {
                    new CustomSharedPreferences().setvalueofExpierTime(300, MainEntryActivityNew.this);
                }
            }

            @Override
            public void onFailure(Call<get_session_response> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
//                showToast(t.getMessage());
                new CustomSharedPreferences().setvalueofExpierTime(300, MainEntryActivityNew.this);
            }
        });


    }

    private void SubscrieChannel() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("wss://ws.bitstamp.net/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {

                String json = "{\"event\":\"bts:subscribe\",\"data\":{\"channel\":\"live_trades_btcusd\"}}";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocketClient.send(String.valueOf(obj));


                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

                Log.i("WebSocket", "Session is starting");
//                Toast.makeText(getApplicationContext(), "opend", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;


                if (s.equals("")) {

                } else {

                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        final String subscription = jsonObject.getString("event");
                        final JSONObject objects = jsonObject.getJSONObject("data");
                        if (subscription.equals("bts:subscription_succeeded")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(subscription);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (objects != null) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Channel_BTCResponseData channel_btcResponseData = new Channel_BTCResponseData();
                                                    channel_btcResponseData.setId(objects.getInt("id"));
                                                    channel_btcResponseData.setTimestamp((objects.getString("timestamp")));
                                                    channel_btcResponseData.setAmount(Double.valueOf(objects.getDouble("amount")));
                                                    channel_btcResponseData.setAmount_str((objects.getString("amount_str")));
                                                    channel_btcResponseData.setPrice(Double.valueOf(objects.getDouble("price")));
                                                    channel_btcResponseData.setPrice_str((objects.getString("price_str")));
                                                    channel_btcResponseData.setType((objects.getInt("type")));
                                                    channel_btcResponseData.setMicrotimestamp((objects.getString("microtimestamp")));
                                                    channel_btcResponseData.setBuy_order_id(objects.getInt("buy_order_id"));
                                                    channel_btcResponseData.setSell_order_id(objects.getInt("sell_order_id"));
                                                    showToast(String.valueOf(channel_btcResponseData.getPrice()));
                                                    GlobalState.getInstance().setChannel_btcResponseData(channel_btcResponseData);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        }

                    } catch (JSONException err) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }


                }


            }

            @Override
            public void onBinaryReceived(byte[] data) {
//                showToast("binary" + data.toString());
            }

            @Override
            public void onPingReceived(byte[] data) {
//                showToast("ping" + data.toString());
            }

            @Override
            public void onPongReceived(byte[] data) {
//                showToast("ping2" + data.toString());
            }

            @Override
            public void onException(final Exception e) {
                System.out.println(e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkstaus = true;
                        dialog_GetInfo(2, e.getMessage());
                    }
                });
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void goTo2FaPasswordDialog(String accessToken) {
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
                // closeSoftKeyBoard();
                String task = String.valueOf(et_2Fa_pass.getText());
                if (task.isEmpty()) {
                    showToast("Enter 2FA Password");
                } else {
                    //Get Session

                    enter2FaPassDialog.dismiss();

                    confirmingProgressDialog.show();
                    confirmingProgressDialog.setCancelable(false);
                    confirmingProgressDialog.setCanceledOnTouchOutside(false);

                    getSessionToken(accessToken, task);
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

    private void getSessionToken(String accessToken, String twoFaCode) {
        Call<get_session_response> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).get_session("merchant", "haiww82uuw92iiwu292isk");
        call.enqueue(new Callback<get_session_response>() {
            @Override
            public void onResponse(Call<get_session_response> call, Response<get_session_response> response) {
                if (response.body() != null) {
                    get_session_response loginresponse = response.body();
                    if (Integer.parseInt(loginresponse.getSession_token()) != -1) {
                        //callRefresh(accessToken, twoFaCode, loginresponse.getSession_token());
                        new CustomSharedPreferences().setvalueofExpierTime(Integer.parseInt(loginresponse.getSession_token()), MainEntryActivityNew.this);
                        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", bContext);
                        getToken(RefToken, twoFaCode);
                    } else {
                        confirmingProgressDialog.dismiss();
                        showToast("Response empty");
                    }
                } else {
                    confirmingProgressDialog.dismiss();
                    try {
                        showToast(response.errorBody().string());
                    } catch (IOException e) {
                        showToast(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<get_session_response> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
                confirmingProgressDialog.dismiss();
                showToast(t.getMessage());
            }
        });
    }

    private void callRefresh(String accessToken, String twoFaCode, String sessionToken) {
        Call<get_session_response> call = ApiClientBoost.getRefreshRetrofit(bContext).create(ApiPaths.class).refresh(accessToken, twoFaCode, sessionToken);
        call.enqueue(new Callback<get_session_response>() {
            @Override
            public void onResponse(Call<get_session_response> call, Response<get_session_response> response) {
                if (response.body() != null) {
                    showToast("Success");
                } else {
                    try {
                        showToast(response.errorBody().string());
                    } catch (IOException e) {
                        showToast(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<get_session_response> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }
}