package com.ppquang06.uniclassapp.ui.student.classes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentAssignmentDetailBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;
import com.ppquang06.uniclassapp.utils.UriFileMultipartHelper;

import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAssignmentDetailFragment extends Fragment {

    public static final String ARG_ASSIGNMENT_ID = "assignmentId";
    public static final String ARG_ASSIGNMENT_TITLE = "assignmentTitle";
    public static final String ARG_CLASS_NAME = "className";

    public static final int STATE_OPEN = 1;
    public static final int STATE_DUE_SOON = 2;
    public static final int STATE_OVERDUE = 3;
    public static final int STATE_FILE_CHOSEN = 4;
    public static final int STATE_SUBMITTED = 5;

    private FragmentStudentAssignmentDetailBinding binding;

    private String assignmentId;
    private String assignmentTitle;
    private String className;

    private AssignmentDetailResponse currentDetail;
    private String selectedFileName;
    private String selectedFileUrl;
    private boolean isUploadingFile = false;
    private Call<ApiResponse<FileUploadResponse>> activeUploadCall;

    public static int resolveAssignmentVisualState(boolean submitted, String deadlineIso, boolean hasChosenFile, long nowMillis) {
        if (submitted && !hasChosenFile) {
            return STATE_SUBMITTED;
        }
        if (hasChosenFile) {
            return STATE_FILE_CHOSEN;
        }
        UiBadgeHelper.Status badgeStatus = UiBadgeHelper.resolveAssignmentStatus(submitted, deadlineIso, nowMillis);
        if (badgeStatus == UiBadgeHelper.Status.OVERDUE) {
            return STATE_OVERDUE;
        }
        if (badgeStatus == UiBadgeHelper.Status.DUE_SOON) {
            return STATE_DUE_SOON;
        }
        return STATE_OPEN;
    }

    public static int getFileIconRes(String fileName) {
        if (fileName == null) return R.drawable.ic_file_document;
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return R.drawable.ic_file_pdf;
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return R.drawable.ic_file_word;
        if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) return R.drawable.ic_file_zip;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) return R.drawable.ic_file_image;
        return R.drawable.ic_file_document;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentAssignmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupTopAppBar();
        setupListeners();
        loadAssignmentDetail();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            assignmentId = args.getString(ARG_ASSIGNMENT_ID, "");
            assignmentTitle = args.getString(ARG_ASSIGNMENT_TITLE, "");
            className = args.getString(ARG_CLASS_NAME, "");
        }
    }

    private void setupTopAppBar() {
        binding.topAppBar.tvTitle.setText(R.string.assignment_detail_title);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);

        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadAssignmentDetail);

        binding.layoutDropzone.setOnClickListener(v -> openFilePicker());

        binding.ivRemoveSelectedFile.setOnClickListener(v -> {
            if (activeUploadCall != null && !activeUploadCall.isCanceled()) {
                activeUploadCall.cancel();
            }
            selectedFileName = null;
            selectedFileUrl = null;
            isUploadingFile = false;
            renderState();
        });

        binding.btnSubmit.setOnClickListener(v -> performSubmitAssignment());

        binding.btnResubmit.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        PickFileTypeBottomSheetDialogFragment sheet = PickFileTypeBottomSheetDialogFragment.newInstance();
        sheet.setOnFilePickedListener(this::handleFilePicked);
        sheet.show(getChildFragmentManager(), "PickFileTypeBottomSheet");
    }

    private void handleFilePicked(@NonNull Uri uri, @NonNull String fileName) {
        if (getContext() == null) return;
        selectedFileName = fileName;
        selectedFileUrl = null;
        isUploadingFile = true;
        renderState();
        uploadSelectedFile(uri, fileName);
    }

    private void uploadSelectedFile(@NonNull Uri uri, @NonNull String fileName) {
        if (getContext() == null) return;
        try {
            MultipartBody.Part filePart = UriFileMultipartHelper.createMultipartPart(requireContext(), uri, "file");
            RequestBody folderPart = UriFileMultipartHelper.createFolderRequestBody("assignments");

            if (activeUploadCall != null && !activeUploadCall.isCanceled()) {
                activeUploadCall.cancel();
            }
            activeUploadCall = ApiClient.getUserClassApiService(requireContext())
                    .uploadFile(filePart, folderPart);
            activeUploadCall.enqueue(new Callback<ApiResponse<FileUploadResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<FileUploadResponse>> call,
                                               @NonNull Response<ApiResponse<FileUploadResponse>> response) {
                            if (call.isCanceled() || !isAdded() || binding == null) return;
                            isUploadingFile = false;

                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                FileUploadResponse data = response.body().getData();
                                selectedFileUrl = data.getFileUrl();
                                if (data.getFileName() != null && !data.getFileName().trim().isEmpty()) {
                                    selectedFileName = data.getFileName();
                                }
                                renderState();
                                if (getContext() != null) {
                                    Toast.makeText(requireContext(), "Tải tệp lên thành công", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String msg = "Tải tệp lên máy chủ thất bại";
                                if (response.body() != null && response.body().getMessage() != null) {
                                    msg = response.body().getMessage();
                                }
                                if (getContext() != null) {
                                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                                selectedFileName = null;
                                selectedFileUrl = null;
                                renderState();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<FileUploadResponse>> call, @NonNull Throwable t) {
                            if (call.isCanceled() || !isAdded() || binding == null) return;
                            isUploadingFile = false;
                            selectedFileName = null;
                            selectedFileUrl = null;
                            renderState();
                            if (getContext() != null) {
                                Toast.makeText(requireContext(), "Lỗi kết nối khi tải tệp", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            isUploadingFile = false;
            selectedFileName = null;
            selectedFileUrl = null;
            renderState();
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Không thể đọc tệp đã chọn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadAssignmentDetail() {
        if (getContext() == null || assignmentId == null || assignmentId.trim().isEmpty()) {
            if (binding != null) {
                binding.swipeRefresh.setRefreshing(false);
            }
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Mã bài tập không hợp lệ", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        binding.swipeRefresh.setRefreshing(true);
        ApiClient.getUserClassApiService(requireContext())
                .getAssignmentDetail(assignmentId)
                .enqueue(new Callback<ApiResponse<AssignmentDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AssignmentDetailResponse>> call,
                                           @NonNull Response<ApiResponse<AssignmentDetailResponse>> response) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            currentDetail = response.body().getData();
                            renderState();
                        } else {
                            String msg = "Không thể tải chi tiết bài tập";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            if (getContext() != null) {
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AssignmentDetailResponse>> call, @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void performSubmitAssignment() {
        if (isUploadingFile) {
            Toast.makeText(requireContext(), "Đang tải tệp lên máy chủ, vui lòng đợi giây lát", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedFileUrl == null || selectedFileUrl.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn tệp bài làm trước khi nộp", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.pbUploadProgress.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        SubmitAssignmentRequest request = new SubmitAssignmentRequest(selectedFileUrl, selectedFileName);
        ApiClient.getUserClassApiService(requireContext())
                .submitAssignment(assignmentId, request)
                .enqueue(new Callback<ApiResponse<AssignmentDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AssignmentDetailResponse>> call,
                                           @NonNull Response<ApiResponse<AssignmentDetailResponse>> response) {
                        if (binding == null) return;
                        binding.pbUploadProgress.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Toast.makeText(requireContext(), R.string.assignment_submit_success, Toast.LENGTH_SHORT).show();
                            currentDetail = response.body().getData();
                            selectedFileName = null;
                            selectedFileUrl = null;
                            isUploadingFile = false;
                            renderState();
                        } else {
                            String msg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage()
                                    : "Nộp bài thất bại. Vui lòng thử lại.";
                            if (getContext() != null) {
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AssignmentDetailResponse>> call, @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.pbUploadProgress.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Nộp bài thất bại: " + (t.getMessage() != null ? t.getMessage() : "Lỗi kết nối máy chủ"), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void renderState() {
        if (binding == null || currentDetail == null) return;

        // 1. Header Card Info
        String resolvedClass = currentDetail.getClassName() != null && !currentDetail.getClassName().isEmpty()
                ? currentDetail.getClassName() : className;
        binding.tvClassName.setText(resolvedClass != null ? resolvedClass : "");

        String resolvedTitle = currentDetail.getTitle() != null && !currentDetail.getTitle().isEmpty()
                ? currentDetail.getTitle() : assignmentTitle;
        binding.tvAssignmentTitle.setText(resolvedTitle != null ? resolvedTitle : "");

        // Deadline Badge & Due Date
        long nowMillis = System.currentTimeMillis();
        boolean isSubmitted = currentDetail.isSubmitted();
        UiBadgeHelper.Status badgeStatus = UiBadgeHelper.resolveAssignmentStatus(isSubmitted, currentDetail.getDeadline(), nowMillis);
        String badgeLabel = UiBadgeHelper.getAssignmentStatusLabel(badgeStatus);
        UiBadgeHelper.applyBadge(binding.tvDeadlineBadge, badgeStatus, badgeLabel);

        String formattedDue = UiBadgeHelper.formatIsoDateShort(currentDetail.getDeadline());
        if (!formattedDue.isEmpty()) {
            binding.tvDueDateText.setText(getString(R.string.assignment_due_prefix, formattedDue));
        } else {
            binding.tvDueDateText.setText("");
        }

        // 2. Banner Logic (Overdue / Due soon)
        if (!isSubmitted && badgeStatus == UiBadgeHelper.Status.OVERDUE) {
            binding.bannerWarningOrOverdue.getRoot().setVisibility(View.VISIBLE);
            binding.bannerWarningOrOverdue.tvBannerTitle.setText(R.string.assignment_warning_overdue);
            binding.bannerWarningOrOverdue.tvBannerSubtitle.setText("Hệ thống đã khóa hoặc ghi nhận bài nộp muộn.");
            binding.bannerWarningOrOverdue.btnBannerAction.setVisibility(View.GONE);
            binding.bannerWarningOrOverdue.ivBannerIcon.setImageResource(R.drawable.ic_info_outline);
        } else if (!isSubmitted && badgeStatus == UiBadgeHelper.Status.DUE_SOON) {
            binding.bannerWarningOrOverdue.getRoot().setVisibility(View.VISIBLE);
            binding.bannerWarningOrOverdue.tvBannerTitle.setText(R.string.assignment_warning_due_soon);
            binding.bannerWarningOrOverdue.tvBannerSubtitle.setText("Vui lòng hoàn thành và nộp bài trước hạn chót.");
            binding.bannerWarningOrOverdue.btnBannerAction.setVisibility(View.GONE);
            binding.bannerWarningOrOverdue.ivBannerIcon.setImageResource(R.drawable.ic_info_outline);
        } else {
            binding.bannerWarningOrOverdue.getRoot().setVisibility(View.GONE);
        }

        // 3. Submission Section
        boolean hasChosenFile = selectedFileName != null && !selectedFileName.trim().isEmpty();

        if (isSubmitted && !hasChosenFile) {
            // State 5: Submitted
            binding.layoutDropzone.setVisibility(View.GONE);
            binding.layoutSelectedFile.setVisibility(View.GONE);
            binding.layoutSubmittedFile.setVisibility(View.VISIBLE);

            String submittedName = extractFileNameFromUrl(currentDetail.getSubmittedFileUrl());
            binding.tvSubmittedFileName.setText(submittedName);
            binding.ivSubmittedFileIcon.setImageResource(getFileIconRes(submittedName));

            String formattedSubmittedAt = UiBadgeHelper.formatIsoDateShort(currentDetail.getSubmittedAt());
            if (!formattedSubmittedAt.isEmpty()) {
                binding.tvSubmittedAt.setText(getString(R.string.assignment_submitted_at_prefix, formattedSubmittedAt));
            } else {
                binding.tvSubmittedAt.setText("Đã nộp thành công");
            }

            // Grading Result
            if (currentDetail.getScore() == null) {
                binding.tvGradingStatus.setVisibility(View.VISIBLE);
                binding.layoutScoreAndFeedback.setVisibility(View.GONE);
            } else {
                binding.tvGradingStatus.setVisibility(View.GONE);
                binding.layoutScoreAndFeedback.setVisibility(View.VISIBLE);
                binding.tvScoreValue.setText(String.format(Locale.getDefault(), "%.1f / 10", currentDetail.getScore()));
                String feedback = currentDetail.getFeedback();
                if (feedback != null && !feedback.trim().isEmpty()) {
                    binding.tvLecturerFeedback.setText(getString(R.string.assignment_feedback_prefix, feedback));
                } else {
                    binding.tvLecturerFeedback.setText("Chưa có nhận xét chi tiết từ giảng viên.");
                }
            }

            // Bottom Buttons
            binding.btnSubmit.setVisibility(View.GONE);
            binding.btnResubmit.setVisibility(View.VISIBLE);

        } else if (hasChosenFile) {
            // State 4: File Chosen (Ready to submit / resubmit)
            binding.layoutDropzone.setVisibility(View.GONE);
            binding.layoutSelectedFile.setVisibility(View.VISIBLE);
            binding.tvSelectedFileName.setText(selectedFileName);
            binding.ivSelectedFileIcon.setImageResource(getFileIconRes(selectedFileName));

            if (isSubmitted) {
                binding.layoutSubmittedFile.setVisibility(View.VISIBLE);
                binding.btnSubmit.setText(R.string.btn_resubmit_assignment);
            } else {
                binding.layoutSubmittedFile.setVisibility(View.GONE);
                binding.btnSubmit.setText(R.string.btn_submit_assignment);
            }

            binding.btnSubmit.setVisibility(View.VISIBLE);
            if (isUploadingFile) {
                binding.pbUploadProgress.setVisibility(View.VISIBLE);
                binding.btnSubmit.setEnabled(false);
                binding.tvSelectedFileMeta.setText("Đang tải tệp lên...");
            } else {
                binding.pbUploadProgress.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(selectedFileUrl != null && !selectedFileUrl.trim().isEmpty());
                binding.tvSelectedFileMeta.setText(selectedFileUrl != null ? "Sẵn sàng nộp bài" : "");
            }
            binding.btnResubmit.setVisibility(View.GONE);

        } else {
            // State 1, 2, or 3: Not submitted & No file chosen
            binding.layoutDropzone.setVisibility(View.VISIBLE);
            binding.layoutSelectedFile.setVisibility(View.GONE);
            binding.layoutSubmittedFile.setVisibility(View.GONE);
            binding.pbUploadProgress.setVisibility(View.GONE);

            binding.btnSubmit.setVisibility(View.VISIBLE);
            binding.btnSubmit.setText(R.string.btn_submit_assignment);
            binding.btnSubmit.setEnabled(false); // Disabled until file chosen
            binding.btnResubmit.setVisibility(View.GONE);
        }

        // 4. Requirement Card & Attachment
        String desc = currentDetail.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            binding.tvAssignmentDescription.setText(desc);
            binding.tvAssignmentDescription.setVisibility(View.VISIBLE);
        } else {
            binding.tvAssignmentDescription.setText("Không có yêu cầu mô tả chi tiết.");
        }

        String attachmentUrl = currentDetail.getAttachmentUrl();
        if (attachmentUrl != null && !attachmentUrl.trim().isEmpty()) {
            binding.layoutAttachment.setVisibility(View.VISIBLE);
            String attName = extractFileNameFromUrl(attachmentUrl);
            binding.tvAttachmentName.setText(attName != null && !attName.isEmpty() ? attName : getString(R.string.assignment_attachment_title));
            binding.layoutAttachment.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(attachmentUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Không thể mở liên kết đính kèm", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            binding.layoutAttachment.setVisibility(View.GONE);
        }
    }

    private String extractFileNameFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "tep_dinh_kem.pdf";
        }
        String clean = url.trim();
        int slashIndex = clean.lastIndexOf('/');
        if (slashIndex != -1 && slashIndex < clean.length() - 1) {
            String name = clean.substring(slashIndex + 1);
            int qIndex = name.indexOf('?');
            return qIndex != -1 ? name.substring(0, qIndex) : name;
        }
        return clean;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (activeUploadCall != null && !activeUploadCall.isCanceled()) {
            activeUploadCall.cancel();
        }
        binding = null;
    }
}
