package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ppquang06.uniclassapp.databinding.ActivityPasswordChangedSuccessBinding;

public class PassChangedSuccessActivity extends AppCompatActivity {
    private ActivityPasswordChangedSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordChangedSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(PassChangedSuccessActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
