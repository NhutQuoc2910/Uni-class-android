package com.ppquang06.uniclassapp.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.ItemAssignmentRowBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

import java.util.ArrayList;
import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    public interface OnAssignmentClickListener {
        void onAssignmentClick(AssignmentItem item);
    }

    private final List<AssignmentItem> items = new ArrayList<>();
    private OnAssignmentClickListener listener;

    public AssignmentAdapter() {}

    public AssignmentAdapter(OnAssignmentClickListener listener) {
        this.listener = listener;
    }

    public void setOnAssignmentClickListener(OnAssignmentClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AssignmentItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setItems(List<AssignmentItem> newItems) {
        submitList(newItems);
    }

    public static AssignmentItem fromClassAssignment(ClassDetailResponse.ClassAssignmentItem item, String className) {
        AssignmentItem mapped = new AssignmentItem();
        if (item == null) {
            return mapped;
        }
        mapped.setId(item.getId());
        mapped.setTitle(item.getTitle());
        mapped.setClassName(className);
        mapped.setDueDate(item.getDueDate());
        mapped.setSubmitted(item.isSubmitted());
        return mapped;
    }

    public static List<AssignmentItem> fromClassAssignments(
            List<ClassDetailResponse.ClassAssignmentItem> source,
            String className
    ) {
        List<AssignmentItem> result = new ArrayList<>();
        if (source != null) {
            for (ClassDetailResponse.ClassAssignmentItem item : source) {
                result.add(fromClassAssignment(item, className));
            }
        }
        return result;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAssignmentRowBinding binding = ItemAssignmentRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AssignmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        AssignmentItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAssignmentRowBinding binding;

        AssignmentViewHolder(ItemAssignmentRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AssignmentItem item, OnAssignmentClickListener listener) {
            String title = item.getTitle() != null ? item.getTitle() : "";
            String className = item.getClassName() != null ? item.getClassName().trim() : "";
            String formattedDue = UiBadgeHelper.formatIsoDateShort(item.getDueDate());

            binding.tvAssignmentTitle.setText(title);

            if (!className.isEmpty() && !formattedDue.isEmpty()) {
                binding.tvAssignmentCourseOrDue.setText(className + " · Hạn nộp " + formattedDue);
            } else if (!formattedDue.isEmpty()) {
                binding.tvAssignmentCourseOrDue.setText("Hạn nộp " + formattedDue);
            } else {
                binding.tvAssignmentCourseOrDue.setText(className);
            }

            UiBadgeHelper.Status status = UiBadgeHelper.resolveAssignmentStatus(
                    item.isSubmitted(),
                    item.getDueDate(),
                    System.currentTimeMillis()
            );
            String label = UiBadgeHelper.getAssignmentStatusLabel(status);
            UiBadgeHelper.applyBadge(binding.tvDeadlineBadge, status, label);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAssignmentClick(item);
                }
            });
        }
    }
}
