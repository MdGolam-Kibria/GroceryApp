package com.example.groceryapp.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.model.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {
    private Context context;
    private ArrayList<ModelReview> reviewArrayList;


    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        ModelReview modelReview = reviewArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String timestamp = modelReview.getTimestamp();
        String review = modelReview.getReview();
        //we also need the user who wrote the review.we can do it using uid of the user
        loadUserDetails(modelReview, holder);
        //convert timestamp to proper format Like = dd/MM/yy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyy", calendar).toString();
        //now set data t view
        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);
        holder.dateTv.setText(dateFormat);


    }

    private void loadUserDetails(ModelReview modelReview, final HolderReview holder) {
        //uid of user who wrote review
        String uid = modelReview.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get user info
                String name = "" + snapshot.child("name").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                //now set data to view
                holder.nameTv.setText(name);
                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_peson_gray).into(holder.profileIv);
                } catch (Exception e) {
                    holder.profileIv.setImageResource(R.drawable.ic_peson_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }

    class HolderReview extends RecyclerView.ViewHolder {
        private ImageView profileIv;
        private TextView nameTv, dateTv, reviewTv;
        private RatingBar ratingBar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            dateTv = itemView.findViewById(R.id.dateTv);
            reviewTv = itemView.findViewById(R.id.reviewTv);
        }
    }
}
