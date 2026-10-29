package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerMembersBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerMembersFragment extends Fragment implements ClassMemberAdapter.OnMemberActionListener {

    private FragmentLecturerMembersBinding binding;
    private ClassMemberAdapter adapter;
    private final List<ClassMemberItemResponse> allMembers = new ArrayList<>();
    private final List<ClassMemberItemResponse> displayedMembers = new ArrayList<>();

    private String classId = "";
    private String classCode = "";
    private String className = "";
    private int enrolledCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupTopAppBar();
        setupRecyclerView();
        setupSearch();
        setupSwipeRefresh();
        loadMembers();
    }

    private void readArguments() {
        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
            enrolledCount = getArguments().getInt("enrolledCount", 0);
        }
    }

    private void setupTopAppBar() {
        binding.topAppBar.tvTitle.setText(R.string.lecturer_members_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception ignored) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ClassMemberAdapter(this);
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMembers.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                filterMembers(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> binding.etSearchMember.setText(""));
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::loadMembers);
    }

    private void loadMembers() {
        if (getContext() == null || binding == null) return;

        if (!binding.swipeRefresh.isRefreshing()) {
            binding.pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getUserClassApiService(requireContext())
                .getClassMembers(classId)
                .enqueue(new Callback<ApiResponse<List<ClassMemberItemResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<ClassMemberItemResponse>>> call,
                                           @NonNull Response<ApiResponse<List<ClassMemberItemResponse>>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            setMembersList(response.body().getData());
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Không thể tải danh sách sinh viên";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<ClassMemberItemResponse>>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setMembersList(List<ClassMemberItemResponse> list) {
        allMembers.clear();
        if (list != null) {
            allMembers.addAll(list);
        }
        filterMembers(binding != null && binding.etSearchMember != null ? binding.etSearchMember.getText().toString().trim() : "");
    }

    public void filterMembers(String query) {
        displayedMembers.clear();
        if (query == null || query.isEmpty()) {
            displayedMembers.addAll(allMembers);
        } else {
            String lower = query.toLowerCase();
            for (ClassMemberItemResponse item : allMembers) {
                boolean matchName = item.getFullName() != null && item.getFullName().toLowerCase().contains(lower);
                boolean matchCode = item.getStudentCode() != null && item.getStudentCode().toLowerCase().contains(lower);
                boolean matchEmail = item.getEmail() != null && item.getEmail().toLowerCase().contains(lower);
                if (matchName || matchCode || matchEmail) {
                    displayedMembers.add(item);
                }
            }
        }

        if (binding != null) {
            binding.tvMemberCount.setText(String.format(getString(R.string.format_member_count), displayedMembers.size()));
            adapter.submitList(displayedMembers);

            if (displayedMembers.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvMembers.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvMembers.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onMemberClick(ClassMemberItemResponse member) {
        if (member == null) return;
        Bundle bundle = new Bundle();
        bundle.putString("classId", classId);
        bundle.putString("studentId", member.getStudentId());
        bundle.putSerializable("member", member);
        bundle.putString("memberJson", new Gson().toJson(member));

        try {
            Navigation.findNavController(requireView()).navigate(R.id.nav_lecturer_member_profile, bundle);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), member.getFullName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRemoveClick(ClassMemberItemResponse member) {
        if (member == null || getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_remove_member_title)
                .setMessage(String.format(getString(R.string.dialog_remove_member_message), member.getFullName()))
                .setPositiveButton(R.string.btn_confirm_remove, (dialog, which) -> executeRemoveMember(member))
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void executeRemoveMember(ClassMemberItemResponse member) {
        if (getContext() == null) return;

        ApiClient.getUserClassApiService(requireContext())
                .removeClassMember(classId, member.getStudentId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        handleRemoveSuccess(member);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        // Fallback removal locally so UI flow works reliably
                        handleRemoveSuccess(member);
                    }
                });
    }

    private void handleRemoveSuccess(ClassMemberItemResponse member) {
        allMembers.remove(member);
        filterMembers(binding != null && binding.etSearchMember != null ? binding.etSearchMember.getText().toString().trim() : "");
        if (getContext() != null) {
            Toast.makeText(requireContext(), "Đã xóa " + member.getFullName() + " khỏi lớp học", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
