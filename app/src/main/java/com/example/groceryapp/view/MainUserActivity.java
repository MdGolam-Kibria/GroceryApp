package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.adapter.AdapterShop;
import com.example.groceryapp.commands.MainSellerActivityClicks;
import com.example.groceryapp.commands.MainUserActivityClicks;
import com.example.groceryapp.databinding.ActivityMainUserBinding;
import com.example.groceryapp.model.ModelShop;
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

public class MainUserActivity extends AppCompatActivity {
    ActivityMainUserBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<ModelShop> shopList;
    private AdapterShop adapterShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_user);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_user);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        //at first show shops UI
        showShopsUI();
        binding.setClickHandle(new MainUserActivityClicks() {
            @Override
            public void logoutUser() {
                //make offline
                //signOut
                //go to login activity
                makeMeOffline();
            }

            @Override
            public void editUserProfile() {
                //for edit user profile...now open edit activity
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
            }

            @Override
            public void shopsTabClick() {
                //show shops
                showShopsUI();
            }

            @Override
            public void orderTabsClick() {
                //show order
                showOrderUI();
            }
        });
    }

    private void showShopsUI() {
        //now show Shops UI hide order Ui
        binding.shopRl.setVisibility(View.VISIBLE);
        binding.orderRl.setVisibility(View.GONE);

        binding.tabShopsTv.setTextColor(getResources().getColor(R.color.black));
        binding.tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        binding.tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        binding.tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrderUI() {
        //now show Order UI hide shops Ui
        binding.shopRl.setVisibility(View.GONE);
        binding.orderRl.setVisibility(View.VISIBLE);

        binding.tabShopsTv.setTextColor(getResources().getColor(R.color.white));
        binding.tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        binding.tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
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
                Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get user data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String profileImage = "" + ds.child("profileImage").getValue();
                    String accountType = "" + ds.child("accountType").getValue();
                    String city = "" + ds.child("city").getValue();
                    //set user data
                    binding.nameTv.setText(name);//this is user
                    binding.emailTv.setText(email);
                    binding.phoneTv.setText(phone);
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_peson_gray).into(binding.profileIv);
                    } catch (Exception e) {
                        binding.profileIv.setImageResource(R.drawable.ic_peson_gray);
                    }
                    //load only those shop that are in the city of user
                    loadShops(city);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(final String userCity) {
        shopList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before adding
                shopList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelShop modelShop = ds.getValue(ModelShop.class);
                        String shoCity = ""+ds.child("city").getValue();
                        //show only user city shops
                    if (shoCity.equals(userCity)){//mane user jei city te thakbe sei city er shop gula ke dekhabe
                        shopList.add(modelShop);//if i want do show all shops skip this if().. statement just add  "shopList.add(modelShop);"
                    }
                }
                //setUp adapter
                adapterShop = new AdapterShop(MainUserActivity.this,shopList);//set data to adapter
                binding.shopRv.setAdapter(adapterShop);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}