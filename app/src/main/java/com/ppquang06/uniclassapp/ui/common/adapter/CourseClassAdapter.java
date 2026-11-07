package com.ppquang06.uniclassapp.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.ItemCourseCardBinding;

import java.util.ArrayList;
import java.util.List;

public class CourseClassAdapter extends RecyclerView.Adapter<CourseClassAdapter.CourseViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(CourseClassItem item);
    }

    private final List<CourseClassItem> items = new ArrayList<>();
    private OnCourseClickListener listener;

    public CourseClassAdapter() {}

    public CourseClassAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void setOnCourseClickListener(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CourseClassItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setItems(List<CourseClassItem> newItems) {
        submitList(newItems);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCourseCardBinding binding = ItemCourseCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CourseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseClassItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ItemCourseCardBinding binding;

        CourseViewHolder(ItemCourseCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CourseClassItem item, OnCourseClickListener listener) {
            String code = item.getCode() != null ? item.getCode() : "";
            String name = item.getName() != null ? item.getName() : "";
            String room = item.getRoom() != null ? item.getRoom().trim() : "";
            String lecturerName = item.getLecturerName() != null ? item.getLecturerName().trim() : "";

            // Calculate prefix for blue circle badge (e.g. "SE", "IT", "NT")
            String prefix = "LH";
            if (!code.isEmpty()) {
                String letters = code.replaceAll("[^a-zA-Z]", "").toUpperCase();
                if (letters.length() >= 2) {
                    prefix = letters.substring(0, 2);
                } else if (!letters.isEmpty()) {
                    prefix = letters;
                } else if (code.length() >= 2) {
                    prefix = code.substring(0, 2).toUpperCase();
                }
            }
            binding.tvCoursePrefix.setText(prefix);

            binding.tvCourseName.setText(name);

            if (!room.isEmpty()) {
                binding.tvCodeAndSchedule.setText(code + " · " + room);
            } else {
                binding.tvCodeAndSchedule.setText(code);
            }

            if (!lecturerName.isEmpty()) {
                if (!lecturerName.startsWith("GV:") && !lecturerName.startsWith("Giảng viên:")) {
                    binding.tvLecturerName.setText("Giảng viên: " + lecturerName);
                } else {
                    binding.tvLecturerName.setText(lecturerName.replace("GV:", "Giảng viên:"));
                }
            } else {
                binding.tvLecturerName.setText("Giảng viên: Chưa cập nhật");
            }

            // Show unread red dot if progress < 50
            if (item.getProgress() > 0 && item.getProgress() < 50) {
                binding.viewUnreadDot.setVisibility(View.VISIBLE);
            } else {
                binding.viewUnreadDot.setVisibility(View.GONE);
            }

            boolean hasStatus = false;
            String status = item.getStatus();
            if (status != null && !status.trim().isEmpty() && !"ACTIVE".equalsIgnoreCase(status.trim())) {
                binding.tvStatusBadge.setText(status);
                binding.tvStatusBadge.setVisibility(View.VISIBLE);
                hasStatus = true;
            } else {
                binding.tvStatusBadge.setVisibility(View.GONE);
            }

            boolean hasSemester = false;
            String semester = item.getSemester();
            if (semester != null && !semester.trim().isEmpty()) {
                binding.tvSemesterBadge.setText(semester.trim());
                binding.tvSemesterBadge.setVisibility(View.VISIBLE);
                hasSemester = true;
            } else {
                binding.tvSemesterBadge.setVisibility(View.GONE);
            }

            if (hasStatus || hasSemester) {
                binding.layoutBadgesRow.setVisibility(View.VISIBLE);
            } else {
                binding.layoutBadgesRow.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(item);
                }
            });
        }
    }
}
