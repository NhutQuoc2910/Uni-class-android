package com.ppquang06.uniclassapp.ui.lecturer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LecturerMemberFlowTest {

    private Gson gson;
    private List<ClassMemberItemResponse> sampleMembers;

    @Before
    public void setUp() {
        gson = new Gson();
        sampleMembers = new ArrayList<>();

        sampleMembers.add(new ClassMemberItemResponse(
                "stu-001", "Nguyễn Văn An", "an.nv@uit.edu.vn", "21520001",
                "NA", 95, 1, 15, 4, 4, 8.8,
                Arrays.asList(
                        new ClassMemberItemResponse.MemberAssignmentSummary("a1", "Bài tập 1: Layouts", "2026-10-15", true, "2026-10-14 20:30", 9.0),
                        new ClassMemberItemResponse.MemberAssignmentSummary("a2", "Bài tập 2: Retrofit", "2026-10-22", true, "2026-10-21 18:15", 8.5)
                )
        ));

        sampleMembers.add(new ClassMemberItemResponse(
                "stu-002", "Trần Thị Bích", "bich.tt@uit.edu.vn", "21520002",
                "TB", 100, 0, 15, 4, 4, 9.2,
                Collections.singletonList(
                        new ClassMemberItemResponse.MemberAssignmentSummary("a1", "Bài tập 1: Layouts", "2026-10-15", true, "2026-10-14 15:00", 9.5)
                )
        ));

        sampleMembers.add(new ClassMemberItemResponse(
                "stu-003", "Lê Hoàng Cường", "cuong.lh@uit.edu.vn", "21520003",
                "LC", 73, 4, 15, 2, 4, 6.5,
                Collections.emptyList()
        ));
    }

    private List<ClassMemberItemResponse> filterMembers(List<ClassMemberItemResponse> list, String query) {
        List<ClassMemberItemResponse> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            result.addAll(list);
            return result;
        }

        String lower = query.trim().toLowerCase();
        for (ClassMemberItemResponse item : list) {
            boolean matchName = item.getFullName() != null && item.getFullName().toLowerCase().contains(lower);
            boolean matchCode = item.getStudentCode() != null && item.getStudentCode().toLowerCase().contains(lower);
            boolean matchEmail = item.getEmail() != null && item.getEmail().toLowerCase().contains(lower);
            if (matchName || matchCode || matchEmail) {
                result.add(item);
            }
        }
        return result;
    }

    @Test
    public void filterMembers_byName_matchesCorrectly() {
        List<ClassMemberItemResponse> results = filterMembers(sampleMembers, "bích");
        assertEquals(1, results.size());
        assertEquals("Trần Thị Bích", results.get(0).getFullName());

        List<ClassMemberItemResponse> resultsUpper = filterMembers(sampleMembers, "AN");
        assertEquals(1, resultsUpper.size());
        assertEquals("Nguyễn Văn An", resultsUpper.get(0).getFullName());
    }

    @Test
    public void filterMembers_byStudentCode_matchesCorrectly() {
        List<ClassMemberItemResponse> results = filterMembers(sampleMembers, "21520003");
        assertEquals(1, results.size());
        assertEquals("Lê Hoàng Cường", results.get(0).getFullName());
    }

    @Test
    public void filterMembers_byEmail_matchesCorrectly() {
        List<ClassMemberItemResponse> results = filterMembers(sampleMembers, "an.nv@uit");
        assertEquals(1, results.size());
        assertEquals("21520001", results.get(0).getStudentCode());
    }

    @Test
    public void filterMembers_emptyOrNullQuery_returnsAll() {
        List<ClassMemberItemResponse> emptyResults = filterMembers(sampleMembers, "");
        assertEquals(3, emptyResults.size());

        List<ClassMemberItemResponse> nullResults = filterMembers(sampleMembers, null);
        assertEquals(3, nullResults.size());
    }

    @Test
    public void filterMembers_noMatch_returnsEmpty() {
        List<ClassMemberItemResponse> results = filterMembers(sampleMembers, "nonexistent_query");
        assertTrue(results.isEmpty());
    }

    @Test
    public void absenceRateCalculation_andThresholdWarning() {
        // Safe attendance (<= 20% absence, <= 3 sessions)
        int absent1 = 1;
        int totalSessions = 15;
        double rate1 = (absent1 * 100.0) / totalSessions;
        boolean warning1 = rate1 > 20.0 || absent1 > 3;
        assertFalse(warning1);
        assertEquals(6.66, rate1, 0.1);

        int absent3 = 3;
        double rate3 = (absent3 * 100.0) / totalSessions;
        boolean warning3 = rate3 > 20.0 || absent3 > 3;
        assertFalse(warning3);
        assertEquals(20.0, rate3, 0.01);

        // Warning threshold (> 20% absence, 4-5 sessions)
        int absent4 = 4;
        double rate4 = (absent4 * 100.0) / totalSessions;
        boolean warning4 = rate4 > 20.0 || absent4 > 3;
        assertTrue(warning4);
        assertTrue(rate4 > 20.0);

        // Critical threshold (> 5 sessions)
        int absent6 = 6;
        double rate6 = (absent6 * 100.0) / totalSessions;
        boolean warning6 = rate6 > 20.0 || absent6 > 3;
        assertTrue(warning6);
        assertEquals(40.0, rate6, 0.01);
    }

    @Test
    public void announcementValidation_andRequestConstruction() {
        String title = "Thông báo nghỉ học thứ Bảy";
        String content = "Tuần này lớp nghỉ học bù vào tuần sau.";
        boolean isUrgent = true;

        assertFalse(title.trim().isEmpty());
        assertFalse(content.trim().isEmpty());

        CreateMaterialRequest request = new CreateMaterialRequest(title, content, "ANNOUNCEMENT", isUrgent);
        String json = gson.toJson(request);

        assertTrue(json.contains("\"title\":\"Thông báo nghỉ học thứ Bảy\""));
        assertTrue(json.contains("\"fileType\":\"ANNOUNCEMENT\""));
        assertTrue(json.contains("\"urgentAnnouncement\":true"));

        CreateMaterialRequest deserialized = gson.fromJson(json, CreateMaterialRequest.class);
        assertEquals(title, deserialized.getTitle());
        assertEquals("ANNOUNCEMENT", deserialized.getFileType());
        assertTrue(deserialized.getUrgentAnnouncement());
    }

    @Test
    public void materialUploadValidation_andFileTypes() {
        List<String> validTypes = Arrays.asList("PDF", "DOCX", "PPTX", "LINK");

        for (String type : validTypes) {
            String title = "Tài liệu mẫu " + type;
            String url = "https://example.com/file." + type.toLowerCase();

            assertFalse(title.trim().isEmpty());
            assertFalse(url.trim().isEmpty());

            CreateMaterialRequest request = new CreateMaterialRequest(title, url, type, false);
            String json = gson.toJson(request);

            assertTrue(json.contains("\"fileType\":\"" + type + "\""));
            assertTrue(json.contains("\"urgentAnnouncement\":false"));

            CreateMaterialRequest deserialized = gson.fromJson(json, CreateMaterialRequest.class);
            assertEquals(type, deserialized.getFileType());
            assertFalse(deserialized.getUrgentAnnouncement());
        }
    }

    @Test
    public void classMemberItemResponse_serializationAndDeserialization() {
        ClassMemberItemResponse member = sampleMembers.get(0);
        String json = gson.toJson(member);

        assertNotNull(json);
        assertTrue(json.contains("\"fullName\":\"Nguyễn Văn An\""));
        assertTrue(json.contains("\"studentCode\":\"21520001\""));
        assertTrue(json.contains("\"attendanceRate\":95"));
        assertTrue(json.contains("\"recentAssignments\":"));

        ClassMemberItemResponse deserialized = gson.fromJson(json, ClassMemberItemResponse.class);
        assertEquals("stu-001", deserialized.getStudentId());
        assertEquals("Nguyễn Văn An", deserialized.getFullName());
        assertEquals("21520001", deserialized.getStudentCode());
        assertEquals(95, deserialized.getAttendanceRate());
        assertEquals(1, deserialized.getAbsentSessions());
        assertEquals(15, deserialized.getTotalSessions());
        assertEquals(4, deserialized.getSubmittedAssignments());
        assertEquals(Double.valueOf(8.8), deserialized.getAverageScore());
        assertEquals(2, deserialized.getRecentAssignments().size());
        assertEquals("Bài tập 1: Layouts", deserialized.getRecentAssignments().get(0).getTitle());
        assertTrue(deserialized.getRecentAssignments().get(0).isSubmitted());
        assertEquals(Double.valueOf(9.0), deserialized.getRecentAssignments().get(0).getScore());
    }
}
