package com.ppquang06.uniclassapp.ui.student.classes;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.databinding.DialogPickFileTypeBottomSheetBinding;
import com.ppquang06.uniclassapp.utils.UriFileMultipartHelper;

public class PickFileTypeBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnFilePickedListener {
        void onFilePicked(@NonNull Uri uri, @NonNull String fileName);
    }

    public interface OnFileSelectedListener {
        void onFileSelected(@NonNull String fileName, @NonNull String fileUrl);
    }

    private DialogPickFileTypeBottomSheetBinding binding;
    private OnFilePickedListener pickedListener;
    private OnFileSelectedListener selectedListener;
    private ActivityResultLauncher<String> filePickerLauncher;

    public static PickFileTypeBottomSheetDialogFragment newInstance() {
        return new PickFileTypeBottomSheetDialogFragment();
    }

    public void setOnFilePickedListener(@Nullable OnFilePickedListener listener) {
        this.pickedListener = listener;
    }

    public void setOnFileSelectedListener(@Nullable OnFileSelectedListener listener) {
        this.selectedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleFileUriSelected
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogPickFileTypeBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide demo mock file button in real picker flow
        binding.btnPickDemoFile.setVisibility(View.GONE);

        binding.optPdf.setOnClickListener(v -> launchPicker("application/pdf"));
        binding.optWord.setOnClickListener(v -> launchPicker("*/*"));
        binding.optZip.setOnClickListener(v -> launchPicker("*/*"));
        binding.optImage.setOnClickListener(v -> launchPicker("image/*"));
    }

    private void launchPicker(String mimeType) {
        try {
            filePickerLauncher.launch(mimeType);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Không thể mở bộ chọn tệp", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        }
    }

    private void handleFileUriSelected(Uri uri) {
        if (uri != null) {
            String fileName = UriFileMultipartHelper.getFileName(getContext(), uri);
            String fileUrl = uri.toString();

            if (pickedListener != null) {
                pickedListener.onFilePicked(uri, fileName);
            }
            if (selectedListener != null) {
                selectedListener.onFileSelected(fileName, fileUrl);
            }
            dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
