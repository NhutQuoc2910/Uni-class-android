package com.ppquang06.uniclassapp.ui.common.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.ItemDocumentRowBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    public interface OnMaterialClickListener {
        void onMaterialClick(ClassDetailResponse.MaterialItem item);
    }

    private final List<ClassDetailResponse.MaterialItem> items = new ArrayList<>();
    private OnMaterialClickListener listener;

    public MaterialAdapter() {}

    public MaterialAdapter(OnMaterialClickListener listener) {
        this.listener = listener;
    }

    public void setOnMaterialClickListener(OnMaterialClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ClassDetailResponse.MaterialItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setItems(List<ClassDetailResponse.MaterialItem> newItems) {
        submitList(newItems);
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDocumentRowBinding binding = ItemDocumentRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MaterialViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        ClassDetailResponse.MaterialItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        private final ItemDocumentRowBinding binding;

        MaterialViewHolder(ItemDocumentRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassDetailResponse.MaterialItem item, OnMaterialClickListener listener) {
            String rawType = item.getFileType() != null ? item.getFileType().trim() : "";
            String ext = rawType.isEmpty() ? "FILE" : rawType.toUpperCase(Locale.ROOT);
            String title = item.getTitle() != null ? item.getTitle() : "";
            String formattedDate = UiBadgeHelper.formatIsoDateShort(item.getCreatedAt());

            binding.tvFileTypeExt.setText(ext);
            binding.tvDocumentTitle.setText(title);
            if (!formattedDate.isEmpty()) {
                binding.tvDocumentMeta.setText(formattedDate + " · " + ext);
            } else {
                binding.tvDocumentMeta.setText(ext);
            }

            int textColorRes;
            int bgColorRes;
            if ("PDF".equals(ext)) {
                textColorRes = R.color.color_error;
                bgColorRes = R.color.color_error_container;
            } else if ("PPTX".equals(ext) || "PPT".equals(ext)) {
                textColorRes = R.color.color_warning;
                bgColorRes = R.color.color_warning_container;
            } else {
                textColorRes = R.color.color_primary;
                bgColorRes = R.color.color_primary_container;
            }
            binding.tvFileTypeExt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), textColorRes));
            ViewCompat.setBackgroundTintList(
                    binding.flFileTypeBadge,
                    ColorStateList.valueOf(ContextCompat.getColor(binding.getRoot().getContext(), bgColorRes))
            );

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMaterialClick(item);
                }
            });
        }
    }
}
