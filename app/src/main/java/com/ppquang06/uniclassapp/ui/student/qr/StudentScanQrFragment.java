package com.ppquang06.uniclassapp.ui.student.qr;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.databinding.FragmentStudentScanQrBinding;

public class StudentScanQrFragment extends Fragment {

    public static final String ARG_SESSION_ID = "sessionId";
    public static final String ARG_WIFI_BSSID = "wifiBssid";
    public static final String ARG_LATITUDE = "latitude";
    public static final String ARG_LONGITUDE = "longitude";
    public static final String ARG_DEVICE_ID = "deviceId";
    public static final String ARG_SIMULATION_MODE = "simulationMode";

    public static final String SIM_MODE_QR_EXPIRED = "QR_EXPIRED";
    public static final String SIM_MODE_WIFI_MISMATCH = "WIFI_MISMATCH";
    public static final String SIM_MODE_GPS_OUT_OF_RANGE = "GPS_OUT_OF_RANGE";
    public static final String SIM_MODE_DEVICE_ALREADY_USED = "DEVICE_ALREADY_USED";

    private FragmentStudentScanQrBinding binding;
    private ObjectAnimator scanLineAnimator;
    private boolean isFlashOn = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentScanQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupFlashToggle();
        setupScanLineAnimation();
        setupQuickScan();
        setupSimulationMenu();
    }

    private void setupFlashToggle() {
        binding.btnFlash.setOnClickListener(v -> {
            isFlashOn = !isFlashOn;
            if (isFlashOn) {
                binding.btnFlash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_warning)));
                binding.btnFlash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_warning_container)));
                Toast.makeText(requireContext(), "Flash bật", Toast.LENGTH_SHORT).show();
            } else {
                binding.btnFlash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_primary)));
                binding.btnFlash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_surface_variant)));
                Toast.makeText(requireContext(), "Flash tắt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupScanLineAnimation() {
        binding.viewScanLine.post(() -> {
            if (binding == null) return;
            float endY = binding.frameViewfinder.getHeight() - binding.viewScanLine.getHeight();
            scanLineAnimator = ObjectAnimator.ofFloat(binding.viewScanLine, "translationY", 0f, endY);
            scanLineAnimator.setDuration(2000);
            scanLineAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scanLineAnimator.setRepeatMode(ValueAnimator.REVERSE);
            scanLineAnimator.start();
        });
    }

    private void setupQuickScan() {
        binding.btnQuickScan.setOnClickListener(v -> {
            // Valid seeded session NT118.Q11
            navigateVerifying(
                    "DEMO_OPEN",
                    "00:11:22:33:44:55",
                    "10.8700",
                    "106.8031",
                    "android-student-device-01",
                    ""
            );
        });
    }

    private void setupSimulationMenu() {
        binding.btnSimulateErrors.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 1, 0, getString(R.string.scan_qr_sim_qr_expired));
            popup.getMenu().add(0, 2, 1, getString(R.string.scan_qr_sim_wifi_mismatch));
            popup.getMenu().add(0, 3, 2, getString(R.string.scan_qr_sim_gps_out));
            popup.getMenu().add(0, 4, 3, getString(R.string.scan_qr_sim_device_used));

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        navigateVerifying("DEMO_EXPIRED", "00:11:22:33:44:55", "10.8700", "106.8031", "android-student-device-01", SIM_MODE_QR_EXPIRED);
                        return true;
                    case 2:
                        navigateVerifying("DEMO_OPEN", "99:99:99:99:99:99", "10.8700", "106.8031", "android-student-device-01", SIM_MODE_WIFI_MISMATCH);
                        return true;
                    case 3:
                        navigateVerifying("DEMO_OPEN", "00:11:22:33:44:55", "10.0000", "105.0000", "android-student-device-01", SIM_MODE_GPS_OUT_OF_RANGE);
                        return true;
                    case 4:
                        navigateVerifying("DEMO_OPEN", "00:11:22:33:44:55", "10.8700", "106.8031", "PROXIED_DEVICE_ID", SIM_MODE_DEVICE_ALREADY_USED);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });
    }

    private void navigateVerifying(String sessionId, String wifiBssid, String latitude,
                                  String longitude, String deviceId, String simulationMode) {
        Bundle bundle = createVerifyingBundle(sessionId, wifiBssid, latitude, longitude, deviceId, simulationMode);
        try {
            Navigation.findNavController(requireView()).navigate(R.id.nav_student_scan_verifying, bundle);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi chuyển màn hình xác thực", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bundle createVerifyingBundle(String sessionId, String wifiBssid, String latitude,
                                               String longitude, String deviceId, String simulationMode) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SESSION_ID, sessionId);
        bundle.putString(ARG_WIFI_BSSID, wifiBssid);
        bundle.putString(ARG_LATITUDE, latitude);
        bundle.putString(ARG_LONGITUDE, longitude);
        bundle.putString(ARG_DEVICE_ID, deviceId);
        bundle.putString(ARG_SIMULATION_MODE, simulationMode);
        return bundle;
    }

    @Override
    public void onDestroyView() {
        if (scanLineAnimator != null) {
            scanLineAnimator.cancel();
            scanLineAnimator = null;
        }
        super.onDestroyView();
        binding = null;
    }
}
