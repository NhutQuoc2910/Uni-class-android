package com.ppquang06.uniclassapp.ui.student.classes;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.databinding.DialogPickFileTypeBottomSheetBinding;

public class PickFileTypeBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnFileSelectedListener {
        void onFileSelected(String fileName, String fileUrl);
    }

    private static final String DEMO_FILE_NAME = "bao_cao_bai_tap_nhom.pdf";
    private static final String DEMO_FILE_URL = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";

    private DialogPickFileTypeBottomSheetBinding binding;
    private OnFileSelectedListener listener;
    private ActivityResultLauncher<String> filePickerLauncher;

    public static PickFileTypeBottomSheetDialogFragment newInstance() {
        return new PickFileTypeBottomSheetDialogFragment();
    }

    public void setOnFileSelectedListener(OnFileSelectedListener listener) {
        this.listener = listener;
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

        binding.optPdf.setOnClickListener(v -> launchPicker("application/pdf"));
        binding.optWord.setOnClickListener(v -> launchPicker("*/*"));
        binding.optZip.setOnClickListener(v -> launchPicker("*/*"));
        binding.optImage.setOnClickListener(v -> launchPicker("image/*"));

        binding.btnPickDemoFile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileSelected(DEMO_FILE_NAME, DEMO_FILE_URL);
            }
            dismiss();
        });
    }

    private void launchPicker(String mimeType) {
        try {
            filePickerLauncher.launch(mimeType);
        } catch (Exception e) {
            if (listener != null) {
                listener.onFileSelected(DEMO_FILE_NAME, DEMO_FILE_URL);
            }
            dismiss();
        }
    }

    private void handleFileUriSelected(Uri uri) {
        if (uri != null) {
            String fileName = queryFileName(uri);
            String fileUrl = uri.toString();
            if (listener != null) {
                listener.onFileSelected(fileName, fileUrl);
            }
            dismiss();
        }
    }

    private String queryFileName(Uri uri) {
        if (uri == null) return DEMO_FILE_NAME;
        String result = null;
        if (getContext() != null && "content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (colIndex != -1) {
                        result = cursor.getString(colIndex);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getLastPathSegment();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return (result != null && !result.trim().isEmpty()) ? result : DEMO_FILE_NAME;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
