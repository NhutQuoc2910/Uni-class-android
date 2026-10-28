package com.ppquang06.uniclassapp.ui.student.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

import org.junit.Test;

import java.time.Instant;

public class AssignmentDetailFlowTest {

    private final Gson gson = new Gson();

    @Test
    public void resolveAssignmentVisualState_openStateWhenFarDeadlineAndNoFile() {
        long now = Instant.parse("2026-10-28T08:00:00Z").toEpochMilli();
        String deadline = "2026-11-10T23:59:59Z"; // 13 days later

        int state = StudentAssignmentDetailFragment.resolveAssignmentVisualState(
                false,
                deadline,
                false,
                now
        );
        assertEquals(StudentAssignmentDetailFragment.STATE_OPEN, state);
    }

    @Test
    public void resolveAssignmentVisualState_dueSoonStateWhenWithin72Hours() {
        long now = Instant.parse("2026-10-28T08:00:00Z").toEpochMilli();
        String deadline = "2026-10-30T10:00:00Z"; // ~50 hours later (< 72h)

        int state = StudentAssignmentDetailFragment.resolveAssignmentVisualState(
                false,
                deadline,
                false,
                now
        );
        assertEquals(StudentAssignmentDetailFragment.STATE_DUE_SOON, state);
    }

    @Test
    public void resolveAssignmentVisualState_overdueStateWhenPastDeadline() {
        long now = Instant.parse("2026-10-28T08:00:00Z").toEpochMilli();
        String deadline = "2026-10-27T23:59:59Z"; // Yesterday

        int state = StudentAssignmentDetailFragment.resolveAssignmentVisualState(
                false,
                deadline,
                false,
                now
        );
        assertEquals(StudentAssignmentDetailFragment.STATE_OVERDUE, state);
    }

    @Test
    public void resolveAssignmentVisualState_fileChosenStateWhenLocalFilePicked() {
        long now = Instant.parse("2026-10-28T08:00:00Z").toEpochMilli();
        String deadline = "2026-11-01T23:59:59Z";

        int state = StudentAssignmentDetailFragment.resolveAssignmentVisualState(
                false,
                deadline,
                true, // file selected
                now
        );
        assertEquals(StudentAssignmentDetailFragment.STATE_FILE_CHOSEN, state);
    }

    @Test
    public void resolveAssignmentVisualState_submittedStateWhenAlreadySubmitted() {
        long now = Instant.parse("2026-10-28T08:00:00Z").toEpochMilli();
        String deadline = "2026-10-27T23:59:59Z"; // even if past deadline

        int state = StudentAssignmentDetailFragment.resolveAssignmentVisualState(
                true,
                deadline,
                false,
                now
        );
        assertEquals(StudentAssignmentDetailFragment.STATE_SUBMITTED, state);
    }

    @Test
    public void getFileIconRes_returnsMatchingIconsForExtensions() {
        assertEquals(R.drawable.ic_file_pdf, StudentAssignmentDetailFragment.getFileIconRes("bao_cao.pdf"));
        assertEquals(R.drawable.ic_file_pdf, StudentAssignmentDetailFragment.getFileIconRes("report.PDF"));

        assertEquals(R.drawable.ic_file_word, StudentAssignmentDetailFragment.getFileIconRes("assignment.docx"));
        assertEquals(R.drawable.ic_file_word, StudentAssignmentDetailFragment.getFileIconRes("doc1.doc"));

        assertEquals(R.drawable.ic_file_zip, StudentAssignmentDetailFragment.getFileIconRes("archive.zip"));
        assertEquals(R.drawable.ic_file_zip, StudentAssignmentDetailFragment.getFileIconRes("project.rar"));

        assertEquals(R.drawable.ic_file_image, StudentAssignmentDetailFragment.getFileIconRes("screenshot.png"));
        assertEquals(R.drawable.ic_file_image, StudentAssignmentDetailFragment.getFileIconRes("photo.jpg"));

        assertEquals(R.drawable.ic_file_document, StudentAssignmentDetailFragment.getFileIconRes("unknown.xyz"));
        assertEquals(R.drawable.ic_file_document, StudentAssignmentDetailFragment.getFileIconRes(null));
    }

    @Test
    public void submitAssignmentRequest_serializesAndDeserializesCorrectly() {
        SubmitAssignmentRequest request = new SubmitAssignmentRequest(
                "https://storage.uniclass.edu/submissions/report.pdf",
                "report.pdf"
        );

        String json = gson.toJson(request);
        SubmitAssignmentRequest parsed = gson.fromJson(json, SubmitAssignmentRequest.class);

        assertNotNull(parsed);
        assertEquals("https://storage.uniclass.edu/submissions/report.pdf", parsed.getFileUrl());
        assertEquals("report.pdf", parsed.getFileName());
    }

    @Test
    public void assignmentDetailResponse_parsesPendingAndGradedSubmissionJson() {
        String pendingJson = "{\n" +
                "  \"success\": true,\n" +
                "  \"code\": 200,\n" +
                "  \"data\": {\n" +
                "    \"id\": \"asg-001\",\n" +
                "    \"classId\": \"cls-001\",\n" +
                "    \"classCode\": \"NT118\",\n" +
                "    \"className\": \"Phát triển ứng dụng di động\",\n" +
                "    \"lecturerName\": \"ThS. Nguyễn Văn B\",\n" +
                "    \"title\": \"Bài tập lớn\",\n" +
                "    \"description\": \"Xây dựng app EduLens bằng Java\",\n" +
                "    \"deadline\": \"2026-10-30T23:59:00Z\",\n" +
                "    \"attachmentUrl\": \"https://example.com/spec.pdf\",\n" +
                "    \"submitted\": true,\n" +
                "    \"submittedFileUrl\": \"https://storage.example.com/my_work.pdf\",\n" +
                "    \"submittedAt\": \"2026-10-28T14:30:00Z\",\n" +
                "    \"score\": null,\n" +
                "    \"feedback\": null\n" +
                "  }\n" +
                "}";

        ApiResponse<?> pendingResponse = gson.fromJson(pendingJson, ApiResponse.class);
        assertNotNull(pendingResponse);
        assertTrue(pendingResponse.isSuccess());

        AssignmentDetailResponse pending = gson.fromJson(gson.toJson(pendingResponse.getData()), AssignmentDetailResponse.class);
        assertEquals("asg-001", pending.getId());
        assertTrue(pending.isSubmitted());
        assertNull(pending.getScore());
        assertNull(pending.getFeedback());

        String gradedJson = "{\n" +
                "  \"id\": \"asg-002\",\n" +
                "  \"submitted\": true,\n" +
                "  \"score\": 9.5,\n" +
                "  \"feedback\": \"Bài làm xuất sắc, UI mượt mà.\"\n" +
                "}";

        AssignmentDetailResponse graded = gson.fromJson(gradedJson, AssignmentDetailResponse.class);
        assertTrue(graded.isSubmitted());
        assertEquals(Double.valueOf(9.5), graded.getScore());
        assertEquals("Bài làm xuất sắc, UI mượt mà.", graded.getFeedback());
    }

    @Test
    public void deadlineBadgeResolution_handlesAllFourBadgeStatuses() {
        long now = Instant.parse("2026-10-28T12:00:00Z").toEpochMilli();

        // Submitted
        UiBadgeHelper.Status s1 = UiBadgeHelper.resolveAssignmentStatus(true, "2026-10-25T12:00:00Z", now);
        assertEquals(UiBadgeHelper.Status.SUBMITTED, s1);
        assertEquals("Đã nộp", UiBadgeHelper.getAssignmentStatusLabel(s1));

        // Overdue
        UiBadgeHelper.Status s2 = UiBadgeHelper.resolveAssignmentStatus(false, "2026-10-27T12:00:00Z", now);
        assertEquals(UiBadgeHelper.Status.OVERDUE, s2);
        assertEquals("Quá hạn", UiBadgeHelper.getAssignmentStatusLabel(s2));

        // Due soon (< 72 hours)
        UiBadgeHelper.Status s3 = UiBadgeHelper.resolveAssignmentStatus(false, "2026-10-30T12:00:00Z", now);
        assertEquals(UiBadgeHelper.Status.DUE_SOON, s3);
        assertEquals("Sắp hết hạn", UiBadgeHelper.getAssignmentStatusLabel(s3));

        // Open (> 72 hours)
        UiBadgeHelper.Status s4 = UiBadgeHelper.resolveAssignmentStatus(false, "2026-11-05T12:00:00Z", now);
        assertEquals(UiBadgeHelper.Status.OPEN, s4);
        assertEquals("Còn hạn", UiBadgeHelper.getAssignmentStatusLabel(s4));
    }
}
