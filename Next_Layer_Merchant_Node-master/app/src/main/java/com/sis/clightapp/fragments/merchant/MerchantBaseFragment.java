package com.sis.clightapp.fragments.merchant;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.model.REST.get_session_response;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantBaseFragment extends Fragment  {
    /**
     * Could handle back press.
     * @return true if back press was handled
     */

    final String ISMERCHANTLOGIN="ismerchantlogin";
    final String MERCHANTID="merchantid";
    final String ISSERVERLOGIN="isserverlogin";
    final String SERVERURL="serverurl";
    final String IS_USER_LOGIN = "isuserlogin";
    final String LASTDATE="lastdate";
    final String THORSTATUS="thorstatus";
    final String LIGHTNINGSTATUS="lightningstatus";
    final String BITCOINSTATUS="bitcoinstatus";
    final String ISALLSERVERUP="isallserverup";
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

    public static final long DAY_In_MINUTES = 1000 * 60 * 1440; //5 minutes in milliseconds
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
    SharedPreferences cLightsharedPreferences;
    public static String getDateFromUTCTimestamp2(long mTimestamp, String mDateFormate) {
        String date = null;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
            cal.setTimeInMillis(mTimestamp * 1000L);
            date = DateFormat.format(mDateFormate, cal.getTimeInMillis()).toString();

            SimpleDateFormat formatter = new SimpleDateFormat(mDateFormate);
            formatter.setTimeZone(TimeZone.getTimeZone("CST"));
            Date value = formatter.parse(date);

            SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            date = dateFormatter.format(value);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
    public  long getDayDiffDates(long millis1,long millis2 ){
        // Calculate difference in milliseconds
        long diff = millis2 - millis1;

        // Calculate difference in seconds
        long diffSeconds = diff / 1000;

        // Calculate difference in minutes
        long diffMinutes = diff / (60 * 1000);

        // Calculate difference in hours
        long diffHours = diff / (60 * 60 * 1000);

        // Calculate difference in days
        long diffDays = diff / (24 * 60 * 60 * 1000);
        return  diffDays;
    }
    public  String getUnixTimeStamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        String uNixtimeStamp=tsLong.toString();
        return  uNixtimeStamp;
    }
    public long getUnixTimeStampInLong() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong;
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    public  double getUsdFromBtc(double btc) {
        double ret=0.0;
        // GlobalState.getInstance().setChannel_btcResponseData(channel_btcResponseData)
        //if(GlobalState.getInstance().getCurrentAllRate()!=null)
        if(GlobalState.getInstance().getChannel_btcResponseData()!=null)
        {
            Log.e("btcbefore",String.valueOf(btc));
            //double btcRate=GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
            double btcRate=GlobalState.getInstance().getChannel_btcResponseData().getPrice();
            double  priceInUSD=btcRate*btc;
            Log.e("btcaftertousd",String.valueOf(priceInUSD));
            ret=priceInUSD;
        }
        else
        {
            ret=0.0;
        }

        return  ret;
    }
    public  double getBtcFromUsd(double usd) {
        double ret=0.0;
        if(GlobalState.getInstance().getCurrentAllRate()!=null)
        {
            Log.e("usdbefore",String.valueOf(usd));
            double btcRatePerDollar=1/GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
            double  priceInBTC=btcRatePerDollar*usd;
            Log.e("usdaftertobtc",String.valueOf(priceInBTC));
            ret=priceInBTC;
        }
        else
        {
            ret=0.0;
        }

        return  ret;
    }
    public String getDateInCorrectFormat(int year, int monthOfYear, int dayOfMonth) {
        String date="";
        String formatedMonth="";
        String formatedDay="";
        if(monthOfYear<9)
        {
            formatedMonth="0"+(monthOfYear+1);
        }else
        {
            formatedMonth=String.valueOf(monthOfYear+1);
        }

        if(dayOfMonth<10){
            formatedDay="0"+dayOfMonth;
        }
        else {
            formatedDay=String.valueOf(dayOfMonth);
        }
        date=formatedMonth + "-" + formatedDay + "-" + year;
        return date;
    }
    public  double UsdFromBtc(double btc) {
        double ret=0.0;
        if(GlobalState.getInstance().getCurrentAllRate()!=null)
        {
            Log.e("btcbefore",String.valueOf(btc));
            double btcRate=GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
            double  priceInUSD=btcRate*btc;
            Log.e("btcaftertousd",String.valueOf(priceInUSD));
            ret=priceInUSD;
        }
        else
        {
            ret=0.0;
        }

        return  ret;
    }
    public  double BtcFromUsd(double usd) {
        double ret=0.0;
        if(GlobalState.getInstance().getCurrentAllRate()!=null)
        {
            Log.e("usdbefore",String.valueOf(usd));
            double btcRatePerDollar=1/GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
            double  priceInBTC=btcRatePerDollar*usd;
            Log.e("usdaftertobtc",String.valueOf(priceInBTC));
            ret=priceInBTC;
        }
        else
        {
            ret=0.0;
        }

        return  ret;
    }
    public  double getTaxOfBTC(double btc) {
        double taxamount=0.0;
        if(GlobalState.getInstance().getTax()!=null)
        {
            Tax t=GlobalState.getInstance().getTax();
            double taxprcntBTC=GlobalState.getInstance().getTax().getTaxpercent()/100;
            taxprcntBTC=taxprcntBTC*btc;
//            double taxprcntUSD=GlobalState.getInstance().getTax().getTaxpercent()/100;
//            taxprcntUSD=1*taxprcntUSD;
            taxamount=taxprcntBTC;
        }
        else {
            taxamount=0.0;
        }
        return  taxamount;
    }
    public  double getTaxOfUSD(double usd) {
        double taxamount=0.0;

        if(GlobalState.getInstance().getTax()!=null)
        {



            double taxprcntUSD=GlobalState.getInstance().getTax().getTaxpercent()/100;
            taxprcntUSD=usd*taxprcntUSD;
            taxamount=taxprcntUSD;
        }
        else
        {
            taxamount=0.0;
        }

        return  taxamount;
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
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return  bitmap;

    }
    public Bitmap getBitMapFromHex(String hex) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, 600, 600);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return  bitmap;

    }
    public String excatFigure(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));

        return  d.toPlainString();
    }
    public static String excatFigure2(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));

        return  d.toPlainString();
    }
    public String getDateFromUTCTimestamp(long mTimestamp, String mDateFormate) {
        String date = null;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
            cal.setTimeInMillis(mTimestamp * 1000L);
            date = DateFormat.format(mDateFormate, cal.getTimeInMillis()).toString();

            SimpleDateFormat formatter = new SimpleDateFormat(mDateFormate);
            formatter.setTimeZone(TimeZone.getTimeZone("CST"));
            Date value = formatter.parse(date);

            SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            date = dateFormatter.format(value);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
    public double mSatoshoToBtc(double msatoshhi) {
        double msatoshiToSatoshi=msatoshhi/AppConstants.satoshiToMSathosi;
        double satoshiToBtc=msatoshiToSatoshi/AppConstants.btcToSathosi;
        return satoshiToBtc;
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
                String twoFaString = String.valueOf(et_2Fa_pass.getText());
                if (twoFaString.isEmpty()) {
                    showToast("Enter 2FA Password");
                } else {
                    //Get Session

                    enter2FaPassDialog.dismiss();

                    confirmingProgressDialog.show();
                    confirmingProgressDialog.setCancelable(false);
                    confirmingProgressDialog.setCanceledOnTouchOutside(false);

                    getSessionToken(twoFaString);
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

    private void getSessionToken(String twoFaCode) {
        Call<get_session_response> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).get_session("merchant", "haiww82uuw92iiwu292isk");
        call.enqueue(new Callback<get_session_response>() {
            @Override
            public void onResponse(Call<get_session_response> call, Response<get_session_response> response) {
                if (response.body() != null) {
                    get_session_response loginresponse = response.body();
                    if (Integer.parseInt(loginresponse.getSession_token()) != -1) {
                        //callRefresh(accessToken, twoFaCode, loginresponse.getSession_token());
                        new CustomSharedPreferences().setvalueofExpierTime(Integer.parseInt(loginresponse.getSession_token()), fContext);
                        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", fContext);
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

    private void getToken(String refresh, String twofactor_key) {
        int time = new CustomSharedPreferences().getvalueofExpierTime(fContext);
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
            public void onFailure(Call<WebSocketOTPresponse> call, Throwable t) {
                confirmingProgressDialog.dismiss();
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });
    }
}