package com.ppquang06.uniclassapp.ui.student.qr;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.ppquang06.uniclassapp.utils.HardwareSensorAttendanceHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentScanVerifyingFragment extends Fragment {

    public static final String ERROR_QR_EXPIRED = "QR_EXPIRED";
    public static final String ERROR_WIFI_MISMATCH = "WIFI_MISMATCH";
    public static final String ERROR_GPS_OUT_OF_RANGE = "GPS_OUT_OF_RANGE";
    public static final String ERROR_DEVICE_ALREADY_USED = "DEVICE_ALREADY_USED";
    public static final String ERROR_NETWORK = "NETWORK_ERROR";

    public static final int STEP_STATE_WAITING = 0;
    public static final int STEP_STATE_CHECKING = 1;
    public static final int STEP_STATE_PASSED = 2;
    public static final int STEP_STATE_FAILED = 3;

    private FragmentStudentScanVerifyingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String sessionId;
    private String qrPayload;
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
        startSequentialHardwareVerification();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            sessionId = args.getString(StudentScanQrFragment.ARG_SESSION_ID, "");
            qrPayload = args.getString("qrPayload", "");
            wifiBssid = args.getString(StudentScanQrFragment.ARG_WIFI_BSSID, "");
            latitudeStr = args.getString(StudentScanQrFragment.ARG_LATITUDE, "");
            longitudeStr = args.getString(StudentScanQrFragment.ARG_LONGITUDE, "");
            deviceId = args.getString(StudentScanQrFragment.ARG_DEVICE_ID, "");
            simulationMode = args.getString(StudentScanQrFragment.ARG_SIMULATION_MODE, "");

            if (TextUtils.isEmpty(sessionId) && !TextUtils.isEmpty(qrPayload)) {
                String[] parts = qrPayload.split(":");
                if (parts.length >= 3) {
                    sessionId = parts[2];
                } else {
                    sessionId = qrPayload;
                }
            }
        }
    }

    private void startSequentialHardwareVerification() {
        // Step 1: Check QR Session Payload
        updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_CHECKING);
        updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_WAITING);
        updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_WAITING);

        if (TextUtils.isEmpty(sessionId) || StudentScanQrFragment.SIM_MODE_QR_EXPIRED.equals(simulationMode)) {
            applyErrorToStepViews(ERROR_QR_EXPIRED);
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                navigateToResult(false, ERROR_QR_EXPIRED, "");
            }, 300);
            return;
        }

        updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_PASSED);

        // Step 2: Acquire WiFi BSSID
        updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_CHECKING);
        if (TextUtils.isEmpty(wifiBssid)) {
            wifiBssid = HardwareSensorAttendanceHelper.getBssid(requireContext());
        }

        if (StudentScanQrFragment.SIM_MODE_WIFI_MISMATCH.equals(simulationMode)) {
            applyErrorToStepViews(ERROR_WIFI_MISMATCH);
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                navigateToResult(false, ERROR_WIFI_MISMATCH, "");
            }, 300);
            return;
        }

        updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_PASSED);

        // Step 3: Acquire Hardware Device ID & Real GPS
        updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_CHECKING);
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = HardwareSensorAttendanceHelper.getDeviceId(requireContext());
        }

        if (StudentScanQrFragment.SIM_MODE_GPS_OUT_OF_RANGE.equals(simulationMode)) {
            applyErrorToStepViews(ERROR_GPS_OUT_OF_RANGE);
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                navigateToResult(false, ERROR_GPS_OUT_OF_RANGE, "");
            }, 300);
            return;
        }

        if (StudentScanQrFragment.SIM_MODE_DEVICE_ALREADY_USED.equals(simulationMode)) {
            applyErrorToStepViews(ERROR_DEVICE_ALREADY_USED);
            handler.postDelayed(() -> {
                if (!isAdded() || binding == null) return;
                navigateToResult(false, ERROR_DEVICE_ALREADY_USED, "");
            }, 300);
            return;
        }

        Double parsedLat = null;
        Double parsedLng = null;
        try {
            if (!TextUtils.isEmpty(latitudeStr)) parsedLat = Double.parseDouble(latitudeStr);
            if (!TextUtils.isEmpty(longitudeStr)) parsedLng = Double.parseDouble(longitudeStr);
        } catch (Exception ignored) {}

        if (parsedLat != null && parsedLng != null) {
            callCheckInApi(sessionId, wifiBssid, parsedLat, parsedLng, deviceId);
        } else {
            HardwareSensorAttendanceHelper.getCurrentLocation(requireContext(), (lat, lng) -> {
                if (!isAdded() || binding == null) return;
                callCheckInApi(sessionId, wifiBssid, lat, lng, deviceId);
            });
        }
    }

    private void callCheckInApi(String sId, String bssid, double lat, double lng, String devId) {
        if (!isAdded() || binding == null || getContext() == null) return;

        AttendanceCheckInRequest request = new AttendanceCheckInRequest(
                sId,
                bssid,
                lat,
                lng,
                devId
        );

        ApiClient.getUserClassApiService(requireContext()).checkInAttendance(request)
                .enqueue(new Callback<ApiResponse<AttendanceCheckInResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AttendanceCheckInResponse>> call,
                                           @NonNull Response<ApiResponse<AttendanceCheckInResponse>> response) {
                        if (!isAdded() || binding == null) return;

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            AttendanceCheckInResponse data = response.body().getData();
                            updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_PASSED);
                            handler.postDelayed(() -> {
                                if (!isAdded() || binding == null) return;
                                navigateToResult(true, "", gson.toJson(data));
                            }, 300);
                        } else {
                            String classifiedError = resolveErrorFromResponse(response);
                            applyErrorToStepViews(classifiedError);
                            handler.postDelayed(() -> {
                                if (!isAdded() || binding == null) return;
                                navigateToResult(false, classifiedError, "");
                            }, 300);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AttendanceCheckInResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        applyErrorToStepViews(ERROR_NETWORK);
                        handler.postDelayed(() -> {
                            if (!isAdded() || binding == null) return;
                            navigateToResult(false, ERROR_NETWORK, "");
                        }, 300);
                    }
                });
    }

    private void applyErrorToStepViews(String errorType) {
        if (binding == null) return;
        if (ERROR_QR_EXPIRED.equals(errorType) || ERROR_NETWORK.equals(errorType)) {
            updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_FAILED);
            updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_WAITING);
            updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_WAITING);
        } else if (ERROR_WIFI_MISMATCH.equals(errorType)) {
            updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_PASSED);
            updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_FAILED);
            updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_WAITING);
        } else {
            // ERROR_GPS_OUT_OF_RANGE or ERROR_DEVICE_ALREADY_USED
            updateStepView(binding.tvStep1State, binding.ivStep1Status, STEP_STATE_PASSED);
            updateStepView(binding.tvStep2State, binding.ivStep2Status, STEP_STATE_PASSED);
            updateStepView(binding.tvStep3State, binding.ivStep3Status, STEP_STATE_FAILED);
        }
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
        if ("NETWORK_ERROR".equalsIgnoreCase(apiErrorCode)) {
            return ERROR_NETWORK;
        }
        return ERROR_QR_EXPIRED;
    }

    private void updateStepView(TextView tvState, ImageView ivStatus, int state) {
        if (getContext() == null || tvState == null || ivStatus == null) return;
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
        if (getView() == null || getContext() == null) return;
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

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }
}
