package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.commands.SettingsActivityClicks;
import com.example.groceryapp.databinding.ActivitySettingsBinding;
import com.example.groceryapp.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String enabledMessage = "Notification Are Enabled";
    private static final String disabledMessage = "Notification Are Disabled";
    private boolean isChecked;
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        firebaseAuth = FirebaseAuth.getInstance();
        //initt...SharedPreferences
        sp = getSharedPreferences("SETTINGS_SP", MODE_PRIVATE);
        //CHECK LAST SELECTED OPTION TRUE OR FALSE
        isChecked = sp.getBoolean("FCM_ENABLED", false);
        binding.fcmSwitch.setChecked(isChecked);
        if (isChecked) {
            //was enabled
            binding.notificationStatusTv.setText(enabledMessage);
        } else {
            //was disabled
            binding.notificationStatusTv.setText(disabledMessage);
        }

        binding.setClickHandle(new SettingsActivityClicks() {
            @Override
            public void backBtnClicks() {
                onBackPressed();
            }
        });
        //add switch check change listener to enable disable notification
        binding.fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //checked enable notification
                    subscribeToTopic();
                } else {
                    //unChecked disabled notification
                    unSubscribeToTopic();
                }
            }
        });
    }

    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //subscribed successfully
                        //save settings in sharedPreferance
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", true);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this, "" + enabledMessage, Toast.LENGTH_SHORT).show();
                        binding.notificationStatusTv.setText(enabledMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed subscribing
                Toast.makeText(SettingsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unSubscribeToTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //unsSubscribed
                        //save settings in sharedPreferance
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", false);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this, "" + disabledMessage, Toast.LENGTH_SHORT).show();
                        binding.notificationStatusTv.setText(disabledMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed unSubscribing
                Toast.makeText(SettingsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}