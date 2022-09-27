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
import com.sis.clightapp.model.Sales;

import java.util.ArrayList;
import java.util.List;

public class ReceiveablesListAdapter  extends ArrayAdapter<Sales> {

    private Context mContext;
    private List<Sales> salesList = new ArrayList<>();

    public ReceiveablesListAdapter(@NonNull Context context, @LayoutRes ArrayList<Sales> list) {
        super(context, 0 , list);
        mContext = context;
        salesList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.saleslistviewitem,parent,false);

        Sales currentSale = salesList.get(position);



        TextView salesid = (TextView) listItem.findViewById(R.id.saleid);
        salesid.setText(String.valueOf(currentSale.getSaleId()));


        TextView itemid = (TextView) listItem.findViewById(R.id.itemid);
        itemid.setText(String.valueOf(currentSale.getItemId()));

        TextView itemname = (TextView) listItem.findViewById(R.id.itemname);
        itemname.setText(currentSale.getItemName());

        TextView itemprice = (TextView) listItem.findViewById(R.id.itemprice);
        itemprice.setText(currentSale.getItemPrice());


        return listItem;
    }
}