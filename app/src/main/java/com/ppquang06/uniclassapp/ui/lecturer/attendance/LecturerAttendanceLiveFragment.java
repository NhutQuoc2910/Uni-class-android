package com.ppquang06.uniclassapp.ui.lecturer.attendance;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse.AttendeeItem;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerAttendanceLiveBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerAttendanceLiveFragment extends Fragment {

    private FragmentLecturerAttendanceLiveBinding binding;
    private LiveAttendeeAdapter adapter;

    private String sessionId = "";
    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String room = "";
    private boolean isSessionClosed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerAttendanceLiveBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            sessionId = getArguments().getString("sessionId", "");
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
            room = getArguments().getString("room", "");
        }

        setupTopBar();
        setupHeader();
        setupRecyclerView();
        setupSwipeRefresh();
        setupCloseSessionAction();

        loadLiveSession();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.attendance_live_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> handleBackNavigation());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    private void handleBackNavigation() {
        if (isSessionClosed) {
            navigateBackToClassDetail();
        } else {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void navigateBackToClassDetail() {
        try {
            boolean popped = NavHostFragment.findNavController(this).popBackStack(R.id.nav_lecturer_class_detail, false);
            if (!popped) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        } catch (Exception e) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void setupHeader() {
        if (!TextUtils.isEmpty(className)) {
            binding.tvLiveClassName.setText(className);
        } else {
            binding.tvLiveClassName.setText("Lớp học");
        }

        StringBuilder roomInfo = new StringBuilder();
        if (!TextUtils.isEmpty(classCode)) {
            roomInfo.append(classCode);
        }
        if (!TextUtils.isEmpty(room)) {
            if (roomInfo.length() > 0) roomInfo.append(" · ");
            roomInfo.append(room);
        }
        if (roomInfo.length() > 0) {
            binding.tvLiveClassCodeRoom.setText(roomInfo.toString());
        }
    }

    private void setupRecyclerView() {
        adapter = new LiveAttendeeAdapter();
        binding.rvLiveAttendees.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLiveAttendees.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLive.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefreshLive.setOnRefreshListener(this::loadLiveSession);
    }

    private void setupCloseSessionAction() {
        binding.btnCloseSessionLive.setOnClickListener(v -> showCloseSessionDialog());
    }

    private void showCloseSessionDialog() {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_close_session_title)
                .setMessage(R.string.dialog_close_session_msg)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.btn_close_session_confirm, (dialog, which) -> executeCloseSession())
                .show();
    }

    private void loadLiveSession() {
        if (getContext() == null || binding == null) return;

        if (!binding.swipeRefreshLive.isRefreshing()) {
            binding.pbLoadingLive.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(sessionId)) {
            binding.pbLoadingLive.setVisibility(View.GONE);
            binding.swipeRefreshLive.setRefreshing(false);
            Toast.makeText(requireContext(), "Thiếu mã phiên điểm danh", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getUserClassApiService(requireContext())
                .getLiveAttendanceSession(sessionId)
                .enqueue(new Callback<ApiResponse<LiveAttendanceSessionResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call,
                                           @NonNull Response<ApiResponse<LiveAttendanceSessionResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoadingLive.setVisibility(View.GONE);
                        binding.swipeRefreshLive.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            bindData(response.body().getData());
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Không thể tải phiên điểm danh";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoadingLive.setVisibility(View.GONE);
                        binding.swipeRefreshLive.setRefreshing(false);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindData(LiveAttendanceSessionResponse data) {
        if (data == null || binding == null) return;

        if (!TextUtils.isEmpty(data.getClassName())) {
            className = data.getClassName();
            binding.tvLiveClassName.setText(className);
        }
        if (!TextUtils.isEmpty(data.getClassCode())) {
            classCode = data.getClassCode();
            binding.tvLiveClassCodeRoom.setText(classCode + (!TextUtils.isEmpty(data.getRoom()) ? " · " + data.getRoom() : ""));
        }

        int total = Math.max(0, data.getTotalStudents());
        int present = data.getPresentCount();
        int absent = data.getAbsentCount() > 0 ? data.getAbsentCount() : Math.max(0, total - present);

        binding.tvPresentCount.setText(String.valueOf(present));
        binding.tvAbsentCount.setText(String.valueOf(absent));
        binding.tvTotalCount.setText(String.valueOf(total));

        int ratePercent = total > 0 ? (int) Math.round((present * 100.0) / total) : 0;
        binding.progressAttendanceLive.setProgress(ratePercent);
        binding.tvProgressRatePercent.setText(ratePercent + "%");
        binding.tvProgressAttendanceText.setText("Đã điểm danh: " + present + "/" + total + " (" + ratePercent + "%)");

        List<AttendeeItem> attendees = data.getAttendees();
        if (attendees == null || attendees.isEmpty()) {
            adapter.setItems(new ArrayList<>());
            binding.rvLiveAttendees.setVisibility(View.GONE);
            binding.layoutEmptyAttendees.setVisibility(View.VISIBLE);
            binding.tvLiveAttendeesCountLabel.setText("0 sinh viên");
        } else {
            adapter.setItems(attendees);
            binding.rvLiveAttendees.setVisibility(View.VISIBLE);
            binding.layoutEmptyAttendees.setVisibility(View.GONE);
            binding.tvLiveAttendeesCountLabel.setText(attendees.size() + " sinh viên");
        }

        if ("CLOSED".equalsIgnoreCase(data.getStatus()) || isSessionClosed) {
            markSessionClosedUi();
        }
    }

    private void executeCloseSession() {
        if (getContext() == null || binding == null) return;

        binding.pbLoadingLive.setVisibility(View.VISIBLE);

        ApiClient.getUserClassApiService(requireContext())
                .closeAttendanceSession(sessionId)
                .enqueue(new Callback<ApiResponse<LiveAttendanceSessionResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call,
                                           @NonNull Response<ApiResponse<LiveAttendanceSessionResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoadingLive.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            markSessionClosedUi();
                            Toast.makeText(requireContext(), "Đã chốt phiên điểm danh thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Chốt phiên điểm danh thất bại";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoadingLive.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markSessionClosedUi() {
        isSessionClosed = true;
        binding.tvLiveSessionStatus.setText(R.string.session_status_closed);
        binding.tvLiveSessionStatus.setBackgroundResource(R.drawable.bg_pill_gray);
        binding.tvLiveSessionStatus.setTextColor(requireContext().getColor(R.color.text_secondary));

        binding.btnCloseSessionLive.setEnabled(true);
        binding.btnCloseSessionLive.setText("Quay về chi tiết lớp");
        binding.btnCloseSessionLive.setOnClickListener(v -> navigateBackToClassDetail());
        binding.btnCloseSessionLive.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.color_primary)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
