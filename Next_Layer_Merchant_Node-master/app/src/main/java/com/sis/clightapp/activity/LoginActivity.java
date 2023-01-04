package com.sis.clightapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.Webservice;
import com.sis.clightapp.R;
import com.sis.clightapp.fragments.printing.PrintDialogFragment;
import com.sis.clightapp.fragments.shared.Auth2FaFragment;
import com.sis.clightapp.util.GlobalState;
import com.sis.clightapp.fragments.shared.ExitDialogFragment;
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
        new ExitDialogFragment(() -> {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("isFromLogin", true);
            startActivity(intent);
            finish();
            return null;
        }).show(getSupportFragmentManager(), null);
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
        etEmail.setText("admin");
        etPassword.setText("abc123");
        loginbtn = findViewById(R.id.btn_login);
        if (GlobalState.getInstance().getMerchantData() != null)
            merchant_id = GlobalState.getInstance().getMerchantData().getMerchant_data_id();
        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            role = (String) b.get("role");
        }
        loginbtn.setOnClickListener(view -> {
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
        Call<Loginresponse> call = ApiClientBoost.getRetrofit().create(Webservice.class).merchantsuser_login(merchant_id, name, password, type);
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