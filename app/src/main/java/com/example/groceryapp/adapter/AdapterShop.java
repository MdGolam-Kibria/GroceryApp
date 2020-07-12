package com.example.groceryapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.model.ModelShop;
import com.example.groceryapp.view.ShopDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {
    private Context context;
    private ArrayList<ModelShop> shopsList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        ModelShop modelShop = shopsList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phone = modelShop.getPhone();
        final String uid = modelShop.getUid();
        String timestamp = modelShop.getTimestamp();
        String shopOpen = modelShop.getShopOpen();
        String state = modelShop.getState();
        String profileImage = modelShop.getProfileImage();
        String shopName = modelShop.getShopName();
        //now set Data to UI
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        if (online.equals("true")) {
            holder.onlineIv.setVisibility(View.VISIBLE);//for showing shop owner in online
        } else {
            holder.onlineIv.setVisibility(View.GONE);//for showing shop owner is offline
        }
        //check if shop open
        if (shopOpen.equals("true")) {
            //shop open
            holder.shopClosedTv.setVisibility(View.GONE);
        } else {
            //shop closed
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }
        try {

            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.shopIv);
        }catch (Exception e){
            holder.shopIv.setImageResource(R.drawable.ic_store_gray);
        }
        //handle click listner show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    public class HolderShop extends RecyclerView.ViewHolder {
        //UI views of row_shops.xml
        private ImageView shopIv, onlineIv;
        private TextView shopClosedTv, shopNameTv, phoneTv, addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);
            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
