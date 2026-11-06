package com.ppquang06.uniclassapp.ui.common.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.databinding.ItemSemesterScheduleBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SemesterScheduleAdapter extends RecyclerView.Adapter<SemesterScheduleAdapter.ViewHolder> {

    public interface OnScheduleClickListener {
        void onScheduleClick(ClassScheduleItem item);
    }

    public interface OnDeleteScheduleListener {
        void onDeleteSchedule(ClassScheduleItem item);
    }

    private final List<ClassScheduleItem> allItems = new ArrayList<>();
    private final List<ClassScheduleItem> filteredItems = new ArrayList<>();
    private int currentDayFilter = 0; // 0 = Tất cả, 1 = Thứ 2, ..., 7 = Chủ nhật
    private final OnScheduleClickListener listener;
    private OnDeleteScheduleListener deleteListener;

    public SemesterScheduleAdapter() {
        this(null);
    }

    public SemesterScheduleAdapter(OnScheduleClickListener listener) {
        this(listener, null);
    }

    public SemesterScheduleAdapter(OnScheduleClickListener listener, OnDeleteScheduleListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void setOnDeleteScheduleListener(OnDeleteScheduleListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void submitList(List<ClassScheduleItem> newItems) {
        allItems.clear();
        if (newItems != null) {
            allItems.addAll(newItems);
        }
        applyFilter();
    }

    public void filterByDayOfWeek(int dayOfWeek) {
        this.currentDayFilter = dayOfWeek;
        applyFilter();
    }

    public int getCurrentDayFilter() {
        return currentDayFilter;
    }

    public List<ClassScheduleItem> getFilteredItems() {
        return new ArrayList<>(filteredItems);
    }

    private void applyFilter() {
        filteredItems.clear();
        if (currentDayFilter <= 0) {
            filteredItems.addAll(allItems);
        } else {
            for (ClassScheduleItem item : allItems) {
                if (item != null && item.getDayOfWeek() != null && item.getDayOfWeek() == currentDayFilter) {
                    filteredItems.add(item);
                }
            }
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
        ItemSemesterScheduleBinding binding = ItemSemesterScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(filteredItems.get(position), listener, deleteListener);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
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

    public static String formatDateRange(String startDate, String endDate) {
        String start = formatDate(startDate);
        String end = formatDate(endDate);
        if (!start.isEmpty() && !end.isEmpty()) {
            return "Áp dụng: " + start + " - " + end;
        } else if (!start.isEmpty()) {
            return "Áp dụng từ: " + start;
        } else if (!end.isEmpty()) {
            return "Áp dụng đến: " + end;
        }
        return "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSemesterScheduleBinding binding;

        ViewHolder(ItemSemesterScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassScheduleItem item, OnScheduleClickListener listener, OnDeleteScheduleListener deleteListener) {
            binding.tvScheduleDayOfWeek.setText(item.getDayOfWeekLabel());

            String timeRange = item.getFormattedTimeRange();
            binding.tvScheduleTime.setText(!TextUtils.isEmpty(timeRange) ? timeRange : "--:--");

            String title = !TextUtils.isEmpty(item.getClassCode())
                    ? (item.getClassCode() + " - " + (item.getClassName() != null ? item.getClassName() : ""))
                    : (item.getClassName() != null ? item.getClassName() : "");
            binding.tvCourseTitle.setText(title);

            String room = !TextUtils.isEmpty(item.getRoom()) ? ("Phòng " + item.getRoom()) : "Chưa xếp phòng";
            binding.tvScheduleRoom.setText(room);

            String dateRange = formatDateRange(item.getStartDate(), item.getEndDate());
            if (!TextUtils.isEmpty(dateRange)) {
                binding.tvScheduleDateRange.setVisibility(View.VISIBLE);
                binding.tvScheduleDateRange.setText(dateRange);
            } else {
                binding.tvScheduleDateRange.setVisibility(View.GONE);
            }

            if (deleteListener != null) {
                binding.btnDeleteSchedule.setVisibility(View.VISIBLE);
                binding.btnDeleteSchedule.setOnClickListener(v -> deleteListener.onDeleteSchedule(item));
            } else {
                binding.btnDeleteSchedule.setVisibility(View.GONE);
                binding.btnDeleteSchedule.setOnClickListener(null);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onScheduleClick(item);
                }
            });
        }
    }
}
