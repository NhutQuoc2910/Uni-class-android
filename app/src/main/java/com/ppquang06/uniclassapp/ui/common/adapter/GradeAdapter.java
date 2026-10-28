package com.ppquang06.uniclassapp.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.databinding.ItemGradeRowBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

import java.util.ArrayList;
import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private final List<ClassGradesResponse.GradeItem> items = new ArrayList<>();

    public GradeAdapter() {}

    public void submitList(List<ClassGradesResponse.GradeItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setItems(List<ClassGradesResponse.GradeItem> newItems) {
        submitList(newItems);
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGradeRowBinding binding = ItemGradeRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GradeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        ClassGradesResponse.GradeItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        private final ItemGradeRowBinding binding;

        GradeViewHolder(ItemGradeRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassGradesResponse.GradeItem item) {
            String title = (item.getAssignmentTitle() != null && !item.getAssignmentTitle().trim().isEmpty())
                    ? item.getAssignmentTitle().trim()
                    : "Bài tập";
            binding.tvAssignmentTitle.setText(title);

            // Submission time info
            if (item.isSubmitted()) {
                String submittedAt = item.getSubmittedAt();
                if (submittedAt != null && !submittedAt.trim().isEmpty()) {
                    String formatted = UiBadgeHelper.formatIsoDateShort(submittedAt.trim());
                    binding.tvSubmissionTime.setText("Đã nộp · " + formatted);
                } else {
                    binding.tvSubmissionTime.setText("Đã nộp");
                }
            } else {
                binding.tvSubmissionTime.setText("Chưa nộp");
            }

            // Score Badge
            UiBadgeHelper.applyGradeBadge(binding.tvScoreBadge, item.getScore());

            // Lecturer Feedback Box
            String feedback = item.getFeedback();
            if (feedback != null && !feedback.trim().isEmpty()) {
                binding.layoutFeedbackBox.setVisibility(View.VISIBLE);
                binding.tvFeedbackContent.setText(feedback.trim());

                String lecturerName = item.getLecturerName();
                if (lecturerName != null && !lecturerName.trim().isEmpty()) {
                    binding.tvLecturerName.setText(lecturerName.trim());
                } else {
                    binding.tvLecturerName.setText("Giảng viên");
                }

                String initial = item.getLecturerInitial();
                if (initial != null && !initial.trim().isEmpty()) {
                    binding.tvLecturerAvatarInitial.setText(initial.trim());
                } else if (lecturerName != null && !lecturerName.trim().isEmpty()) {
                    String trimmedName = lecturerName.trim();
                    int lastSpace = trimmedName.lastIndexOf(' ');
                    String lastWord = lastSpace >= 0 ? trimmedName.substring(lastSpace + 1) : trimmedName;
                    binding.tvLecturerAvatarInitial.setText(lastWord.isEmpty() ? "GV" : lastWord.substring(0, 1).toUpperCase());
                } else {
                    binding.tvLecturerAvatarInitial.setText("GV");
                }

                String submittedAt = item.getSubmittedAt();
                if (submittedAt != null && !submittedAt.trim().isEmpty()) {
                    binding.tvFeedbackTime.setText(UiBadgeHelper.formatIsoDateShort(submittedAt.trim()));
                    binding.tvFeedbackTime.setVisibility(View.VISIBLE);
                } else {
                    binding.tvFeedbackTime.setVisibility(View.GONE);
                }
            } else {
                binding.layoutFeedbackBox.setVisibility(View.GONE);
            }
        }
    }
}
