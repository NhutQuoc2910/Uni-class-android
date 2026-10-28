package com.ppquang06.uniclassapp.ui.lecturer.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerNotificationsBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.NotificationAdapter;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper.NotificationFilter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerNotificationsFragment extends Fragment {

    private FragmentLecturerNotificationsBinding binding;
    private NotificationAdapter notificationAdapter;
    private final List<NotificationItemResponse> masterList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        setupRecyclerView();
        setupListeners();
        loadNotifications();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_notifications);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(item -> {
            // Click handler can be extended for deep linking
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(notificationAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadNotifications);
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> applyCurrentFilter());
    }

    private void loadNotifications() {
        if (getContext() == null) return;

        if (!binding.swipeRefresh.isRefreshing() && masterList.isEmpty()) {
            binding.pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getUserClassApiService(requireContext())
                .getNotifications()
                .enqueue(new Callback<ApiResponse<List<NotificationItemResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<NotificationItemResponse>>> call,
                                           @NonNull Response<ApiResponse<List<NotificationItemResponse>>> response) {
                        if (!isAdded() || binding == null) return;

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            masterList.clear();
                            masterList.addAll(response.body().getData());
                            hideLoading();
                            applyCurrentFilter();
                        } else {
                            loadHomeSummaryFallback();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<NotificationItemResponse>>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        loadHomeSummaryFallback();
                    }
                });
    }

    private void loadHomeSummaryFallback() {
        if (getContext() == null) return;

        ApiClient.getUserClassApiService(requireContext())
                .getHomeSummary()
                .enqueue(new Callback<ApiResponse<HomeSummaryResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<HomeSummaryResponse>> call,
                                           @NonNull Response<ApiResponse<HomeSummaryResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        hideLoading();

                        masterList.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<AssignmentItem> assignments = response.body().getData().getDueSoonAssignments();
                            masterList.addAll(NotificationFilterHelper.synthesizeFromAssignments(assignments));
                        }
                        applyCurrentFilter();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<HomeSummaryResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        hideLoading();
                        masterList.clear();
                        applyCurrentFilter();
                    }
                });
    }

    private void applyCurrentFilter() {
        if (binding == null) return;

        int checkedId = binding.chipGroupFilter.getCheckedChipId();
        NotificationFilter filter = NotificationFilter.ALL;
        if (checkedId == R.id.chipUnread) {
            filter = NotificationFilter.UNREAD;
        } else if (checkedId == R.id.chipUrgent) {
            filter = NotificationFilter.URGENT;
        } else if (checkedId == R.id.chipAssignments) {
            filter = NotificationFilter.ASSIGNMENTS;
        } else if (checkedId == R.id.chipGrades) {
            filter = NotificationFilter.GRADES;
        }

        List<NotificationItemResponse> filtered = NotificationFilterHelper.filterNotifications(masterList, filter);
        notificationAdapter.submitList(filtered);

        if (filtered.isEmpty()) {
            binding.rvNotifications.setVisibility(View.GONE);
            binding.tvEmptyNotifications.setVisibility(View.VISIBLE);
        } else {
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.tvEmptyNotifications.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (binding == null) return;
        binding.pbLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
