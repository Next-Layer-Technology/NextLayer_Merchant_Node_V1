package com.sis.clightapp.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.activity.CheckOutMain11;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.REST.StoreClients;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectClientList  extends ArrayAdapter<StoreClients> {
        private Context mContext;
        private List<StoreClients> invenotryItemList = new ArrayList<StoreClients>();
        public SelectClientList(@NonNull Context context,  List<StoreClients> list) {
            super(context, 0 , list);
            mContext = context;
            invenotryItemList = list;
        }
        public void refresh(ArrayList<StoreClients>list) {
            this.invenotryItemList=list;
            this.notifyDataSetChanged();
        }
        @NonNull
        @Override
        public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.select_client_list_view,parent,false);
            final StoreClients currentItem = invenotryItemList.get(position);
             CircleImageView imageView=listItem.findViewById(R.id.img_client_pic);
             String url= "https://boostterminal.nextlayer.live/black/img/clients/"+currentItem.getClient_image_id();
            Glide.with(mContext).load(url).into(imageView);
            TextView name = (TextView) listItem.findViewById(R.id.tv_client_name);
            name.setText(currentItem.getClient_name());

            return listItem;
        }
    }

