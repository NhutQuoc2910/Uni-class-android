package com.ppquang06.uniclassapp.ui.student.qr;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentScanVerifyingBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentScanVerifyingFragment extends Fragment {

    public static final String ERROR_QR_EXPIRED = "QR_EXPIRED";
    public static final String ERROR_WIFI_MISMATCH = "WIFI_MISMATCH";
    public static final String ERROR_GPS_OUT_OF_RANGE = "GPS_OUT_OF_RANGE";
    public static final String ERROR_DEVICE_ALREADY_USED = "DEVICE_ALREADY_USED";

    public static final int STEP_STATE_WAITING = 0;
    public static final int STEP_STATE_CHECKING = 1;
    public static final int STEP_STATE_PASSED = 2;
    public static final int STEP_STATE_FAILED = 3;

    private FragmentStudentScanVerifyingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String sessionId;
    private String wifiBssid;
    private String latitudeStr;
    private String longitudeStr;
    private String deviceId;
    private String simulationMode;

    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentScanVerifyingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        startVerificationFlow();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            sessionId = args.getString(StudentScanQrFragment.ARG_SESSION_ID, "DEMO_OPEN");
            wifiBssid = args.getString(StudentScanQrFragment.ARG_WIFI_BSSID, "00:11:22:33:44:55");
            latitudeStr = args.getString(StudentScanQrFragment.ARG_LATITUDE, "10.8700");
            longitudeStr = args.getString(StudentScanQrFragment.ARG_LONGITUDE, "106.8031");
            deviceId = args.getString(StudentScanQrFragment.ARG_DEVICE_ID, "android-student-device-01");
            simulationMode = args.getString(StudentScanQrFragment.ARG_SIMULATION_MODE, "");
        }
    }

    private void startVerificationFlow() {
        // Initial visual states
        updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_CHECKING);
        updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_WAITING);
        updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_WAITING);

        Double lat = 10.8700;
        Double lng = 106.8031;
        try {
            lat = Double.parseDouble(latitudeStr);
            lng = Double.parseDouble(longitudeStr);
        } catch (Exception ignored) {}

        AttendanceCheckInRequest request = new AttendanceCheckInRequest(
                sessionId,
                wifiBssid,
                lat,
                lng,
                deviceId
        );

        if (simulationMode != null && !simulationMode.isEmpty()) {
            // Direct simulation mode
            animateVerificationSequence(false, simulationMode, null);
            return;
        }

        // Call real backend API
        ApiClient.getUserClassApiService(requireContext()).checkInAttendance(request)
                .enqueue(new Callback<ApiResponse<AttendanceCheckInResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AttendanceCheckInResponse>> call,
                                           @NonNull Response<ApiResponse<AttendanceCheckInResponse>> response) {
                        if (!isAdded() || binding == null) return;

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AttendanceCheckInResponse data = response.body().getData();
                            animateVerificationSequence(true, null, data);
                        } else {
                            String classifiedError = resolveErrorFromResponse(response);
                            animateVerificationSequence(false, classifiedError, null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AttendanceCheckInResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        animateVerificationSequence(false, ERROR_QR_EXPIRED, null);
                    }
                });
    }

    private String resolveErrorFromResponse(Response<ApiResponse<AttendanceCheckInResponse>> response) {
        int code = response.code();
        String errorCode = "";
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ApiResponse<?> errRes = gson.fromJson(errorJson, ApiResponse.class);
                if (errRes != null) {
                    if (errRes.getCode() != 0) code = errRes.getCode();
                    if (errRes.getErrorCode() != null) errorCode = errRes.getErrorCode();
                }
            }
        } catch (Exception ignored) {}

        return classifyError(code, errorCode, simulationMode);
    }

    public static String classifyError(int apiCode, String apiErrorCode, String simMode) {
        if (simMode != null && !simMode.trim().isEmpty()) {
            return simMode.trim();
        }
        if (apiCode == 40010 || "QR_EXPIRED".equalsIgnoreCase(apiErrorCode)) {
            return ERROR_QR_EXPIRED;
        }
        if (apiCode == 40011 || "WIFI_MISMATCH".equalsIgnoreCase(apiErrorCode)) {
            return ERROR_WIFI_MISMATCH;
        }
        if (apiCode == 40012 || "GPS_OUT_OF_RANGE".equalsIgnoreCase(apiErrorCode)) {
            return ERROR_GPS_OUT_OF_RANGE;
        }
        if (apiCode == 40013 || "DEVICE_ALREADY_USED".equalsIgnoreCase(apiErrorCode)) {
            return ERROR_DEVICE_ALREADY_USED;
        }
        return ERROR_QR_EXPIRED;
    }

    private void animateVerificationSequence(boolean isSuccess, @Nullable String errorType, @Nullable AttendanceCheckInResponse successData) {
        if (isSuccess) {
            // Step 1 pass at 500ms
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_PASSED);
                updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_CHECKING);
            }, 500);

            // Step 2 pass at 1000ms
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_PASSED);
                updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_CHECKING);
            }, 1000);

            // Step 3 pass at 1500ms
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_PASSED);
            }, 1500);

            // Navigate to Result at 2000ms
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                String json = successData != null ? gson.toJson(successData) : createDefaultSuccessJson();
                navigateToResult(true, "", json);
            }, 2000);

        } else {
            String err = errorType != null ? errorType : ERROR_QR_EXPIRED;

            if (ERROR_QR_EXPIRED.equals(err)) {
                // Fails on Step 1 at 700ms
                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_FAILED);
                }, 700);

                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    navigateToResult(false, ERROR_QR_EXPIRED, "");
                }, 1400);

            } else if (ERROR_WIFI_MISMATCH.equals(err)) {
                // Step 1 passes at 500ms, Step 2 checking
                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_PASSED);
                    updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_CHECKING);
                }, 500);

                // Step 2 fails at 1100ms
                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_FAILED);
                }, 1100);

                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    navigateToResult(false, ERROR_WIFI_MISMATCH, "");
                }, 1800);

            } else {
                // Step 1 passes at 500ms
                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_PASSED);
                    updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_CHECKING);
                }, 500);

                // Step 2 passes at 1000ms
                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_PASSED);
                    updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_CHECKING);
                }, 1000);

                // Step 3 fails at 1500ms
                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_FAILED);
                }, 1500);

                handler.postDelayed(() -> {
                    if (!isAdded() || binding == null) return;
                    navigateToResult(false, err, "");
                }, 2200);
            }
        }
    }

    private void updateStepView(TextView tvState, ImageView ivStatus, int state) {
        switch (state) {
            case STEP_STATE_CHECKING:
                tvState.setText(R.string.verifying_state_checking);
                tvState.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary));
                ivStatus.setVisibility(View.GONE);
                break;
            case STEP_STATE_PASSED:
                tvState.setText(R.string.verifying_state_passed);
                tvState.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_success));
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.ic_check_circle);
                break;
            case STEP_STATE_FAILED:
                tvState.setText(R.string.verifying_state_failed);
                tvState.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_error));
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.ic_error_circle);
                break;
            case STEP_STATE_WAITING:
            default:
                tvState.setText(R.string.verifying_state_waiting);
                tvState.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
                ivStatus.setVisibility(View.GONE);
                break;
        }
    }

    private void navigateToResult(boolean isSuccess, String errorType, String resultJson) {
        Bundle bundle = createResultBundle(isSuccess, errorType, resultJson);
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_student_scan_verifying, true)
                .build();
        try {
            Navigation.findNavController(requireView()).navigate(R.id.nav_student_scan_result, bundle, navOptions);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Lỗi hiển thị kết quả", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static Bundle createResultBundle(boolean isSuccess, String errorType, String resultJson) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSuccess", isSuccess);
        bundle.putString("errorType", errorType != null ? errorType : "");
        bundle.putString("resultJson", resultJson != null ? resultJson : "");
        return bundle;
    }

    private String createDefaultSuccessJson() {
        AttendanceCheckInResponse fallback = new AttendanceCheckInResponse(
                "rec-" + System.currentTimeMillis(),
                sessionId,
                "NT118.Q11",
                "Phát triển ứng dụng trên thiết bị di động",
                "B1.12",
                "ThS. Trần Minh Khoa",
                "21:30 · 28/10/2026",
                deviceId,
                "PRESENT",
                true,
                true,
                true,
                5.2
        );
        return gson.toJson(fallback);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }
}
