package com.sis.clightapp.adapter;

import static com.sis.clightapp.util.UtilsKt.QR_CODE;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.util.Log;
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
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.util.AppConstants;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

//TODO: Same as AdminReceiveablesListAdapter
public class MerchantSalesListAdapter  extends ArrayAdapter<Invoice> {

    private Context mContext;
    private List<Invoice> salesList;

    public MerchantSalesListAdapter(@NonNull Context context, ArrayList<Invoice> list) {
        super(context, 0 , list);
        mContext = context;
        salesList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.merchant_sale_list_item_layout,parent,false);

        Invoice currentSale = salesList.get(position);


        TextView salelabel = listItem.findViewById(R.id.labelval);
        salelabel.setText(String.valueOf(currentSale.getLabel()));
        TextView amountsat = listItem.findViewById(R.id.amountsatval);
        amountsat.setText(String.valueOf(round(currentSale.getMsatoshi(),9)));
        amountsat.setText(excatFigure(mSatoshoToBtc(currentSale.getMsatoshi()))+"BTC");
        TextView paidat = listItem.findViewById(R.id.paidatval);
        paidat.setText(getDateFromUTCTimestamp(currentSale.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
        TextView description= listItem.findViewById(R.id.descriptionval);
        description.setText(currentSale.getDescription());
        ImageView paymentpreimage= listItem.findViewById(R.id.paypreimageval);
        if(currentSale.getPayment_preimage()!=null) {
            paymentpreimage.setImageBitmap(getBitMapFromHex(currentSale.getPayment_preimage()));
        }
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
            Log.d(QR_CODE,hex);
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