package com.ppquang06.uniclassapp.ui.student.qr;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentScanQrBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper;
import com.ppquang06.uniclassapp.utils.HardwareSensorAttendanceHelper;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class StudentScanQrFragment extends Fragment {

    private static final String TAG = "StudentScanQrFragment";

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
    private HomeViewModel homeViewModel;
    private ObjectAnimator scanLineAnimator;
    private boolean isFlashOn = false;
    private int currentZoomLevel = 1;

    private String currentBssid = HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_BSSID;
    private double currentLat = HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LAT;
    private double currentLng = HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LNG;
    private String currentDeviceId = "android-student-device-01";

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private BarcodeScanner barcodeScanner;
    private final AtomicBoolean isScanned = new AtomicBoolean(false);

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else if (getContext() != null) {
                    Toast.makeText(requireContext(), "Cần cấp quyền camera để quét mã QR điểm danh", Toast.LENGTH_SHORT).show();
                }
            });

    public static float resolveZoomRatio(int zoomLevel) {
        if (zoomLevel == 2) return 2.0f;
        if (zoomLevel == 3) return 3.0f;
        return 1.0f;
    }

    public static String formatShortBssid(String bssid) {
        if (bssid == null || bssid.trim().isEmpty()) {
            return "WiFi · Sẵn sàng";
        }
        String clean = bssid.trim();
        if (clean.length() >= 5) {
            return "WiFi · " + clean.substring(clean.length() - 5);
        }
        return "WiFi · " + clean;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentScanQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupTopBar();
        setupFlashToggle();
        setupZoomControls();
        setupScanLineAnimation();
        setupSecurityBar();
        observeLiveSessions();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isScanned.set(false);
        refreshSecuritySensors();
        if (homeViewModel != null) {
            homeViewModel.loadNotifications();
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraProvider == null) {
                startCamera();
            } else {
                bindCameraUseCases();
            }
        }
    }

    private void observeLiveSessions() {
        homeViewModel.getNotificationsState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            if (state.isSuccess() && state.getData() != null) {
                NotificationItemResponse active = NotificationFilterHelper.findActiveQrNotification(state.getData());
                if (active != null) {
                    binding.cardQrLiveBanner.setVisibility(View.VISIBLE);
                    String courseText = (active.getCourseCode() != null && !active.getCourseCode().trim().isEmpty())
                            ? active.getCourseCode() + " · " + active.getTitle()
                            : active.getTitle();
                    binding.tvQrBannerCourse.setText(courseText);
                    binding.cardQrLiveBanner.setOnClickListener(v -> {
                        String sessionId = active.getId().startsWith("att-") ? active.getId().substring(4) : active.getId();
                        navigateVerifying(
                                sessionId,
                                currentBssid,
                                String.valueOf(currentLat),
                                String.valueOf(currentLng),
                                currentDeviceId
                        );
                    });
                } else {
                    binding.cardQrLiveBanner.setVisibility(View.GONE);
                }
            }
        });
        homeViewModel.loadNotifications();
    }

    private void setupZoomControls() {
        binding.btnZoom1x.setOnClickListener(v -> setZoomLevel(1));
        binding.btnZoom2x.setOnClickListener(v -> setZoomLevel(2));
        binding.btnZoom3x.setOnClickListener(v -> setZoomLevel(3));
    }

    private void setZoomLevel(int level) {
        currentZoomLevel = level;
        if (camera != null) {
            try {
                camera.getCameraControl().setZoomRatio(resolveZoomRatio(level));
            } catch (Exception e) {
                Log.w(TAG, "Failed to set zoom ratio", e);
            }
        }
        updateZoomButtonsUi(level);
    }

    private void updateZoomButtonsUi(int level) {
        if (binding == null || getContext() == null) return;
        binding.btnZoom1x.setBackgroundResource(level == 1 ? R.drawable.bg_zoom_pill_active : R.drawable.bg_zoom_pill_idle);
        binding.btnZoom1x.setTextColor(ContextCompat.getColor(requireContext(), level == 1 ? R.color.p1_midnight : R.color.p1_silver));

        binding.btnZoom2x.setBackgroundResource(level == 2 ? R.drawable.bg_zoom_pill_active : R.drawable.bg_zoom_pill_idle);
        binding.btnZoom2x.setTextColor(ContextCompat.getColor(requireContext(), level == 2 ? R.color.p1_midnight : R.color.p1_silver));

        binding.btnZoom3x.setBackgroundResource(level == 3 ? R.drawable.bg_zoom_pill_active : R.drawable.bg_zoom_pill_idle);
        binding.btnZoom3x.setTextColor(ContextCompat.getColor(requireContext(), level == 3 ? R.color.p1_midnight : R.color.p1_silver));
    }

    private void setupSecurityBar() {
        refreshSecuritySensors();

        View.OnClickListener openSecuritySheet = v -> {
            SecurityCheckBottomSheetDialogFragment sheet = SecurityCheckBottomSheetDialogFragment.newInstance(
                    currentBssid,
                    currentLat,
                    currentLng,
                    currentDeviceId
            );
            sheet.show(getChildFragmentManager(), "SecurityCheckSheet");
        };

        binding.cardSecWifi.setOnClickListener(openSecuritySheet);
        binding.cardSecGps.setOnClickListener(openSecuritySheet);
        binding.cardSecDevice.setOnClickListener(openSecuritySheet);
        binding.btnRecheckSecurity.setOnClickListener(v -> {
            refreshSecuritySensors();
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Đã cập nhật trạng thái cảm biến thiết bị", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshSecuritySensors() {
        if (getContext() == null || binding == null) return;
        currentBssid = HardwareSensorAttendanceHelper.getBssid(getContext());
        currentDeviceId = HardwareSensorAttendanceHelper.getDeviceId(getContext());

        binding.tvSecWifiText.setText(formatShortBssid(currentBssid));
        String shortDev = currentDeviceId.length() > 6 ? currentDeviceId.substring(0, 6) : currentDeviceId;
        binding.tvSecDeviceText.setText("Thiết bị · " + shortDev);

        HardwareSensorAttendanceHelper.getCurrentLocation(getContext(), (lat, lng) -> {
            currentLat = lat;
            currentLng = lng;
            if (binding != null && binding.tvSecGpsText != null) {
                binding.tvSecGpsText.setText(String.format(Locale.US, "GPS · %.2f, %.2f", lat, lng));
            }
        });
    }

    private void startCamera() {
        if (cameraExecutor == null || cameraExecutor.isShutdown()) {
            cameraExecutor = Executors.newSingleThreadExecutor();
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ProcessCameraProvider", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null || binding == null || !isAdded()) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        if (barcodeScanner == null) {
            barcodeScanner = BarcodeScanning.getClient(options);
        }

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            try {
                @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
                android.media.Image mediaImage = imageProxy.getImage();
                if (mediaImage != null && !isScanned.get()) {
                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    barcodeScanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {
                                    String rawValue = barcode.getRawValue();
                                    if (rawValue != null && rawValue.startsWith("UNICLASS:") && isScanned.compareAndSet(false, true)) {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() -> onBarcodeDetected(rawValue));
                                        }
                                        break;
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error processing barcode image", e))
                            .addOnCompleteListener(task -> imageProxy.close());
                } else {
                    imageProxy.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing image", e);
                imageProxy.close();
            }
        });

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
            if (currentZoomLevel != 1) {
                camera.getCameraControl().setZoomRatio(resolveZoomRatio(currentZoomLevel));
            }
            if (binding.ivQrCenter != null) {
                binding.ivQrCenter.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Camera use case binding failed", e);
        }
    }

    private void onBarcodeDetected(String rawValue) {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        String sessionId = "";
        if (rawValue != null) {
            String[] parts = rawValue.split(":");
            if (parts.length >= 3) {
                sessionId = parts[2];
            }
        }
        if (TextUtils.isEmpty(sessionId)) {
            sessionId = rawValue;
        }

        Bundle bundle = createVerifyingBundle(
                sessionId,
                currentBssid,
                String.valueOf(currentLat),
                String.valueOf(currentLng),
                currentDeviceId,
                ""
        );
        bundle.putString("qrPayload", rawValue);

        try {
            Navigation.findNavController(requireView()).navigate(R.id.nav_student_scan_verifying, bundle);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Lỗi chuyển màn hình xác thực", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupTopBar() {
        if (binding == null || binding.topAppBar == null) return;
        binding.topAppBar.tvTitle.setText(R.string.scan_qr_screen_title);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_student);
        binding.topAppBar.ivAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof com.ppquang06.uniclassapp.ui.student.StudentMainActivity) {
                ((com.ppquang06.uniclassapp.ui.student.StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_profile);
            }
        });
    }

    private void setupFlashToggle() {
        binding.btnFlash.setOnClickListener(v -> {
            isFlashOn = !isFlashOn;
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(isFlashOn);
            }
            if (isFlashOn) {
                binding.btnFlash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_warning)));
                binding.btnFlash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_warning_container)));
                Toast.makeText(requireContext(), "Flash bật", Toast.LENGTH_SHORT).show();
            } else {
                binding.btnFlash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.p2_lavender)));
                binding.btnFlash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.p1_midnight)));
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

    private void navigateVerifying(String sessionId, String wifiBssid, String latitude,
                                  String longitude, String deviceId) {
        Bundle bundle = createVerifyingBundle(sessionId, wifiBssid, latitude, longitude, deviceId, "");
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
        if (barcodeScanner != null) {
            barcodeScanner.close();
            barcodeScanner = null;
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        super.onDestroyView();
        binding = null;
    }
}
