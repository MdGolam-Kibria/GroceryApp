package com.example.groceryapp.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.filter.FilterProducts;
import com.example.groceryapp.model.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller>implements Filterable {
    Context context;
    public ArrayList<ModelProduct> productsList,filterList;
    private FilterProducts filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        ModelProduct modelProduct = productsList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timeStamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();
        //now set data to view
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountNoteTv.setText(discountNote+"%"+" OFF");
        holder.discountedPriceTv.setText("$" + discountPrice);
        holder.originalPriceTv.setText("$" + originalPrice);
        if (discountAvailable.equals("true")) {
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        } else {
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks show item details...
            }
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {//for search a product after make a filter class called filter products then implement Filterable
        if (filter==null){
            filter = new FilterProducts(this,filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder {
        private ImageView productIconIv;
        private TextView discountNoteTv, titleTv, quantityTv, discountedPriceTv, originalPriceTv;


        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }
}
