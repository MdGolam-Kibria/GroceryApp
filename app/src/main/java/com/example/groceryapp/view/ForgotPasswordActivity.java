package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.commands.ForgotPasswordActivityClicks;
import com.example.groceryapp.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    ActivityForgotPasswordBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_forgot_password);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.setClickHandle(new ForgotPasswordActivityClicks() {
            @Override
            public void backButtonClick() {
                onBackPressed();
            }

            @Override
            public void recoverButtonClick() {
                recoverPassword();
            }
        });
    }

    private String email;

    private void recoverPassword() {
        email = binding.emailEt.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email Address");
            binding.emailEt.requestFocus();
            return;
        }
        progressDialog.setMessage("Sending instructions to reset password....");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //instruction send
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, "Password reset instruction send to your email", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //instruction send failed
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}