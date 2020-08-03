package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.adapter.AdapterOrderedItem;
import com.example.groceryapp.commands.OrderDetailsUsersActivityClicks;
import com.example.groceryapp.databinding.ActivityOrderDetailsUsersBinding;
import com.example.groceryapp.model.ModelOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUsersActivity extends AppCompatActivity {
    ActivityOrderDetailsUsersBinding binding;
    private String orderTo, orderId;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;//custom adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(OrderDetailsUsersActivity.this, R.layout.activity_order_details_users);
        firebaseAuth = FirebaseAuth.getInstance();
        final Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");//orderTo contains uid of the shop where we placed order("shopUid")
        orderId = intent.getStringExtra("orderId");
        loadShopInfo();
        loadOrderDetails();
        loadOrderItems();
        binding.setClickHandle(new OrderDetailsUsersActivityClicks() {
            @Override
            public void backBtnClick() {
                onBackPressed();
            }

            @Override
            public void writeReviewClick() {
                //for send rate and review
                Intent intent1 = new Intent(OrderDetailsUsersActivity.this, WriteReviewActivity.class);
                intent1.putExtra("shopUid", orderTo);//ekhane jei shop e order korbe sei shop er order id dew ase "orderTo"
                startActivity(intent1);
            }
        });

    }

    private void loadOrderItems() {
        orderedItemArrayList = new ArrayList<>();///init list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).child("Items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderedItemArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                    //add to list
                    orderedItemArrayList.add(modelOrderedItem);
                }
                //all items added to list
                adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUsersActivity.this, orderedItemArrayList);
                binding.orderDetailsRV.setAdapter(adapterOrderedItem);
                //set order items count
                binding.totalItemsTv.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = "" + snapshot.child("shopName").getValue();
                binding.shopNameTv.setText(shopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadOrderDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String orderBy = "" + snapshot.child("orderBy").getValue();
                String orderCost = "" + snapshot.child("orderCost").getValue();
                String orderId = "" + snapshot.child("orderId").getValue();
                String orderStatus = "" + snapshot.child("orderStatus").getValue();
                String orderTime = "" + snapshot.child("orderTime").getValue();
                String orderTo = "" + snapshot.child("orderTo").getValue();
                String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String latitude = "" + snapshot.child("latitude").getValue();
                String longitude = "" + snapshot.child("longitude").getValue();
                //now convert timestamp to proper format
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(orderTime));
                String formatedDate = DateFormat.format("ss/MM/yyy hh:mm a", calendar).toString();
                if (orderStatus.equals("In Progress")) {
                    binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else if (orderStatus.equals("Completed")) {
                    binding.orderStatusTv.setTextColor(getResources().getColor(R.color.green));
                } else if (orderStatus.equals("Cancelled")) {
                    binding.orderStatusTv.setTextColor(getResources().getColor(R.color.red));
                }
                //now set data to view
                binding.orderedIdTv.setText(orderId);
                binding.orderStatusTv.setText(orderStatus);
                binding.amountTv.setText("$" + orderCost + "[Including Delivery Fee $]" + deliveryFee);
                binding.dateTv.setText(formatedDate);
                findAddresforLocation(latitude, longitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findAddresforLocation(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        //find address,country,state,city
        Geocoder geocoder;
        List<Address> addresses;//here Address is Geo coder class
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);//compete address
            binding.addressTv.setText(address);//set full address to view
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}