package com.ppquang06.uniclassapp.ui.lecturer.assignments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse.SubmissionItem;
import com.ppquang06.uniclassapp.databinding.DialogPublishGradeBinding;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerGradingBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerGradingFragment extends Fragment {

    private FragmentLecturerGradingBinding binding;
    private final Gson gson = new Gson();

    private String assignmentId = "";
    private String assignmentTitle = "";
    private String studentId = "";
    private String studentName = "";

    private List<SubmissionItem> submissions = new ArrayList<>();
    private int currentIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerGradingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parseArguments();
        if (submissions.isEmpty()) {
            Toast.makeText(requireContext(), "Không có bài nộp nào để chấm", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        setupTopBar();
        setupQuickScoreChips();
        setupStudentSwitcher();
        setupActionButtons();

        displayStudent(currentIndex);
    }

    private void parseArguments() {
        if (getArguments() == null) return;

        assignmentId = getArguments().getString("assignmentId", "");
        assignmentTitle = getArguments().getString("assignmentTitle", "");
        studentId = getArguments().getString("studentId", "");
        studentName = getArguments().getString("studentName", "");
        int initialIndex = getArguments().getInt("initialIndex", 0);

        String json = getArguments().getString("submissionsJson", "");
        if (!TextUtils.isEmpty(json)) {
            try {
                Type listType = new TypeToken<List<SubmissionItem>>() {}.getType();
                List<SubmissionItem> parsed = gson.fromJson(json, listType);
                if (parsed != null && !parsed.isEmpty()) {
                    submissions.addAll(parsed);
                }
            } catch (Exception ignored) {}
        }

        // Clamp index
        if (!submissions.isEmpty()) {
            if (initialIndex >= 0 && initialIndex < submissions.size()) {
                currentIndex = initialIndex;
            } else {
                currentIndex = 0;
                // Try matching studentId
                for (int i = 0; i < submissions.size(); i++) {
                    if (submissions.get(i).getStudentId().equals(studentId)) {
                        currentIndex = i;
                        break;
                    }
                }
            }
        }
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.grading_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupQuickScoreChips() {
        binding.chipScore10.setOnClickListener(v -> setScoreInputValue(10.0));
        binding.chipScore9.setOnClickListener(v -> setScoreInputValue(9.0));
        binding.chipScore85.setOnClickListener(v -> setScoreInputValue(8.5));
        binding.chipScore8.setOnClickListener(v -> setScoreInputValue(8.0));
        binding.chipScore7.setOnClickListener(v -> setScoreInputValue(7.0));
        binding.chipScore5.setOnClickListener(v -> setScoreInputValue(5.0));
    }

    private void setScoreInputValue(double score) {
        String scoreStr = (score == Math.floor(score)) ?
                String.format(Locale.getDefault(), "%.0f", score) :
                String.format(Locale.getDefault(), "%.1f", score);
        binding.etScore.setText(scoreStr);
        binding.tilScore.setError(null);
    }

    private void setupStudentSwitcher() {
        binding.btnPrevStudent.setOnClickListener(v -> {
            if (currentIndex > 0) {
                saveCurrentInputsToMemory();
                currentIndex--;
                displayStudent(currentIndex);
            }
        });

        binding.btnNextStudent.setOnClickListener(v -> {
            if (currentIndex < submissions.size() - 1) {
                saveCurrentInputsToMemory();
                currentIndex++;
                displayStudent(currentIndex);
            }
        });
    }

    private void setupActionButtons() {
        binding.btnOpenFile.setOnClickListener(v -> openSubmittedFile());
        binding.btnSaveDraft.setOnClickListener(v -> saveDraftGrade());
        binding.btnPublishGrade.setOnClickListener(v -> showPublishGradeDialog());
    }

    private void displayStudent(int index) {
        if (index < 0 || index >= submissions.size() || binding == null) return;

        SubmissionItem item = submissions.get(index);

        // Avatar Initial
        String initial = item.getAvatarInitial();
        if (TextUtils.isEmpty(initial)) {
            initial = getInitialFromName(item.getStudentName());
        }
        binding.tvAvatarInitial.setText(initial);

        // Student Info
        binding.tvStudentName.setText(item.getStudentName() != null ? item.getStudentName() : "");
        String code = item.getStudentCode() != null ? "MSSV: " + item.getStudentCode() : "";
        binding.tvStudentCode.setText(code);
        binding.tvCurrentStudentInfo.setText(item.getStudentName() + " - " + item.getStudentCode());

        // File details
        if (!TextUtils.isEmpty(item.getFileUrl())) {
            binding.layoutFileDetails.setVisibility(View.VISIBLE);
            binding.tvNoFileWarning.setVisibility(View.GONE);
            binding.tvFileName.setText(extractFileName(item.getFileUrl()));
            binding.btnOpenFile.setEnabled(true);

            if (item.isSubmitted()) {
                String subDate = item.getSubmittedAt() != null ? item.getSubmittedAt() : "";
                binding.tvSubmissionDate.setText("Nộp lúc: " + subDate);
                if (item.isLate()) {
                    binding.tvFileStatusBadge.setText(R.string.status_late);
                    binding.tvFileStatusBadge.setBackgroundResource(R.drawable.bg_pill_warning);
                    binding.tvFileStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_warning));
                } else {
                    binding.tvFileStatusBadge.setText(R.string.status_on_time);
                    binding.tvFileStatusBadge.setBackgroundResource(R.drawable.bg_pill_green);
                    binding.tvFileStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_success));
                }
            } else {
                binding.tvSubmissionDate.setText("Chưa nộp");
                binding.tvFileStatusBadge.setText(R.string.status_not_submitted);
                binding.tvFileStatusBadge.setBackgroundResource(R.drawable.bg_pill_gray);
                binding.tvFileStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }
        } else {
            binding.layoutFileDetails.setVisibility(View.GONE);
            binding.tvNoFileWarning.setVisibility(View.VISIBLE);
            binding.btnOpenFile.setEnabled(false);
        }

        // Score
        binding.tilScore.setError(null);
        if (item.getScore() != null) {
            String scoreStr = (item.getScore() == Math.floor(item.getScore())) ?
                    String.format(Locale.getDefault(), "%.0f", item.getScore()) :
                    String.format(Locale.getDefault(), "%.1f", item.getScore());
            binding.etScore.setText(scoreStr);
        } else {
            binding.etScore.setText("");
        }

        // Feedback
        if (!TextUtils.isEmpty(item.getFeedback())) {
            binding.etFeedback.setText(item.getFeedback());
        } else {
            binding.etFeedback.setText("");
        }

        // Navigation buttons state
        binding.btnPrevStudent.setEnabled(index > 0);
        binding.btnNextStudent.setEnabled(index < submissions.size() - 1);

        updateGradingProgress();
    }

    private void updateGradingProgress() {
        int gradedCount = 0;
        int total = submissions.size();
        for (SubmissionItem item : submissions) {
            if (item.getScore() != null) {
                gradedCount++;
            }
        }

        binding.tvGradingProgress.setText(getString(R.string.label_grading_progress, gradedCount, total));
        int percent = (total > 0) ? (gradedCount * 100) / total : 0;
        binding.progressGraded.setProgress(percent);
        binding.tvGradingPercent.setText(percent + "%");
    }

    private void saveCurrentInputsToMemory() {
        if (currentIndex < 0 || currentIndex >= submissions.size() || binding == null) return;
        SubmissionItem item = submissions.get(currentIndex);

        String scoreStr = binding.etScore.getText() != null ? binding.etScore.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(scoreStr)) {
            try {
                double val = Double.parseDouble(scoreStr);
                if (val >= 0.0 && val <= 10.0) {
                    item.setScore(val);
                }
            } catch (NumberFormatException ignored) {}
        }

        String fb = binding.etFeedback.getText() != null ? binding.etFeedback.getText().toString().trim() : "";
        item.setFeedback(fb);
    }

    private Double validateAndGetScore() {
        binding.tilScore.setError(null);
        String scoreStr = binding.etScore.getText() != null ? binding.etScore.getText().toString().trim() : "";
        if (TextUtils.isEmpty(scoreStr)) {
            binding.tilScore.setError(getString(R.string.err_invalid_score));
            return null;
        }

        try {
            double score = Double.parseDouble(scoreStr);
            if (score < 0.0 || score > 10.0) {
                binding.tilScore.setError(getString(R.string.err_invalid_score));
                return null;
            }
            return score;
        } catch (NumberFormatException e) {
            binding.tilScore.setError(getString(R.string.err_invalid_score));
            return null;
        }
    }

    private void openSubmittedFile() {
        if (currentIndex < 0 || currentIndex >= submissions.size()) return;
        SubmissionItem item = submissions.get(currentIndex);
        if (TextUtils.isEmpty(item.getFileUrl())) {
            Toast.makeText(requireContext(), R.string.no_file_attached, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(item.getFileUrl()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không tìm thấy ứng dụng mở tệp: " + item.getFileUrl(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDraftGrade() {
        Double score = validateAndGetScore();
        if (score == null) return;

        SubmissionItem item = submissions.get(currentIndex);
        String feedback = binding.etFeedback.getText() != null ? binding.etFeedback.getText().toString().trim() : "";

        item.setScore(score);
        item.setFeedback(feedback);
        updateGradingProgress();

        GradeSubmissionRequest request = new GradeSubmissionRequest(score, feedback, false);

        binding.pbLoading.setVisibility(View.VISIBLE);
        ApiClient.getUserClassApiService(requireContext())
                .gradeSubmission(assignmentId, item.getStudentId(), request)
                .enqueue(new Callback<ApiResponse<AssignmentSubmissionListResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AssignmentSubmissionListResponse>> call,
                                           @NonNull Response<ApiResponse<AssignmentSubmissionListResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), R.string.toast_draft_saved, Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Lưu bản nháp thất bại";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AssignmentSubmissionListResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPublishGradeDialog() {
        Double score = validateAndGetScore();
        if (score == null) return;

        DialogPublishGradeBinding dialogBinding = DialogPublishGradeBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.btn_confirm_publish, (dialog, which) -> {
                    boolean publishAll = dialogBinding.rbPublishAll.isChecked();
                    confirmPublishGrade(score, publishAll);
                })
                .show();
    }

    private void confirmPublishGrade(double score, boolean publishAll) {
        SubmissionItem item = submissions.get(currentIndex);
        String feedback = binding.etFeedback.getText() != null ? binding.etFeedback.getText().toString().trim() : "";

        item.setScore(score);
        item.setFeedback(feedback);
        updateGradingProgress();

        GradeSubmissionRequest request = new GradeSubmissionRequest(score, feedback, publishAll);

        binding.pbLoading.setVisibility(View.VISIBLE);
        ApiClient.getUserClassApiService(requireContext())
                .gradeSubmission(assignmentId, item.getStudentId(), request)
                .enqueue(new Callback<ApiResponse<AssignmentSubmissionListResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AssignmentSubmissionListResponse>> call,
                                           @NonNull Response<ApiResponse<AssignmentSubmissionListResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            onGradePublishedSuccess();
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage() : "Chấm điểm thất bại";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AssignmentSubmissionListResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onGradePublishedSuccess() {
        Toast.makeText(requireContext(), R.string.toast_grade_published, Toast.LENGTH_SHORT).show();

        // Advance to next student or pop back
        if (currentIndex < submissions.size() - 1) {
            currentIndex++;
            displayStudent(currentIndex);
        } else {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private static String getInitialFromName(String name) {
        if (TextUtils.isEmpty(name)) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }

    private static String extractFileName(String url) {
        if (url == null) return "attachment";
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < url.length() - 1) {
            return url.substring(lastSlash + 1);
        }
        return url;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
