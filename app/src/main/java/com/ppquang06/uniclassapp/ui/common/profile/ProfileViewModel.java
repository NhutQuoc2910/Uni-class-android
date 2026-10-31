package com.ppquang06.uniclassapp.ui.common.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.ChangePasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.data.repository.UserRepository;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

/**
 * ViewModel managing user profile retrieval, profile update, and password change.
 */
public class ProfileViewModel extends BaseViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<UiState<UserResponse>> profileState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<UserResponse>> updateProfileState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Void>> changePasswordState = new MutableLiveData<>(UiState.idle());

    public ProfileViewModel(@NonNull Application application) {
        this(application, new UserRepository(application));
    }

    public ProfileViewModel(@NonNull UserRepository userRepository) {
        this(new Application(), userRepository);
    }

    public ProfileViewModel(@NonNull Application application, @NonNull UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
    }

    public LiveData<UiState<UserResponse>> getProfileState() {
        return profileState;
    }

    public LiveData<UiState<UserResponse>> getUpdateProfileState() {
        return updateProfileState;
    }

    public LiveData<UiState<Void>> getChangePasswordState() {
        return changePasswordState;
    }

    public void loadProfile() {
        executeCall(
                userRepository.getCurrentUser(),
                profileState,
                "Không thể tải thông tin cá nhân"
        );
    }

    public void updateProfile(String fullName, String phone) {
        updateProfile(new UpdateProfileRequest(fullName, phone));
    }

    public void updateProfile(@NonNull UpdateProfileRequest request) {
        executeCall(
                userRepository.updateCurrentUser(request),
                updateProfileState,
                "Không thể cập nhật thông tin. Vui lòng thử lại."
        );
    }

    public void changePassword(String currentPassword, String newPassword) {
        changePassword(new ChangePasswordRequest(currentPassword, newPassword));
    }

    public void changePassword(@NonNull ChangePasswordRequest request) {
        executeCall(
                userRepository.changePassword(request),
                changePasswordState,
                "Không thể đổi mật khẩu. Vui lòng thử lại."
        );
    }

    public void resetUpdateProfileState() {
        dispatchState(updateProfileState, UiState.idle());
    }

    public void resetChangePasswordState() {
        dispatchState(changePasswordState, UiState.idle());
    }
}
