package com.sis.clightapp.fragments.admin;

import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.model.Tax;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AdminBaseFragment extends Fragment {
    public String TAG="CLighting App";
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
        else
        {
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
            Log.e("errorhe","getBitMapFromHex");
        }
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return  bitmap;

    }
    public double mSatoshoToBtc(double msatoshhi) {
        double msatoshiToSatoshi=msatoshhi/ AppConstants.satoshiToMSathosi;
        double satoshiToBtc=msatoshiToSatoshi/AppConstants.btcToSathosi;
        return satoshiToBtc;
    }
    public String excatFigure(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));
        return  d.toPlainString();
    }
    private String getCentralStandardTime(long paid_at) {
        return String.valueOf(paid_at);
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
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    public  String getUnixTimeStamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        String uNixtimeStamp=tsLong.toString();
        return  uNixtimeStamp;
    }
}