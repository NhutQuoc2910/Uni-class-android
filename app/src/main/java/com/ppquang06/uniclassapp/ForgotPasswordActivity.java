package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle saveInstaceState){
        super.onCreate(saveInstaceState);
        setContentView(R.layout.activity_forgot_password);
        TextView tvReturnToLogin = findViewById(R.id.tvReturnToLogin);
        tvReturnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        Button btnSendOTP = findViewById(R.id.btnSendOTP);
        btnSendOTP.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPassOTPActivity.class);
                startActivity(intent);
            }
        });
    }

}
