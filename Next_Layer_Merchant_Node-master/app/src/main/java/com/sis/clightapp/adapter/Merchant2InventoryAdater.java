package com.sis.clightapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sis.clightapp.R;
import com.sis.clightapp.model.InvenotryItem;


import java.util.ArrayList;
import java.util.List;

public class Merchant2InventoryAdater extends ArrayAdapter<InvenotryItem> {

    private Context mContext;
    private List<InvenotryItem> invenotryItemList = new ArrayList<>();


    public Merchant2InventoryAdater(@NonNull Context context, @LayoutRes ArrayList<InvenotryItem> list) {
        super(context, 0 , list);
        mContext = context;
        invenotryItemList = list;
    }

    public void updateinventory(List<InvenotryItem> invenotryItemList1)
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

        InvenotryItem currentItem = invenotryItemList.get(position);



        TextView name = (TextView) listItem.findViewById(R.id.textView_name3);
        name.setText(currentItem.getmName());

        TextView extra = (TextView) listItem.findViewById(R.id.textView_description3);
        extra.setText(currentItem.getmExtra());

        TextView price = (TextView) listItem.findViewById(R.id.price);
        price.setText(currentItem.getmPrice());

        TextView quantity=(TextView) listItem.findViewById(R.id.textViewquatity);
        quantity.setText(currentItem.getmQuantity());


        return listItem;
    }
}