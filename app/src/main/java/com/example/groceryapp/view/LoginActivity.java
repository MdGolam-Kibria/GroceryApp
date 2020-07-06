package com.example.groceryapp.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.groceryapp.R;
import com.example.groceryapp.commands.LoginActivityClicks;
import com.example.groceryapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
ActivityLoginBinding activityLoginBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        activityLoginBinding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        activityLoginBinding.setClick(new LoginActivityClicks() {
            @Override
            public void onClickLoginButton() {

            }

            @Override
            public void onClickResisterUserButton() {
                startActivity(new Intent(LoginActivity.this,ResisterUserActivity.class));
            }

            @Override
            public void onClickForgotTv() {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));

            }
        });
    }
}