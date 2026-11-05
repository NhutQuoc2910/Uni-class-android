package com.ppquang06.uniclassapp.ui.common.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.data.model.response.TodayScheduleItem;
import com.ppquang06.uniclassapp.databinding.ItemTodayScheduleBinding;

import java.util.ArrayList;
import java.util.List;

public class TodayScheduleAdapter extends RecyclerView.Adapter<TodayScheduleAdapter.ViewHolder> {

    public interface OnScheduleClickListener {
        void onScheduleClick(TodayScheduleItem item);
    }

    private final List<TodayScheduleItem> items = new ArrayList<>();
    private final OnScheduleClickListener listener;

    public TodayScheduleAdapter(OnScheduleClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TodayScheduleItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodayScheduleBinding binding = ItemTodayScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTodayScheduleBinding binding;

        ViewHolder(ItemTodayScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TodayScheduleItem item, OnScheduleClickListener listener) {
            String timeRange = item.getFormattedTimeRange();
            binding.tvScheduleTime.setText(!TextUtils.isEmpty(timeRange) ? timeRange : "--:--");
            binding.tvScheduleTypeBadge.setText(item.getScheduleTypeLabel());

            String title = !TextUtils.isEmpty(item.getClassCode())
                    ? (item.getClassCode() + " - " + (item.getClassName() != null ? item.getClassName() : ""))
                    : (item.getClassName() != null ? item.getClassName() : "");
            binding.tvScheduleClassName.setText(title);

            String roomText = !TextUtils.isEmpty(item.getRoom())
                    ? ("Phòng " + item.getRoom())
                    : "Chưa xếp phòng";
            binding.tvScheduleRoom.setText(roomText);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onScheduleClick(item);
                }
            });
        }
    }
}
