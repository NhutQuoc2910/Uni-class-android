package com.ppquang06.uniclassapp.ui.lecturer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LecturerClassFlowTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    @Test
    public void createClassRequest_serializationAndDeserialization() {
        CreateClassRequest request = new CreateClassRequest("Thiết kế giao diện", "NT118.O21", "HK1 (2026-2027)", "B5.12");
        String json = gson.toJson(request);

        assertTrue(json.contains("\"name\":\"Thiết kế giao diện\""));
        assertTrue(json.contains("\"code\":\"NT118.O21\""));
        assertTrue(json.contains("\"semester\":\"HK1 (2026-2027)\""));
        assertTrue(json.contains("\"room\":\"B5.12\""));

        CreateClassRequest deserialized = gson.fromJson(json, CreateClassRequest.class);
        assertEquals("Thiết kế giao diện", deserialized.getName());
        assertEquals("NT118.O21", deserialized.getCode());
        assertEquals("HK1 (2026-2027)", deserialized.getSemester());
        assertEquals("B5.12", deserialized.getRoom());
    }

    @Test
    public void courseClassItem_withInviteCodeAndStatus() {
        CourseClassItem item = new CourseClassItem(
                "class-uuid-001",
                "NT118.O21",
                "Thiết kế giao diện",
                "HK1",
                "B5.12",
                "TS. Nguyễn Văn A",
                "K7M-2QX",
                "ACTIVE",
                90,
                45
        );

        String json = gson.toJson(item);
        assertTrue(json.contains("\"inviteCode\":\"K7M-2QX\""));
        assertTrue(json.contains("\"status\":\"ACTIVE\""));

        CourseClassItem deserialized = gson.fromJson(json, CourseClassItem.class);
        assertEquals("class-uuid-001", deserialized.getId());
        assertEquals("K7M-2QX", deserialized.getInviteCode());
        assertEquals("ACTIVE", deserialized.getStatus());
        assertEquals(45, deserialized.getEnrolledCount());
    }

    @Test
    public void otherRequestDtos_serializationAndDeserialization() {
        CreateMaterialRequest matReq = new CreateMaterialRequest("Slide Bài 1", "https://cdn.example.com/slide1.pdf", "PDF", true);
        String matJson = gson.toJson(matReq);
        CreateMaterialRequest matDeserialized = gson.fromJson(matJson, CreateMaterialRequest.class);
        assertEquals("Slide Bài 1", matDeserialized.getTitle());
        assertTrue(matDeserialized.getUrgentAnnouncement());

        CreateAssignmentRequest assignReq = new CreateAssignmentRequest("Bài tập 1", "Mô tả", "2026-11-01T23:59:00Z", "https://cdn.example.com/spec.pdf", true, false);
        String assignJson = gson.toJson(assignReq);
        CreateAssignmentRequest assignDeserialized = gson.fromJson(assignJson, CreateAssignmentRequest.class);
        assertEquals("Bài tập 1", assignDeserialized.getTitle());
        assertFalse(assignDeserialized.getAllowLate());

        GradeSubmissionRequest gradeReq = new GradeSubmissionRequest(9.5, "Làm tốt", true);
        String gradeJson = gson.toJson(gradeReq);
        GradeSubmissionRequest gradeDeserialized = gson.fromJson(gradeJson, GradeSubmissionRequest.class);
        assertEquals(Double.valueOf(9.5), gradeDeserialized.getScore());
        assertTrue(gradeDeserialized.getPublishAll());

        OpenQrSessionRequest qrReq = new OpenQrSessionRequest("00:11:22:33:44:55", 10.8700, 106.8030, 50.0);
        String qrJson = gson.toJson(qrReq);
        OpenQrSessionRequest qrDeserialized = gson.fromJson(qrJson, OpenQrSessionRequest.class);
        assertEquals("00:11:22:33:44:55", qrDeserialized.getWifiBssid());
        assertEquals(Double.valueOf(50.0), qrDeserialized.getRadiusMeters());
    }

    @Test
    public void classMemberItemResponse_serializationAndDeserialization() {
        ClassMemberItemResponse.MemberAssignmentSummary summary = new ClassMemberItemResponse.MemberAssignmentSummary(
                "assign-1", "Bài tập 1", "2026-11-01", true, "2026-10-30T10:00:00Z", 9.0
        );

        ClassMemberItemResponse member = new ClassMemberItemResponse(
                "student-001",
                "Trần Thị B",
                "b@uit.edu.vn",
                "21520001",
                "B",
                95,
                1,
                15,
                4,
                4,
                8.7,
                Collections.singletonList(summary)
        );

        String json = gson.toJson(member);
        assertTrue(json.contains("\"studentCode\":\"21520001\""));
        assertTrue(json.contains("\"recentAssignments\":["));

        ClassMemberItemResponse deserialized = gson.fromJson(json, ClassMemberItemResponse.class);
        assertEquals("student-001", deserialized.getStudentId());
        assertEquals("Trần Thị B", deserialized.getFullName());
        assertEquals(95, deserialized.getAttendanceRate());
        assertEquals(Double.valueOf(8.7), deserialized.getAverageScore());
        assertEquals(1, deserialized.getRecentAssignments().size());
        assertEquals(Double.valueOf(9.0), deserialized.getRecentAssignments().get(0).getScore());
    }

    @Test
    public void assignmentSubmissionListResponse_serializationAndDeserialization() {
        AssignmentSubmissionListResponse.SubmissionItem item = new AssignmentSubmissionListResponse.SubmissionItem(
                "sub-1", "student-1", "Nguyễn Văn C", "21520002", "C", true, false, "2026-10-29", "https://cdn.example.com/b1.zip", 8.5, "Bài làm khá"
        );

        AssignmentSubmissionListResponse response = new AssignmentSubmissionListResponse(
                "assign-1", "Bài tập Figma", "NT118.O21", "2026-11-01 23:59", 40, 35, 10, 30, 5, 5,
                Collections.singletonList(item)
        );

        String json = gson.toJson(response);
        AssignmentSubmissionListResponse deserialized = gson.fromJson(json, AssignmentSubmissionListResponse.class);

        assertEquals("assign-1", deserialized.getAssignmentId());
        assertEquals(40, deserialized.getTotalStudents());
        assertEquals(35, deserialized.getSubmittedCount());
        assertEquals(1, deserialized.getSubmissions().size());
        assertEquals(Double.valueOf(8.5), deserialized.getSubmissions().get(0).getScore());
    }

    @Test
    public void liveAttendanceSessionResponse_serializationAndDeserialization() {
        LiveAttendanceSessionResponse.AttendeeItem attendee = new LiveAttendanceSessionResponse.AttendeeItem(
                "student-1", "Lê Văn D", "21520003", "D", "PRESENT", "2026-10-28 08:05:00", "device-xyz"
        );

        LiveAttendanceSessionResponse session = new LiveAttendanceSessionResponse(
                "sess-1", "class-1", "NT118.O21", "Thiết kế UI", "B5.12", "K7M-2QX",
                "OPEN", "00:11:22:33:44:55", 10.87, 106.80, 50.0, "2026-10-28 08:00:00",
                45, 38, 7, Collections.singletonList(attendee)
        );

        String json = gson.toJson(session);
        LiveAttendanceSessionResponse deserialized = gson.fromJson(json, LiveAttendanceSessionResponse.class);

        assertEquals("sess-1", deserialized.getSessionId());
        assertEquals("OPEN", deserialized.getStatus());
        assertEquals(38, deserialized.getPresentCount());
        assertEquals(1, deserialized.getAttendees().size());
        assertEquals("PRESENT", deserialized.getAttendees().get(0).getStatus());
    }

    @Test
    public void classStatisticsResponse_serializationAndDeserialization() {
        ClassStatisticsResponse.ScoreBucket b1 = new ClassStatisticsResponse.ScoreBucket("8–9", 15, 33);
        ClassStatisticsResponse stats = new ClassStatisticsResponse(
                "class-1", "NT118.O21", "Thiết kế UI", "HK1 (2026-2027)", "ACTIVE",
                45, 92, 3, 8.2,
                Collections.emptyList(),
                Collections.singletonList(b1)
        );

        String json = gson.toJson(stats);
        ClassStatisticsResponse deserialized = gson.fromJson(json, ClassStatisticsResponse.class);

        assertEquals("class-1", deserialized.getClassId());
        assertEquals(92, deserialized.getAttendanceRate());
        assertEquals(8.2, deserialized.getAverageScore(), 0.001);
        assertEquals(1, deserialized.getScoreDistribution().size());
        assertEquals("8–9", deserialized.getScoreDistribution().get(0).getLabel());
    }

    @Test
    public void classFilter_activeVsArchived() {
        List<CourseClassItem> classes = Arrays.asList(
                new CourseClassItem("1", "C1", "Lớp 1", "HK1", "R1", "GV1", "CODE-1", "ACTIVE", 80, 30),
                new CourseClassItem("2", "C2", "Lớp 2", "HK1", "R2", "GV2", "CODE-2", "ARCHIVED", 75, 40),
                new CourseClassItem("3", "C3", "Lớp 3", "HK1", "R3", "GV3", "CODE-3", "active", 90, 35),
                new CourseClassItem("4", "C4", "Lớp 4", "HK1", "R4", "GV4", "CODE-4", null, 60, 25)
        );

        // Active filter (showArchived = false)
        List<CourseClassItem> activeClasses = new ArrayList<>();
        for (CourseClassItem c : classes) {
            boolean isArchived = "ARCHIVED".equalsIgnoreCase(c.getStatus());
            if (!isArchived) {
                activeClasses.add(c);
            }
        }
        assertEquals(3, activeClasses.size());
        assertEquals("1", activeClasses.get(0).getId());
        assertEquals("3", activeClasses.get(1).getId());
        assertEquals("4", activeClasses.get(2).getId());

        // Archived filter (showArchived = true)
        List<CourseClassItem> archivedClasses = new ArrayList<>();
        for (CourseClassItem c : classes) {
            boolean isArchived = "ARCHIVED".equalsIgnoreCase(c.getStatus());
            if (isArchived) {
                archivedClasses.add(c);
            }
        }
        assertEquals(1, archivedClasses.size());
        assertEquals("2", archivedClasses.get(0).getId());
    }

    @Test
    public void inviteCodeFormat_andShareMessageFormat() {
        String inviteCode = "K7M-2QX";
        assertTrue(inviteCode.matches("^[A-Z0-9]{3}-[A-Z0-9]{3}$"));

        String className = "Thiết kế giao diện người dùng";
        String shareFormat = "Mã tham gia lớp học %1$s trên EduLens: %2$s";
        String formatted = String.format(shareFormat, className, inviteCode);

        assertEquals("Mã tham gia lớp học Thiết kế giao diện người dùng trên EduLens: K7M-2QX", formatted);
    }

    @Test
    public void classDetailResponse_emptyListsAndZeroMetrics_deserializesCleanly() {
        ClassDetailResponse response = new ClassDetailResponse();
        response.setId("c-empty-001");
        response.setCode("NT118.NEW");
        response.setName("Lớp mới tạo");
        response.setSemester("HK1 (2026-2027)");
        response.setRoom("B1.01");
        response.setLecturerName("TS. Nguyễn Văn A");
        response.setInviteCode("NEW-123");
        response.setEnrolledCount(0);
        response.setAttendanceRate(0);
        response.setPresentCount(0);
        response.setAbsentCount(0);
        response.setTotalSessions(15);
        response.setAnnouncements(Collections.emptyList());
        response.setMaterials(Collections.emptyList());
        response.setAssignments(Collections.emptyList());

        String json = gson.toJson(response);
        ClassDetailResponse deserialized = gson.fromJson(json, ClassDetailResponse.class);

        assertEquals(0, deserialized.getEnrolledCount());
        assertEquals(0, deserialized.getAttendanceRate());
        assertEquals(0, deserialized.getPresentCount());
        assertEquals(0, deserialized.getAbsentCount());
        assertNotNull(deserialized.getAnnouncements());
        assertTrue(deserialized.getAnnouncements().isEmpty());
        assertNotNull(deserialized.getMaterials());
        assertTrue(deserialized.getMaterials().isEmpty());
        assertNotNull(deserialized.getAssignments());
        assertTrue(deserialized.getAssignments().isEmpty());
    }
}
