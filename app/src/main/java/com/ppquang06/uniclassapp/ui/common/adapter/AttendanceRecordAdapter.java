package com.ppquang06.uniclassapp.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.ItemAttendanceRowBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

import java.util.ArrayList;
import java.util.List;

public class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordAdapter.AttendanceViewHolder> {

    private final List<ClassDetailResponse.AttendanceSessionRecordItem> items = new ArrayList<>();

    public AttendanceRecordAdapter() {}

    public void submitList(List<ClassDetailResponse.AttendanceSessionRecordItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setItems(List<ClassDetailResponse.AttendanceSessionRecordItem> newItems) {
        submitList(newItems);
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceRowBinding binding = ItemAttendanceRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AttendanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        ClassDetailResponse.AttendanceSessionRecordItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceRowBinding binding;

        AttendanceViewHolder(ItemAttendanceRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassDetailResponse.AttendanceSessionRecordItem item) {
            binding.tvSessionTitle.setText("Buổi " + item.getSessionNumber());

            String dateText = UiBadgeHelper.formatIsoDateShort(item.getDate());
            String scannedAt = item.getScannedAt() != null ? item.getScannedAt().trim() : "";
            if (!scannedAt.isEmpty()) {
                String formattedScannedAt = UiBadgeHelper.formatIsoDateShort(scannedAt);
                if (!dateText.isEmpty()) {
                    dateText = dateText + " · Quét lúc " + formattedScannedAt;
                } else {
                    dateText = "Quét lúc " + formattedScannedAt;
                }
            }
            binding.tvSessionDate.setText(dateText);

            if ("PRESENT".equalsIgnoreCase(item.getStatus())) {
                UiBadgeHelper.applyBadge(
                        binding.tvAttendanceStatusBadge,
                        UiBadgeHelper.Status.PRESENT,
                        "Có mặt"
                );
            } else {
                UiBadgeHelper.applyBadge(
                        binding.tvAttendanceStatusBadge,
                        UiBadgeHelper.Status.ABSENT,
                        "Vắng"
                );
            }
        }
    }
}
