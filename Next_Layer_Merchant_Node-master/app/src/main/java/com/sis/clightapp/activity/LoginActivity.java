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
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sis.clightapp.Interface.ApiClient;
import com.sis.clightapp.Interface.ApiClient2;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Interface.ApiPaths2;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.NetworkManager;
import com.sis.clightapp.fragments.checkout.CheckOutFragment1;
import com.sis.clightapp.model.Data;
import com.sis.clightapp.model.REST.Loginresponse;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.UserInfo;
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse;
import com.sis.clightapp.session.MyLogOutService;

import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    String role = "";
    Button loginbtn;
    //    RelativeLayout btnForgetPassword;
    EditText etEmail, etPassword;
    int ADMINROLE = 0;
    int MERCHANTROLE = 1000;
    final int CHECKOUT = 2000;
    TextView setTextWithSpan;
    String merchant_id = "";

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        ask_exit();
    }

    private void ask_exit() {
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog = new Dialog(LoginActivity.this);
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
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("isFromLogin", true);
                startActivity(intent);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);


        setTextWithSpan = findViewById(R.id.imageView3);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);

        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Connecting...");
        loginDialog = new ProgressDialog(LoginActivity.this);
        loginDialog.setMessage("Logging In");
        loginLodingProgressDialog = new ProgressDialog(LoginActivity.this);
        loginLodingProgressDialog.setMessage("Logging In");
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        //btnForgetPassword = findViewById(R.id.btn_forget_password);
        loginbtn = findViewById(R.id.btn_login);
        //  checkLocationPermisiion();
//        getLatestLocation();
        merchant_id = GlobalState.getInstance().getMerchantData().getMerchant_data_id();
        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            role = (String) b.get("role");
        }
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clodeSoftKeyBoard();
                String strEmail = etEmail.getText().toString();
                String strPassword = etPassword.getText().toString();
                if (strEmail.isEmpty()) {
                    showToast(getString(R.string.empty));
                    return;
                }
                if (strEmail.isEmpty()) {
                    showToast(getString(R.string.empty));
                    //showToast(getString(R.string.email_format));
                    return;
                }
                if (strPassword.isEmpty()) {
                    showToast(getString(R.string.empty));
                    return;
                }
                setlogin(strEmail, strPassword);
            }

        });
//        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

    private void setlogin(String strEmail, String strPassword) {

        if (role.equals("admin")) {

            ClickLogin(merchant_id, strEmail, strPassword, "Admin");

            /* RPC call
            final int ROLE = 0;
            UserInfo userInfo = new UserInfo();
            userInfo.setUserID(strEmail);
            userInfo.setUserPass(strPassword);
            userInfo.setUserRole(ROLE);
            userInfo.setImageUrl("www.image.com/admin.jpg");
            GlobalState.getInstance().setUserInfo(userInfo);
            String ip = "";
            String portstring = "";
            if (GlobalState.getInstance().getServerData() != null) {
                ip = GlobalState.getInstance().getServerData().getIp();
                portstring = GlobalState.getInstance().getServerData().getPort();

                ConnectToServerAsAdmin connectToServer = new ConnectToServerAsAdmin(LoginActivity.this);
                connectToServer.execute(new String[]{new String(ip),
                        new String(portstring)});


            }
*/

        } else if (role.equals("merchant")) {

            ClickLogin(merchant_id, strEmail, strPassword, "Merchant");

             /* RPC call
             final int ROLE = 1000;
            UserInfo userInfo = new UserInfo();
            userInfo.setUserID(strEmail);
            userInfo.setUserPass(strPassword);
            userInfo.setUserRole(ROLE);
            userInfo.setImageUrl("www.image.com/merchant.jpg");
            GlobalState.getInstance().setUserInfo(userInfo);
            String ip = "";
            String portstring = "";
            if (GlobalState.getInstance().getServerData() != null) {
                ip = GlobalState.getInstance().getServerData().getIp();
                portstring = GlobalState.getInstance().getServerData().getPort();
                ConnectToServerAsMerchant connectToServer = new ConnectToServerAsMerchant(LoginActivity.this);
                connectToServer.execute(new String[]{new String(ip),
                        new String(portstring)});

            }

              */

        } else if (role.equals("checkout")) {

            ClickLogin(merchant_id, strEmail, strPassword, "Checkout");

             /* RPC call

            final int ROLE = 1000;
            UserInfo userInfo = new UserInfo();
            userInfo.setUserID(strEmail);
            userInfo.setUserPass(strPassword);
            userInfo.setUserRole(ROLE);
            userInfo.setImageUrl("www.image.com/checkout.jpg");
            GlobalState.getInstance().setUserInfo(userInfo);

            String ip = "";
            String portstring = "";
            if (GlobalState.getInstance().getServerData() != null) {
                ip = GlobalState.getInstance().getServerData().getIp();
                portstring = GlobalState.getInstance().getServerData().getPort();
                ConnectToServerAsCheckout connectToServer = new ConnectToServerAsCheckout(LoginActivity.this);
                connectToServer.execute(new String[]{new String(ip),
                        new String(portstring)});
            }
            */
        }
    }
    private void getTaxCheckout() {
        GlobalState.getInstance().setItemCountInCart(0);
        sharedPreferences.setBoolean(true, IS_USER_LOGIN, this);
        Intent i = new Intent(getApplicationContext(), CheckOutMain11.class);
        // i.putExtra("role","checkout");
        startActivity(i);
        finish();
    }

    private void getTaxMerchant() {
        GlobalState.getInstance().setItemCountInCart(0);
        sharedPreferences.setBoolean(true, IS_USER_LOGIN, this);
        Intent i = new Intent(getApplicationContext(), MerchnatMain11.class);
        // i.putExtra("role","merchant");
        startActivity(i);
        finish();
    }

    private void getTaxAdmin() {
        GlobalState.getInstance().setItemCountInCart(0);
        sharedPreferences.setBoolean(true, IS_USER_LOGIN, this);
        Intent i = new Intent(getApplicationContext(), AdminMain11.class);
        //i.putExtra("role","admin");
        startActivity(i);
        finish();
    }

    private void ClickLogin(String merchant_id, String name, String password, String type) {
        Call<Loginresponse> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).merchantsuser_login(merchant_id, name, password, type);
        call.enqueue(new Callback<Loginresponse>() {
            @Override
            public void onResponse(Call<Loginresponse> call, Response<Loginresponse> response) {
                if (response.body() != null) {
                    Loginresponse loginresponse = response.body();

                    if (loginresponse.getMessage().equals("successfully done")) {
                        if (loginresponse.getLoginData() != null) {
                            sharedPreferences.setBoolean(true, IS_USER_LOGIN, LoginActivity.this);
                            if (loginresponse.getLoginData().getUser_type().equals("Checkout")) {
                                Intent i = new Intent(getApplicationContext(), CheckOutMain11.class);
                                startActivity(i);
                                //finish();
                            } else if (loginresponse.getLoginData().getUser_type().equals("Admin")) {

                                sharedPreferences.setBoolean(true, IS_USER_LOGIN, LoginActivity.this);
                                Intent i = new Intent(getApplicationContext(), AdminMain11.class);
                                //i.putExtra("role","admin");
                                startActivity(i);

                            } else if (loginresponse.getLoginData().getUser_type().equals("Merchant")) {
                                sharedPreferences.setBoolean(true, IS_USER_LOGIN, LoginActivity.this);
                                Intent i = new Intent(getApplicationContext(), MerchnatMain11.class);
                                // i.putExtra("role","merchant");
                                startActivity(i);

                            }else {
                                showToast("text mismatch");
                            }

                        }else {
                            showToast("Response empty");
                        }
                    }else {
                        showToast("Invalid User Name Or Password");

                    }
                }
            }

            @Override
            public void onFailure(Call<Loginresponse> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
                showToast(t.getMessage());
            }
        });
    }
}