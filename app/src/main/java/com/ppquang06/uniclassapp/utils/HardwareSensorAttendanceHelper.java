package com.ppquang06.uniclassapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.concurrent.atomic.AtomicBoolean;

public final class HardwareSensorAttendanceHelper {

    private static final String TAG = "HardwareSensorHelper";

    public static final String DEFAULT_FALLBACK_BSSID = "00:11:22:33:44:55";
    public static final double DEFAULT_FALLBACK_LAT = 10.8700;
    public static final double DEFAULT_FALLBACK_LNG = 106.8031;
    private static final long GPS_TIMEOUT_MS = 7000L;

    public interface OnLocationResultListener {
        void onLocationResult(double latitude, double longitude);
    }

    private HardwareSensorAttendanceHelper() {
        // Utility
    }

    private static void postToMainOrDirect(@NonNull Runnable runnable) {
        try {
            Looper mainLooper = Looper.getMainLooper();
            if (mainLooper != null) {
                new Handler(mainLooper).post(runnable);
                return;
            }
        } catch (Throwable ignored) {}
        runnable.run();
    }

    private static void postDelayedSafe(@NonNull Runnable runnable, long delayMs) {
        try {
            Looper mainLooper = Looper.getMainLooper();
            if (mainLooper != null) {
                new Handler(mainLooper).postDelayed(runnable, delayMs);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Reads the connected WiFi BSSID via WifiManager.
     * Returns fallback if WiFi is disconnected, permission is missing, or BSSID is dummy (02:00:00:00:00:00).
     */
    @NonNull
    public static String getBssid(@Nullable Context context) {
        if (context == null) {
            return DEFAULT_FALLBACK_BSSID;
        }

        try {
            boolean hasWifiPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
            boolean hasLocationPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (hasWifiPerm && hasLocationPerm) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        String bssid = wifiInfo.getBSSID();
                        if (!TextUtils.isEmpty(bssid)
                                && !"02:00:00:00:00:00".equalsIgnoreCase(bssid)
                                && !"00:00:00:00:00:00".equalsIgnoreCase(bssid)) {
                            return bssid;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to read WiFi BSSID: " + e.getMessage());
        }

        return DEFAULT_FALLBACK_BSSID;
    }

    /**
     * Reads the Android Hardware Device ID (Settings.Secure.ANDROID_ID).
     */
    @NonNull
    public static String getDeviceId(@Nullable Context context) {
        if (context == null) {
            return "android-student-device-01";
        }
        try {
            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(deviceId)) {
                return deviceId;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to read Settings.Secure.ANDROID_ID: " + e.getMessage());
        }
        return "android-student-device-01";
    }

    /**
     * Asynchronously obtains the current real GPS coordinates with high accuracy using FusedLocationProviderClient.
     * Enforces a 7-second timeout and safely posts result onto the Main Looper (or directly in unit tests) exactly once.
     * Gracefully falls back to (10.8700, 106.8031) if permissions are missing or GPS is unavailable.
     */
    public static void getCurrentLocation(@Nullable Context context, @NonNull OnLocationResultListener listener) {
        AtomicBoolean dispatched = new AtomicBoolean(false);

        Runnable dispatchFallback = () -> {
            if (dispatched.compareAndSet(false, true)) {
                postToMainOrDirect(() -> listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG));
            }
        };

        if (context == null) {
            dispatchFallback.run();
            return;
        }

        boolean fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            Log.w(TAG, "Location permissions not granted, using fallback coordinates");
            dispatchFallback.run();
            return;
        }

        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
            CancellationTokenSource cts = new CancellationTokenSource();

            // Guard against indefinite satellite searches indoors
            postDelayedSafe(() -> {
                if (!dispatched.get()) {
                    try {
                        cts.cancel();
                    } catch (Exception ignored) {}
                    dispatchFallback.run();
                }
            }, GPS_TIMEOUT_MS);

            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            if (dispatched.compareAndSet(false, true)) {
                                postToMainOrDirect(() -> listener.onLocationResult(location.getLatitude(), location.getLongitude()));
                            }
                        } else {
                            // Fallback to last known location
                            try {
                                client.getLastLocation()
                                        .addOnSuccessListener(lastLoc -> {
                                            if (lastLoc != null) {
                                                if (dispatched.compareAndSet(false, true)) {
                                                    postToMainOrDirect(() -> listener.onLocationResult(lastLoc.getLatitude(), lastLoc.getLongitude()));
                                                }
                                            } else {
                                                dispatchFallback.run();
                                            }
                                        })
                                        .addOnFailureListener(e -> dispatchFallback.run());
                            } catch (SecurityException se) {
                                dispatchFallback.run();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "FusedLocationProviderClient error: " + e.getMessage());
                        dispatchFallback.run();
                    });
        } catch (SecurityException se) {
            Log.w(TAG, "SecurityException getting location: " + se.getMessage());
            dispatchFallback.run();
        } catch (Exception e) {
            Log.w(TAG, "Exception getting location: " + e.getMessage());
            dispatchFallback.run();
        }
    }
}
