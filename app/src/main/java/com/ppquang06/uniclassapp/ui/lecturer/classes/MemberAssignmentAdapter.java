package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.databinding.ItemMemberAssignmentRowBinding;

import java.util.ArrayList;
import java.util.List;

public class MemberAssignmentAdapter extends RecyclerView.Adapter<MemberAssignmentAdapter.AssignmentViewHolder> {

    private final List<ClassMemberItemResponse.MemberAssignmentSummary> items = new ArrayList<>();

    public void submitList(List<ClassMemberItemResponse.MemberAssignmentSummary> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberAssignmentRowBinding binding = ItemMemberAssignmentRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AssignmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberAssignmentRowBinding binding;

        AssignmentViewHolder(ItemMemberAssignmentRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassMemberItemResponse.MemberAssignmentSummary item) {
            Context context = itemView.getContext();

            binding.tvAssignmentTitle.setText(item.getTitle() != null ? item.getTitle() : "Bài tập");

            StringBuilder subtitle = new StringBuilder();
            if (item.getDeadline() != null && !item.getDeadline().isEmpty()) {
                subtitle.append("Hạn: ").append(item.getDeadline());
            }
            if (item.isSubmitted() && item.getSubmittedAt() != null && !item.getSubmittedAt().isEmpty()) {
                if (subtitle.length() > 0) subtitle.append(" · ");
                subtitle.append("Nộp: ").append(item.getSubmittedAt());
            }
            binding.tvAssignmentSubtitle.setText(subtitle.toString());

            if (item.isSubmitted()) {
                binding.tvStatusBadge.setText(R.string.status_submitted);
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_green);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.color_success));
            } else {
                binding.tvStatusBadge.setText(R.string.not_submitted);
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_red);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.color_error));
            }

            if (item.getScore() != null) {
                binding.tvScoreBadge.setVisibility(View.VISIBLE);
                binding.tvScoreBadge.setText(String.format(java.util.Locale.US, "%.1f / 10", item.getScore()));
                binding.tvScoreBadge.setTextColor(ContextCompat.getColor(context, R.color.color_primary));
            } else if (item.isSubmitted()) {
                binding.tvScoreBadge.setVisibility(View.VISIBLE);
                binding.tvScoreBadge.setText(R.string.status_not_graded);
                binding.tvScoreBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            } else {
                binding.tvScoreBadge.setVisibility(View.GONE);
            }
        }
    }
}
