package com.sis.clightapp.adapter;

import android.app.AlertDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.bumptech.glide.Glide;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.activity.CheckOutMain11;
import com.sis.clightapp.fragments.checkout.CheckOutsFragment3;
import com.sis.clightapp.model.GsonModel.Items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class CheckOutPayItemAdapter extends ArrayAdapter<Items> {

    private Context mContext;
    private ArrayList<Items> invenotryItemList = new ArrayList<>();
    Fragment myCheckOutFragment3;


    public CheckOutPayItemAdapter(@NonNull Context context, @LayoutRes ArrayList<Items> list, CheckOutsFragment3 checkOutsFragment3) {
        super(context, 0 , list);
        mContext = context;
        invenotryItemList = list;
        myCheckOutFragment3=checkOutsFragment3;
    }

    public void updateinventory(ArrayList<Items> invenotryItemList1)
    {
        this.invenotryItemList=invenotryItemList1;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.checkout3itemlistitemlayout,parent,false);
        final Items currentItem = invenotryItemList.get(position);
        CircleImageView imageView=listItem.findViewById(R.id.tv_title);
        TextView name = (TextView) listItem.findViewById(R.id.tv_card_numb);
        name.setText(currentItem.getName());
        TextView price = (TextView) listItem.findViewById(R.id.tv_card_expiry);
        if (currentItem.getPrice()!=null){
            price.setText("$"+String.format("%.2f",round(Double.parseDouble(currentItem.getPrice()),2)));

        }else {
            price.setText("$"+String.format("%.2f",round(Double.parseDouble("100"),2)));

        }
//        TextView quantity=(TextView) listItem.findViewById(R.id.tv_cvv);
//        quantity.setText(currentItem.getQuantity());
        Glide.with(mContext).load(AppConstants.MERCHANT_ITEM_IMAGE + currentItem.getImageUrl()).into(imageView);

        final TextView count=listItem.findViewById(R.id.countvalue);
        count.setText(String.valueOf(currentItem.getSelectQuatity()));
        ImageView plus=listItem.findViewById(R.id.plus);
        ImageView minus=listItem.findViewById(R.id.minus);

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int countvl=Integer.parseInt(count.getText().toString());


                if(countvl<Integer.parseInt(currentItem.getQuantity()))
                {
                    ArrayList<Items> itemsArrayList= GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
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

                                Set<Items> s2= new HashSet<Items>();
                                s2.addAll(itemsArrayList);
                                itemsArrayList = new ArrayList<Items>();
                                itemsArrayList.addAll(s2);
                                ArrayList<Items> c=itemsArrayList;


                                GlobalState.getInstance().addAllmSeletedForPayDataSourceCheckOutInventory(list);
                                // GlobalState.getInstance().setmSeletedForPayDataSourceCheckOutInventory(before);
                                ArrayList<Items> after=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
                                /*Here*/
                                /*for purpose of page 1 dataSource*/
                             //   GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(itemsArrayList);
                                int countitem=0;
                                for(Items items:after)
                                {
                                    countitem=countitem+items.getSelectQuatity();
                                }
                                ((CheckOutMain11)mContext).updateCartIcon(countitem);
                                ((CheckOutsFragment3)myCheckOutFragment3).setAdapter();
                                refresh(after);
                              //  Toast.makeText(getContext(),"Add",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                else
                {
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

                if(countvl<2) {//do nothing when slsected quaity is 1
                }
                else
                {
                     ArrayList<Items> itemsArrayList= GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
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

                                Set<Items> s2= new HashSet<Items>();
                                s2.addAll(itemsArrayList);
                                itemsArrayList = new ArrayList<Items>();
                                itemsArrayList.addAll(s2);
                                ArrayList<Items> c=itemsArrayList;


                                GlobalState.getInstance().addAllmSeletedForPayDataSourceCheckOutInventory(list);
                                // GlobalState.getInstance().setmSeletedForPayDataSourceCheckOutInventory(before);
                                ArrayList<Items> after=GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
                                /*Here*/
                                /*for purpose of page 1 dataSource*/
                                //   GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(itemsArrayList);
                                int countitem=0;
                                for(Items items:after)
                                {
                                    countitem=countitem+items.getSelectQuatity();
                                }
                                ((CheckOutMain11)mContext).updateCartIcon(countitem);
                                ((CheckOutsFragment3)myCheckOutFragment3).setAdapter();
                                refresh(after);




                            }
                        }
                    }

                   // refresh(GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory());
                    //  Toast.makeText(getContext(),"Minus",Toast.LENGTH_SHORT).show();
                }

            }
        });


        return listItem;
    }
    public void refresh(ArrayList<Items>list)
    {
//        this.invenotryItemList=list;
//        ArrayList<Items> t=this.invenotryItemList;
//        ArrayList<Items> itemsArrayList= GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
//        this.notifyDataSetChanged();



        invenotryItemList.clear();
        invenotryItemList.addAll(list);
        ArrayList<Items> t=this.invenotryItemList;
        ArrayList<Items> itemsArrayList= GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
        this.notifyDataSetChanged();



    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}