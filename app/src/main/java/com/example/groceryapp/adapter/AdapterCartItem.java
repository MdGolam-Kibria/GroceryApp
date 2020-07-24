package com.example.groceryapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.model.ModelCartItem;
import com.example.groceryapp.view.ShopDetailsActivity;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

@AllArgsConstructor
public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {
    Context context;
    ArrayList<ModelCartItem> cartItems;

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cart_item, null);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, final int position) {
        final ModelCartItem cartItem = cartItems.get(position);
        final String id = cartItem.getId();
        String getPid = cartItem.getPId();
        String title = cartItem.getName();
        final String cost = cartItem.getCost();
        String price = cartItem.getPrice();
        String quantity = cartItem.getQuantity();
        //now set data to my view
        holder.itemTitleTv.setText("" + title);
        holder.itemPriceTv.setText("" + cost);
        holder.itemQuantityTv.setText("[" + quantity + "]");
        holder.itemPriceEachTv.setText("" + price);
        //handle remove click listenor for delete a item from cart
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will create table if not exists,but in that case will must exists
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")//here "ITEMS_DB" is database name
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Removed From Cart", Toast.LENGTH_LONG).show();
                //now refresh the list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();
                double tx = Double.parseDouble((((ShopDetailsActivity) context).allTotalPriceTv.getText().toString().trim().replace("$", "")));
                double totalPrice = tx - Double.parseDouble(cost.replace("$", ""));
                double deliveryFee = Double.parseDouble((((ShopDetailsActivity) context).deliveryFee.replace("$", "")));
                double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice)) - Double.parseDouble(String.format("%.2f", deliveryFee));
                ((ShopDetailsActivity) context).allTotalPrice = 0.00;
                ((ShopDetailsActivity) context).sTotalTv.setText("$" + String.format("%.2f", sTotalPrice));
                ((ShopDetailsActivity) context).allTotalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", totalPrice))));
                //after removing cart item update the cart item count
                ((ShopDetailsActivity) context).cartCount();
            }
        });


    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder {
        //ui View from row_cart_item.xml
        TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);

        }
    }
}

