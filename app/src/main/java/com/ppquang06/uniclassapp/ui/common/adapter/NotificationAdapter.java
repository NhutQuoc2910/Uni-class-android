package com.ppquang06.uniclassapp.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.databinding.ItemNotificationRowBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItemResponse item);
    }

    private final List<NotificationItemResponse> items = new ArrayList<>();
    private OnNotificationClickListener listener;

    public NotificationAdapter() {}

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NotificationItemResponse> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setItems(List<NotificationItemResponse> newItems) {
        submitList(newItems);
    }

    public static NotificationItemResponse fromAnnouncement(
            ClassDetailResponse.AnnouncementItem item,
            String courseCode
    ) {
        NotificationItemResponse mapped = new NotificationItemResponse();
        if (item == null) {
            return mapped;
        }
        mapped.setId(item.getId());
        mapped.setTitle(item.getTitle());
        mapped.setBody(item.getContent());
        mapped.setCourseCode(courseCode);
        mapped.setCreatedAt(item.getCreatedAt());
        mapped.setCategory(item.isUrgent() ? "URGENT" : "GENERAL");
        mapped.setUnread(item.isUnread());
        return mapped;
    }

    public static List<NotificationItemResponse> fromAnnouncements(
            List<ClassDetailResponse.AnnouncementItem> source,
            String courseCode
    ) {
        List<NotificationItemResponse> result = new ArrayList<>();
        if (source != null) {
            for (ClassDetailResponse.AnnouncementItem item : source) {
                result.add(fromAnnouncement(item, courseCode));
            }
        }
        return result;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationRowBinding binding = ItemNotificationRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItemResponse item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationRowBinding binding;

        NotificationViewHolder(ItemNotificationRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NotificationItemResponse item, OnNotificationClickListener listener) {
            String title = item.getTitle() != null ? item.getTitle() : "";
            String body = item.getBody() != null ? item.getBody() : "";
            String courseCode = item.getCourseCode() != null ? item.getCourseCode().trim() : "";
            String formattedDate = UiBadgeHelper.formatIsoDateShort(item.getCreatedAt());

            binding.tvNotifTitle.setText(title);
            binding.tvNotifContent.setText(body);

            if (!courseCode.isEmpty() && !formattedDate.isEmpty()) {
                binding.tvNotifTime.setText(courseCode + " · " + formattedDate);
            } else if (!courseCode.isEmpty()) {
                binding.tvNotifTime.setText(courseCode);
            } else {
                binding.tvNotifTime.setText(formattedDate);
            }

            if ("URGENT".equalsIgnoreCase(item.getCategory())) {
                binding.tvUrgentTag.setVisibility(View.VISIBLE);
            } else {
                binding.tvUrgentTag.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item);
                }
            });
        }
    }
}
