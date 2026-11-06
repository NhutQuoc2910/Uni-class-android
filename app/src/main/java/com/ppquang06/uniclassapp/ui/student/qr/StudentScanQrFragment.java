package com.ppquang06.uniclassapp.ui.student.qr;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.databinding.FragmentStudentScanQrBinding;
import com.ppquang06.uniclassapp.utils.QrCodeGenerator;

import java.io.InputStream;
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
    private ObjectAnimator scanLineAnimator;
    private boolean isFlashOn = false;

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

    private final ActivityResultLauncher<String> pickQrImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processPickedImageUri(uri);
                }
            });

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
        setupUploadQrButton();

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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraProvider == null) {
                startCamera();
            } else {
                bindCameraUseCases();
            }
        }
    }

    private void setupUploadQrButton() {
        if (binding != null && binding.btnUploadQrImage != null) {
            binding.btnUploadQrImage.setOnClickListener(v -> pickQrImageLauncher.launch("image/*"));
        }
    }

    private void processPickedImageUri(Uri uri) {
        if (uri == null || getContext() == null) {
            return;
        }

        try {
            Bitmap bitmap = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> decoder.setMutableRequired(true));
            } else {
                try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                }
            }

            String decodedPayload = null;
            if (bitmap != null) {
                decodedPayload = QrCodeGenerator.decodeQrBitmap(bitmap);
            }

            if (decodedPayload != null && decodedPayload.startsWith("UNICLASS:")) {
                if (isScanned.compareAndSet(false, true)) {
                    onBarcodeDetected(decodedPayload);
                }
                return;
            }

            // Fallback via ML Kit InputImage
            try {
                InputImage image = InputImage.fromFilePath(requireContext(), uri);
                if (barcodeScanner == null) {
                    BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build();
                    barcodeScanner = BarcodeScanning.getClient(options);
                }
                barcodeScanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            boolean found = false;
                            for (Barcode barcode : barcodes) {
                                String raw = barcode.getRawValue();
                                if (raw != null && raw.startsWith("UNICLASS:") && isScanned.compareAndSet(false, true)) {
                                    found = true;
                                    onBarcodeDetected(raw);
                                    break;
                                }
                            }
                            if (!found && getContext() != null) {
                                Toast.makeText(requireContext(), "Không tìm thấy mã QR hợp lệ trong ảnh đã chọn", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (getContext() != null) {
                                Toast.makeText(requireContext(), "Không tìm thấy mã QR hợp lệ trong ảnh đã chọn", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception mlEx) {
                Log.e(TAG, "ML Kit fallback error", mlEx);
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Không tìm thấy mã QR hợp lệ trong ảnh đã chọn", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decoding picked image URI", e);
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Không tìm thấy mã QR hợp lệ trong ảnh đã chọn", Toast.LENGTH_SHORT).show();
            }
        }
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

        Bundle bundle = new Bundle();
        bundle.putString(ARG_SESSION_ID, sessionId);
        bundle.putString("qrPayload", rawValue);
        bundle.putString(ARG_WIFI_BSSID, "");
        bundle.putString(ARG_LATITUDE, "");
        bundle.putString(ARG_LONGITUDE, "");
        bundle.putString(ARG_DEVICE_ID, "");
        bundle.putString(ARG_SIMULATION_MODE, "");

        try {
            Navigation.findNavController(requireView()).navigate(R.id.nav_student_scan_verifying, bundle);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Lỗi chuyển màn hình xác thực", Toast.LENGTH_SHORT).show();
            }
        }
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
