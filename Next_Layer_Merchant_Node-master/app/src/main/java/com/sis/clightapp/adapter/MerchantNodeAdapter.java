package com.sis.clightapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.listener.NearbyClientClickListener;
import com.sis.clightapp.model.REST.nearby_clients.NearbyClients;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MerchantNodeAdapter extends RecyclerView.Adapter<MerchantNodeAdapter.ViewHolder> {

    private List<NearbyClients> data;
    private Context mContext;
    private NearbyClientClickListener listener;

    // RecyclerView recyclerView;
    public MerchantNodeAdapter(List<NearbyClients> data, Context context, NearbyClientClickListener listener) {
        this.data = data;
        this.mContext = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_merchant_node, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NearbyClients currentItem = data.get(position);
        Glide.with(mContext).load(currentItem.getClient_image_url())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.d("NearbyClients_ADAPTER", "onLoadFailed: " + e.getMessage());
                        holder.ivUserPicture.setVisibility(View.GONE);
                        holder.ivThumbnail.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.ivUserPicture);

        holder.ivUserPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.itemClicked(currentItem);
            }
        });

        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.itemClicked(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView ivUserPicture, ivThumbnail;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            ivUserPicture = itemView.findViewById(R.id.ivUserPicture);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}