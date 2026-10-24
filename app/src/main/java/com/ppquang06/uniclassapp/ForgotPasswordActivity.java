package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.ForgotPasswordRequest;
import com.ppquang06.uniclassapp.databinding.ActivityForgotPasswordBinding;
import com.ppquang06.uniclassapp.utils.Constants;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.tvReturnToLogin.setOnClickListener(v -> finish());

        binding.btnSendOTP.setOnClickListener(v -> performSendOtp());
    }

    private void performSendOtp() {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Vui lòng nhập địa chỉ email");
            binding.edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.setError("Địa chỉ email không hợp lệ");
            binding.edtEmail.requestFocus();
            return;
        }

        setLoading(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        ApiClient.getAuthApiService(this).forgotPassword(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi đến email của bạn!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPassOTPActivity.class);
                    intent.putExtra(Constants.EXTRA_EMAIL, email);
                    startActivity(intent);
                } else {
                    String errorMsg = "Gửi mã OTP thất bại";
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errJson = new JSONObject(response.errorBody().string());
                            if (errJson.has("message")) {
                                errorMsg = errJson.getString("message");
                            }
                        } else if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = response.body().getMessage();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.btnSendOTP.setEnabled(!isLoading);
        binding.btnSendOTP.setText(isLoading ? "Đang gửi OTP..." : getString(R.string.send_otp_button));
    }
}
