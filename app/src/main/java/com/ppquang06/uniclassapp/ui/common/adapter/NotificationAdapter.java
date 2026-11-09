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
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper;

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

    public interface OnScanQrNowClickListener {
        void onScanQrNowClick(NotificationItemResponse item);
    }

    public interface OnToggleReadClickListener {
        void onToggleReadClick(NotificationItemResponse item);
    }

    private final List<NotificationItemResponse> items = new ArrayList<>();
    private OnNotificationClickListener listener;
    private OnDeleteNotificationListener deleteListener;
    private OnDownloadQrClickListener downloadQrClickListener;
    private OnScanQrNowClickListener scanQrNowClickListener;
    private OnToggleReadClickListener toggleReadClickListener;

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

    public void setOnScanQrNowClickListener(OnScanQrNowClickListener listener) {
        this.scanQrNowClickListener = listener;
    }

    public void setOnToggleReadClickListener(OnToggleReadClickListener listener) {
        this.toggleReadClickListener = listener;
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

    public OnScanQrNowClickListener getOnScanQrNowClickListener() {
        return scanQrNowClickListener;
    }

    public OnToggleReadClickListener getOnToggleReadClickListener() {
        return toggleReadClickListener;
    }

    public static boolean shouldShowDownloadQr(NotificationItemResponse item, OnDownloadQrClickListener listener) {
        return item != null && item.getId() != null && item.getId().startsWith("att-") && listener != null;
    }

    public static boolean shouldShowScanQrNow(NotificationItemResponse item, OnScanQrNowClickListener listener) {
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
        holder.bind(item, listener, deleteListener, downloadQrClickListener, scanQrNowClickListener, toggleReadClickListener);
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
                OnDownloadQrClickListener downloadQrClickListener,
                OnScanQrNowClickListener scanQrNowClickListener,
                OnToggleReadClickListener toggleReadClickListener
        ) {
            String title = item != null && item.getTitle() != null ? item.getTitle() : "";
            String body = item != null && item.getBody() != null ? item.getBody() : "";
            String courseCode = item != null && item.getCourseCode() != null ? item.getCourseCode().trim() : "";
            String formattedDate = item != null ? UiBadgeHelper.formatIsoDateShort(item.getCreatedAt()) : "";

            binding.tvNotifTitle.setText(title);
            binding.tvNotifContent.setText(body);

            boolean isUnread = item != null && item.isUnread();
            if (binding.viewUnreadDot != null) {
                binding.viewUnreadDot.setVisibility(isUnread ? View.VISIBLE : View.GONE);
            }
            if (binding.tvCategoryBadge != null) {
                binding.tvCategoryBadge.setText(NotificationFilterHelper.resolveCategoryBadgeLabel(item));
            }
            if (binding.btnToggleRead != null) {
                if (toggleReadClickListener != null) {
                    binding.btnToggleRead.setVisibility(View.VISIBLE);
                    binding.btnToggleRead.setText(isUnread ? "Đã đọc ✓" : "Chưa đọc");
                    binding.btnToggleRead.setOnClickListener(v -> toggleReadClickListener.onToggleReadClick(item));
                } else {
                    binding.btnToggleRead.setVisibility(View.GONE);
                    binding.btnToggleRead.setOnClickListener(null);
                }
            }

            try {
                Context context = binding.getRoot().getContext();
                if (context != null) {
                    binding.tvNotifTitle.setTextColor(ContextCompat.getColor(context, R.color.uc_text_primary));
                    int secondaryColor = ContextCompat.getColor(context, R.color.uc_text_secondary);
                    binding.tvNotifContent.setTextColor(secondaryColor);
                    binding.tvNotifTime.setTextColor(secondaryColor);
                    binding.getRoot().setAlpha(isUnread ? 1.0f : 0.85f);
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

            if (item != null && NotificationFilterHelper.isUrgentItem(item)) {
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

            boolean showDownload = shouldShowDownloadQr(item, downloadQrClickListener);
            boolean showScanNow = shouldShowScanQrNow(item, scanQrNowClickListener);

            if (binding.layoutNotifActions != null) {
                binding.layoutNotifActions.setVisibility((showDownload || showScanNow) ? View.VISIBLE : View.GONE);
            }

            if (showDownload) {
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

            if (binding.btnScanQrNow != null) {
                if (showScanNow) {
                    binding.btnScanQrNow.setVisibility(View.VISIBLE);
                    binding.btnScanQrNow.setOnClickListener(v -> {
                        if (scanQrNowClickListener != null) {
                            scanQrNowClickListener.onScanQrNowClick(item);
                        }
                    });
                } else {
                    binding.btnScanQrNow.setVisibility(View.GONE);
                    binding.btnScanQrNow.setOnClickListener(null);
                }
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item);
                }
            });
        }
    }
}
