package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerCreateClassBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerCreateClassFragment extends Fragment {

    private FragmentLecturerCreateClassBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerCreateClassBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        setupListeners();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.create_class_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupListeners() {
        binding.btnSubmitCreateClass.setOnClickListener(v -> attemptCreateClass());
    }

    private void attemptCreateClass() {
        clearErrors();

        String name = binding.etClassName.getText() != null ? binding.etClassName.getText().toString().trim() : "";
        String code = binding.etClassCode.getText() != null ? binding.etClassCode.getText().toString().trim() : "";
        String semester = binding.etSemester.getText() != null ? binding.etSemester.getText().toString().trim() : "";
        String room = binding.etRoom.getText() != null ? binding.etRoom.getText().toString().trim() : "";

        boolean hasError = false;
        if (TextUtils.isEmpty(name)) {
            binding.tilClassName.setError(getString(R.string.err_required_field));
            hasError = true;
        }
        if (TextUtils.isEmpty(code)) {
            binding.tilClassCode.setError(getString(R.string.err_required_field));
            hasError = true;
        }
        if (TextUtils.isEmpty(semester)) {
            binding.tilSemester.setError(getString(R.string.err_required_field));
            hasError = true;
        }

        if (hasError) return;

        setLoading(true);

        CreateClassRequest request = new CreateClassRequest(name, code, semester, room);
        ApiClient.getUserClassApiService(requireContext())
                .createClass(request)
                .enqueue(new Callback<ApiResponse<CourseClassItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<CourseClassItem>> call,
                                           @NonNull Response<ApiResponse<CourseClassItem>> response) {
                        if (!isAdded() || binding == null) return;
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            CourseClassItem created = response.body().getData();
                            navigateToInvite(created.getId(), created.getCode(), created.getName(), created.getInviteCode());
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : getString(R.string.cannot_load_classes);
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<CourseClassItem>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        setLoading(false);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void navigateToInvite(String classId, String classCode, String className, String inviteCode) {
        Bundle args = new Bundle();
        args.putString("classId", classId != null ? classId : "");
        args.putString("classCode", classCode != null ? classCode : "");
        args.putString("className", className != null ? className : "");
        args.putString("inviteCode", inviteCode != null ? inviteCode : "");

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_lecturer_create_class, true)
                .build();

        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.nav_lecturer_class_invite, args, navOptions);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Mã mời: " + inviteCode, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void clearErrors() {
        binding.tilClassName.setError(null);
        binding.tilClassCode.setError(null);
        binding.tilSemester.setError(null);
        binding.tilRoom.setError(null);
    }

    private void setLoading(boolean loading) {
        binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmitCreateClass.setEnabled(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
