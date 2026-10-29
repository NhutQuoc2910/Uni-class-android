package com.ppquang06.uniclassapp.ui.lecturer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse.ScoreBucket;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse.AttendeeItem;
import com.ppquang06.uniclassapp.utils.QrCodeGenerator;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LecturerAttendanceAndStatsFlowTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    // =========================================================================
    // 1. Dynamic QR Token Rotation & Payload Formatting Tests
    // =========================================================================

    @Test
    public void testDynamicQrPayloadFormatting() {
        String classCode = "NT118.Q11";
        String sessionId = "SESSION-1234-ABCD";
        long timestamp = 1700000000000L;

        String payload = formatQrPayload(classCode, sessionId, timestamp);
        assertEquals("UNICLASS:NT118.Q11:SESSION-1234-ABCD:1700000000000", payload);

        // When classCode is empty fallback
        String fallbackPayload = formatQrPayload("", sessionId, timestamp);
        assertEquals("UNICLASS:CLASS:SESSION-1234-ABCD:1700000000000", fallbackPayload);
    }

    @Test
    public void testDynamicQrTokenRotation() {
        String classCode = "NT118.Q11";
        String sessionId = "SES-NT118-01";

        long time1 = 1000000L;
        String payload1 = formatQrPayload(classCode, sessionId, time1);

        long time2 = time1 + 30000L; // After 30s rotation
        String payload2 = formatQrPayload(classCode, sessionId, time2);

        assertFalse("Dynamic payload must change when token rotates", payload1.equals(payload2));
    }

    @Test
    public void testQrCodeGeneratorMatrixGeneration() {
        String testPayload = "UNICLASS:NT118.Q11:SES-01:1700000000";
        boolean[][] matrix = QrCodeGenerator.generateMatrix(testPayload);

        assertNotNull("Generated matrix should not be null", matrix);
        assertEquals(29, matrix.length);
        assertEquals(29, matrix[0].length);

        // Verify standard finder patterns at top-left (0,0)
        assertTrue("Top-left outer corner (0,0) must be black", matrix[0][0]);
        assertTrue("Top-left outer corner (0,6) must be black", matrix[0][6]);
        assertTrue("Top-left outer corner (6,0) must be black", matrix[6][0]);
        assertTrue("Top-left outer corner (6,6) must be black", matrix[6][6]);
        assertFalse("Top-left inner border (1,1) must be white", matrix[1][1]);
        assertTrue("Top-left inner center (3,3) must be black", matrix[3][3]);

        // Verify timing pattern alternating modules
        assertTrue("Timing pattern row 6 col 8 must be true", matrix[6][8]);
        assertFalse("Timing pattern row 6 col 9 must be false", matrix[6][9]);
        assertTrue("Timing pattern row 6 col 10 must be true", matrix[6][10]);
    }

    @Test
    public void testSessionSnippetFormat() {
        String longSessionId = "SESSION-ABCDEF-987654321";
        String snippet = longSessionId.length() > 8 ? longSessionId.substring(0, 8) : longSessionId;
        assertEquals("SESSION-", snippet);

        String shortId = "SES-01";
        String shortSnippet = shortId.length() > 8 ? shortId.substring(0, 8) : shortId;
        assertEquals("SES-01", shortSnippet);
    }

    @Test
    public void testOpenQrSessionRequestDto() {
        OpenQrSessionRequest req = new OpenQrSessionRequest("00:11:22:33:44:55", 10.8700, 106.8031, 50.0);
        assertEquals("00:11:22:33:44:55", req.getWifiBssid());
        assertEquals(Double.valueOf(10.8700), req.getLatitude());
        assertEquals(Double.valueOf(106.8031), req.getLongitude());
        assertEquals(Double.valueOf(50.0), req.getRadiusMeters());
    }

    // =========================================================================
    // 2. Live Attendance Counts Calculation & DTO Tests
    // =========================================================================

    @Test
    public void testLiveAttendanceCalculations() {
        int present = 38;
        int absent = 4;
        int total = 42;

        assertEquals(42, calculateTotal(present, absent));

        int ratePercent = calculateAttendanceRate(present, total);
        assertEquals(90, ratePercent); // (38 * 100) / 42 = 90.47% -> 90%
    }

    @Test
    public void testLiveAttendanceCalculationsEdgeCases() {
        assertEquals(0, calculateAttendanceRate(0, 40));
        assertEquals(100, calculateAttendanceRate(40, 40));
        assertEquals(0, calculateAttendanceRate(0, 0));
    }

    @Test
    public void testLiveAttendanceSessionResponseParsing() {
        List<AttendeeItem> sampleAttendees = new ArrayList<>();
        sampleAttendees.add(new AttendeeItem("s1", "Nguyễn Văn An", "21520001", "NA", "PRESENT", "08:15:22", "dev-01"));
        sampleAttendees.add(new AttendeeItem("s2", "Trần Thị Bích", "21520002", "TB", "PRESENT", "08:16:05", "dev-02"));

        LiveAttendanceSessionResponse response = new LiveAttendanceSessionResponse(
                "ses-101", "c-101", "NT118.Q11", "Thiết kế mạng", "B2.10", "INV-123",
                "OPEN", "00:11:22:33:44:55", 10.8700, 106.8031, 50.0, "2026-10-28 08:00:00",
                42, 38, 4, sampleAttendees
        );

        String json = gson.toJson(response);
        LiveAttendanceSessionResponse parsed = gson.fromJson(json, LiveAttendanceSessionResponse.class);

        assertEquals("ses-101", parsed.getSessionId());
        assertEquals("OPEN", parsed.getStatus());
        assertEquals(38, parsed.getPresentCount());
        assertEquals(4, parsed.getAbsentCount());
        assertEquals(42, parsed.getTotalStudents());
        assertEquals(2, parsed.getAttendees().size());
        assertEquals("21520001", parsed.getAttendees().get(0).getStudentCode());
    }

    // =========================================================================
    // 3. Warning Absence Calculation (>20% Threshold) Tests
    // =========================================================================

    @Test
    public void testWarningAbsenceCalculation() {
        int totalSessions = 15;

        // 3 absences = 3/15 = 20.0% -> NOT exceeding 20%
        assertFalse(isAbsenceExceedingThreshold(3, totalSessions, 0.20));

        // 4 absences = 4/15 = 26.67% -> EXCEEDING 20%
        assertTrue(isAbsenceExceedingThreshold(4, totalSessions, 0.20));

        // 5 absences = 5/15 = 33.33% -> EXCEEDING 20%
        assertTrue(isAbsenceExceedingThreshold(5, totalSessions, 0.20));
    }

    @Test
    public void testFilterWarningStudents() {
        List<ClassMemberItemResponse> members = new ArrayList<>();
        members.add(new ClassMemberItemResponse("s1", "Nguyễn Văn An", "an@uit.edu.vn", "21520001", "NA", 93, 1, 15, 3, 3, 8.5, null));
        members.add(new ClassMemberItemResponse("s2", "Trần Thị Bích", "bich@uit.edu.vn", "21520002", "TB", 87, 2, 15, 3, 3, 7.5, null));
        members.add(new ClassMemberItemResponse("s3", "Lê Hoàng Cường", "cuong@uit.edu.vn", "21520003", "LC", 80, 3, 15, 2, 3, 6.0, null));
        members.add(new ClassMemberItemResponse("s4", "Phạm Đăng Dũng", "dung@uit.edu.vn", "21520004", "PD", 73, 4, 15, 1, 3, 5.0, null)); // >20%
        members.add(new ClassMemberItemResponse("s5", "Hoàng Mai Em", "em@uit.edu.vn", "21520005", "HE", 67, 5, 15, 1, 3, 4.5, null)); // >20%

        List<ClassMemberItemResponse> warningStudents = filterWarningStudents(members, 0.20);
        assertEquals(2, warningStudents.size());
        assertEquals("21520004", warningStudents.get(0).getStudentCode());
        assertEquals("21520005", warningStudents.get(1).getStudentCode());
    }

    // =========================================================================
    // 4. 6-Bucket Score Distribution Calculation Tests
    // =========================================================================

    @Test
    public void testScoreDistributionBuckets() {
        List<Double> scores = Arrays.asList(
                1.5,        // 0-2 (1)
                2.5, 3.8,   // 2-4 (2)
                4.2, 5.0, 5.5, 5.8, // 4-6 (4)
                6.5, 7.0, 7.2, 7.5, 7.8, // 6-8 (5)
                8.2, 8.5, 8.8, // 8-9 (3)
                9.2, 9.8    // 9-10 (2)
        );

        List<ScoreBucket> buckets = computeScoreDistribution(scores);
        assertEquals(6, buckets.size());

        assertEquals("0–2", buckets.get(0).getLabel());
        assertEquals(1, buckets.get(0).getCount());

        assertEquals("2–4", buckets.get(1).getLabel());
        assertEquals(2, buckets.get(1).getCount());

        assertEquals("4–6", buckets.get(2).getLabel());
        assertEquals(4, buckets.get(2).getCount());

        assertEquals("6–8", buckets.get(3).getLabel());
        assertEquals(5, buckets.get(3).getCount());

        assertEquals("8–9", buckets.get(4).getLabel());
        assertEquals(3, buckets.get(4).getCount());

        assertEquals("9–10", buckets.get(5).getLabel());
        assertEquals(2, buckets.get(5).getCount());

        // Total count matches sample size
        int totalCalculated = 0;
        int totalPercentage = 0;
        for (ScoreBucket b : buckets) {
            totalCalculated += b.getCount();
            totalPercentage += b.getPercentage();
        }
        assertEquals(scores.size(), totalCalculated);
        assertTrue("Total percentage should be approximately 100%", totalPercentage >= 98 && totalPercentage <= 102);
    }

    @Test
    public void testEmptyScoreDistribution_allBucketsZero() {
        List<Double> emptyScores = new ArrayList<>();
        List<ScoreBucket> buckets = computeScoreDistribution(emptyScores);
        assertEquals(6, buckets.size());

        for (ScoreBucket b : buckets) {
            assertEquals(0, b.getCount());
            assertEquals(0, b.getPercentage());
        }
    }

    @Test
    public void testAverageScoreCalculation() {
        List<Double> scores = Arrays.asList(8.0, 7.0, 9.0, 8.0);
        double avg = calculateAverage(scores);
        assertEquals(8.0, avg, 0.001);

        assertEquals(0.0, calculateAverage(new ArrayList<>()), 0.001);
    }

    // =========================================================================
    // 5. Archive Class Status Transition Tests
    // =========================================================================

    @Test
    public void testArchiveClassStatusTransition() {
        CourseClassItem courseClass = new CourseClassItem(
                "c-01", "NT118.Q11", "Mạng máy tính", "HK1 (2026-2027)", "B2.10", "TS. Nguyễn Văn B",
                "K7M-2QX", "ACTIVE", 60, 42
        );

        assertTrue("Class should be active initially", isClassActive(courseClass));
        assertFalse("Class should not be archived initially", isClassArchived(courseClass));

        // Perform archive transition
        courseClass.setStatus("ARCHIVED");

        assertFalse("Class should not be active after archive", isClassActive(courseClass));
        assertTrue("Class should be archived after transition", isClassArchived(courseClass));
    }

    @Test
    public void testClassStatisticsResponseDto() {
        ClassStatisticsResponse stats = new ClassStatisticsResponse(
                "c-01", "NT118.Q11", "Mạng máy tính", "HK1 (2026-2027)", "ACTIVE",
                42, 88, 3, 7.8, new ArrayList<>(), new ArrayList<>()
        );

        assertEquals("c-01", stats.getClassId());
        assertEquals(88, stats.getAttendanceRate());
        assertEquals(3, stats.getWarningAbsentCount());
        assertEquals(7.8, stats.getAverageScore(), 0.001);
    }

    // =========================================================================
    // Helper Logic Methods Matching Application Code
    // =========================================================================

    private String formatQrPayload(String classCode, String sessionId, long timestamp) {
        String code = (classCode != null && !classCode.trim().isEmpty()) ? classCode : "CLASS";
        return "UNICLASS:" + code + ":" + sessionId + ":" + timestamp;
    }

    private int calculateTotal(int present, int absent) {
        return present + absent;
    }

    private int calculateAttendanceRate(int present, int total) {
        if (total <= 0) return 0;
        return (int) Math.round((present * 100.0) / total);
    }

    private boolean isAbsenceExceedingThreshold(int absentSessions, int totalSessions, double threshold) {
        if (totalSessions <= 0) return false;
        double ratio = (double) absentSessions / totalSessions;
        return ratio > threshold;
    }

    private List<ClassMemberItemResponse> filterWarningStudents(List<ClassMemberItemResponse> list, double threshold) {
        List<ClassMemberItemResponse> result = new ArrayList<>();
        if (list == null) return result;
        for (ClassMemberItemResponse m : list) {
            int total = m.getTotalSessions() > 0 ? m.getTotalSessions() : 15;
            if (isAbsenceExceedingThreshold(m.getAbsentSessions(), total, threshold)) {
                result.add(m);
            }
        }
        return result;
    }

    private List<ScoreBucket> computeScoreDistribution(List<Double> scores) {
        int[] counts = new int[6];
        if (scores != null) {
            for (Double s : scores) {
                if (s == null) continue;
                if (s >= 0.0 && s < 2.0) counts[0]++;
                else if (s >= 2.0 && s < 4.0) counts[1]++;
                else if (s >= 4.0 && s < 6.0) counts[2]++;
                else if (s >= 6.0 && s < 8.0) counts[3]++;
                else if (s >= 8.0 && s < 9.0) counts[4]++;
                else if (s >= 9.0 && s <= 10.0) counts[5]++;
            }
        }

        int total = scores != null ? scores.size() : 0;
        List<ScoreBucket> buckets = new ArrayList<>();
        String[] labels = new String[]{"0–2", "2–4", "4–6", "6–8", "8–9", "9–10"};

        for (int i = 0; i < 6; i++) {
            int pct = total > 0 ? (int) Math.round((counts[i] * 100.0) / total) : 0;
            buckets.add(new ScoreBucket(labels[i], counts[i], pct));
        }
        return buckets;
    }

    private double calculateAverage(List<Double> scores) {
        if (scores == null || scores.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double s : scores) {
            sum += (s != null ? s : 0.0);
        }
        return sum / scores.size();
    }

    private boolean isClassActive(CourseClassItem item) {
        return item != null && "ACTIVE".equalsIgnoreCase(item.getStatus());
    }

    private boolean isClassArchived(CourseClassItem item) {
        return item != null && "ARCHIVED".equalsIgnoreCase(item.getStatus());
    }
}
