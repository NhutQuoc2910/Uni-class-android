package com.ppquang06.uniclassapp.ui.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.R;
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

    public interface OnDeleteNotificationListener {
        void onDeleteNotification(NotificationItemResponse item);
    }

    public interface OnDownloadQrClickListener {
        void onDownloadQrClick(NotificationItemResponse item);
    }

    private final List<NotificationItemResponse> items = new ArrayList<>();
    private OnNotificationClickListener listener;
    private OnDeleteNotificationListener deleteListener;
    private OnDownloadQrClickListener downloadQrClickListener;

    public NotificationAdapter() {}

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public NotificationAdapter(OnNotificationClickListener listener, OnDeleteNotificationListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public NotificationAdapter(
            OnNotificationClickListener listener,
            OnDeleteNotificationListener deleteListener,
            OnDownloadQrClickListener downloadQrClickListener
    ) {
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.downloadQrClickListener = downloadQrClickListener;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteNotificationListener(OnDeleteNotificationListener listener) {
        this.deleteListener = listener;
    }

    public void setOnDownloadQrClickListener(OnDownloadQrClickListener listener) {
        this.downloadQrClickListener = listener;
    }

    public OnNotificationClickListener getOnNotificationClickListener() {
        return listener;
    }

    public OnDeleteNotificationListener getOnDeleteNotificationListener() {
        return deleteListener;
    }

    public OnDownloadQrClickListener getOnDownloadQrClickListener() {
        return downloadQrClickListener;
    }

    public static boolean shouldShowDownloadQr(NotificationItemResponse item, OnDownloadQrClickListener listener) {
        return item != null && item.getId() != null && item.getId().startsWith("att-") && listener != null;
    }

    public static boolean shouldShowDelete(OnDeleteNotificationListener deleteListener) {
        return deleteListener != null;
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
        holder.bind(item, listener, deleteListener, downloadQrClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final ItemNotificationRowBinding binding;

        public NotificationViewHolder(ItemNotificationRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(
                NotificationItemResponse item,
                OnNotificationClickListener listener,
                OnDeleteNotificationListener deleteListener,
                OnDownloadQrClickListener downloadQrClickListener
        ) {
            String title = item != null && item.getTitle() != null ? item.getTitle() : "";
            String body = item != null && item.getBody() != null ? item.getBody() : "";
            String courseCode = item != null && item.getCourseCode() != null ? item.getCourseCode().trim() : "";
            String formattedDate = item != null ? UiBadgeHelper.formatIsoDateShort(item.getCreatedAt()) : "";

            binding.tvNotifTitle.setText(title);
            binding.tvNotifContent.setText(body);

            try {
                Context context = binding.getRoot().getContext();
                if (context != null) {
                    binding.tvNotifTitle.setTextColor(ContextCompat.getColor(context, R.color.uc_text_primary));
                    int secondaryColor = ContextCompat.getColor(context, R.color.uc_text_secondary);
                    binding.tvNotifContent.setTextColor(secondaryColor);
                    binding.tvNotifTime.setTextColor(secondaryColor);
                }
            } catch (Exception ignored) {
                // Fallback for JVM unit tests where Context or Resources are unavailable
            }

            if (!courseCode.isEmpty() && !formattedDate.isEmpty()) {
                binding.tvNotifTime.setText(courseCode + " · " + formattedDate);
            } else if (!courseCode.isEmpty()) {
                binding.tvNotifTime.setText(courseCode);
            } else {
                binding.tvNotifTime.setText(formattedDate);
            }

            if (item != null && "URGENT".equalsIgnoreCase(item.getCategory())) {
                binding.tvUrgentTag.setVisibility(View.VISIBLE);
            } else {
                binding.tvUrgentTag.setVisibility(View.GONE);
            }

            if (shouldShowDelete(deleteListener)) {
                binding.btnDeleteNotif.setVisibility(View.VISIBLE);
                binding.btnDeleteNotif.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteNotification(item);
                    }
                });
            } else {
                binding.btnDeleteNotif.setVisibility(View.GONE);
                binding.btnDeleteNotif.setOnClickListener(null);
            }

            if (shouldShowDownloadQr(item, downloadQrClickListener)) {
                binding.btnDownloadQr.setVisibility(View.VISIBLE);
                binding.btnDownloadQr.setOnClickListener(v -> {
                    if (downloadQrClickListener != null) {
                        downloadQrClickListener.onDownloadQrClick(item);
                    }
                });
            } else {
                binding.btnDownloadQr.setVisibility(View.GONE);
                binding.btnDownloadQr.setOnClickListener(null);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item);
                }
            });
        }
    }
}
