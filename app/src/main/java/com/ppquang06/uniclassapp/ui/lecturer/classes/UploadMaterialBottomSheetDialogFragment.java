package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.DialogUploadMaterialBottomSheetBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadMaterialBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnMaterialUploadedListener {
        void onMaterialUploaded();
    }

    private static final String ARG_CLASS_ID = "classId";

    private DialogUploadMaterialBottomSheetBinding binding;
    private String classId = "";
    private OnMaterialUploadedListener listener;

    public static UploadMaterialBottomSheetDialogFragment newInstance(String classId) {
        UploadMaterialBottomSheetDialogFragment fragment = new UploadMaterialBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_ID, classId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnMaterialUploadedListener(OnMaterialUploadedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogUploadMaterialBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classId = getArguments().getString(ARG_CLASS_ID, "");
        }

        binding.btnSubmitMaterial.setOnClickListener(v -> submitMaterial());
    }

    private void submitMaterial() {
        String title = binding.etMaterialTitle.getText() != null ? binding.etMaterialTitle.getText().toString().trim() : "";
        String url = binding.etMaterialUrl.getText() != null ? binding.etMaterialUrl.getText().toString().trim() : "";

        boolean hasError = false;
        if (title.isEmpty()) {
            binding.tilMaterialTitle.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilMaterialTitle.setError(null);
        }

        if (url.isEmpty()) {
            binding.tilMaterialUrl.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilMaterialUrl.setError(null);
        }

        if (hasError || getContext() == null) return;

        String fileType = "PDF";
        int checkedId = binding.chipGroupFileType.getCheckedChipId();
        if (checkedId == R.id.chipDocx) {
            fileType = "DOCX";
        } else if (checkedId == R.id.chipPptx) {
            fileType = "PPTX";
        } else if (checkedId == R.id.chipLink) {
            fileType = "LINK";
        }

        binding.pbSubmitting.setVisibility(View.VISIBLE);
        binding.btnSubmitMaterial.setEnabled(false);

        CreateMaterialRequest request = new CreateMaterialRequest(title, url, fileType, false);

        ApiClient.getUserClassApiService(requireContext())
                .createMaterial(classId, request)
                .enqueue(new Callback<ApiResponse<ClassDetailResponse.MaterialItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ClassDetailResponse.MaterialItem>> call,
                                           @NonNull Response<ApiResponse<ClassDetailResponse.MaterialItem>> response) {
                        if (!isAdded() || binding == null) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            handleComplete();
                        } else {
                            String msg = response.body() != null && response.body().getMessage() != null ? response.body().getMessage() : getString(R.string.network_error);
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            binding.pbSubmitting.setVisibility(View.GONE);
                            binding.btnSubmitMaterial.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse.MaterialItem>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbSubmitting.setVisibility(View.GONE);
                        binding.btnSubmitMaterial.setEnabled(true);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleComplete() {
        if (!isAdded()) return;
        if (getContext() != null) {
            Toast.makeText(requireContext(), "Đã tải tài liệu lên thành công!", Toast.LENGTH_SHORT).show();
        }
        if (listener != null) {
            listener.onMaterialUploaded();
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
