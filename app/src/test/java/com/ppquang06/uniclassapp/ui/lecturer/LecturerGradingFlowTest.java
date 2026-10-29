package com.ppquang06.uniclassapp.ui.lecturer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse.SubmissionItem;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LecturerGradingFlowTest {

    private Gson gson;
    private List<SubmissionItem> sampleSubmissions;

    // Quick score chip presets matching Figma 98:710
    private static final double[] QUICK_SCORE_CHIPS = new double[]{10.0, 9.0, 8.5, 8.0, 7.0, 5.0};

    @Before
    public void setUp() {
        gson = new Gson();
        sampleSubmissions = new ArrayList<>();
        sampleSubmissions.add(new SubmissionItem("sub-1", "stu-1", "Nguyễn Văn An", "21520001", "NA", true, false, "2026-10-28 20:30", "https://storage.uniclass.edu.vn/sub/21520001_bt1.pdf", 9.0, "Bài làm rất tốt"));
        sampleSubmissions.add(new SubmissionItem("sub-2", "stu-2", "Trần Thị Bích", "21520002", "TB", true, false, "2026-10-28 21:15", "https://storage.uniclass.edu.vn/sub/21520002_bt1.pdf", 8.5, "Đầy đủ yêu cầu"));
        sampleSubmissions.add(new SubmissionItem("sub-3", "stu-3", "Lê Hoàng Cường", "21520003", "LC", true, true, "2026-10-29 08:45", "https://storage.uniclass.edu.vn/sub/21520003_bt1.docx", null, null));
        sampleSubmissions.add(new SubmissionItem("sub-4", "stu-4", "Phạm Đăng Dũng", "21520004", "PD", true, false, "2026-10-28 22:10", "https://storage.uniclass.edu.vn/sub/21520004_bt1.zip", null, null));
        sampleSubmissions.add(new SubmissionItem("sub-5", "stu-5", "Hoàng Mai Em", "21520005", "HE", false, false, null, null, null, null));
    }

    private boolean isValidScore(String rawScore) {
        if (rawScore == null || rawScore.trim().isEmpty()) {
            return false;
        }
        try {
            double val = Double.parseDouble(rawScore.trim());
            return val >= 0.0 && val <= 10.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<SubmissionItem> filterSubmissions(List<SubmissionItem> list, int filterTab) {
        List<SubmissionItem> result = new ArrayList<>();
        if (list == null) return result;

        if (filterTab == 1) {
            // "Chưa chấm" filter (score == null)
            for (SubmissionItem item : list) {
                if (item.getScore() == null) {
                    result.add(item);
                }
            }
        } else {
            // "Tất cả bài nộp"
            result.addAll(list);
        }
        return result;
    }

    @Test
    public void quickScoreChips_valuesAreAllValidAndMatchSpec() {
        assertEquals(6, QUICK_SCORE_CHIPS.length);
        assertEquals(10.0, QUICK_SCORE_CHIPS[0], 0.001);
        assertEquals(9.0, QUICK_SCORE_CHIPS[1], 0.001);
        assertEquals(8.5, QUICK_SCORE_CHIPS[2], 0.001);
        assertEquals(8.0, QUICK_SCORE_CHIPS[3], 0.001);
        assertEquals(7.0, QUICK_SCORE_CHIPS[4], 0.001);
        assertEquals(5.0, QUICK_SCORE_CHIPS[5], 0.001);

        for (double chipVal : QUICK_SCORE_CHIPS) {
            assertTrue(isValidScore(String.valueOf(chipVal)));
        }
    }

    @Test
    public void scoreRangeValidation_acceptsValidRange_rejectsOutOfRange() {
        // Valid boundaries
        assertTrue(isValidScore("0.0"));
        assertTrue(isValidScore("0"));
        assertTrue(isValidScore("5.5"));
        assertTrue(isValidScore("8.75"));
        assertTrue(isValidScore("10.0"));
        assertTrue(isValidScore("10"));

        // Out of range (< 0 or > 10)
        assertFalse(isValidScore("-0.1"));
        assertFalse(isValidScore("-5.0"));
        assertFalse(isValidScore("10.1"));
        assertFalse(isValidScore("15.0"));
        assertFalse(isValidScore("100"));

        // Invalid inputs
        assertFalse(isValidScore(""));
        assertFalse(isValidScore("   "));
        assertFalse(isValidScore("abc"));
        assertFalse(isValidScore(null));
    }

    @Test
    public void submissionsFilter_allVsPendingGrading() {
        // Tab 0: All
        List<SubmissionItem> all = filterSubmissions(sampleSubmissions, 0);
        assertEquals(5, all.size());

        // Tab 1: Pending grading (score == null)
        List<SubmissionItem> pending = filterSubmissions(sampleSubmissions, 1);
        assertEquals(3, pending.size());
        for (SubmissionItem item : pending) {
            assertNull(item.getScore());
        }
        assertEquals("stu-3", pending.get(0).getStudentId());
        assertEquals("stu-4", pending.get(1).getStudentId());
        assertEquals("stu-5", pending.get(2).getStudentId());
    }

    @Test
    public void submissionsFilter_emptyList_returnsEmpty() {
        List<SubmissionItem> emptyList = new ArrayList<>();
        List<SubmissionItem> all = filterSubmissions(emptyList, 0);
        assertNotNull(all);
        assertTrue(all.isEmpty());

        List<SubmissionItem> pending = filterSubmissions(emptyList, 1);
        assertNotNull(pending);
        assertTrue(pending.isEmpty());
    }

    @Test
    public void nextPreviousStudentNavigation_indexBoundsCheck() {
        int listSize = sampleSubmissions.size(); // 5
        int currentIndex = 0;

        // At first student: cannot go previous
        boolean canPrev = currentIndex > 0;
        boolean canNext = currentIndex < listSize - 1;
        assertFalse(canPrev);
        assertTrue(canNext);

        // Move to middle student (index 2)
        currentIndex = 2;
        canPrev = currentIndex > 0;
        canNext = currentIndex < listSize - 1;
        assertTrue(canPrev);
        assertTrue(canNext);

        // Move to last student (index 4)
        currentIndex = listSize - 1;
        canPrev = currentIndex > 0;
        canNext = currentIndex < listSize - 1;
        assertTrue(canPrev);
        assertFalse(canNext);

        // Clamping invalid bounds
        int outOfBoundsNegative = -1;
        int clampedNegative = Math.max(0, Math.min(outOfBoundsNegative, listSize - 1));
        assertEquals(0, clampedNegative);

        int outOfBoundsPositive = 99;
        int clampedPositive = Math.max(0, Math.min(outOfBoundsPositive, listSize - 1));
        assertEquals(4, clampedPositive);
    }

    @Test
    public void gradeSubmissionRequest_construction_publishAllFlag() {
        // Single student publish
        GradeSubmissionRequest reqCurrent = new GradeSubmissionRequest(9.0, "Làm tốt", false);
        assertEquals(Double.valueOf(9.0), reqCurrent.getScore());
        assertEquals("Làm tốt", reqCurrent.getFeedback());
        assertEquals(Boolean.FALSE, reqCurrent.getPublishAll());

        // All students publish
        GradeSubmissionRequest reqAll = new GradeSubmissionRequest(8.5, "Hoàn thành", true);
        assertEquals(Double.valueOf(8.5), reqAll.getScore());
        assertEquals("Hoàn thành", reqAll.getFeedback());
        assertEquals(Boolean.TRUE, reqAll.getPublishAll());

        // Serialization to JSON
        String jsonCurrent = gson.toJson(reqCurrent);
        assertTrue(jsonCurrent.contains("\"publishAll\":false"));
        assertTrue(jsonCurrent.contains("\"score\":9.0"));

        String jsonAll = gson.toJson(reqAll);
        assertTrue(jsonAll.contains("\"publishAll\":true"));
        assertTrue(jsonAll.contains("\"score\":8.5"));
    }

    @Test
    public void createAssignmentRequest_validationAndSerialization() {
        CreateAssignmentRequest req = new CreateAssignmentRequest(
                "Bài tập 3: Room Database & Caching",
                "Tạo DAO và Database instance",
                "2026-11-15 23:59:00",
                "https://drive.google.com/sample_req.pdf",
                true,
                true
        );

        assertEquals("Bài tập 3: Room Database & Caching", req.getTitle());
        assertEquals("Tạo DAO và Database instance", req.getDescription());
        assertEquals("2026-11-15 23:59:00", req.getDeadline());
        assertEquals("https://drive.google.com/sample_req.pdf", req.getAttachmentUrl());
        assertTrue(req.getNotifyStudents());
        assertTrue(req.getAllowLate());

        String json = gson.toJson(req);
        assertTrue(json.contains("Bài tập 3"));
        assertTrue(json.contains("\"notifyStudents\":true"));
        assertTrue(json.contains("\"allowLate\":true"));

        CreateAssignmentRequest parsed = gson.fromJson(json, CreateAssignmentRequest.class);
        assertNotNull(parsed);
        assertEquals(req.getTitle(), parsed.getTitle());
        assertEquals(req.getDeadline(), parsed.getDeadline());
    }

    @Test
    public void submissionListJsonSerialization_roundTrip_preservesAllData() {
        String json = gson.toJson(sampleSubmissions);
        assertNotNull(json);

        Type listType = new TypeToken<List<SubmissionItem>>() {}.getType();
        List<SubmissionItem> deserialized = gson.fromJson(json, listType);

        assertNotNull(deserialized);
        assertEquals(sampleSubmissions.size(), deserialized.size());
        assertEquals(sampleSubmissions.get(0).getStudentName(), deserialized.get(0).getStudentName());
        assertEquals(sampleSubmissions.get(0).getScore(), deserialized.get(0).getScore());
        assertEquals(sampleSubmissions.get(2).getScore(), deserialized.get(2).getScore()); // null
        assertEquals(sampleSubmissions.get(2).isLate(), deserialized.get(2).isLate());
    }
}
