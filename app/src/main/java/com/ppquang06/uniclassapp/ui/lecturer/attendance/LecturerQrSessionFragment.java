package com.ppquang06.uniclassapp.ui.lecturer.attendance;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerQrSessionBinding;
import com.ppquang06.uniclassapp.utils.HardwareSensorAttendanceHelper;
import com.ppquang06.uniclassapp.utils.QrCodeGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerQrSessionFragment extends Fragment {

    private FragmentLecturerQrSessionBinding binding;

    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String room = "";

    private String sessionId = "";
    private boolean isSessionOpen = true;
    private String wifiBssid = HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_BSSID;
    private double latitude = HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LAT;
    private double longitude = HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LNG;
    private double radiusMeters = 50.0;

    private CountDownTimer countDownTimer;
    private int rotationCount = 1;
    private static final int COUNTDOWN_SECONDS = 30;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                fetchTelemetryAndOpenSession();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerQrSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
            room = getArguments().getString("room", "");
        }

        setupTopBar();
        setupHeader();
        setupActions();
        openOrInitializeSession();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.qr_session_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupHeader() {
        if (!TextUtils.isEmpty(className)) {
            binding.tvClassName.setText(className);
        } else {
            binding.tvClassName.setText("Lớp học");
        }

        StringBuilder roomInfo = new StringBuilder();
        if (!TextUtils.isEmpty(classCode)) {
            roomInfo.append(classCode);
        }
        if (!TextUtils.isEmpty(room)) {
            if (roomInfo.length() > 0) roomInfo.append(" · ");
            roomInfo.append(room);
        }
        if (roomInfo.length() > 0) {
            binding.tvClassCodeRoom.setText(roomInfo.toString());
        }

        binding.tvWifiInfo.setText("BSSID: " + wifiBssid + " · Khớp mạng phòng học");
        binding.tvGpsInfo.setText("Tọa độ: " + latitude + ", " + longitude + " · Bán kính " + (int) radiusMeters + "m");
    }

    private void setupActions() {
        binding.btnTrackLive.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("sessionId", sessionId);
            bundle.putString("classId", classId);
            bundle.putString("classCode", classCode);
            bundle.putString("className", className);
            bundle.putString("room", room);

            try {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.nav_lecturer_attendance_live, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Điều hướng theo dõi trực tiếp", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnCloseSession.setOnClickListener(v -> confirmCloseSession());
    }

    private void openOrInitializeSession() {
        if (getContext() == null || TextUtils.isEmpty(classId)) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Thiếu thông tin lớp học", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fineGranted) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            fetchTelemetryAndOpenSession();
        }
    }

    private void fetchTelemetryAndOpenSession() {
        if (getContext() == null || TextUtils.isEmpty(classId)) {
            return;
        }

        binding.pbQrLoading.setVisibility(View.VISIBLE);

        wifiBssid = HardwareSensorAttendanceHelper.getBssid(requireContext());

        HardwareSensorAttendanceHelper.getCurrentLocation(requireContext(), (lat, lng) -> {
            if (!isAdded() || binding == null) return;
            latitude = lat;
            longitude = lng;
            setupHeader();

            OpenQrSessionRequest request = new OpenQrSessionRequest(wifiBssid, latitude, longitude, radiusMeters);

            ApiClient.getUserClassApiService(requireContext())
                    .openQrSession(classId, request)
                    .enqueue(new Callback<ApiResponse<LiveAttendanceSessionResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call,
                                               @NonNull Response<ApiResponse<LiveAttendanceSessionResponse>> response) {
                            if (!isAdded() || binding == null) return;
                            binding.pbQrLoading.setVisibility(View.GONE);

                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                                LiveAttendanceSessionResponse data = response.body().getData();
                                sessionId = data.getSessionId() != null ? data.getSessionId() : "";
                                if (data.getWifiBssid() != null) wifiBssid = data.getWifiBssid();
                                if (data.getLatitude() != null) latitude = data.getLatitude();
                                if (data.getLongitude() != null) longitude = data.getLongitude();
                                setupHeader();
                                startDynamicQrRotation();
                            } else {
                                String msg = (response.body() != null && response.body().getMessage() != null)
                                        ? response.body().getMessage() : "Không thể khởi tạo phiên điểm danh";
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call, @NonNull Throwable t) {
                            if (!isAdded() || binding == null) return;
                            binding.pbQrLoading.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void startDynamicQrRotation() {
        refreshDynamicQr();
        startTimer();
    }

    private void refreshDynamicQr() {
        if (!isAdded() || binding == null || !isSessionOpen || TextUtils.isEmpty(sessionId)) return;

        // Dynamic payload rotates based on sessionId and current rotation count/time
        String payload = "UNICLASS:" + (TextUtils.isEmpty(classCode) ? "CLASS" : classCode)
                + ":" + sessionId + ":" + System.currentTimeMillis();

        binding.ivQrCode.setImageBitmap(QrCodeGenerator.generateQrBitmap(payload, 512));

        String sessionSnippet = sessionId.length() > 8 ? sessionId.substring(0, 8) : sessionId;
        binding.tvTokenPreview.setText("Mã phiên: " + sessionSnippet + " · Lần xoay #" + rotationCount);
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (!isSessionOpen) return;

        countDownTimer = new CountDownTimer(COUNTDOWN_SECONDS * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded() || binding == null) return;
                int seconds = (int) (millisUntilFinished / 1000L);
                binding.tvTimerCountdown.setText(getString(R.string.qr_refresh_countdown_format, seconds));
                binding.progressCountdown.setProgress(seconds);
            }

            @Override
            public void onFinish() {
                if (!isAdded() || binding == null || !isSessionOpen) return;
                rotationCount++;
                refreshDynamicQr();
                startTimer();
            }
        };
        countDownTimer.start();
    }

    private void confirmCloseSession() {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_close_session_title)
                .setMessage(R.string.dialog_close_session_msg)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.btn_close_session_confirm, (dialog, which) -> executeCloseSession())
                .show();
    }

    private void executeCloseSession() {
        if (getContext() == null || TextUtils.isEmpty(sessionId)) {
            markSessionClosedUi();
            return;
        }

        ApiClient.getUserClassApiService(requireContext())
                .closeAttendanceSession(sessionId)
                .enqueue(new Callback<ApiResponse<LiveAttendanceSessionResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call,
                                           @NonNull Response<ApiResponse<LiveAttendanceSessionResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            markSessionClosedUi();
                            Toast.makeText(requireContext(), "Đã chốt phiên điểm danh thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Chốt phiên điểm danh thất bại";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markSessionClosedUi() {
        isSessionOpen = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        binding.tvSessionStatus.setText(R.string.session_status_closed);
        binding.tvSessionStatus.setBackgroundResource(R.drawable.bg_pill_gray);
        binding.tvSessionStatus.setTextColor(requireContext().getColor(R.color.text_secondary));

        binding.tvTimerCountdown.setText("Phiên điểm danh đã kết thúc");
        binding.progressCountdown.setProgress(0);
        binding.tvTokenPreview.setText("Mã QR đã hết hiệu lực");
        binding.ivQrCode.setAlpha(0.35f);

        binding.btnCloseSession.setEnabled(false);
        binding.btnCloseSession.setText("Phiên đã kết thúc");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        binding = null;
    }
}
