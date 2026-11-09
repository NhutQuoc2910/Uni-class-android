package com.ppquang06.uniclassapp.ui.student.qr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ppquang06.uniclassapp.databinding.DialogSecurityCheckBottomSheetBinding;
import com.ppquang06.uniclassapp.ui.common.BaseBottomSheetDialogFragment;

import java.util.Locale;

public class SecurityCheckBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

    private static final String ARG_BSSID = "arg_bssid";
    private static final String ARG_LAT = "arg_lat";
    private static final String ARG_LNG = "arg_lng";
    private static final String ARG_DEVICE_ID = "arg_device_id";

    private DialogSecurityCheckBottomSheetBinding binding;

    public static SecurityCheckBottomSheetDialogFragment newInstance(
            String bssid,
            double latitude,
            double longitude,
            String deviceId
    ) {
        SecurityCheckBottomSheetDialogFragment fragment = new SecurityCheckBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BSSID, bssid);
        args.putDouble(ARG_LAT, latitude);
        args.putDouble(ARG_LNG, longitude);
        args.putString(ARG_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSecurityCheckBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String bssid = args != null ? args.getString(ARG_BSSID, "00:11:22:33:44:55") : "00:11:22:33:44:55";
        double lat = args != null ? args.getDouble(ARG_LAT, 10.8700) : 10.8700;
        double lng = args != null ? args.getDouble(ARG_LNG, 106.8031) : 106.8031;
        String deviceId = args != null ? args.getString(ARG_DEVICE_ID, "android-student-device-01") : "android-student-device-01";

        binding.tvSheetWifiDetail.setText("BSSID hiện tại: " + bssid + " · Sẵn sàng đối chiếu với trạm phát của lớp học");
        binding.tvSheetGpsDetail.setText(String.format(
                Locale.US,
                "Tọa độ hiện tại: (%.4f, %.4f) · Bán kính kiểm tra 50m quanh phòng học",
                lat,
                lng
        ));
        binding.tvSheetDeviceDetail.setText("Hardware ID: " + deviceId + " · Chống điểm danh hộ trên cùng thiết bị");

        binding.btnCloseSecuritySheet.setOnClickListener(v -> dismiss());
        binding.btnConfirmSecuritySheet.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
