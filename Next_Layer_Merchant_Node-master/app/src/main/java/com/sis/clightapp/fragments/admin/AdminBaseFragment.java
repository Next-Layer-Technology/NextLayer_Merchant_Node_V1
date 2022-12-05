package com.sis.clightapp.fragments.admin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sis.clightapp.Interface.ApiClient2;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Interface.ApiPaths2;
import com.sis.clightapp.R;
import com.sis.clightapp.util.AppConstants;
import com.sis.clightapp.util.CustomSharedPreferences;
import com.sis.clightapp.util.GlobalState;
import com.sis.clightapp.model.REST.get_session_response;
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBaseFragment extends Fragment {
    public String TAG = "CLighting App";
    ProgressDialog confirmingProgressDialog;
    Context fContext;
    CustomSharedPreferences sharedPreferences;

    @Override
    public void onResume() {
        super.onResume();
        fContext = getContext();
        sharedPreferences = new CustomSharedPreferences();
        confirmingProgressDialog = new ProgressDialog(fContext);
        confirmingProgressDialog.setMessage("Confirming...");
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setCanceledOnTouchOutside(false);
    }


    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    void setTextWithSpan(TextView textView, String text, String spanText, StyleSpan style) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        int start = text.indexOf(spanText);
        int end = start + spanText.length();
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(sb);
    }


    public double getUsdFromBtc(double btc) {
        double ret;
        if (GlobalState.getInstance().getChannel_btcResponseData() != null) {
            Log.e("btc before", String.valueOf(btc));
            double btcRate = GlobalState.getInstance().getChannel_btcResponseData().getPrice();
            double priceInUSD = btcRate * btc;
            Log.e("btc after to usd", String.valueOf(priceInUSD));
            ret = priceInUSD;
        } else {
            ret = 0.0;
        }

        return ret;
    }


    public Bitmap getBitMapImg(String hex, int widht, int height) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, widht, height);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        return barcodeEncoder.createBitmap(bitMatrix);

    }


    public double mSatoshoToBtc(double msatoshhi) {
        double msatoshiToSatoshi = msatoshhi / AppConstants.satoshiToMSathosi;
        return msatoshiToSatoshi / AppConstants.btcToSathosi;
    }

    public String excatFigure(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));
        return d.toPlainString();
    }


    public String getDateFromUTCTimestamp(long mTimestamp, String mDateFormate) {
        String date = null;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
            cal.setTimeInMillis(mTimestamp * 1000L);
            date = DateFormat.format(mDateFormate, cal.getTimeInMillis()).toString();

            SimpleDateFormat formatter = new SimpleDateFormat(mDateFormate, Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("CST"));
            Date value = formatter.parse(date);

            SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate, Locale.US);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            date = dateFormatter.format(Objects.requireNonNull(value));
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public String getUnixTimeStamp() {
        long tsLong = System.currentTimeMillis() / 1000;
        return Long.toString(tsLong);
    }

    public void goTo2FaPasswordDialog() {
        final Dialog enter2FaPassDialog;
        enter2FaPassDialog = new Dialog(fContext);
        enter2FaPassDialog.setContentView(R.layout.dialog_authenticate_session);
        Objects.requireNonNull(enter2FaPassDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        enter2FaPassDialog.setCancelable(false);
        final EditText et_2Fa_pass = enter2FaPassDialog.findViewById(R.id.taskEditText);
        final Button btn_confirm = enter2FaPassDialog.findViewById(R.id.btn_confirm);
        final Button btn_cancel = enter2FaPassDialog.findViewById(R.id.btn_cancel);
        final ImageView iv_back = enter2FaPassDialog.findViewById(R.id.iv_back_invoice);
        iv_back.setOnClickListener(v -> enter2FaPassDialog.dismiss());
        btn_confirm.setOnClickListener(v -> {
            String twoFaString = String.valueOf(et_2Fa_pass.getText());
            if (twoFaString.isEmpty()) {
                showToast("Enter 2FA Password");
            } else {
                enter2FaPassDialog.dismiss();
                confirmingProgressDialog.show();
                confirmingProgressDialog.setCancelable(false);
                confirmingProgressDialog.setCanceledOnTouchOutside(false);
                getSessionToken(twoFaString);
            }

        });
        btn_cancel.setOnClickListener(v -> enter2FaPassDialog.dismiss());
        enter2FaPassDialog.show();
    }

    private void getSessionToken(String twoFaCode) {
        Call<get_session_response> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).get_session("merchant", "haiww82uuw92iiwu292isk");
        call.enqueue(new Callback<get_session_response>() {
            @Override
            public void onResponse(@NonNull Call<get_session_response> call, @NonNull Response<get_session_response> response) {
                if (response.body() != null) {
                    get_session_response loginresponse = response.body();
                    if (Integer.parseInt(loginresponse.getSession_token()) != -1) {
                        new CustomSharedPreferences().setvalueofExpierTime(Integer.parseInt(loginresponse.getSession_token()), fContext);
                        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", fContext);
                        getToken(RefToken, twoFaCode);
                    } else {
                        confirmingProgressDialog.dismiss();
                        showToast("Response empty");
                    }
                } else {
                    confirmingProgressDialog.dismiss();
                    if (response.errorBody() != null) {
                        showToast(response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<get_session_response> call, @NonNull Throwable t) {
                Log.e("get-funding-nodes:", Objects.requireNonNull(t.getMessage()));
                confirmingProgressDialog.dismiss();
                showToast(t.getMessage());
            }
        });
    }

    private void getToken(String refresh, String twofactor_key) {
        int time = new CustomSharedPreferences().getvalueofExpierTime(fContext);
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
                        sharedPreferences.setislogin(true, "registered", fContext);
                        if (!webSocketOTPresponse.getToken().equals("")) {
                            sharedPreferences.setvalueofaccestoken(webSocketOTPresponse.getToken(), "accessToken", fContext);
                        }
                        showToast("Access token successfully registered");
                    } else if (webSocketOTPresponse.getCode() == 701) {
                        showToast("Missing 2FA code when requesting an access token");
                    } else if (webSocketOTPresponse.getCode() == 702) {
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
                        showToast(webSocketOTPresponse.getCode() + ": " + webSocketOTPresponse.getMessage());
                    } else if (webSocketOTPresponse.getCode() == 725) {
                        showToast("Misc websocket error, \"message\" field will include more data");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WebSocketOTPresponse> call, @NonNull Throwable t) {
                confirmingProgressDialog.dismiss();
                Log.e("get-funding-nodes:", Objects.requireNonNull(t.getMessage()));
            }
        });
    }
}