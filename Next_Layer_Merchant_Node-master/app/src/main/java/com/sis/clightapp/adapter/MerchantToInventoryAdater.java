package com.sis.clightapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
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


import com.sis.clightapp.R;
import com.sis.clightapp.Utills.ImageBase64Encrpytion;
import com.sis.clightapp.Utills.ImageToBase16Hex;
import com.sis.clightapp.model.GsonModel.Items;

import java.util.ArrayList;
import java.util.List;

public class MerchantToInventoryAdater extends ArrayAdapter<Items> {

    private Context mContext;
    private List<Items> invenotryItemList = new ArrayList<>();


    public MerchantToInventoryAdater(@NonNull Context context, @LayoutRes ArrayList<Items> list) {
        super(context, 0 , list);
        mContext = context;
        invenotryItemList = list;
    }

    public void updateinventory(List<Items> invenotryItemList1)
    {
        this.invenotryItemList=invenotryItemList1;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.merchantinventoryitemlist,parent,false);

        Items currentItem = invenotryItemList.get(position);
        ImageView imageView=(ImageView)listItem.findViewById(R.id.imageView);
        if(currentItem.getImageInHex()!=null) {
            Bitmap bitmap=null;
            try {
                String hexTobase16str=currentItem.getImageInHex();
               // bitmap = ImageBase64Encrpytion.Base64StringToBitMap(hexTobase16str);
                bitmap= ImageToBase16Hex.base16StringToBitMap(hexTobase16str);
                //  bitmap = ImageBase64Encrpytion.Base64StringToBitMap(currentItem.getImageInHex());

            }
            catch (Error error) {
                Log.e("Decrption Error:",error.getMessage());
            }
               if(bitmap!=null){
                imageView.setImageBitmap(bitmap);
            }
        }
          TextView name = (TextView) listItem.findViewById(R.id.textView_name3);
           name.setText(currentItem.getName());

//        TextView extra = (TextView) listItem.findViewById(R.id.textView_description3);
//        extra.setText(currentItem.getAdditionalInfo());

        TextView price = (TextView) listItem.findViewById(R.id.price);
        price.setText("$"+round(Double.parseDouble(currentItem.getPrice()),2));

        TextView quantity=(TextView) listItem.findViewById(R.id.textViewquatity);
        quantity.setText(currentItem.getQuantity());


        return listItem;
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}