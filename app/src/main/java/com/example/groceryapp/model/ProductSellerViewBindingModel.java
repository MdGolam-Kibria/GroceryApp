package com.example.groceryapp.model;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.example.groceryapp.R;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSellerViewBindingModel {
    public String productIcon,discountNote,productTitle,productQuantity,discountPrice,originalPrice;
//    @BindingAdapter({"bind:imageUrl"})
//    public static void loadImage(ImageView imageView, String imageUrl){
//        Glide.with(imageView.getContext()).load(imageUrl).placeholder(R.drawable.ic_launcher_background).into(imageView);
//    }
//    public String getImageUrl(){
//        return productIcon;
//    }
}
