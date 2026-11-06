package com.ppquang06.uniclassapp.ui.common.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;
import com.ppquang06.uniclassapp.databinding.ItemScheduleExceptionBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleExceptionAdapter extends RecyclerView.Adapter<ScheduleExceptionAdapter.ViewHolder> {

    public interface OnExceptionClickListener {
        void onExceptionClick(ScheduleExceptionItem item);
    }

    public interface OnExceptionActionListener {
        void onPublishClick(ScheduleExceptionItem item);
        void onRevokeClick(ScheduleExceptionItem item);
    }

    private final List<ScheduleExceptionItem> items = new ArrayList<>();
    private final OnExceptionClickListener listener;
    private boolean isManagementMode = false;
    private OnExceptionActionListener actionListener;

    public ScheduleExceptionAdapter() {
        this(null, false, null);
    }

    public ScheduleExceptionAdapter(OnExceptionClickListener listener) {
        this(listener, false, null);
    }

    public ScheduleExceptionAdapter(boolean isManagementMode, OnExceptionActionListener actionListener) {
        this(null, isManagementMode, actionListener);
    }

    public ScheduleExceptionAdapter(OnExceptionClickListener listener, boolean isManagementMode, OnExceptionActionListener actionListener) {
        this.listener = listener;
        this.isManagementMode = isManagementMode;
        this.actionListener = actionListener;
    }

    public boolean isManagementMode() {
        return isManagementMode;
    }

    public void setManagementMode(boolean managementMode) {
        this.isManagementMode = managementMode;
    }

    public OnExceptionActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(OnExceptionActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void submitList(List<ScheduleExceptionItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        try {
            notifyDataSetChanged();
        } catch (Exception ignored) {
            // Ignored in unit tests where Android AdapterDataObservable is not initialized
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleExceptionBinding binding = ItemScheduleExceptionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener, isManagementMode, actionListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static String formatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "";
        }
        String trimmed = rawDate.trim();
        if (trimmed.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return trimmed;
        }
        if (trimmed.length() >= 10 && trimmed.charAt(4) == '-' && trimmed.charAt(7) == '-') {
            String y = trimmed.substring(0, 4);
            String m = trimmed.substring(5, 7);
            String d = trimmed.substring(8, 10);
            return d + "/" + m + "/" + y;
        }
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(trimmed);
            if (date != null) {
                SimpleDateFormat outFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return outFormat.format(date);
            }
        } catch (Exception ignored) {
        }
        return trimmed;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemScheduleExceptionBinding binding;

        ViewHolder(ItemScheduleExceptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScheduleExceptionItem item, OnExceptionClickListener listener,
                  boolean isManagementMode, OnExceptionActionListener actionListener) {
            binding.tvExceptionTypeBadge.setText(item.getExceptionTypeLabel());

            String type = item.getExceptionType() != null ? item.getExceptionType().trim().toUpperCase() : "";
            int badgeColor;
            if ("CANCELLED".equals(type)) {
                badgeColor = Color.parseColor("#DC2626"); // Red
            } else if ("MAKEUP".equals(type)) {
                badgeColor = Color.parseColor("#16A34A"); // Green
            } else {
                badgeColor = Color.parseColor("#D97706"); // Orange
            }
            binding.tvExceptionTypeBadge.setTextColor(badgeColor);

            binding.tvExceptionStatus.setText(item.getStatusLabel());

            String title = !TextUtils.isEmpty(item.getClassCode())
                    ? (item.getClassCode() + " - " + (item.getClassName() != null ? item.getClassName() : ""))
                    : (item.getClassName() != null ? item.getClassName() : "");
            binding.tvCourseTitle.setText(title);

            String orig = formatDate(item.getOriginalDate());
            String occur = formatDate(item.getOccurrenceDate());
            String dateText = "";
            if ("CANCELLED".equals(type)) {
                dateText = !orig.isEmpty() ? ("Ngày gốc: " + orig) : (!occur.isEmpty() ? ("Ngày gốc: " + occur) : "");
            } else if ("MAKEUP".equals(type)) {
                if (!occur.isEmpty() && !orig.isEmpty()) {
                    dateText = "Học bù ngày: " + occur + " (Nghỉ: " + orig + ")";
                } else if (!occur.isEmpty()) {
                    dateText = "Học bù ngày: " + occur;
                } else if (!orig.isEmpty()) {
                    dateText = "Học bù thay ngày: " + orig;
                }
            } else {
                if (!occur.isEmpty() && !orig.isEmpty()) {
                    dateText = "Chuyển sang: " + occur + " (Lịch cũ: " + orig + ")";
                } else if (!occur.isEmpty()) {
                    dateText = "Ngày áp dụng: " + occur;
                } else if (!orig.isEmpty()) {
                    dateText = "Ngày gốc: " + orig;
                }
            }
            if (!TextUtils.isEmpty(dateText)) {
                binding.tvExceptionDate.setVisibility(View.VISIBLE);
                binding.tvExceptionDate.setText(dateText);
            } else {
                binding.tvExceptionDate.setVisibility(View.GONE);
            }

            String timeRange = item.getFormattedTimeRange();
            String room = !TextUtils.isEmpty(item.getRoom()) ? ("Phòng " + item.getRoom()) : "";
            String timeRoom = "";
            if (!timeRange.isEmpty() && !room.isEmpty()) {
                timeRoom = timeRange + " · " + room;
            } else if (!timeRange.isEmpty()) {
                timeRoom = timeRange;
            } else if (!room.isEmpty()) {
                timeRoom = room;
            }
            if (!TextUtils.isEmpty(timeRoom)) {
                binding.tvExceptionTimeRoom.setVisibility(View.VISIBLE);
                binding.tvExceptionTimeRoom.setText(timeRoom);
            } else {
                binding.tvExceptionTimeRoom.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(item.getReason())) {
                binding.tvExceptionReason.setVisibility(View.VISIBLE);
                binding.tvExceptionReason.setText("Lý do: " + item.getReason());
            } else {
                binding.tvExceptionReason.setVisibility(View.GONE);
            }

            if (!isManagementMode) {
                binding.llLecturerActions.setVisibility(View.GONE);
            } else {
                String status = item.getStatus() != null ? item.getStatus().trim() : "";
                if ("DRAFT".equalsIgnoreCase(status)) {
                    binding.llLecturerActions.setVisibility(View.VISIBLE);
                    binding.btnPublishException.setVisibility(View.VISIBLE);
                    binding.btnRevokeException.setVisibility(View.GONE);
                } else if ("PUBLISHED".equalsIgnoreCase(status)) {
                    binding.llLecturerActions.setVisibility(View.VISIBLE);
                    binding.btnPublishException.setVisibility(View.GONE);
                    binding.btnRevokeException.setVisibility(View.VISIBLE);
                } else if ("REVOKED".equalsIgnoreCase(status)) {
                    binding.llLecturerActions.setVisibility(View.GONE);
                } else {
                    binding.llLecturerActions.setVisibility(View.GONE);
                }

                binding.btnPublishException.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onPublishClick(item);
                    }
                });

                binding.btnRevokeException.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onRevokeClick(item);
                    }
                });
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExceptionClick(item);
                }
            });
        }
    }
}
