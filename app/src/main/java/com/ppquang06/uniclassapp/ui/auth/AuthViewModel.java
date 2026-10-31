package com.ppquang06.uniclassapp.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.ForgotPasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.LoginRequest;
import com.ppquang06.uniclassapp.data.model.request.RegisterRequest;
import com.ppquang06.uniclassapp.data.model.request.ResetPasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.VerifyOtpRequest;
import com.ppquang06.uniclassapp.data.model.response.AuthResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.data.repository.AuthRepository;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

import java.util.Map;

/**
 * ViewModel managing authentication flows: login, registration, forgot password,
 * OTP verification, and password reset.
 */
public class AuthViewModel extends BaseViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<UiState<AuthResponse>> loginState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<UserResponse>> registerState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<String>> forgotPasswordState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Map<String, Object>>> verifyOtpState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<String>> resetPasswordState = new MutableLiveData<>(UiState.idle());

    public AuthViewModel(@NonNull Application application) {
        this(application, new AuthRepository(application));
    }

    public AuthViewModel(@NonNull AuthRepository authRepository) {
        this(new Application(), authRepository);
    }

    public AuthViewModel(@NonNull Application application, @NonNull AuthRepository authRepository) {
        super(application);
        this.authRepository = authRepository;
    }

    public LiveData<UiState<AuthResponse>> getLoginState() {
        return loginState;
    }

    public LiveData<UiState<UserResponse>> getRegisterState() {
        return registerState;
    }

    public LiveData<UiState<String>> getForgotPasswordState() {
        return forgotPasswordState;
    }

    public LiveData<UiState<Map<String, Object>>> getVerifyOtpState() {
        return verifyOtpState;
    }

    public LiveData<UiState<String>> getResetPasswordState() {
        return resetPasswordState;
    }

    public void login(String email, String password) {
        login(new LoginRequest(email, password));
    }

    public void login(@NonNull LoginRequest request) {
        executeCall(
                authRepository.login(request),
                loginState,
                "Email/Số điện thoại hoặc mật khẩu không chính xác"
        );
    }

    public void register(@NonNull RegisterRequest request) {
        executeCall(
                authRepository.register(request),
                registerState,
                "Đăng ký không thành công"
        );
    }

    public void forgotPassword(String email) {
        forgotPassword(new ForgotPasswordRequest(email));
    }

    public void forgotPassword(@NonNull ForgotPasswordRequest request) {
        executeCall(
                authRepository.forgotPassword(request),
                forgotPasswordState,
                "Gửi mã OTP thất bại"
        );
    }

    public void verifyOtp(String email, String otp) {
        verifyOtp(new VerifyOtpRequest(email, otp));
    }

    public void verifyOtp(@NonNull VerifyOtpRequest request) {
        executeCall(
                authRepository.verifyOtp(request),
                verifyOtpState,
                "Mã OTP không chính xác hoặc đã hết hạn"
        );
    }

    public void resetPassword(String email, String otp, String newPassword) {
        resetPassword(new ResetPasswordRequest(email, otp, newPassword));
    }

    public void resetPassword(@NonNull ResetPasswordRequest request) {
        executeCall(
                authRepository.resetPassword(request),
                resetPasswordState,
                "Đặt lại mật khẩu thất bại"
        );
    }

    public void resetLoginState() {
        dispatchState(loginState, UiState.idle());
    }

    public void resetRegisterState() {
        dispatchState(registerState, UiState.idle());
    }

    public void resetForgotPasswordState() {
        dispatchState(forgotPasswordState, UiState.idle());
    }

    public void resetVerifyOtpState() {
        dispatchState(verifyOtpState, UiState.idle());
    }

    public void resetResetPasswordState() {
        dispatchState(resetPasswordState, UiState.idle());
    }
}
