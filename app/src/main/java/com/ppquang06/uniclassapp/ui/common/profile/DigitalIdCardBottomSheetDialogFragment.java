package com.ppquang06.uniclassapp.ui.common.profile;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.DialogDigitalIdCardBottomSheetBinding;
import com.ppquang06.uniclassapp.ui.common.BaseBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.utils.AcademicProfileHelper;
import com.ppquang06.uniclassapp.utils.QrCodeGenerator;
import com.ppquang06.uniclassapp.utils.QrImageStorageHelper;
import com.ppquang06.uniclassapp.utils.SessionManager;
import com.ppquang06.uniclassapp.utils.UriFileMultipartHelper;

public class DigitalIdCardBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

    private static final String ARG_IS_LECTURER = "isLecturer";

    private DialogDigitalIdCardBottomSheetBinding binding;
    private boolean isLecturer = false;
    private Bitmap currentQrBitmap;

    public static DigitalIdCardBottomSheetDialogFragment newInstance(boolean isLecturer) {
        DigitalIdCardBottomSheetDialogFragment fragment = new DigitalIdCardBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_LECTURER, isLecturer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isLecturer = getArguments().getBoolean(ARG_IS_LECTURER, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogDigitalIdCardBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindData();
        setupListeners();
    }

    private void bindData() {
        if (binding == null || getContext() == null) return;

        UserResponse user = SessionManager.getInstance(requireContext()).getUser();
        if (user == null) {
            dismiss();
            return;
        }

        // Header and Title
        if (isLecturer) {
            binding.tvSheetTitle.setText("THẺ GIẢNG VIÊN SỐ");
            binding.ivHeaderIcon.setImageResource(R.drawable.ic_shield_check);
            binding.tvCardRoleBadge.setText(R.string.role_badge_lecturer);
        } else {
            binding.tvSheetTitle.setText("THẺ SINH VIÊN SỐ");
            binding.ivHeaderIcon.setImageResource(R.drawable.ic_graduation_cap);
            binding.tvCardRoleBadge.setText(R.string.role_badge_student);
        }

        // Full Name
        String fullName = !TextUtils.isEmpty(user.getFullName())
                ? user.getFullName()
                : (isLecturer ? "Giảng viên UniClass" : "Sinh viên UniClass");
        binding.tvCardFullName.setText(fullName);

        // ID Number
        String idNumber = isLecturer
                ? AcademicProfileHelper.resolveLecturerId(user)
                : AcademicProfileHelper.resolveStudentId(user);
        String idLabel;
        if (!AcademicProfileHelper.NOT_SET_LABEL.equals(idNumber)) {
            idLabel = (isLecturer ? "MSGV: " : "MSSV: ") + idNumber;
        } else {
            idLabel = !TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : idNumber;
        }
        binding.tvCardIdNumber.setText(idLabel);

        // Avatar
        String initial = !TextUtils.isEmpty(fullName) ? fullName.trim().substring(0, 1).toUpperCase() : (isLecturer ? "G" : "S");
        binding.tvCardAvatarInitial.setText(initial);
        if (!TextUtils.isEmpty(user.getAvatarUrl())) {
            String resolvedAvatar = UriFileMultipartHelper.resolveFullFileUrl(user.getAvatarUrl());
            binding.ivCardAvatar.setVisibility(View.VISIBLE);
            binding.tvCardAvatarInitial.setVisibility(View.GONE);
            Glide.with(this)
                    .load(resolvedAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.bg_circle_aura)
                    .error(R.drawable.bg_circle_aura)
                    .into(binding.ivCardAvatar);
        } else {
            binding.ivCardAvatar.setVisibility(View.GONE);
            binding.tvCardAvatarInitial.setVisibility(View.VISIBLE);
        }

        // Academic details
        if (isLecturer) {
            binding.tvLabelField1.setText("KHOA / BỘ MÔN");
            binding.tvCardField1.setText(AcademicProfileHelper.resolveFaculty(user));
            binding.tvLabelField2.setText("HỌC VỊ / HỌC HÀM");
            binding.tvCardField2.setText(AcademicProfileHelper.formatAcademicTitle(user));
        } else {
            binding.tvLabelField1.setText("KHOA / BỘ MÔN");
            binding.tvCardField1.setText(AcademicProfileHelper.resolveFaculty(user));
            binding.tvLabelField2.setText("HỆ ĐÀO TẠO");
            binding.tvCardField2.setText(AcademicProfileHelper.resolveEducationProgram(user));
        }

        // Generate Digital ID QR Bitmap
        String payload = AcademicProfileHelper.buildDigitalIdQrPayload(user, isLecturer);
        binding.tvQrPayloadHint.setText(payload);

        try {
            currentQrBitmap = QrCodeGenerator.generateQrBitmap(payload, 512);
            binding.ivCardQrCode.setImageBitmap(currentQrBitmap);
        } catch (Exception e) {
            binding.tvQrPayloadHint.setText("Lỗi tạo mã QR: " + e.getMessage());
        }
    }

    private void setupListeners() {
        if (binding == null) return;

        binding.ivClose.setOnClickListener(v -> dismiss());
        binding.btnCloseCard.setOnClickListener(v -> dismiss());

        binding.btnSaveQrCode.setOnClickListener(v -> {
            if (currentQrBitmap != null && getContext() != null) {
                UserResponse user = SessionManager.getInstance(requireContext()).getUser();
                String id = isLecturer ? AcademicProfileHelper.resolveLecturerId(user) : AcademicProfileHelper.resolveStudentId(user);
                if (AcademicProfileHelper.NOT_SET_LABEL.equals(id)) {
                    id = "UniClass";
                }
                String title = (isLecturer ? "The_Giang_Vien_" : "The_Sinh_Vien_") + id;
                Uri saved = QrImageStorageHelper.saveQrBitmapToGallery(requireContext(), currentQrBitmap, title);
                if (saved != null) {
                    Toast.makeText(requireContext(), "Đã lưu mã QR thẻ vào thư viện ảnh!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Không thể lưu ảnh vào bộ sưu tập", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
