package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.adapter.AdapterProductUser;
import com.example.groceryapp.commands.ShopDetailsActivityClicks;
import com.example.groceryapp.databinding.ActivityShopDetailsBinding;
import com.example.groceryapp.model.ModelProduct;
import com.example.groceryapp.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopDetailsActivity extends AppCompatActivity {
    ActivityShopDetailsBinding binding;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private String myLatitude, myLongitude;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_shop_details);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_details);
        shopUid = getIntent().getStringExtra("shopUid");//get uid of the shop from intent.
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        //search
        binding.searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.setClickHandle(new ShopDetailsActivityClicks() {
            @Override
            public void backBtnClick() {
                onBackPressed();
            }

            @Override
            public void cartBtn() {

            }

            @Override
            public void callBtn() {
                dialPhone();

            }

            @Override
            public void mapBtn() {
                openMap();
            }

            @Override
            public void filterProductbtn() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category").setItems(Constants.PRODUCT_CATEGORY1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selected = Constants.PRODUCT_CATEGORY1[which];
                        binding.filteredProductsTv.setText(selected);
                        if (selected.equals("All")) {
                            loadShopProducts();
                        } else {
                            //load filtered product
                            adapterProductUser.getFilter().filter(selected);
                        }
                    }
                }).create().show();
            }
        });

    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void openMap() {//for open google map using GPS lat long
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get user dat
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String profileImage = "" + ds.child("profileImage").getValue();
                    String accountType = "" + ds.child("accountType").getValue();
                    String city = "" + ds.child("city").getValue();
                    myLatitude = "" + ds.child("latitude").getValue();
                    myLongitude = "" + ds.child("longitude").getValue();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();
                //set data
                binding.shopNameTv.setText(shopName);
                binding.emailTv.setText(shopEmail);
                binding.deliveryFeeTv.setText("deliveryFee : $" + deliveryFee);
                binding.addressTv.setText(shopAddress);
                binding.phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")) {
                    binding.openCloseTv.setText("Open");
                } else {
                    binding.openCloseTv.setText("Close");
                }
                try {
                    Picasso.get().load(profileImage).into(binding.shopIv);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {
        productsList = new ArrayList<>();//init.list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before adding to list
                productsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productsList.add(modelProduct);
                }
                //now setup adapter
                adapterProductUser = new AdapterProductUser(productsList, ShopDetailsActivity.this);
                //now set adapter to recyclerview
                binding.productsRv.setAdapter(adapterProductUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}