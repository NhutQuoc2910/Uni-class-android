package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ppquang06.uniclassapp.R;

public class LecturerClassDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(requireContext());
        tv.setText("Chi tiết lớp học");
        tv.setPadding(32, 32, 32, 32);
        tv.setTextSize(18);
        return tv;
    }
}
