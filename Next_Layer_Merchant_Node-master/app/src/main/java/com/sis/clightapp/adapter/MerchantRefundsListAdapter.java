package com.sis.clightapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.model.GsonModel.Refund;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

//TODO: Same as the AdminSendablesListAdapter
public class MerchantRefundsListAdapter extends ArrayAdapter<Refund> {

    private Context mContext;
    private List<Refund> refundsList = new ArrayList<>();

    public MerchantRefundsListAdapter(@NonNull Context context, @LayoutRes ArrayList<Refund> list) {
        super(context, 0 , list);
        mContext = context;
        refundsList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.merchant_refund_list_item_layout,parent,false);
        Refund currentRefund = refundsList.get(position);
        TextView time = (TextView) listItem.findViewById(R.id.timeval);
        time.setText( getDateFromUTCTimestamp(currentRefund.getCreated_at(), AppConstants.OUTPUT_DATE_FORMATE));
        TextView amountsat = (TextView) listItem.findViewById(R.id.amountsatval);
        amountsat.setText(excatFigure(round(mSatoshoToBtc(currentRefund.getMsatoshi()),9))+"BTC");
        ImageView bolt11InvoiceID=(ImageView)listItem.findViewById(R.id.boltval);
        if(currentRefund.getBolt11()!=null) { bolt11InvoiceID.setImageBitmap(getBitMapFromHex(currentRefund.getBolt11()));}
        ImageView paymenthash=(ImageView)listItem.findViewById(R.id.paymenthashval);
        if(currentRefund.getPayment_hash()!=null){ paymenthash.setImageBitmap(getBitMapFromHex(currentRefund.getPayment_hash()));}
        return listItem;
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

//            SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate);
//            dateFormatter.setTimeZone(TimeZone.getTimeZone("CST"));
//            date = dateFormatter.format(value);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
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
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    public double mSatoshoToBtc(double msatoshhi) {
        double msatoshiToSatoshi=msatoshhi/AppConstants.satoshiToMSathosi;
        double satoshiToBtc=msatoshiToSatoshi/AppConstants.btcToSathosi;
        return satoshiToBtc;
    }

    public String excatFigure(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));

        return  d.toPlainString();
    }
}