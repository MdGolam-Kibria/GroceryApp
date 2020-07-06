package com.example.groceryapp.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
ActivityLoginBinding activityLoginBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        activityLoginBinding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        activityLoginBinding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResisterUserActivity.class));
            }
        });
        activityLoginBinding.forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });




        //////////////////////13:44
    }
}