package com.example.groceryapp.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
ActivityForgotPasswordBinding activityForgotPasswordBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_forgot_password);
        activityForgotPasswordBinding = DataBindingUtil.setContentView(this,R.layout.activity_forgot_password);
        activityForgotPasswordBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}