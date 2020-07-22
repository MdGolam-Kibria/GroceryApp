package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.adapter.AdapterCartItem;
import com.example.groceryapp.adapter.AdapterProductUser;
import com.example.groceryapp.commands.ShopDetailsActivityClicks;
import com.example.groceryapp.databinding.ActivityShopDetailsBinding;
import com.example.groceryapp.model.ModelCartItem;
import com.example.groceryapp.model.ModelProduct;
import com.example.groceryapp.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
    ActivityShopDetailsBinding binding;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private String myLatitude, myLongitude, myPhone;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee;
    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;
    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;
    private ProgressDialog progressDialog;
    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_shop_details);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_details);
        shopUid = getIntent().getStringExtra("shopUid");//get uid of the shop from intent.adapterShop.class
        firebaseAuth = FirebaseAuth.getInstance();
        //init..progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        //declare it to class level  inside onCreate
         easyDB = EasyDB.init(ShopDetailsActivity.this, "ITEMS_DB")//here "ITEMS_DB" is database name
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        //each shop have its own products and order so user add items to cart and go back and open cart in  different shop then cart should be different
        //show at first delete cart data whenever user open this activity..
        deleteCartData();
        cartCount();
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
                showCartDialog();
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

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();//delete all records from cart when open this activity
        //Toast.makeText(this, "Delete previous data", Toast.LENGTH_SHORT).show();
    }
    public void cartCount(){
        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            //no any item in cart so hide cart item count TextView
            binding.cartCountTv.setVisibility(View.GONE);
        }else {
            //have item so so int cart item textView
            binding.cartCountTv.setVisibility(View.VISIBLE);
            binding.cartCountTv.setText(""+count);//concatenate with string cause we cant set int value in view
        }
    }

    public double allTotalPrice = 0.0;
    //need to access those views in adapter
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;

    private void showCartDialog() {
        cartItemList = new ArrayList<>();
        //inflate layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //inti...views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        TextView sTotalLabelTv = view.findViewById(R.id.sTotalLabelTv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        TextView dFeeLabelTv = view.findViewById(R.id.dFeeLabelTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        TextView totalLabelTv = view.findViewById(R.id.totalLabelTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkOutBtn = view.findViewById(R.id.checkOutBtn);
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);//this is my custom view for this dialog
        //now set data to views
        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")//here "ITEMS_DB" is database name
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        //now get all records from database
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem(
                    "" + "",
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity
            );
            cartItemList.add(modelCartItem);
        }
        //now set the adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));
        //for show dialog
        final AlertDialog dialog = builder.create();
        dialog.show();
        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        //place order
        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    //user don;t enter address in her profile so.....
                    Toast.makeText(ShopDetailsActivity.this, "Please Enter Your Address Before Placing Order...", Toast.LENGTH_LONG).show();
                    return;
                }
                if (myPhone.equals("") || myPhone.equals("null")) {
                    //user don't Enter her phone Number So....
                    Toast.makeText(ShopDetailsActivity.this, "Please Enter Your Phone Number Before Placing Order...", Toast.LENGTH_LONG).show();
                    return;
                }
                if (cartItemList.size() == 0) {
                    //cart list empty
                    Toast.makeText(ShopDetailsActivity.this, "No Item In Cart", Toast.LENGTH_LONG).show();
                    return;
                }
                submitOrder(dialog);

            }
        });


    }

    private void submitOrder(final AlertDialog dialog) {//for order submitting
        progressDialog.setMessage("Placing Order...");
        progressDialog.show();
        final String timestamp = "" + System.currentTimeMillis();//for order id and order time
        String cost = allTotalPriceTv.getText().toString().trim().replace("$", "");//remove $ if contains
        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", timestamp);
        hashMap.put("orderStatus", "In Progress");//in progress/complete/cancelled
        hashMap.put("orderCost", cost);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        //now add to database
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        reference.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //order info added now add order items
                for (int i = 0; i < cartItemList.size(); i++) {
                    String pId = cartItemList.get(i).getPId();
                    String id = cartItemList.get(i).getId();
                    String cost = cartItemList.get(i).getCost();
                    String name= cartItemList.get(i).getName();
                    String price = cartItemList.get(i).getPrice();
                    String quantity = cartItemList.get(i).getQuantity();

                    HashMap<String,String> map = new HashMap<>();
                    map.put("pId",pId);
                    map.put("name",name);
                    map.put("cost",cost);
                    map.put("price",price);
                    map.put("quantity",quantity);

                    reference.child(timestamp).child("Items").child(pId).setValue(map);
                }
                progressDialog.dismiss();
                Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();
               deleteCartData();
               cartCount();
               dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ShopDetailsActivity.this, "Order Placed Failed..."+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    myPhone = "" + ds.child("phone").getValue();
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
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
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