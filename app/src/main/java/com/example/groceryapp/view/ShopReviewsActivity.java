package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.AdapterView;

import com.example.groceryapp.R;
import com.example.groceryapp.adapter.AdapterReview;
import com.example.groceryapp.commands.ShopReviewsActivityClicks;
import com.example.groceryapp.commands.WriteReviewActivityClicks;
import com.example.groceryapp.databinding.ActivityShopReviewsBinding;
import com.example.groceryapp.databinding.ActivityWriteReviewBinding;
import com.example.groceryapp.model.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {
    private ActivityShopReviewsBinding binding;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_reviews);
        firebaseAuth = FirebaseAuth.getInstance();
        //get shopuid from intent
        shopUid = getIntent().getStringExtra("shopUid");
        loadShopDetails();//for sho name,image and average ratings
        loadReviews();//for reviews list,average ratings
        binding.setClickHandle(new ShopReviewsActivityClicks() {
            @Override
            public void backBtnclick() {
                onBackPressed();
            }
        });

    }

    private float ratingSum = 0;

    private void loadReviews() {
        reviewArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        reviewArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());//e.g 4.3
                            ratingSum = ratingSum + rating;//for avg rating, add (addition of) all ratings later we will divide by number of reviews

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //now setup adapter
                        adapterView = new AdapterReview(ShopReviewsActivity.this, reviewArrayList);
                        binding.reviewsRv.setAdapter(adapterView);
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRatings = ratingSum / numberOfReviews;
                        binding.ratingBarTv.setText(String.format("%2f",avgRatings)+"["+numberOfReviews+"]");//e.g = 4.7 [10]
                        binding.ratingBar.setRating(avgRatings);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();

                        binding.shopNameTv.setText(shopName);

                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(binding.profileIv);
                        } catch (Exception e) {
                            binding.profileIv.setImageResource(R.drawable.ic_store_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}