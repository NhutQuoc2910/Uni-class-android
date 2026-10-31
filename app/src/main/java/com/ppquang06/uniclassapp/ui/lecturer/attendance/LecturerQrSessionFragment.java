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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerQrSessionBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.utils.HardwareSensorAttendanceHelper;
import com.ppquang06.uniclassapp.utils.QrCodeGenerator;

import java.util.Locale;

public class LecturerQrSessionFragment extends Fragment {

    private FragmentLecturerQrSessionBinding binding;
    private AttendanceViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        readArguments();
        setupHeader();
        setupActions();
        observeViewModel();

        openOrInitializeSession();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            classId = args.getString("classId", "");
            classCode = args.getString("classCode", "");
            className = args.getString("className", "");
            room = args.getString("room", "");
            sessionId = args.getString("sessionId", "");
        }
    }

    private void setupHeader() {
        if (binding == null) return;
        if (!TextUtils.isEmpty(className)) {
            binding.tvClassName.setText(className);
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
        String formattedGps = String.format(Locale.US, "%.4f, %.4f", latitude, longitude);
        binding.tvGpsInfo.setText("Tọa độ: " + formattedGps + " · Bán kính " + (int) radiusMeters + "m");
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

    private void observeViewModel() {
        viewModel.getOpenSessionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbQrLoading.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.pbQrLoading.setVisibility(View.GONE);
                    LiveAttendanceSessionResponse data = state.getData();
                    if (data != null) {
                        sessionId = data.getSessionId() != null ? data.getSessionId() : "";
                        if (data.getWifiBssid() != null) wifiBssid = data.getWifiBssid();
                        if (data.getLatitude() != null) latitude = data.getLatitude();
                        if (data.getLongitude() != null) longitude = data.getLongitude();
                        setupHeader();
                        startDynamicQrRotation();
                    }
                    break;
                case ERROR:
                    binding.pbQrLoading.setVisibility(View.GONE);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Không thể khởi tạo phiên điểm danh";
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case IDLE:
                    break;
            }
        });

        viewModel.getCloseSessionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbQrLoading.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.pbQrLoading.setVisibility(View.GONE);
                    markSessionClosedUi();
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Đã chốt phiên điểm danh thành công", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    binding.pbQrLoading.setVisibility(View.GONE);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Chốt phiên điểm danh thất bại";
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void openOrInitializeSession() {
        if (getContext() == null) return;

        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
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
            viewModel.openQrSession(classId, request);
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

        viewModel.closeSession(sessionId);
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
