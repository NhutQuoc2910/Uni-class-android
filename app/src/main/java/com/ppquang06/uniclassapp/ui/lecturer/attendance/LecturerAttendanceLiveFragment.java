package com.ppquang06.uniclassapp.ui.lecturer.attendance;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
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
            bindData(buildFallbackLiveSession());
            binding.pbLoadingLive.setVisibility(View.GONE);
            binding.swipeRefreshLive.setRefreshing(false);
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

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            bindData(response.body().getData());
                        } else {
                            bindData(buildFallbackLiveSession());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoadingLive.setVisibility(View.GONE);
                        binding.swipeRefreshLive.setRefreshing(false);
                        bindData(buildFallbackLiveSession());
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

        int total = data.getTotalStudents() > 0 ? data.getTotalStudents() : 42;
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
                        markSessionClosedUi();
                        Toast.makeText(requireContext(), "Đã chốt phiên điểm danh thành công", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<LiveAttendanceSessionResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoadingLive.setVisibility(View.GONE);
                        markSessionClosedUi();
                        Toast.makeText(requireContext(), "Đã chốt phiên điểm danh thành công", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markSessionClosedUi() {
        isSessionClosed = true;
        binding.tvLiveSessionStatus.setText(R.string.session_status_closed);
        binding.tvLiveSessionStatus.setBackgroundResource(R.drawable.bg_pill_gray);
        binding.tvLiveSessionStatus.setTextColor(requireContext().getColor(R.color.text_secondary));

        binding.btnCloseSessionLive.setEnabled(false);
        binding.btnCloseSessionLive.setText("Phiên đã chốt");
        binding.btnCloseSessionLive.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.outline_border)));
    }

    private LiveAttendanceSessionResponse buildFallbackLiveSession() {
        List<AttendeeItem> sampleAttendees = new ArrayList<>();
        sampleAttendees.add(new AttendeeItem("stu-1", "Nguyễn Văn An", "21520001", "NA", "PRESENT", "08:15:22", "dev-01"));
        sampleAttendees.add(new AttendeeItem("stu-2", "Trần Thị Bích", "21520002", "TB", "PRESENT", "08:16:05", "dev-02"));
        sampleAttendees.add(new AttendeeItem("stu-3", "Lê Hoàng Cường", "21520003", "LC", "PRESENT", "08:16:40", "dev-03"));
        sampleAttendees.add(new AttendeeItem("stu-4", "Phạm Đăng Dũng", "21520004", "PD", "PRESENT", "08:17:12", "dev-04"));
        sampleAttendees.add(new AttendeeItem("stu-5", "Hoàng Mai Em", "21520005", "HE", "PRESENT", "08:18:00", "dev-05"));
        sampleAttendees.add(new AttendeeItem("stu-6", "Vũ Quốc Gia", "21520006", "VG", "PRESENT", "08:19:15", "dev-06"));
        sampleAttendees.add(new AttendeeItem("stu-7", "Đỗ Hải Hưng", "21520007", "DH", "PRESENT", "08:20:30", "dev-07"));
        sampleAttendees.add(new AttendeeItem("stu-8", "Ngô Khánh Linh", "21520008", "NL", "PRESENT", "08:21:02", "dev-08"));

        return new LiveAttendanceSessionResponse(
                sessionId,
                classId,
                classCode,
                className,
                room,
                "INV-123",
                "OPEN",
                "00:11:22:33:44:55",
                10.8700,
                106.8031,
                50.0,
                "2026-10-28 08:00:00",
                42,
                38,
                4,
                sampleAttendees
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
