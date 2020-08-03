package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.adapter.AdapterOrderShop;
import com.example.groceryapp.adapter.AdapterProductSeller;
import com.example.groceryapp.commands.MainSellerActivityClicks;
import com.example.groceryapp.databinding.ActivityMainSellerBinding;
import com.example.groceryapp.model.ModelOrderShop;
import com.example.groceryapp.model.ModelProduct;
import com.example.groceryapp.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {
    ActivityMainSellerBinding binding;
    FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productList;
    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterProductSeller adapterProductSeller;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_seller);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_seller);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        showProductsUI();
        loadAllOrders();
        //search
        binding.searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.setClickhandle(new MainSellerActivityClicks() {
            @Override
            public void logoutSeller() {
                //make offline
                //signOut
                //go to login activity
                makeMeOffline();
            }

            @Override
            public void editSellerProfile() {
                //for edit seller profile...now open edit activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }

            @Override
            public void addProductBtn() {
                //for add product...open add product activity.
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }

            @Override
            public void tabProductsClicks() {
                //load products
                showProductsUI();
            }

            @Override
            public void tabOrdersClicks() {
                //load orders
                showOrdersUI();
            }

            @Override
            public void filterProductBtnClick() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category").setItems(Constants.PRODUCT_CATEGORY1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selected = Constants.PRODUCT_CATEGORY1[which];
                        binding.filteredProductsTv.setText(selected);
                        if (selected.equals("All")) {
                            loadAllProducts();
                        } else {
                            //load filtered product
                            loadFilteredProducts(selected);
                        }
                    }
                }).create().show();

            }

            @Override
            public void filterOrderBtnclick() {
                final String options[] = {"All", "In Progress", "Complete", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders :")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //all
                                    binding.filteredOrdersTv.setText("Showing All");
                                    adapterOrderShop.getFilter().filter("");//show all orders
                                } else {
                                    String optionClicked = options[which];
                                    binding.filteredOrdersTv.setText("Showing " + optionClicked + " Orders");
                                    adapterOrderShop.getFilter().filter(optionClicked);

                                }
                            }
                        });
                builder.create().show();
            }

            @Override
            public void reviewsBtnClick() {
                //open same reviews activity as uses user review acivity
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", "" + firebaseAuth.getUid());
                startActivity(intent);
            }
        });

    }

    private void loadAllOrders() {
        orderShopArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderShopArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            orderShopArrayList.add(modelOrderShop);
                        }
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        binding.ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(final String selected) {
        productList = new ArrayList<>();
        //get All products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting rest list
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String productcategory = "" + ds.child("productCategory").getValue();
                    if (selected.equals(productcategory)) {//if selected categories matches product categories then add in list
                        ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                        productList.add(modelProduct);
                    }

                }
                //now setup adapter
                adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                //set Adapter
                binding.productsRv.setAdapter(adapterProductSeller);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        //get All products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before getting rest list
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productList.add(modelProduct);
                }
                //now setup adapter
                adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                //set Adapter
                binding.productsRv.setAdapter(adapterProductSeller);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showProductsUI() {
        //show products UI and hide orders UI..
        binding.productsRl.setVisibility(View.VISIBLE);
        binding.ordersRl.setVisibility(View.GONE);

        binding.tabProductsTv.setTextColor(getResources().getColor(R.color.black));
        binding.tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        binding.tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show order UI and hide products UI
        binding.productsRl.setVisibility(View.GONE);
        binding.ordersRl.setVisibility(View.VISIBLE);

        binding.tabProductsTv.setTextColor(getResources().getColor(R.color.white));
        binding.tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        binding.tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);


    }

    private void makeMeOffline() {
        //after login in make user login
        progressDialog.setMessage("Logging out");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //update successfully
                firebaseAuth.signOut();
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed update
                progressDialog.dismiss();
                Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String accountType = "" + ds.child("accountType").getValue();
                    String email = "" + ds.child("email").getValue();
                    String shopName = "" + ds.child("shopName").getValue();
                    String profileImage = "" + ds.child("profileImage").getValue();
                    binding.nameTv.setText(name);
                    binding.shopNameTv.setText(shopName);
                    binding.emailTv.setText(email);
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(binding.profileIv);
                    } catch (Exception e) {
                        binding.profileIv.setImageResource(R.drawable.ic_store_gray);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}