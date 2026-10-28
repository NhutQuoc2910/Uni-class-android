package com.ppquang06.uniclassapp.ui.student.qr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.databinding.FragmentStudentScanQrBinding;

public class StudentScanQrFragment extends Fragment {

    private FragmentStudentScanQrBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentScanQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        setupEmptyState();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_qr);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_student);
    }

    private void setupEmptyState() {
        binding.emptyScanQr.tvEmptyTitle.setText(R.string.scan_qr_title);
        binding.emptyScanQr.tvEmptyMessage.setText(R.string.scan_qr_desc);
        binding.emptyScanQr.ivEmptyIcon.setImageResource(R.drawable.ic_qr_code);
        binding.emptyScanQr.btnEmptyAction.setVisibility(View.VISIBLE);
        binding.emptyScanQr.btnEmptyAction.setText(R.string.scan_qr_btn_action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
