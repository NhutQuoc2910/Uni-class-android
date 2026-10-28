package com.ppquang06.uniclassapp.ui.lecturer.assignments;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse.SubmissionItem;
import com.ppquang06.uniclassapp.databinding.ItemLecturerSubmissionRowBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubmissionListAdapter extends RecyclerView.Adapter<SubmissionListAdapter.SubmissionViewHolder> {

    public interface OnSubmissionClickListener {
        void onSubmissionClick(SubmissionItem item, int position);
    }

    private final List<SubmissionItem> items = new ArrayList<>();
    private OnSubmissionClickListener listener;

    public SubmissionListAdapter() {}

    public SubmissionListAdapter(OnSubmissionClickListener listener) {
        this.listener = listener;
    }

    public void setOnSubmissionClickListener(OnSubmissionClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SubmissionItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<SubmissionItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLecturerSubmissionRowBinding binding = ItemLecturerSubmissionRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubmissionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        holder.bind(items.get(position), listener, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SubmissionViewHolder extends RecyclerView.ViewHolder {
        private final ItemLecturerSubmissionRowBinding binding;

        SubmissionViewHolder(ItemLecturerSubmissionRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SubmissionItem item, OnSubmissionClickListener listener, int position) {
            Context context = binding.getRoot().getContext();

            // Avatar initial
            String initial = item.getAvatarInitial();
            if (TextUtils.isEmpty(initial)) {
                initial = getInitialFromName(item.getStudentName());
            }
            binding.tvAvatarInitial.setText(initial);

            // Student name & code
            binding.tvStudentName.setText(item.getStudentName() != null ? item.getStudentName() : "");
            String codeText = item.getStudentCode() != null ? "MSSV: " + item.getStudentCode() : "";
            binding.tvStudentCode.setText(codeText);

            // Submission timestamp
            if (item.isSubmitted()) {
                String submittedAt = item.getSubmittedAt() != null ? item.getSubmittedAt() : "";
                binding.tvSubmittedAt.setText("Nộp lúc: " + submittedAt);
            } else {
                binding.tvSubmittedAt.setText("Chưa nộp bài");
            }

            // File URL
            if (!TextUtils.isEmpty(item.getFileUrl())) {
                binding.tvFileInfo.setVisibility(View.VISIBLE);
                String displayFileName = extractFileName(item.getFileUrl());
                binding.tvFileInfo.setText(displayFileName);
            } else {
                binding.tvFileInfo.setVisibility(View.GONE);
            }

            // Status Badge
            if (item.getScore() != null) {
                String scoreStr = (item.getScore() == Math.floor(item.getScore())) ?
                        String.format(Locale.getDefault(), "%.0f", item.getScore()) :
                        String.format(Locale.getDefault(), "%.1f", item.getScore());
                binding.tvStatusBadge.setText(context.getString(R.string.status_graded_prefix) + scoreStr);
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_green);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.color_success));
            } else if (item.isSubmitted()) {
                if (item.isLate()) {
                    binding.tvStatusBadge.setText(R.string.status_late);
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_warning);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
                } else {
                    binding.tvStatusBadge.setText(R.string.status_submitted_on_time);
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_green);
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                }
            } else {
                binding.tvStatusBadge.setText(R.string.status_not_submitted);
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_gray);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubmissionClick(item, position);
                }
            });
        }

        private static String getInitialFromName(String name) {
            if (TextUtils.isEmpty(name)) return "U";
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 1) {
                return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            }
            String first = parts[0].substring(0, 1);
            String last = parts[parts.length - 1].substring(0, 1);
            return (first + last).toUpperCase();
        }

        private static String extractFileName(String url) {
            if (url == null) return "attachment";
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < url.length() - 1) {
                return url.substring(lastSlash + 1);
            }
            return url;
        }
    }
}
