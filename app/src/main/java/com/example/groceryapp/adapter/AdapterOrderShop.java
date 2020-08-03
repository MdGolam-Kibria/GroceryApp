package com.example.groceryapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.filter.FilterOrderShop;
import com.example.groceryapp.model.ModelOrderShop;
import com.example.groceryapp.view.OrderDetailsSellerActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;


public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop>implements Filterable {
    private Context context;
    public ArrayList<ModelOrderShop> orderShopArrayList,filterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        this.filterList =orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent, false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        ModelOrderShop modelOrderShop = orderShopArrayList.get(position);
        final String orderId = modelOrderShop.getOrderId();
        final String orderBy = modelOrderShop.getOrderBy();
        String orderCost = modelOrderShop.getOrderCost();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderTime = modelOrderShop.getOrderTime();
        String orderTo = modelOrderShop.getOrderTo();

        //load user/buyer info
        loadUserInfo(modelOrderShop,holder);


        holder.amountTv.setText("Amount: $" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Order Id: " + orderId);
        //change order status text color
        if (orderStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.toolbarColor));
        }
        if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        if (orderStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.red));
        }
        //now convert timestamp to proper time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyy",calendar).toString();
        holder.orderDateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Order details
                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId",orderId);//to load order info
                intent.putExtra("orderBy",orderBy);//to load info of the user who placed order
                context.startActivity(intent);
            }
        });
    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, final HolderOrderShop holder) {
        //to load of email of the user/buyer : modelOrderShop.getOrderBy() contains uid of the user/buyer
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String email= ""+snapshot.child("email").getValue();
                            holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            //init..filter
            filter = new FilterOrderShop(this,filterList);

        }
        return filter;
    }

    class HolderOrderShop extends RecyclerView.ViewHolder {
        private TextView orderIdTv, orderDateTv, emailTv, amountTv, statusTv;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);
            orderIdTv = itemView.findViewById(R.id.orderDateTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
}
