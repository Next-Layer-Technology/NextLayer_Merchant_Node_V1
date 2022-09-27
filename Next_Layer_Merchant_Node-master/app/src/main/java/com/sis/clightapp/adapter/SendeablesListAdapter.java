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
import com.sis.clightapp.model.Refunds;

import java.util.ArrayList;
import java.util.List;

public class SendeablesListAdapter extends ArrayAdapter<Refunds> {

    private Context mContext;
    private List<Refunds> refundsList = new ArrayList<>();

    public SendeablesListAdapter(@NonNull Context context, @LayoutRes ArrayList<Refunds> list) {
        super(context, 0 , list);
        mContext = context;
        refundsList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.refundlistviewitem,parent,false);

        Refunds currentRefund = refundsList.get(position);



        TextView refundId = (TextView) listItem.findViewById(R.id.saleid);
        refundId.setText( String.valueOf(currentRefund.getRefundId()));
        TextView saleId = (TextView) listItem.findViewById(R.id.itemid);
        saleId.setText( String.valueOf(currentRefund.getSaleId()));
        TextView itemid = (TextView) listItem.findViewById(R.id.itemname);
        itemid.setText( String.valueOf(currentRefund.getItemid()));
        TextView itemName = (TextView) listItem.findViewById(R.id.itemprice);
        itemName.setText(currentRefund.getItemName());
        TextView refundPrice = (TextView) listItem.findViewById(R.id.itempricee);
        refundPrice.setText(currentRefund.getRefundPrice());

        return listItem;
    }
}