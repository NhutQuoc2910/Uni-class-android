package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.RegisterRequest;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.ActivityRegisterBinding;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private String selectedRole = "STUDENT";
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRoleSelection();
        setupPasswordToggles();
        setupListeners();
    }

    private void setupRoleSelection() {
        binding.cardStudent.setOnClickListener(v -> selectRole("STUDENT"));
        binding.cardLecturer.setOnClickListener(v -> selectRole("LECTURER"));
    }

    private void selectRole(String role) {
        selectedRole = role;
        if ("STUDENT".equals(role)) {
            binding.cardStudent.setBackgroundResource(R.drawable.bg_role_selected);
            binding.tvStudentTitle.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));

            binding.cardLecturer.setBackgroundResource(R.drawable.bg_role_unselected);
            binding.tvLecturerTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            binding.cardLecturer.setBackgroundResource(R.drawable.bg_role_selected);
            binding.tvLecturerTitle.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));

            binding.cardStudent.setBackgroundResource(R.drawable.bg_role_unselected);
            binding.tvStudentTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void setupPasswordToggles() {
        binding.ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
        });

        binding.ivToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                binding.edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.getText().length());
        });
    }

    private void setupListeners() {
        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        binding.btnRegister.setOnClickListener(v -> performRegister());
    }

    private void performRegister() {
        String fullName = binding.edtFullName.getText() != null ? binding.edtFullName.getText().toString().trim() : "";
        String contact = binding.edtContact.getText() != null ? binding.edtContact.getText().toString().trim() : "";
        String password = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString() : "";
        String confirmPassword = binding.edtConfirmPassword.getText() != null ? binding.edtConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(fullName)) {
            binding.edtFullName.setError("Vui lòng nhập họ và tên");
            binding.edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contact)) {
            binding.edtContact.setError("Vui lòng nhập email hoặc số điện thoại");
            binding.edtContact.requestFocus();
            return;
        }

        String email = "";
        String phone = "";
        if (Patterns.EMAIL_ADDRESS.matcher(contact).matches()) {
            email = contact;
        } else if (contact.matches("^[0-9+]{9,15}$")) {
            phone = contact;
            email = contact + "@student.edulens.edu.vn";
        } else {
            binding.edtContact.setError("Email hoặc số điện thoại không hợp lệ");
            binding.edtContact.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            binding.edtPassword.setError("Mật khẩu phải từ 8 ký tự trở lên");
            binding.edtPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            binding.edtConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        RegisterRequest request = new RegisterRequest(email, password, fullName, phone, selectedRole);
        ApiClient.getAuthApiService(this).register(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, RegisterSuccessActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Đăng ký không thành công";
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
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnRegister.setText(isLoading ? "Đang xử lý..." : getString(R.string.register_button));
    }
}
