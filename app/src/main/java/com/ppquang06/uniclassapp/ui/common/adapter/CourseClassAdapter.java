package com.ppquang06.uniclassapp.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.ItemCourseCardBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

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
            String semester = item.getSemester() != null ? item.getSemester() : "";
            String name = item.getName() != null ? item.getName() : "";
            String room = item.getRoom() != null ? item.getRoom().trim() : "";
            String lecturerName = item.getLecturerName() != null ? item.getLecturerName().trim() : "";

            binding.tvCourseCode.setText(code);
            binding.tvSemesterBadge.setText(semester);
            binding.tvCourseName.setText(name);

            if (!room.isEmpty()) {
                binding.tvRoomSchedule.setText(
                        semester.isEmpty() ? ("Phòng " + room) : ("Phòng " + room + " · " + semester)
                );
            } else {
                binding.tvRoomSchedule.setText(semester);
            }

            if (!lecturerName.isEmpty()) {
                binding.tvLecturerOrCount.setText("GV: " + lecturerName + " · " + item.getEnrolledCount() + " SV");
            } else {
                binding.tvLecturerOrCount.setText(item.getEnrolledCount() + " sinh viên");
            }

            int progress = item.getProgress();
            UiBadgeHelper.Status badgeStatus;
            if (progress >= 80) {
                badgeStatus = UiBadgeHelper.Status.PRESENT;
            } else if (progress >= 60) {
                badgeStatus = UiBadgeHelper.Status.DUE_SOON;
            } else {
                badgeStatus = UiBadgeHelper.Status.OVERDUE;
            }
            UiBadgeHelper.applyBadge(
                    binding.chipAttendanceOrInvite,
                    badgeStatus,
                    "Chuyên cần: " + progress + "%"
            );

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(item);
                }
            });
        }
    }
}
