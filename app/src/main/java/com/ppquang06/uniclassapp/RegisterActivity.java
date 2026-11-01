package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.data.model.request.RegisterRequest;
import com.ppquang06.uniclassapp.databinding.ActivityRegisterBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;
    private String selectedRole = "STUDENT";
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View root = binding.getRoot();

        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets ime = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + bottom
            );

            return insets;
        });
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupErrorClearing();
        setupRoleSelection();
        setupPasswordToggles();
        setupListeners();
        observeViewModel();
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

    private void setupErrorClearing() {
        binding.edtPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.edtPassword.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        binding.edtConfirmPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.edtConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    private void observeViewModel() {
        authViewModel.getRegisterState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                authViewModel.resetRegisterState();
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, RegisterSuccessActivity.class);
                startActivity(intent);
                finish();
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null ? state.getMessage() : "Đăng ký không thành công";
                authViewModel.resetRegisterState();
                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
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
            email = contact.trim().toLowerCase();
        } else if (contact.matches("^\\+?[0-9]{9,15}$")) {
            phone = contact.trim();
            email = phone + ("LECTURER".equalsIgnoreCase(selectedRole) ? "@lecturer.edulens.edu.vn" : "@student.edulens.edu.vn");
        } else {
            binding.edtContact.setError("Email hoặc số điện thoại không hợp lệ");
            binding.edtContact.requestFocus();
            return;
        }

        String passwordError = validatePassword(password);

        if (passwordError != null) {
            binding.edtPassword.setError(passwordError);
            binding.edtPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.edtConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            binding.edtConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            binding.edtConfirmPassword.requestFocus();
            return;
        }

        String roleToSend = "LECTURER".equalsIgnoreCase(selectedRole) ? "ROLE_LECTURER" : "ROLE_STUDENT";
        RegisterRequest request = new RegisterRequest(email, password, fullName, phone, roleToSend);
        authViewModel.register(request);
    }
    private String validatePassword(String password){
        if(password.length() < 8){
            return "Mật khẩu phải có ít nhất 8 ký tự";
        }
        if(!password.matches(".*[A-Z].*")){
            return "Mật khẩu phải có ít nhất 1 chữ viết hoa";
        }
        if (!password.matches(".*[a-z].*")){
            return "Mật khẩu phải có ít nhất 1 chữ viết thường";
        }
        if(!password.matches(".*[0-9].*")){
            return "Mật khẩu phải có ít nhất 1 chữ số";
        }
        if (!password.matches(".*[^a-zA-Z0-9\\s].*")) {
            return "Mật khẩu phải có ít nhất 1 ký tự đặc biệt";
        }

        return null;
    }
    private void setLoading(boolean isLoading) {
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnRegister.setText(isLoading ? "Đang xử lý..." : getString(R.string.register_button));
    }
}
