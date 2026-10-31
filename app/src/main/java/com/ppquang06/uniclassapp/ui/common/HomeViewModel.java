package com.ppquang06.uniclassapp.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.data.repository.UserRepository;

import java.util.List;

/**
 * ViewModel managing home summary (with offline Room caching), notifications,
 * and current user profile state.
 */
public class HomeViewModel extends BaseViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<UiState<HomeSummaryResponse>> homeSummaryState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<List<NotificationItemResponse>>> notificationsState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<UserResponse>> currentUserState = new MutableLiveData<>(UiState.idle());

    public HomeViewModel(@NonNull Application application) {
        this(application, new UserRepository(application));
    }

    public HomeViewModel(@NonNull UserRepository userRepository) {
        this(new Application(), userRepository);
    }

    public HomeViewModel(@NonNull Application application, @NonNull UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
    }

    public LiveData<UiState<HomeSummaryResponse>> getHomeSummaryState() {
        return homeSummaryState;
    }

    public LiveData<UiState<List<NotificationItemResponse>>> getNotificationsState() {
        return notificationsState;
    }

    public LiveData<UiState<UserResponse>> getCurrentUserState() {
        return currentUserState;
    }

    public void loadHomeSummary() {
        userRepository.fetchHomeSummaryOfflineFirst(homeSummaryState, getActiveCallsMutable());
    }

    public void loadNotifications() {
        executeCall(
                userRepository.getNotifications(),
                notificationsState,
                "Không thể tải danh sách thông báo"
        );
    }

    public void loadCurrentUser() {
        executeCall(
                userRepository.getCurrentUser(),
                currentUserState,
                "Không thể tải thông tin người dùng"
        );
    }
}
