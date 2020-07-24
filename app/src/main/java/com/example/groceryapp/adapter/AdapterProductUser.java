package com.example.groceryapp.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.filter.FilterProductsUser;
import com.example.groceryapp.model.ModelProduct;
import com.example.groceryapp.view.ShopDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {
    private FilterProductsUser filter;
    public ArrayList<ModelProduct> productsList, filterList;
    private Context context;

    public AdapterProductUser(ArrayList<ModelProduct> productsList, Context context) {
        this.productsList = productsList;
        this.context = context;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        final ModelProduct modelProduct = productsList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();
        //now set data to my view
        holder.titleTv.setText(productTitle);
        holder.discountedNoteTv.setText(discountNote + "% OFF");
        holder.descriptionTv.setText(productDescription);
        holder.originalPriceTv.setText("$" + originalPrice);
        holder.discountedPriceTv.setText("$" + discountPrice);

        if (discountAvailable.equals("true")) {
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        } else {
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product to cart
                showQuantityDialog(modelProduct);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details
            }
        });

    }

    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;

    private void showQuantityDialog(ModelProduct modelProduct) {//for add to cart
        //now inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);//inflate my custom layout
        //now initi...layout views
        ImageView productIv = view.findViewById(R.id.productIv);
        final TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        final TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        final TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        final TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);
        //get data from Model
        final String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String pQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();
        final String price;
        if (modelProduct.getDiscountAvailable().equals("true")) {
            price = modelProduct.getDiscountPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike though on original price
        } else {
            //product don't have any discount...
            discountedNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }
        cost = Double.parseDouble(price.replaceAll("$", ""));
        finalCost = Double.parseDouble(price.replaceAll("$", ""));
        quantity = 1;
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);//set my custom layout to AlertDialog
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(productIv);
        } catch (Exception e) {
            productIv.setImageResource(R.drawable.ic_cart_gray);
        }
        //now set data to views
        titleTv.setText("" + title);
        pQuantityTv.setText("" + pQuantity);
        descriptionTv.setText("" + description);
        discountedNoteTv.setText("" + discountNote+"%"+" OFF");
        quantityTv.setText("" + quantity);
        originalPriceTv.setText("$" + modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("$" + modelProduct.getDiscountPrice());
        finalPriceTv.setText("$" + finalCost);
        final AlertDialog dialog = builder.create();
        dialog.show();
        incrementBtn.setOnClickListener(new View.OnClickListener() {//increase quantity of the product
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity++;
                finalPriceTv.setText("$" + finalCost);//set total final cost of those product
                quantityTv.setText("" + quantity);

            }
        });
        decrementBtn.setOnClickListener(new View.OnClickListener() {//decrement from total cost of those product
            @Override
            public void onClick(View v) {
                if (quantity > 1) {//at least have one product in cart
                    finalCost = finalCost-cost;
                    quantity--;
                    finalPriceTv.setText("$" + finalCost);//set total final cost of those product
                    quantityTv.setText("" + quantity);
                }
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPriceTv.getText().toString().trim().replace("$", "");
                String quantity = quantityTv.getText().toString().trim();
                //now add to cart
                addToCart(productId, title, priceEach, totalPrice, quantity);//add to database(SQL Lite)/////////////////////////////////
                dialog.dismiss();//here dialog off

            }
        });
    }

    private int itemId = 1;//This is item id for store in database

    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {//add carted item to SQL Lite database
        itemId++;//prottekbar item add hobe  1 kore barbe itemId er value...
        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")//here "ITEMS_DB" is database name
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        Boolean b = easyDB.addData("item_Id",itemId)
                .addData("item_PID",productId)
                .addData("item_Name",title)
                .addData("item_Price_Each",priceEach)
                .addData("item_Price",price)
                .addData("item_Quantity",quantity)
                .doneDataAdding();
         Toast.makeText(context, "Added to cart...", Toast.LENGTH_SHORT).show();
    //update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProductsUser(this, filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {
        private ImageView productIconIv;
        private TextView discountedNoteTv, titleTv, descriptionTv, addToCart, discountedPriceTv, originalPriceTv;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            addToCart = itemView.findViewById(R.id.addToCart);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);


        }
    }
}
