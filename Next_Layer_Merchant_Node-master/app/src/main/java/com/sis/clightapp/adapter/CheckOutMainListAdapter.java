package com.sis.clightapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.ImageToBase16Hex;
import com.sis.clightapp.activity.CheckOutMain11;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.Item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class CheckOutMainListAdapter  extends ArrayAdapter<Items> {
    private Context mContext;
    private List<Items> invenotryItemList = new ArrayList<>();
    public CheckOutMainListAdapter(@NonNull Context context, ArrayList<Items> list) {
        super(context, 0 , list);
        mContext = context;
        invenotryItemList = list;
    }
    public void refresh(ArrayList<Items>list) {
        this.invenotryItemList=list;
         this.notifyDataSetChanged();
    }
    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.checkoutinventoryitemlist,parent,false);
        final Items currentItem = invenotryItemList.get(position);
       CircleImageView imageView=listItem.findViewById(R.id.tv_title);

        Glide.with(mContext).load(AppConstants.MERCHANT_ITEM_IMAGE + currentItem.getImageUrl()).into(imageView);

        TextView name = (TextView) listItem.findViewById(R.id.tv_card_numb);
//        name.setText(currentItem.getName());
        TextView price = (TextView) listItem.findViewById(R.id.tv_card_expiry);
        name.setText(currentItem.getName());
        price.setText(currentItem.getPrice());
        //luqman
//        price.setText("$"+String.format("%.2f",round(Double.parseDouble(currentItem.getPrice()),2)));
        final TextView count=listItem.findViewById(R.id.countvalue);
        count.setText(String.valueOf(currentItem.getSelectQuatity()));
        ImageView plus=listItem.findViewById(R.id.plus);
        ImageView minus=listItem.findViewById(R.id.minus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int countvl=Integer.parseInt(count.getText().toString());
                ArrayList<Items> itemsArrayList=GlobalState.getInstance().getmDataScanedSourceCheckOutInventory();
                if(countvl<Integer.parseInt(currentItem.getQuantity())) {
                    if(itemsArrayList!=null)
                    {
                        for(int itration=0;itration<itemsArrayList.size();itration++)
                        {
                            if(itemsArrayList.get(itration).equals(currentItem))
                            {
                                countvl++;
                                itemsArrayList.get(itration).setSelectQuatity(countvl);
                                /*for update list for page 3 From*/
                                ArrayList<Items> before=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
//                                before.addAllmSeletedForPayDataSourceCheckOutInventory(itemsArrayList);
                                GlobalState.getInstance().addAllmSeletedForPayDataSourceCheckOutInventory(itemsArrayList);
                                ArrayList<Items> list=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
                                //Removing Duplicates;
                                Set<Items> s= new HashSet<Items>();
                                s.addAll(list);
                                list = new ArrayList<Items>();
                                list.addAll(s);
                                GlobalState.getInstance().addAllmSeletedForPayDataSourceCheckOutInventory(list);
                                // GlobalState.getInstance().setmSeletedForPayDataSourceCheckOutInventory(before);
                                ArrayList<Items> after=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
                                /*Here*/
                                int countitem=0;
                                for(Items items:after)
                                {
                                    countitem=countitem+items.getSelectQuatity();
                                }
                                ((CheckOutMain11)mContext).updateCartIcon(countitem);
                                /*for purpose of page 1 dataSource*/
                                GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(itemsArrayList);
                                refresh(itemsArrayList);
                               // Toast.makeText(getContext(),"Add",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                else {
                    // do nothing when selected quatity = item total quantity
                    new AlertDialog.Builder(getContext())
                            .setMessage("Total Quantity is:"+currentItem.getQuantity())
                            .setPositiveButton("Ok", null)
                            .show();
                }
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int countvl=Integer.parseInt(count.getText().toString());
                if(countvl<2) {
                    //do nothing when slsected quaity is 1
                     }
                else {
                    ArrayList<Items> itemsArrayList=GlobalState.getInstance().getmDataScanedSourceCheckOutInventory();
                    if(itemsArrayList!=null)
                    {
                        for(int itration=0;itration<itemsArrayList.size();itration++)
                        {
                            if(itemsArrayList.get(itration).equals(currentItem))
                            {
                                countvl--;
                                itemsArrayList.get(itration).setSelectQuatity(countvl);
                                /*for update list for page 3 From*/
                                ArrayList<Items> before=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
//                                before.addAllmSeletedForPayDataSourceCheckOutInventory(itemsArrayList);

                                GlobalState.getInstance().addAllmSeletedForPayDataSourceCheckOutInventory(itemsArrayList);
                                ArrayList<Items> list=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();

                                //Removing Duplicates;
                                Set<Items> s= new HashSet<Items>();
                                s.addAll(list);
                                list = new ArrayList<Items>();
                                list.addAll(s);
                                GlobalState.getInstance().addAllmSeletedForPayDataSourceCheckOutInventory(list);
                                // GlobalState.getInstance().setmSeletedForPayDataSourceCheckOutInventory(before);
                                ArrayList<Items> after=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();

                                /*Here*/
                                int countitem=0;
                                for(Items items:after)
                                {
                                    countitem=countitem+items.getSelectQuatity();
                                }
                                ((CheckOutMain11)mContext).updateCartIcon(countitem);
                                /*for purpose of page 1 dataSource*/
                                GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(itemsArrayList);


                                refresh(GlobalState.getInstance().getmDataScanedSourceCheckOutInventory());

                            }
                        }
                    }

                   refresh(GlobalState.getInstance().getmDataScanedSourceCheckOutInventory());
                  //  Toast.makeText(getContext(),"Minus",Toast.LENGTH_SHORT).show();
                }
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