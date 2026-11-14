package com.ppquang06.uniclassapp.ui.common.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.DialogEditProfileBottomSheetBinding;
import com.ppquang06.uniclassapp.ui.common.BaseBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.utils.AcademicProfileHelper;
import com.ppquang06.uniclassapp.utils.RoleRouter;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class EditProfileBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

    public interface OnProfileUpdatedListener {
        void onProfileUpdated(UserResponse updatedUser);
    }

    private DialogEditProfileBottomSheetBinding binding;
    private ProfileViewModel profileViewModel;
    private OnProfileUpdatedListener listener;

    private String pendingFullName = "";
    private String pendingPhone = "";
    private String pendingStudentId = "";
    private String pendingLecturerId = "";
    private String pendingFaculty = "";
    private String pendingCohort = "";
    private String pendingEducationProgram = "";
    private String pendingAcademicDegree = "";
    private String pendingAcademicRank = "";
    private String pendingPosition = "";
    private boolean isLecturer = false;

    public static EditProfileBottomSheetDialogFragment newInstance() {
        return new EditProfileBottomSheetDialogFragment();
    }

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogEditProfileBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        populateCurrentData();
        setupListeners();
        observeViewModel();
    }

    private void populateCurrentData() {
        if (getContext() == null || binding == null) return;
        UserResponse user = SessionManager.getInstance(requireContext()).getUser();
        isLecturer = RoleRouter.isLecturerRole(user != null ? user.getRole() : null);

        // Setup Faculty dropdown
        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                AcademicProfileHelper.FACULTY_OPTIONS
        );
        binding.actvFaculty.setAdapter(facultyAdapter);

        if (user != null) {
            if (!TextUtils.isEmpty(user.getFullName())) {
                binding.etFullName.setText(user.getFullName());
                binding.etFullName.setSelection(user.getFullName().length());
            }
            if (!TextUtils.isEmpty(user.getPhone())) {
                binding.etPhone.setText(user.getPhone());
            }
            if (!TextUtils.isEmpty(user.getFaculty())) {
                binding.actvFaculty.setText(user.getFaculty(), false);
            }
        }

        if (isLecturer) {
            binding.tilAcademicId.setHint("Mã số giảng viên (MSGV)");
            binding.layoutStudentFields.setVisibility(View.GONE);
            binding.layoutLecturerFields.setVisibility(View.VISIBLE);

            ArrayAdapter<String> degreeAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    AcademicProfileHelper.ACADEMIC_DEGREE_OPTIONS
            );
            binding.actvAcademicDegree.setAdapter(degreeAdapter);

            ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    AcademicProfileHelper.ACADEMIC_RANK_OPTIONS
            );
            binding.actvAcademicRank.setAdapter(rankAdapter);

            ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    AcademicProfileHelper.POSITION_OPTIONS
            );
            binding.actvPosition.setAdapter(positionAdapter);

            if (user != null) {
                if (!TextUtils.isEmpty(user.getLecturerId())) {
                    binding.etAcademicId.setText(user.getLecturerId());
                }
                if (!TextUtils.isEmpty(user.getAcademicDegree())) {
                    binding.actvAcademicDegree.setText(user.getAcademicDegree(), false);
                }
                if (!TextUtils.isEmpty(user.getAcademicRank())) {
                    binding.actvAcademicRank.setText(user.getAcademicRank(), false);
                }
                if (!TextUtils.isEmpty(user.getPosition())) {
                    binding.actvPosition.setText(user.getPosition(), false);
                }
            }
        } else {
            binding.tilAcademicId.setHint("Mã số sinh viên (MSSV)");
            binding.layoutStudentFields.setVisibility(View.VISIBLE);
            binding.layoutLecturerFields.setVisibility(View.GONE);

            ArrayAdapter<String> eduAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    AcademicProfileHelper.EDUCATION_PROGRAM_OPTIONS
            );
            binding.actvEducationProgram.setAdapter(eduAdapter);

            if (user != null) {
                if (!TextUtils.isEmpty(user.getStudentId())) {
                    binding.etAcademicId.setText(user.getStudentId());
                }
                if (!TextUtils.isEmpty(user.getEducationProgram())) {
                    binding.actvEducationProgram.setText(user.getEducationProgram(), false);
                }
                if (!TextUtils.isEmpty(user.getCohort())) {
                    binding.etCohort.setText(user.getCohort());
                }
            }
        }
    }

    private void setupListeners() {
        if (binding == null) return;

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());

        binding.etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        profileViewModel.getUpdateProfileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                UserResponse updatedUser = state.getData();
                profileViewModel.resetUpdateProfileState();

                if (getContext() != null) {
                    if (updatedUser == null) {
                        updatedUser = SessionManager.getInstance(requireContext()).getUser();
                        if (updatedUser == null) {
                            updatedUser = new UserResponse();
                        }
                    }

                    // Ensure all edited fields are persisted in local SessionManager
                    updatedUser.setFullName(pendingFullName);
                    updatedUser.setPhone(pendingPhone);
                    updatedUser.setFaculty(pendingFaculty);
                    if (isLecturer) {
                        updatedUser.setLecturerId(pendingLecturerId);
                        updatedUser.setAcademicDegree(pendingAcademicDegree);
                        updatedUser.setAcademicRank(pendingAcademicRank);
                        updatedUser.setPosition(pendingPosition);
                    } else {
                        updatedUser.setStudentId(pendingStudentId);
                        updatedUser.setCohort(pendingCohort);
                        updatedUser.setEducationProgram(pendingEducationProgram);
                    }

                    SessionManager.getInstance(requireContext()).saveUser(updatedUser);

                    if (listener != null) {
                        listener.onProfileUpdated(updatedUser);
                    }

                    Toast.makeText(requireContext(), R.string.edit_profile_success, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                        ? state.getMessage()
                        : getString(R.string.edit_profile_error_generic);
                profileViewModel.resetUpdateProfileState();
                binding.tilFullName.setError(errorMsg);
            }
        });
    }

    private void saveProfile() {
        if (getContext() == null || binding == null) return;

        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";

        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.edit_profile_error_name_empty));
            return;
        }

        String academicId = binding.etAcademicId.getText() != null ? binding.etAcademicId.getText().toString().trim() : "";
        String faculty = binding.actvFaculty.getText() != null ? binding.actvFaculty.getText().toString().trim() : "";

        pendingFullName = fullName;
        pendingPhone = phone;
        pendingFaculty = faculty;

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(fullName);
        request.setPhone(phone);
        request.setFaculty(faculty);

        if (isLecturer) {
            String degree = binding.actvAcademicDegree.getText() != null ? binding.actvAcademicDegree.getText().toString().trim() : "";
            String rawRank = binding.actvAcademicRank.getText() != null ? binding.actvAcademicRank.getText().toString().trim() : "";
            String rank = AcademicProfileHelper.NO_ACADEMIC_RANK.equals(rawRank) ? "" : rawRank;
            String position = binding.actvPosition.getText() != null ? binding.actvPosition.getText().toString().trim() : "";

            pendingLecturerId = academicId;
            pendingAcademicDegree = degree;
            pendingAcademicRank = rank;
            pendingPosition = position;

            request.setLecturerId(academicId);
            request.setAcademicDegree(degree);
            request.setAcademicRank(rank);
            request.setPosition(position);
        } else {
            String eduProgram = binding.actvEducationProgram.getText() != null ? binding.actvEducationProgram.getText().toString().trim() : "";
            String cohort = binding.etCohort.getText() != null ? binding.etCohort.getText().toString().trim() : "";

            pendingStudentId = academicId;
            pendingEducationProgram = eduProgram;
            pendingCohort = cohort;

            request.setStudentId(academicId);
            request.setEducationProgram(eduProgram);
            request.setCohort(cohort);
        }

        profileViewModel.updateProfile(request);
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!loading);
        binding.btnCancel.setEnabled(!loading);
        binding.etFullName.setEnabled(!loading);
        binding.etPhone.setEnabled(!loading);
        binding.etAcademicId.setEnabled(!loading);
        binding.actvFaculty.setEnabled(!loading);
        binding.actvEducationProgram.setEnabled(!loading);
        binding.etCohort.setEnabled(!loading);
        binding.actvAcademicDegree.setEnabled(!loading);
        binding.actvAcademicRank.setEnabled(!loading);
        binding.actvPosition.setEnabled(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
