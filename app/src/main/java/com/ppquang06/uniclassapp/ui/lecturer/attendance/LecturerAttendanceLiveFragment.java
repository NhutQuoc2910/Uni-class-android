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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.StompWebSocketClient;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse.AttendeeItem;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerAttendanceLiveBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class LecturerAttendanceLiveFragment extends Fragment {

    private static final String TAG = "LecturerAttendanceLive";

    private FragmentLecturerAttendanceLiveBinding binding;
    private AttendanceViewModel viewModel;
    private LiveAttendeeAdapter adapter;
    private StompWebSocketClient stompClient;

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

        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

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
        observeViewModel();

        loadLiveSession();
    }

    @Override
    public void onStart() {
        super.onStart();
        connectWebSocket();
    }

    @Override
    public void onStop() {
        super.onStop();
        disconnectWebSocket();
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

    private void observeViewModel() {
        viewModel.getLiveSessionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (!binding.swipeRefreshLive.isRefreshing()) {
                        binding.pbLoadingLive.setVisibility(View.VISIBLE);
                    }
                    break;
                case SUCCESS:
                    binding.pbLoadingLive.setVisibility(View.GONE);
                    binding.swipeRefreshLive.setRefreshing(false);
                    bindData(state.getData());
                    break;
                case ERROR:
                    binding.pbLoadingLive.setVisibility(View.GONE);
                    binding.swipeRefreshLive.setRefreshing(false);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Không thể tải phiên điểm danh";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    break;
            }
        });

        viewModel.getCloseSessionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbLoadingLive.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.pbLoadingLive.setVisibility(View.GONE);
                    markSessionClosedUi();
                    Toast.makeText(requireContext(), "Đã chốt phiên điểm danh thành công", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    binding.pbLoadingLive.setVisibility(View.GONE);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Chốt phiên điểm danh thất bại";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void loadLiveSession() {
        if (getContext() == null || binding == null) return;

        if (TextUtils.isEmpty(sessionId)) {
            binding.pbLoadingLive.setVisibility(View.GONE);
            binding.swipeRefreshLive.setRefreshing(false);
            Toast.makeText(requireContext(), "Thiếu mã phiên điểm danh", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.loadLiveSession(sessionId);
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
        viewModel.closeSession(sessionId);
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

    private void connectWebSocket() {
        if (TextUtils.isEmpty(sessionId) || getContext() == null) {
            return;
        }
        disconnectWebSocket();
        stompClient = new StompWebSocketClient();
        String wsUrl = StompWebSocketClient.getDefaultWebSocketUrl();
        String token = SessionManager.getInstance(requireContext()).getToken();

        stompClient.connect(wsUrl, token, new StompWebSocketClient.StompListener() {
            @Override
            public void onConnected() {
                if (!isAdded() || binding == null || stompClient == null) return;
                stompClient.subscribe("/topic/attendance-sessions/" + sessionId + "/live", payload -> {
                    if (!isAdded() || binding == null || TextUtils.isEmpty(payload)) return;
                    try {
                        LiveAttendanceSessionResponse data = new Gson().fromJson(payload, LiveAttendanceSessionResponse.class);
                        if (data != null) {
                            bindData(data);
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error parsing live STOMP payload", e);
                    }
                });
            }

            @Override
            public void onDisconnected() {
                android.util.Log.d(TAG, "STOMP WebSocket disconnected");
            }

            @Override
            public void onError(Throwable t) {
                android.util.Log.w(TAG, "STOMP WebSocket error: " + (t != null ? t.getMessage() : "unknown"));
            }
        });
    }

    private void disconnectWebSocket() {
        if (stompClient != null) {
            try {
                stompClient.disconnect();
            } catch (Exception ignored) {
            }
            stompClient = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disconnectWebSocket();
        binding = null;
    }
}
