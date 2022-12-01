package com.sis.clightapp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.model.REST.Loginresponse;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    String role = "";
    Button loginbtn;
    EditText etEmail, etPassword;
    TextView setTextWithSpan;
    String merchant_id = "";

    @Override
    public void onBackPressed() {
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
        yesbtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("isFromLogin", true);
            startActivity(intent);
            finish();
        });
        nobtn.setOnClickListener(v -> goAlertDialogwithOneBTnDialog.dismiss());
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
        etEmail.setText("checkout");
        etPassword.setText("abc123");
        loginbtn = findViewById(R.id.btn_login);
        merchant_id = GlobalState.getInstance().getMerchantData().getMerchant_data_id();
        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            role = (String) b.get("role");
        }
        loginbtn.setOnClickListener(view -> {
            clodeSoftKeyBoard();
            String strEmail = etEmail.getText().toString();
            String strPassword = etPassword.getText().toString();
            if (strEmail.isEmpty()) {
                showToast(getString(R.string.empty));
                return;
            }
            if (strPassword.isEmpty()) {
                return;
            }
            setlogin(strEmail, strPassword);
        });
    }

    private void setlogin(String strEmail, String strPassword) {
        switch (role) {
            case "admin":
                onLoginClicked(merchant_id, strEmail, strPassword, "Admin");
                break;
            case "merchant":
                onLoginClicked(merchant_id, strEmail, strPassword, "Merchant");
                break;
            case "checkout":
                onLoginClicked(merchant_id, strEmail, strPassword, "Checkout");
                break;
        }
    }

    private void onLoginClicked(String merchant_id, String name, String password, String type) {
        Call<Loginresponse> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).merchantsuser_login(merchant_id, name, password, type);
        call.enqueue(new Callback<Loginresponse>() {
            @Override
            public void onResponse(@NonNull Call<Loginresponse> call, @NonNull Response<Loginresponse> response) {
                if (response.body() != null) {
                    Loginresponse loginresponse = response.body();

                    if (loginresponse.getMessage().equals("successfully done")) {
                        if (loginresponse.getLoginData() != null) {
                            sharedPreferences.setBoolean(true, IS_USER_LOGIN, LoginActivity.this);
                            switch (loginresponse.getLoginData().getUser_type()) {
                                case "Checkout": {
                                    Intent i = new Intent(getApplicationContext(), CheckOutMainActivity.class);
                                    startActivity(i);
                                    break;
                                }
                                case "Admin": {
                                    sharedPreferences.setBoolean(true, IS_USER_LOGIN, LoginActivity.this);
                                    Intent i = new Intent(getApplicationContext(), AdminMainActivity.class);
                                    startActivity(i);
                                    break;
                                }
                                case "Merchant": {
                                    sharedPreferences.setBoolean(true, IS_USER_LOGIN, LoginActivity.this);
                                    Intent i = new Intent(getApplicationContext(), MerchantMainActivity.class);
                                    startActivity(i);
                                    break;
                                }
                                default:
                                    showToast("text mismatch");
                                    break;
                            }

                        } else {
                            showToast("Response empty");
                        }
                    } else {
                        showToast("Invalid User Name Or Password");

                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Loginresponse> call, @NonNull Throwable t) {
                Log.e("get-funding-nodes:", Objects.requireNonNull(t.getMessage()));
                showToast(t.getMessage());
            }
        });
    }
}