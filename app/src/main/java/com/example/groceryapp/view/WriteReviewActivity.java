package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.commands.WriteReviewActivityClicks;
import com.example.groceryapp.databinding.ActivityWriteReviewBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {
    ActivityWriteReviewBinding binding;
    private FirebaseAuth firebaseAuth;
    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_write_review);
        firebaseAuth = FirebaseAuth.getInstance();
        shopUid = getIntent().getStringExtra("shopUid");
        //load shopinfo like shopname shopimage
        loadShopInfo();
        //if user has writen review to the shop then load it
        loadMyReview();
        binding.setClickHandle(new WriteReviewActivityClicks() {
            @Override
            public void sendReviewBtn() {
                inputData();
            }

            @Override
            public void backBtnClick() {
                onBackPressed();
            }
        });

    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String shopName = "" + snapshot.child("shopName").getValue();
                String shopImage = "" + snapshot.child("profileImage").getValue();
                //set data to view
                binding.shopNameTv.setText(shopName);
                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_gray).into(binding.profileIv);
                } catch (Exception e) {
                    binding.profileIv.setImageResource(R.drawable.ic_store_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {//jodi data thake i mean jodi kono feedback database e thake.
                            //my review is available for the shop now load the review
                            String uid = "" + snapshot.child("uid").getValue();
                            String ratings = "" + snapshot.child("ratings").getValue();
                            String review = "" + snapshot.child("review").getValue();
                            String timestamp = "" + snapshot.child("timestamp").getValue();
                            //set details to our UI
                            float myRating = Float.parseFloat(ratings);
                            binding.ratingBar.setRating(myRating);
                            binding.reviewEt.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(WriteReviewActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void inputData() {
        if (binding.reviewEt.getText().toString().equals("")) {
            binding.reviewEt.setError("Please Type Review");
            binding.reviewEt.requestFocus();
            return;
        }
        String rating = "" + binding.ratingBar.getRating();
        String review = binding.reviewEt.getText().toString().trim();
        String timestamp = "" + System.currentTimeMillis();//for time of review
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", firebaseAuth.getUid());
        hashMap.put("ratings", "" + rating);
        hashMap.put("review", "" + review);
        hashMap.put("timestamp", "" + timestamp);
        //now send data to database
        //DB>Users>ShopUid>Ratings>firebaseAuth.getUid()
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(WriteReviewActivity.this, "Review Published Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WriteReviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}