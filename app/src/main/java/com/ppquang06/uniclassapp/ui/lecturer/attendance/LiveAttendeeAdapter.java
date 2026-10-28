package com.ppquang06.uniclassapp.ui.lecturer.attendance;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse.AttendeeItem;
import com.ppquang06.uniclassapp.databinding.ItemLiveAttendeeRowBinding;

import java.util.ArrayList;
import java.util.List;

public class LiveAttendeeAdapter extends RecyclerView.Adapter<LiveAttendeeAdapter.AttendeeViewHolder> {

    private final List<AttendeeItem> items = new ArrayList<>();

    public void setItems(List<AttendeeItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<AttendeeItem> getItems() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLiveAttendeeRowBinding binding = ItemLiveAttendeeRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AttendeeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AttendeeViewHolder extends RecyclerView.ViewHolder {
        private final ItemLiveAttendeeRowBinding binding;

        AttendeeViewHolder(ItemLiveAttendeeRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AttendeeItem item) {
            if (item == null) return;
            Context context = itemView.getContext();

            // Avatar initial
            String initial = item.getAvatarInitial();
            if (TextUtils.isEmpty(initial) && !TextUtils.isEmpty(item.getStudentName())) {
                initial = getInitialFromName(item.getStudentName());
            }
            binding.tvAvatarInitial.setText(!TextUtils.isEmpty(initial) ? initial : "SV");

            // Student Name & Code
            binding.tvStudentName.setText(item.getStudentName() != null ? item.getStudentName() : "Sinh viên");
            binding.tvStudentCode.setText(item.getStudentCode() != null ? item.getStudentCode() : "");

            // Timestamp
            String scannedAt = item.getScannedAt();
            if (!TextUtils.isEmpty(scannedAt)) {
                // If timestamp contains full date-time, format or show HH:mm:ss
                if (scannedAt.contains("T") || scannedAt.contains(" ")) {
                    String timePart = scannedAt.contains("T") ? scannedAt.substring(scannedAt.indexOf("T") + 1) : scannedAt.substring(scannedAt.indexOf(" ") + 1);
                    if (timePart.length() >= 8) {
                        timePart = timePart.substring(0, 8);
                    }
                    binding.tvScannedAt.setText("Quét lúc: " + timePart);
                } else {
                    binding.tvScannedAt.setText("Quét lúc: " + scannedAt);
                }
            } else {
                binding.tvScannedAt.setText("Vừa quét mã");
            }

            // Status Pill
            binding.tvStatusPill.setText("Có mặt");
            binding.tvStatusPill.setBackgroundResource(R.drawable.bg_pill_green);
            binding.tvStatusPill.setTextColor(context.getColor(R.color.color_success));
        }

        private String getInitialFromName(String name) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
            } else if (parts.length == 1 && !parts[0].isEmpty()) {
                return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            }
            return "SV";
        }
    }
}
