//package com.sis.clightapp.adapter;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.bumptech.glide.Glide;
//import com.sis.clightapp.R;
//import com.sis.clightapp.Utills.AppConstants;
//import com.sis.clightapp.activity.CheckOutMainActivity;
//import com.sis.clightapp.fragments.checkout.CheckOutsFragment3;
//import com.sis.clightapp.model.GsonModel.Items;
//
//import java.util.ArrayList;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class CheckOutPayItemAdapter extends ArrayAdapter<Items> {
//
//    private final Context mContext;
//    private final ArrayList<Items> invenotryItemList;
//    Fragment myCheckOutFragment3;
//
//
//    public CheckOutPayItemAdapter(@NonNull Context context, ArrayList<Items> list, CheckOutsFragment3 checkOutsFragment3) {
//        super(context, 0, list);
//        mContext = context;
//        invenotryItemList = list;
//        myCheckOutFragment3 = checkOutsFragment3;
//    }
//
//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        View listItem = convertView;
//        if (listItem == null)
//            listItem = LayoutInflater.from(mContext).inflate(R.layout.checkout3itemlistitemlayout, parent, false);
//        final Items currentItem = invenotryItemList.get(position);
//        CircleImageView imageView = listItem.findViewById(R.id.tv_title);
//        TextView name = listItem.findViewById(R.id.tv_card_numb);
//        name.setText(currentItem.getName());
//        TextView price = listItem.findViewById(R.id.tv_card_expiry);
//        if (currentItem.getPrice() != null) {
//            price.setText("$" + String.format("%.2f", round(Double.parseDouble(currentItem.getPrice()), 2)));
//
//        } else {
//            price.setText("$" + String.format("%.2f", round(Double.parseDouble("100"), 2)));
//
//        }
//        Glide.with(mContext).load(AppConstants.MERCHANT_ITEM_IMAGE + currentItem.getImageUrl()).into(imageView);
//
//        final TextView tvQty = listItem.findViewById(R.id.countvalue);
//        tvQty.setText(String.valueOf(currentItem.getSelectQuatity()));
//        ImageView plus = listItem.findViewById(R.id.plus);
//        ImageView minus = listItem.findViewById(R.id.minus);
//
//        plus.setOnClickListener(view -> {
//            if (currentItem.getSelectQuatity() < Integer.parseInt(currentItem.getQuantity())) {
//                currentItem.setSelectQuatity(currentItem.getSelectQuatity() + 1);
//                int count = 0;
//                for (Items items : invenotryItemList) {
//                    count = count + items.getSelectQuatity();
//                }
//                ((CheckOutMainActivity) mContext).updateCartIcon(count);
//                this.notifyDataSetChanged();
//            } else {
//                // do nothing when selected quatity = item total quantity
//                new AlertDialog.Builder(getContext())
//                        .setMessage("Total Quantity is:" + currentItem.getQuantity())
//                        .setPositiveButton("Ok", null)
//                        .show();
//            }
//        });
//        minus.setOnClickListener(view -> {
//            if (currentItem.getSelectQuatity() > 1) {
//                currentItem.setSelectQuatity(currentItem.getSelectQuatity() - 1);
//                int count = 0;
//                for (Items items : invenotryItemList) {
//                    count = count + items.getSelectQuatity();
//                }
//                ((CheckOutMainActivity) mContext).updateCartIcon(count);
//                this.notifyDataSetChanged();
//            }
//        });
//
//
//        return listItem;
//    }
//
//    public void refresh(ArrayList<Items> list) {
//        invenotryItemList.clear();
//        invenotryItemList.addAll(list);
//        this.notifyDataSetChanged();
//    }
//
//    public static double round(double value, int places) {
//        if (places < 0) throw new IllegalArgumentException();
//        long factor = (long) Math.pow(10, places);
//        value = value * factor;
//        long tmp = Math.round(value);
//        return (double) tmp / factor;
//    }
//}