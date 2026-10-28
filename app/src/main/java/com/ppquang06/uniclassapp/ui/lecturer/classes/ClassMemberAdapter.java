package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.databinding.ItemLecturerMemberRowBinding;

import java.util.ArrayList;
import java.util.List;

public class ClassMemberAdapter extends RecyclerView.Adapter<ClassMemberAdapter.MemberViewHolder> {

    public interface OnMemberActionListener {
        void onMemberClick(ClassMemberItemResponse member);
        void onRemoveClick(ClassMemberItemResponse member);
    }

    private final List<ClassMemberItemResponse> memberList = new ArrayList<>();
    private final OnMemberActionListener listener;

    public ClassMemberAdapter(OnMemberActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ClassMemberItemResponse> list) {
        memberList.clear();
        if (list != null) {
            memberList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLecturerMemberRowBinding binding = ItemLecturerMemberRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(memberList.get(position));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemLecturerMemberRowBinding binding;

        MemberViewHolder(ItemLecturerMemberRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ClassMemberItemResponse member) {
            Context context = itemView.getContext();

            // Avatar initial
            String initial = member.getAvatarInitial();
            if (initial == null || initial.isEmpty()) {
                String name = member.getFullName();
                if (name != null && !name.trim().isEmpty()) {
                    String[] parts = name.trim().split("\\s+");
                    if (parts.length == 1) {
                        initial = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                    } else {
                        initial = ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
                    }
                } else {
                    initial = "SV";
                }
            }
            binding.tvAvatarInitial.setText(initial);

            // Name
            binding.tvMemberName.setText(member.getFullName() != null ? member.getFullName() : "Sinh viên");

            // Code & Email
            String code = member.getStudentCode() != null ? member.getStudentCode() : "";
            String email = member.getEmail() != null ? member.getEmail() : "";
            if (!code.isEmpty() && !email.isEmpty()) {
                binding.tvMemberCodeEmail.setText(code + " · " + email);
            } else if (!code.isEmpty()) {
                binding.tvMemberCodeEmail.setText(code);
            } else {
                binding.tvMemberCodeEmail.setText(email);
            }

            // Attendance rate chip
            int rate = member.getAttendanceRate();
            binding.tvAttendanceChip.setText(rate + "%");
            if (rate >= 80) {
                binding.tvAttendanceChip.setBackgroundResource(R.drawable.bg_pill_green);
                binding.tvAttendanceChip.setTextColor(ContextCompat.getColor(context, R.color.color_success));
            } else if (rate >= 60) {
                binding.tvAttendanceChip.setBackgroundResource(R.drawable.bg_pill_warning);
                binding.tvAttendanceChip.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
            } else {
                binding.tvAttendanceChip.setBackgroundResource(R.drawable.bg_pill_red);
                binding.tvAttendanceChip.setTextColor(ContextCompat.getColor(context, R.color.color_error));
            }

            // Absent sessions
            int absent = member.getAbsentSessions();
            int total = member.getTotalSessions() > 0 ? member.getTotalSessions() : 15;
            binding.tvAbsentCount.setText(absent + "/" + total + " buổi");

            // Listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onMemberClick(member);
            });

            binding.btnRemoveMember.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveClick(member);
            });
        }
    }
}
