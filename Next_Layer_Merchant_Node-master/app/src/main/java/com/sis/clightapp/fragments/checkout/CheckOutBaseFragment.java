package com.sis.clightapp.fragments.checkout;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Utills.Functions;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.Tax;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CheckOutBaseFragment extends Fragment {
    /**
     * Could handle back press.
     * @return true if back press was handled
     */
    ProgressDialog addItemprogressDialog,getItemListprogressDialog,exitFromServerProgressDialog,createInvoiceProgressDialog,confirmInvoicePamentProgressDialog,connectCLiChannel,updatingInventoryProgressDialog;
    Context fContext;
    String TAG="CLighting App";

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
    public void cleanAllDataSource() {
        GlobalState.getInstance().setmSelectedDataSourceCheckOutInventory(new ArrayList<Items>());
        GlobalState.getInstance().setmSeletedForPayDataSourceCheckOutInventory(new ArrayList<Items>());
        GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(new ArrayList<Items>());
        GlobalState.getInstance().setmDataScannedForPage1(new ArrayList<Items>());
        ArrayList<Items> temp1=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
        ArrayList<Items> temp2=GlobalState.getInstance().getmDataScanedSourceCheckOutInventory();
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
    public static String removeLastChars(String str, int chars) {
        return str.substring(0, str.length() - chars);
    }
    public static String excatFigure2(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));

        return  d.toPlainString();
    }
}