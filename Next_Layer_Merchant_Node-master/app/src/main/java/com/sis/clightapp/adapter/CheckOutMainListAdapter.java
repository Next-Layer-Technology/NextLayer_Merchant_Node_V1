package com.sis.clightapp.adapter;

import static com.sis.clightapp.di.AppModuleKt.getMerchantContainerUrl;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sis.clightapp.R;
import com.sis.clightapp.services.SessionService;
import com.sis.clightapp.activity.CheckOutMainActivity;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.util.GlobalState;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CheckOutMainListAdapter extends ArrayAdapter<Items> {
    private final Context mContext;
    private final List<Items> inventoryItemList;
    private final SessionService sessionService;

    public CheckOutMainListAdapter(@NonNull Context context, SessionService sessionService, List<Items> list) {
        super(context, 0, list);
        mContext = context;
        inventoryItemList = list;
        this.sessionService = sessionService;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.checkoutinventoryitemlist, parent, false);
        final Items currentItem = inventoryItemList.get(position);
        CircleImageView imageView = listItem.findViewById(R.id.tv_title);

        Glide.with(mContext).load(getMerchantContainerUrl(sessionService) + currentItem.getImageUrl()).into(imageView);

        TextView name = listItem.findViewById(R.id.tv_card_numb);
        TextView price = listItem.findViewById(R.id.tv_card_expiry);
        name.setText(currentItem.getName());
        price.setText(currentItem.getPrice());
        final TextView tvQty = listItem.findViewById(R.id.countvalue);
        tvQty.setText(String.valueOf(currentItem.getSelectQuatity()));
        ImageView plus = listItem.findViewById(R.id.plus);
        ImageView minus = listItem.findViewById(R.id.minus);
        plus.setOnClickListener(view -> {
            if (currentItem.getSelectQuatity() < Integer.parseInt(currentItem.getQuantity())) {
                currentItem.setSelectQuatity(currentItem.getSelectQuatity() + 1);
                int count = 0;
                for (Items items : inventoryItemList) {
                    count = count + items.getSelectQuatity();
                }
                GlobalState.getInstance().selectedItems.add(currentItem);
                ((CheckOutMainActivity) mContext).updateCartIcon(count);
                this.notifyDataSetChanged();
            } else {
                new AlertDialog.Builder(getContext())
                        .setMessage("Total Quantity is:" + currentItem.getQuantity())
                        .setPositiveButton("Ok", null)
                        .show();
            }
        });
        minus.setOnClickListener(view -> {
            if (currentItem.getSelectQuatity() > 1) {
                currentItem.setSelectQuatity(currentItem.getSelectQuatity() - 1);
                int count = 0;
                for (Items items : inventoryItemList) {
                    count = count + items.getSelectQuatity();
                }
                ((CheckOutMainActivity) mContext).updateCartIcon(count);
                this.notifyDataSetChanged();
            }
        });
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