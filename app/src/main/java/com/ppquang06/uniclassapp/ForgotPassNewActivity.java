package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.ResetPasswordRequest;
import com.ppquang06.uniclassapp.databinding.ActivityForgotPassNewBinding;
import com.ppquang06.uniclassapp.utils.Constants;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassNewActivity extends AppCompatActivity {
    private ActivityForgotPassNewBinding binding;
    private String email = "";
    private String otp = "";
    private boolean isNewPassVisible = false;
    private boolean isConfirmPassVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = getIntent().getStringExtra(Constants.EXTRA_EMAIL);
        otp = getIntent().getStringExtra(Constants.EXTRA_OTP);
        if (email == null) email = "";
        if (otp == null) otp = "";

        setupPasswordToggles();
        setupListeners();
    }

    private void setupPasswordToggles() {
        binding.ivToggleNewPassword.setOnClickListener(v -> {
            isNewPassVisible = !isNewPassVisible;
            if (isNewPassVisible) {
                binding.edtNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.edtNewPassword.setSelection(binding.edtNewPassword.getText().length());
        });

        binding.ivToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPassVisible = !isConfirmPassVisible;
            if (isConfirmPassVisible) {
                binding.edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.getText().length());
        });
    }

    private void setupListeners() {
        binding.tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassNewActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnResetPassword.setOnClickListener(v -> performResetPassword());
    }

    private void performResetPassword() {
        String newPassword = binding.edtNewPassword.getText() != null ? binding.edtNewPassword.getText().toString() : "";
        String confirmPassword = binding.edtConfirmPassword.getText() != null ? binding.edtConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 8) {
            binding.edtNewPassword.setError("Mật khẩu mới phải từ 8 ký tự trở lên");
            binding.edtNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            binding.edtConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        ResetPasswordRequest request = new ResetPasswordRequest(email, otp, newPassword);
        ApiClient.getAuthApiService(this).resetPassword(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ForgotPassNewActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPassNewActivity.this, PassChangedSuccessActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Đặt lại mật khẩu thất bại";
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
                    Toast.makeText(ForgotPassNewActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ForgotPassNewActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.btnResetPassword.setEnabled(!isLoading);
        binding.btnResetPassword.setText(isLoading ? "Đang đổi mật khẩu..." : getString(R.string.change_password_button));
    }
}
