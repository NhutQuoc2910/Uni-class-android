package com.ppquang06.uniclassapp.ui.student.qr;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentScanResultBinding;
import com.ppquang06.uniclassapp.ui.student.StudentMainActivity;

public class StudentScanResultFragment extends Fragment {

    public static final String ARG_IS_SUCCESS = "isSuccess";
    public static final String ARG_ERROR_TYPE = "errorType";
    public static final String ARG_RESULT_JSON = "resultJson";

    public static class ErrorConfig {
        public final int titleRes;
        public final int subtitleRes;
        public final int adviceRes;
        public final int iconRes;
        public final int iconTintRes;
        public final int heroBgRes;
        public final int step1State;
        public final int step2State;
        public final int step3State;

        public ErrorConfig(int titleRes, int subtitleRes, int adviceRes, int iconRes,
                           int iconTintRes, int heroBgRes, int step1State, int step2State, int step3State) {
            this.titleRes = titleRes;
            this.subtitleRes = subtitleRes;
            this.adviceRes = adviceRes;
            this.iconRes = iconRes;
            this.iconTintRes = iconTintRes;
            this.heroBgRes = heroBgRes;
            this.step1State = step1State;
            this.step2State = step2State;
            this.step3State = step3State;
        }
    }

    private FragmentStudentScanResultBinding binding;
    private boolean isSuccess = false;
    private String errorType = "";
    private String resultJson = "";

    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentScanResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupView();
        setupButtons();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            isSuccess = args.getBoolean(ARG_IS_SUCCESS, false);
            errorType = args.getString(ARG_ERROR_TYPE, "");
            resultJson = args.getString(ARG_RESULT_JSON, "");
        }
    }

    private void setupView() {
        if (isSuccess) {
            binding.layoutSuccess.setVisibility(View.VISIBLE);
            binding.layoutError.setVisibility(View.GONE);
            displaySuccessDetails();
        } else {
            binding.layoutSuccess.setVisibility(View.GONE);
            binding.layoutError.setVisibility(View.VISIBLE);
            displayErrorDetails();
        }
    }

    private void displaySuccessDetails() {
        if (resultJson != null && !resultJson.trim().isEmpty()) {
            try {
                AttendanceCheckInResponse data = gson.fromJson(resultJson, AttendanceCheckInResponse.class);
                if (data != null) {
                    if (data.getClassName() != null && !data.getClassName().isEmpty()) {
                        String courseText = (data.getClassCode() != null ? data.getClassCode() + " · " : "") + data.getClassName();
                        binding.tvDetailCourseValue.setText(courseText);
                    }
                    if (data.getRoom() != null && !data.getRoom().isEmpty()) {
                        binding.tvDetailRoomValue.setText(data.getRoom());
                    }
                    if (data.getLecturerName() != null && !data.getLecturerName().isEmpty()) {
                        binding.tvDetailLecturerValue.setText(data.getLecturerName());
                    }
                    if (data.getScannedAt() != null && !data.getScannedAt().isEmpty()) {
                        binding.tvDetailTimeValue.setText(data.getScannedAt());
                    }
                    if (data.getDeviceId() != null && !data.getDeviceId().isEmpty()) {
                        binding.tvDetailDeviceValue.setText(getString(R.string.scan_device_default) + " (" + data.getDeviceId() + ")");
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void displayErrorDetails() {
        ErrorConfig config = resolveErrorConfig(errorType);

        binding.tvErrorTitle.setText(config.titleRes);
        binding.tvErrorSubtitle.setText(config.subtitleRes);
        binding.tvAdviceMessage.setText(config.adviceRes);

        binding.frameErrorHero.setBackgroundResource(config.heroBgRes);
        binding.ivErrorHero.setImageResource(config.iconRes);
        binding.ivErrorHero.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), config.iconTintRes)));

        bindStepState(binding.tvErrStep1State, binding.ivErrStep1Status, config.step1State);
        bindStepState(binding.tvErrStep2State, binding.ivErrStep2Status, config.step2State);
        bindStepState(binding.tvErrStep3State, binding.ivErrStep3Status, config.step3State);
    }

    public static ErrorConfig resolveErrorConfig(String errorType) {
        if (StudentScanVerifyingFragment.ERROR_WIFI_MISMATCH.equals(errorType)) {
            return new ErrorConfig(
                    R.string.error_wifi_mismatch_title,
                    R.string.error_wifi_mismatch_desc,
                    R.string.error_wifi_mismatch_advice,
                    R.drawable.ic_wifi,
                    R.color.color_warning,
                    R.drawable.bg_circle_amber_light,
                    StudentScanVerifyingFragment.STEP_STATE_PASSED,
                    StudentScanVerifyingFragment.STEP_STATE_FAILED,
                    StudentScanVerifyingFragment.STEP_STATE_WAITING
            );
        } else if (StudentScanVerifyingFragment.ERROR_GPS_OUT_OF_RANGE.equals(errorType)) {
            return new ErrorConfig(
                    R.string.error_gps_out_title,
                    R.string.error_gps_out_desc,
                    R.string.error_gps_out_advice,
                    R.drawable.ic_gps,
                    R.color.color_error,
                    R.drawable.bg_circle_red_light,
                    StudentScanVerifyingFragment.STEP_STATE_PASSED,
                    StudentScanVerifyingFragment.STEP_STATE_PASSED,
                    StudentScanVerifyingFragment.STEP_STATE_FAILED
            );
        } else if (StudentScanVerifyingFragment.ERROR_DEVICE_ALREADY_USED.equals(errorType)) {
            return new ErrorConfig(
                    R.string.error_device_used_title,
                    R.string.error_device_used_desc,
                    R.string.error_device_used_advice,
                    R.drawable.ic_smartphone,
                    R.color.color_error,
                    R.drawable.bg_circle_red_light,
                    StudentScanVerifyingFragment.STEP_STATE_PASSED,
                    StudentScanVerifyingFragment.STEP_STATE_PASSED,
                    StudentScanVerifyingFragment.STEP_STATE_FAILED
            );
        } else {
            // Default QR_EXPIRED
            return new ErrorConfig(
                    R.string.error_qr_expired_title,
                    R.string.error_qr_expired_desc,
                    R.string.error_qr_expired_advice,
                    R.drawable.ic_clock,
                    R.color.color_error,
                    R.drawable.bg_circle_red_light,
                    StudentScanVerifyingFragment.STEP_STATE_FAILED,
                    StudentScanVerifyingFragment.STEP_STATE_WAITING,
                    StudentScanVerifyingFragment.STEP_STATE_WAITING
            );
        }
    }

    private void bindStepState(TextView tvState, ImageView ivStatus, int state) {
        if (state == StudentScanVerifyingFragment.STEP_STATE_PASSED) {
            tvState.setText(R.string.verifying_state_passed);
            tvState.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_success));
            ivStatus.setVisibility(View.VISIBLE);
            ivStatus.setImageResource(R.drawable.ic_check_circle);
        } else if (state == StudentScanVerifyingFragment.STEP_STATE_FAILED) {
            tvState.setText(R.string.verifying_state_failed);
            tvState.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_error));
            ivStatus.setVisibility(View.VISIBLE);
            ivStatus.setImageResource(R.drawable.ic_error_circle);
        } else {
            tvState.setText(R.string.verifying_state_waiting);
            tvState.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            ivStatus.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        View.OnClickListener goHomeListener = v -> {
            try {
                Navigation.findNavController(v).popBackStack(R.id.nav_student_scan_qr, true);
            } catch (Exception ignored) {}
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_home);
            } else {
                try {
                    boolean popped = Navigation.findNavController(v).popBackStack(R.id.nav_student_home, false);
                    if (!popped) {
                        Navigation.findNavController(v).navigate(R.id.nav_student_home);
                    }
                } catch (Exception ignored) {}
            }
        };

        binding.btnSuccessHome.setOnClickListener(goHomeListener);
        binding.btnErrorHome.setOnClickListener(goHomeListener);

        binding.btnErrorRescan.setOnClickListener(v -> {
            try {
                boolean popped = Navigation.findNavController(v).popBackStack(R.id.nav_student_scan_qr, false);
                if (!popped) {
                    Navigation.findNavController(v).navigate(R.id.nav_student_scan_qr);
                }
            } catch (Exception ignored) {
                try {
                    Navigation.findNavController(v).navigate(R.id.nav_student_scan_qr);
                } catch (Exception ignored2) {}
            }
        });

        View.OnClickListener backListener = v -> {
            try {
                if (isSuccess) {
                    goHomeListener.onClick(v);
                } else {
                    boolean popped = Navigation.findNavController(v).popBackStack(R.id.nav_student_scan_qr, false);
                    if (!popped) {
                        goHomeListener.onClick(v);
                    }
                }
            } catch (Exception e) {
                goHomeListener.onClick(v);
            }
        };

        if (binding.ivBack != null) {
            binding.ivBack.setOnClickListener(backListener);
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backListener.onClick(binding.getRoot());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
