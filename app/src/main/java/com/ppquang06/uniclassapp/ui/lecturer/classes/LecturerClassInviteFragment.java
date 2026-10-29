package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerClassInviteBinding;

public class LecturerClassInviteFragment extends Fragment {

    private FragmentLecturerClassInviteBinding binding;
    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String inviteCode = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerClassInviteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        extractArguments();
        setupTopBar();
        bindData();
        setupListeners();
    }

    private void extractArguments() {
        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
            inviteCode = getArguments().getString("inviteCode", "");
        }
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.create_class_success_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> navigateBackToClasses());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToClasses();
            }
        });
    }

    private void navigateBackToClasses() {
        try {
            boolean popped = NavHostFragment.findNavController(this).popBackStack(R.id.nav_lecturer_classes, false);
            if (!popped) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        } catch (Exception e) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void bindData() {
        if (!className.isEmpty() || !classCode.isEmpty()) {
            binding.tvClassSubtitle.setText(className + (!classCode.isEmpty() ? (" · " + classCode) : ""));
        }
        binding.tvInviteCode.setText(!inviteCode.isEmpty() ? inviteCode : "K7M-2QX");
    }

    private void setupListeners() {
        binding.btnCopyCode.setOnClickListener(v -> copyInviteCode());
        binding.btnShareCode.setOnClickListener(v -> shareInviteCode());
        binding.btnEnterClass.setOnClickListener(v -> enterClassDetail());
    }

    public void copyInviteCode() {
        if (getContext() == null) return;
        String codeToCopy = !inviteCode.isEmpty() ? inviteCode : binding.tvInviteCode.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("EduLens Invite Code", codeToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), R.string.copied_invite_code, Toast.LENGTH_SHORT).show();
        }
    }

    public void shareInviteCode() {
        String codeToShare = !inviteCode.isEmpty() ? inviteCode : binding.tvInviteCode.getText().toString();
        String shareBody = String.format(getString(R.string.share_code_format),
                !className.isEmpty() ? className : classCode,
                codeToShare);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_code_subject));
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Chia sẻ mã mời"));
    }

    private void enterClassDetail() {
        Bundle args = new Bundle();
        args.putString("classId", classId);
        args.putString("classCode", classCode);
        args.putString("className", className);
        args.putString("inviteCode", inviteCode);
        args.putString("status", "ACTIVE");

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_lecturer_class_invite, true)
                .build();

        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.nav_lecturer_class_detail, args, navOptions);
        } catch (Exception e) {
            try {
                NavHostFragment.findNavController(this).popBackStack(R.id.nav_lecturer_classes, false);
            } catch (Exception ex) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
