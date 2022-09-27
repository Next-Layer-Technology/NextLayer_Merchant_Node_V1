package com.sis.clightapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.ImageToBase16Hex;
import com.sis.clightapp.fragments.merchant.MerchantFragment2;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemLIstModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MerchantItemAdapter extends RecyclerView.Adapter<MerchantItemAdapter.ViewHolder> {

    private List<ItemLIstModel> itemsArrayList;
    private  Context mContext;
    private MerchantFragment2 merchantFragment2;
    
    // RecyclerView recyclerView;
    public MerchantItemAdapter(List<ItemLIstModel> listdata, Context context, MerchantFragment2 merchantFragment2) {
        this.itemsArrayList = listdata;
        this.mContext=context;
        this.merchantFragment2=merchantFragment2;
    }
    public void updateList(List<ItemLIstModel> itemList){
        itemsArrayList.clear();
        this.itemsArrayList.addAll(itemList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.merchant_inventroy_item_list2, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ItemLIstModel currentItem = itemsArrayList.get(position);
        holder.setIsRecyclable(false);

        Glide.with(mContext).load(AppConstants.MERCHANT_ITEM_IMAGE + currentItem.getImage_path()).into(holder.imageView);
        holder.name.setText(currentItem.getName());
        holder.price.setText("$"+excatFigure(round(Double.parseDouble(currentItem.getUnit_price()),2)));
        holder.quantity.setText(currentItem.getQuantity_left());
        holder.customRowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView image = new ImageView(mContext);
                Bitmap bitmap=getBitMapFromHex(currentItem.getUpc_code());
                if(bitmap!=null){
                    image.setImageBitmap(bitmap);
                }else {
                    image.setImageResource(R.drawable.ic_launcher2);
                }
                androidx.appcompat.app.AlertDialog.Builder builder =
                        new androidx.appcompat.app.AlertDialog.Builder(mContext).setCancelable(false).
                                setMessage("Item UPC:"+currentItem.getUpc_code()).
                                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                     }
                                }).
                                setView(image);
                builder.create().show();

//
//                new AlertDialog.Builder(mContext)
//                        .setMessage("UPC:"+currentItem.getUPC())
//                        .setPositiveButton("Ok", null)
//                        .show();
               // Toast.makeText(view.getContext(),currentItem.getName(), Toast.LENGTH_LONG).show();


            }
        });

        holder.customRowLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //merchantFragment2.dialogBoxForUpdateItemMerchant2(currentItem.getName());
                merchantFragment2.dialogBoxForUpdateDelItem(currentItem);
                return true;
            }
        });
    }
    @Override
    public int getItemCount() {
        return itemsArrayList.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public RelativeLayout customRowLayout;
        public CircleImageView imageView;
        public TextView name;
        public TextView price;
        public TextView quantity;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            customRowLayout=(RelativeLayout)itemView.findViewById(R.id.linearLayout2);
            imageView=(CircleImageView) itemView.findViewById(R.id.imageView);
            name = (TextView) itemView.findViewById(R.id.textView_name3);
            price = (TextView) itemView.findViewById(R.id.price);
            quantity=(TextView) itemView.findViewById(R.id.textViewquatity);
        }
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
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
}