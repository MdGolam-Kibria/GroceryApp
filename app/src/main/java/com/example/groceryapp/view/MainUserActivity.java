package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.commands.MainSellerActivityClicks;
import com.example.groceryapp.commands.MainUserActivityClicks;
import com.example.groceryapp.databinding.ActivityMainUserBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {
ActivityMainUserBinding binding;
private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_user);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main_user);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth  =FirebaseAuth.getInstance();
        checkUser();
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
                startActivity(new Intent(MainUserActivity.this,ProfileEditUserActivity.class));
            }
        });
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
                    String name = "" + ds.child("name").getValue();
                    String accountType = "" + ds.child("accountType").getValue();
                    binding.nameTv.setText(name + "(" + accountType + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}