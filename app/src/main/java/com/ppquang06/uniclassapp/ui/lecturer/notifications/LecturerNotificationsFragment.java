package com.ppquang06.uniclassapp.ui.lecturer.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerNotificationsBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.adapter.NotificationAdapter;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper.CategoryFilter;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper.StatusFilter;
import com.ppquang06.uniclassapp.ui.lecturer.LecturerMainActivity;

import java.util.ArrayList;
import java.util.List;

public class LecturerNotificationsFragment extends Fragment {

    private FragmentLecturerNotificationsBinding binding;
    private HomeViewModel homeViewModel;
    private NotificationAdapter notificationAdapter;
    private final List<NotificationItemResponse> masterList = new ArrayList<>();
    private StatusFilter currentStatusFilter = StatusFilter.ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupTopBar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        loadNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTopBarAvatar();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_notifications);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
        binding.topAppBar.ivAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof LecturerMainActivity) {
                ((LecturerMainActivity) getActivity()).switchToTab(R.id.nav_lecturer_profile);
            }
        });
        updateTopBarAvatar();
    }

    private void updateTopBarAvatar() {
        if (getContext() != null && binding != null) {
            com.ppquang06.uniclassapp.data.model.response.UserResponse user =
                    com.ppquang06.uniclassapp.utils.SessionManager.getInstance(requireContext()).getUser();
            com.ppquang06.uniclassapp.utils.AvatarUiHelper.loadTopBarAvatar(
                    binding.topAppBar.ivAvatar, user != null ? user.getAvatarUrl() : null);
        }
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(item -> {
            if (item != null && item.isUnread()) {
                item.setUnread(false);
                applyCurrentFilter();
            }
        });
        notificationAdapter.setOnToggleReadClickListener(item -> {
            if (item != null) {
                item.setUnread(!item.isUnread());
                applyCurrentFilter();
            }
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(notificationAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadNotifications);
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> applyCurrentFilter());
        binding.cardNotifStatTotal.setOnClickListener(v -> {
            currentStatusFilter = StatusFilter.ALL;
            applyCurrentFilter();
        });
        binding.cardNotifStatUnread.setOnClickListener(v -> {
            currentStatusFilter = StatusFilter.UNREAD;
            applyCurrentFilter();
        });
        binding.cardNotifStatUrgent.setOnClickListener(v -> {
            currentStatusFilter = StatusFilter.URGENT;
            applyCurrentFilter();
        });
        binding.btnMarkAllRead.setOnClickListener(v -> {
            int marked = NotificationFilterHelper.markAllAsRead(masterList);
            applyCurrentFilter();
            if (getContext() != null) {
                String msg = marked > 0
                        ? "Đã đánh dấu đã đọc " + marked + " thông báo"
                        : "Tất cả thông báo đã ở trạng thái đã đọc";
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        homeViewModel.getNotificationsState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isLoading()) {
                if (!binding.swipeRefresh.isRefreshing() && masterList.isEmpty()) {
                    binding.pbLoading.setVisibility(View.VISIBLE);
                }
            } else if (state.isSuccess()) {
                if (state.getData() != null) {
                    masterList.clear();
                    masterList.addAll(state.getData());
                    hideLoading();
                    applyCurrentFilter();
                } else {
                    homeViewModel.loadHomeSummary();
                }
            } else if (state.isError()) {
                homeViewModel.loadHomeSummary();
            }
        });

        homeViewModel.getHomeSummaryState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || state.isLoading() || binding == null) {
                return;
            }
            hideLoading();
            masterList.clear();
            if (state.getData() != null) {
                List<AssignmentItem> assignments = state.getData().getDueSoonAssignments();
                masterList.addAll(NotificationFilterHelper.synthesizeFromAssignments(assignments));
            }
            applyCurrentFilter();
        });
    }

    private void loadNotifications() {
        if (homeViewModel != null) {
            homeViewModel.loadNotifications();
        }
    }

    private void applyCurrentFilter() {
        if (binding == null) return;

        int totalCount = masterList.size();
        int unreadCount = 0;
        int urgentCount = 0;
        for (NotificationItemResponse item : masterList) {
            if (item != null) {
                if (item.isUnread()) {
                    unreadCount++;
                }
                if (NotificationFilterHelper.isUrgentItem(item)) {
                    urgentCount++;
                }
            }
        }

        binding.tvNotifTotalCount.setText(String.valueOf(totalCount));
        binding.tvNotifUnreadCount.setText(String.valueOf(unreadCount));
        binding.tvNotifUrgentCount.setText(String.valueOf(urgentCount));

        binding.cardNotifStatTotal.setBackgroundResource(
                currentStatusFilter == StatusFilter.ALL
                        ? R.drawable.bg_unified_bento_stat_active
                        : R.drawable.bg_unified_bento_stat);
        binding.cardNotifStatUnread.setBackgroundResource(
                currentStatusFilter == StatusFilter.UNREAD
                        ? R.drawable.bg_unified_bento_stat_active
                        : R.drawable.bg_unified_bento_stat);
        binding.cardNotifStatUrgent.setBackgroundResource(
                currentStatusFilter == StatusFilter.URGENT
                        ? R.drawable.bg_unified_bento_stat_active
                        : R.drawable.bg_unified_bento_stat);

        if (unreadCount > 0) {
            binding.tvNotifHeroSubtitle.setText("Bạn có " + unreadCount + " thông báo chưa đọc");
        } else {
            binding.tvNotifHeroSubtitle.setText("Tất cả thông báo đã được cập nhật");
        }

        int checkedId = binding.chipGroupFilter.getCheckedChipId();
        CategoryFilter categoryFilter = CategoryFilter.ALL;
        if (checkedId == R.id.chipQr) {
            categoryFilter = CategoryFilter.QR;
        } else if (checkedId == R.id.chipAssignments) {
            categoryFilter = CategoryFilter.ASSIGNMENTS;
        } else if (checkedId == R.id.chipGrades) {
            categoryFilter = CategoryFilter.GRADES;
        } else if (checkedId == R.id.chipGeneral) {
            categoryFilter = CategoryFilter.GENERAL;
        } else if (checkedId == R.id.chipUnread) {
            currentStatusFilter = StatusFilter.UNREAD;
        } else if (checkedId == R.id.chipUrgent) {
            currentStatusFilter = StatusFilter.URGENT;
        }

        List<NotificationItemResponse> filtered = NotificationFilterHelper.filterByStatusAndCategory(
                masterList,
                currentStatusFilter,
                categoryFilter
        );
        notificationAdapter.submitList(filtered);

        if (filtered.isEmpty()) {
            binding.rvNotifications.setVisibility(View.GONE);
            binding.layoutEmptyNotifications.setVisibility(View.VISIBLE);
            binding.tvEmptyNotifications.setVisibility(View.VISIBLE);
        } else {
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.layoutEmptyNotifications.setVisibility(View.GONE);
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
