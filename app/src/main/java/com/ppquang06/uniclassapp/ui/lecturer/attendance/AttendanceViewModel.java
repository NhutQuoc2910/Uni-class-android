package com.ppquang06.uniclassapp.ui.lecturer.attendance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.data.repository.AttendanceRepository;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

/**
 * ViewModel managing lecturer QR attendance sessions (opening, polling live state, closing)
 * and student QR attendance check-in.
 */
public class AttendanceViewModel extends BaseViewModel {

    private final AttendanceRepository attendanceRepository;

    private final MutableLiveData<UiState<LiveAttendanceSessionResponse>> openSessionState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<LiveAttendanceSessionResponse>> liveSessionState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<LiveAttendanceSessionResponse>> closeSessionState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<AttendanceCheckInResponse>> checkInState = new MutableLiveData<>(UiState.idle());

    public AttendanceViewModel(@NonNull Application application) {
        this(application, new AttendanceRepository(application));
    }

    public AttendanceViewModel(@NonNull AttendanceRepository attendanceRepository) {
        this(new Application(), attendanceRepository);
    }

    public AttendanceViewModel(@NonNull Application application,
                               @NonNull AttendanceRepository attendanceRepository) {
        super(application);
        this.attendanceRepository = attendanceRepository;
    }

    public LiveData<UiState<LiveAttendanceSessionResponse>> getOpenSessionState() {
        return openSessionState;
    }

    public LiveData<UiState<LiveAttendanceSessionResponse>> getLiveSessionState() {
        return liveSessionState;
    }

    public LiveData<UiState<LiveAttendanceSessionResponse>> getCloseSessionState() {
        return closeSessionState;
    }

    public LiveData<UiState<AttendanceCheckInResponse>> getCheckInState() {
        return checkInState;
    }

    public void openQrSession(@NonNull String classId, @NonNull OpenQrSessionRequest request) {
        executeCall(
                attendanceRepository.openQrSession(classId, request),
                openSessionState,
                "Không thể mở phiên điểm danh QR"
        );
    }

    public void loadLiveSession(@NonNull String sessionId) {
        executeCall(
                attendanceRepository.getLiveAttendanceSession(sessionId),
                liveSessionState,
                "Không thể tải thông tin phiên điểm danh"
        );
    }

    public void closeSession(@NonNull String sessionId) {
        executeCall(
                attendanceRepository.closeAttendanceSession(sessionId),
                closeSessionState,
                "Không thể đóng phiên điểm danh"
        );
    }

    public void checkIn(@NonNull AttendanceCheckInRequest request) {
        executeCall(
                attendanceRepository.checkInAttendance(request),
                checkInState,
                "Điểm danh thất bại"
        );
    }

    public void resetOpenSessionState() {
        dispatchState(openSessionState, UiState.idle());
    }

    public void resetCloseSessionState() {
        dispatchState(closeSessionState, UiState.idle());
    }

    public void resetCheckInState() {
        dispatchState(checkInState, UiState.idle());
    }
}
