package com.ppquang06.uniclassapp.ui.student.classes;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentClassTabListBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.NotificationAdapter;
import com.ppquang06.uniclassapp.utils.QrCodeGenerator;
import com.ppquang06.uniclassapp.utils.QrImageStorageHelper;

import java.util.Collections;
import java.util.List;

public class ClassAnnouncementsTabFragment extends Fragment {

    private FragmentClassTabListBinding binding;
    private NotificationAdapter adapter;
    private ClassDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClassTabListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupListeners();
        setupViewModel();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(item -> {
            if (getContext() != null && item != null) {
                Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnDownloadQrClickListener(this::handleDownloadQr);
        binding.rvTabItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTabItems.setAdapter(adapter);
        binding.tvEmptyMessage.setText(R.string.empty_announcements);
    }

    private void handleDownloadQr(NotificationItemResponse item) {
        if (item == null || item.getId() == null || getContext() == null) {
            return;
        }
        String sessionId = item.getId().startsWith("att-") ? item.getId().substring(4) : item.getId();
        String courseCode = (item.getCourseCode() != null && !item.getCourseCode().trim().isEmpty())
                ? item.getCourseCode().trim()
                : "CLASS";
        String payload = "UNICLASS:" + courseCode + ":" + sessionId + ":" + System.currentTimeMillis();
        Bitmap qrBitmap = QrCodeGenerator.generateQrBitmap(payload, 512);
        Uri savedUri = QrImageStorageHelper.saveQrBitmapToGallery(
                requireContext(),
                qrBitmap,
                "QR_" + (item.getCourseCode() != null ? item.getCourseCode() : "Attendance")
        );
        if (savedUri != null) {
            Toast.makeText(requireContext(), "Đã lưu ảnh mã QR vào Thư viện ảnh!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Không thể lưu ảnh QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (viewModel != null && getContext() != null) {
                viewModel.refresh(requireContext());
            } else {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireParentFragment()).get(ClassDetailViewModel.class);
        viewModel.getClassDetail().observe(getViewLifecycleOwner(), detail -> {
            if (detail != null) {
                List<ClassDetailResponse.AnnouncementItem> source = detail.getAnnouncements();
                List<NotificationItemResponse> items = NotificationAdapter.fromAnnouncements(source, detail.getCode());
                adapter.submitList(items);
                binding.tvEmptyMessage.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                adapter.submitList(Collections.emptyList());
                binding.tvEmptyMessage.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                binding.swipeRefresh.setRefreshing(loading);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
