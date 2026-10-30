package com.ppquang06.uniclassapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/**
 * Helper utility to acquire real hardware telemetry:
 * 1. WiFi BSSID (access point MAC)
 * 2. Hardware Device ID (Settings.Secure.ANDROID_ID)
 * 3. Real High-Accuracy GPS coordinates (FusedLocationProviderClient)
 * with robust fallbacks for emulators, denied permissions, or missing sensors.
 */
public final class HardwareSensorAttendanceHelper {

    private static final String TAG = "HardwareSensorHelper";
    public static final String DEFAULT_FALLBACK_BSSID = "00:11:22:33:44:55";
    public static final double DEFAULT_FALLBACK_LAT = 10.8700;
    public static final double DEFAULT_FALLBACK_LNG = 106.8031;

    public interface OnLocationResultListener {
        void onLocationResult(double latitude, double longitude);
    }

    private HardwareSensorAttendanceHelper() {}

    /**
     * Reads the real hardware BSSID of the currently connected WiFi network.
     * Falls back to "00:11:22:33:44:55" if permission is not granted, WiFi is disabled,
     * or running on an emulator / dummy connection.
     */
    @SuppressLint("HardwareIds")
    public static String getBssid(Context context) {
        if (context == null) {
            return DEFAULT_FALLBACK_BSSID;
        }

        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null) {
                    String bssid = info.getBSSID();
                    if (!TextUtils.isEmpty(bssid)
                            && !"02:00:00:00:00:00".equalsIgnoreCase(bssid)
                            && !"00:00:00:00:00:00".equalsIgnoreCase(bssid)) {
                        return bssid;
                    }
                }
            }
        } catch (SecurityException se) {
            Log.w(TAG, "ACCESS_WIFI_STATE or location permission not granted for BSSID: " + se.getMessage());
        } catch (Exception e) {
            Log.w(TAG, "Failed to read WiFi BSSID: " + e.getMessage());
        }

        return DEFAULT_FALLBACK_BSSID;
    }

    /**
     * Reads unique hardware device ID (ANDROID_ID).
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
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
     * Gracefully falls back to (10.8700, 106.8031) if permissions are missing or GPS is unavailable.
     */
    public static void getCurrentLocation(Context context, @NonNull OnLocationResultListener listener) {
        if (context == null) {
            listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG);
            return;
        }

        boolean fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            Log.w(TAG, "Location permissions not granted, using fallback coordinates");
            listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG);
            return;
        }

        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
            CancellationTokenSource cts = new CancellationTokenSource();

            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            listener.onLocationResult(location.getLatitude(), location.getLongitude());
                        } else {
                            // Fallback to last known location
                            try {
                                client.getLastLocation()
                                        .addOnSuccessListener(lastLoc -> {
                                            if (lastLoc != null) {
                                                listener.onLocationResult(lastLoc.getLatitude(), lastLoc.getLongitude());
                                            } else {
                                                listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG);
                                            }
                                        })
                                        .addOnFailureListener(e -> listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG));
                            } catch (SecurityException se) {
                                listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "FusedLocationProviderClient error: " + e.getMessage());
                        listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG);
                    });
        } catch (SecurityException se) {
            Log.w(TAG, "SecurityException getting location: " + se.getMessage());
            listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG);
        } catch (Exception e) {
            Log.w(TAG, "Exception getting location: " + e.getMessage());
            listener.onLocationResult(DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LNG);
        }
    }
}
