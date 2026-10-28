package com.ppquang06.uniclassapp.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse.MaterialItem;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.ui.student.classes.ClassMaterialsTabFragment;
import com.ppquang06.uniclassapp.ui.student.classes.ClassMaterialsTabFragment.MaterialFilter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AssignmentAndAttendanceDtoTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    @Test
    public void submitAssignmentRequest_serializationAndDeserialization() {
        SubmitAssignmentRequest request = new SubmitAssignmentRequest("https://storage.example.com/reports/hw1.pdf", "hw1.pdf");
        String json = gson.toJson(request);

        assertTrue(json.contains("\"fileUrl\":\"https://storage.example.com/reports/hw1.pdf\""));
        assertTrue(json.contains("\"fileName\":\"hw1.pdf\""));

        SubmitAssignmentRequest deserialized = gson.fromJson(json, SubmitAssignmentRequest.class);
        assertEquals("https://storage.example.com/reports/hw1.pdf", deserialized.getFileUrl());
        assertEquals("hw1.pdf", deserialized.getFileName());

        // Single-arg constructor
        SubmitAssignmentRequest singleArg = new SubmitAssignmentRequest("https://storage.example.com/reports/hw2.pdf");
        assertEquals("https://storage.example.com/reports/hw2.pdf", singleArg.getFileUrl());
        assertNull(singleArg.getFileName());
    }

    @Test
    public void attendanceCheckInRequest_serializationAndDeserialization() {
        AttendanceCheckInRequest request = new AttendanceCheckInRequest(
                "session-uuid-123",
                "00:11:22:33:44:55",
                10.8700,
                106.8030,
                "device-fingerprint-xyz"
        );
        String json = gson.toJson(request);

        assertTrue(json.contains("\"sessionId\":\"session-uuid-123\""));
        assertTrue(json.contains("\"wifiBssid\":\"00:11:22:33:44:55\""));
        assertTrue(json.contains("\"latitude\":10.87"));
        assertTrue(json.contains("\"longitude\":106.803"));
        assertTrue(json.contains("\"deviceId\":\"device-fingerprint-xyz\""));

        AttendanceCheckInRequest deserialized = gson.fromJson(json, AttendanceCheckInRequest.class);
        assertEquals("session-uuid-123", deserialized.getSessionId());
        assertEquals("00:11:22:33:44:55", deserialized.getWifiBssid());
        assertEquals(Double.valueOf(10.8700), deserialized.getLatitude());
        assertEquals(Double.valueOf(106.8030), deserialized.getLongitude());
        assertEquals("device-fingerprint-xyz", deserialized.getDeviceId());
    }

    @Test
    public void assignmentDetailResponse_serializationAndDeserialization() {
        String json = "{\n" +
                "  \"id\": \"assign-1\",\n" +
                "  \"classId\": \"class-10\",\n" +
                "  \"classCode\": \"NT118.Q11\",\n" +
                "  \"className\": \"Phát triển ứng dụng di động\",\n" +
                "  \"lecturerName\": \"ThS. Nguyễn Văn A\",\n" +
                "  \"title\": \"Bài tập lớn tuần 3\",\n" +
                "  \"description\": \"Xây dựng giao diện ứng dụng theo bản vẽ Figma\",\n" +
                "  \"deadline\": \"2026-11-15T23:59:59Z\",\n" +
                "  \"attachmentUrl\": \"https://example.com/specs.pdf\",\n" +
                "  \"submitted\": true,\n" +
                "  \"submittedFileUrl\": \"https://example.com/submission.zip\",\n" +
                "  \"submittedAt\": \"2026-11-14T20:15:00Z\",\n" +
                "  \"score\": 9.5,\n" +
                "  \"feedback\": \"Giao diện chuẩn chỉ, code sạch sẽ\"\n" +
                "}";

        AssignmentDetailResponse response = gson.fromJson(json, AssignmentDetailResponse.class);
        assertNotNull(response);
        assertEquals("assign-1", response.getId());
        assertEquals("class-10", response.getClassId());
        assertEquals("NT118.Q11", response.getClassCode());
        assertEquals("Phát triển ứng dụng di động", response.getClassName());
        assertEquals("ThS. Nguyễn Văn A", response.getLecturerName());
        assertEquals("Bài tập lớn tuần 3", response.getTitle());
        assertEquals("Xây dựng giao diện ứng dụng theo bản vẽ Figma", response.getDescription());
        assertEquals("2026-11-15T23:59:59Z", response.getDeadline());
        assertEquals("https://example.com/specs.pdf", response.getAttachmentUrl());
        assertTrue(response.isSubmitted());
        assertEquals("https://example.com/submission.zip", response.getSubmittedFileUrl());
        assertEquals("2026-11-14T20:15:00Z", response.getSubmittedAt());
        assertEquals(Double.valueOf(9.5), response.getScore());
        assertEquals("Giao diện chuẩn chỉ, code sạch sẽ", response.getFeedback());

        // Re-serialize and verify
        String reSerialized = gson.toJson(response);
        assertTrue(reSerialized.contains("\"submitted\":true"));
        assertTrue(reSerialized.contains("\"score\":9.5"));
    }

    @Test
    public void attendanceCheckInResponse_serializationAndDeserialization() {
        String json = "{\n" +
                "  \"recordId\": \"rec-456\",\n" +
                "  \"sessionId\": \"sess-789\",\n" +
                "  \"classCode\": \"NT118\",\n" +
                "  \"className\": \"Ứng dụng di động\",\n" +
                "  \"room\": \"B4.02\",\n" +
                "  \"lecturerName\": \"ThS. Nguyễn Văn A\",\n" +
                "  \"scannedAt\": \"2026-10-28T08:15:30Z\",\n" +
                "  \"deviceId\": \"device-abc\",\n" +
                "  \"status\": \"PRESENT\",\n" +
                "  \"qrVerified\": true,\n" +
                "  \"wifiVerified\": true,\n" +
                "  \"gpsVerified\": true,\n" +
                "  \"distanceMeters\": 12.5\n" +
                "}";

        AttendanceCheckInResponse response = gson.fromJson(json, AttendanceCheckInResponse.class);
        assertNotNull(response);
        assertEquals("rec-456", response.getRecordId());
        assertEquals("sess-789", response.getSessionId());
        assertEquals("NT118", response.getClassCode());
        assertEquals("Ứng dụng di động", response.getClassName());
        assertEquals("B4.02", response.getRoom());
        assertEquals("ThS. Nguyễn Văn A", response.getLecturerName());
        assertEquals("2026-10-28T08:15:30Z", response.getScannedAt());
        assertEquals("device-abc", response.getDeviceId());
        assertEquals("PRESENT", response.getStatus());
        assertTrue(response.isQrVerified());
        assertTrue(response.isWifiVerified());
        assertTrue(response.isGpsVerified());
        assertEquals(12.5, response.getDistanceMeters(), 0.001);
    }

    @Test
    public void classGradesResponse_serializationAndDeserialization() {
        ClassGradesResponse.GradeItem item1 = new ClassGradesResponse.GradeItem(
                "assign-1", "Lab 1", "2026-10-10T10:00:00Z", true, 9.0, "Tốt", "Trần Minh Khoa", "TMK"
        );
        ClassGradesResponse.GradeItem item2 = new ClassGradesResponse.GradeItem(
                "assign-2", "Lab 2", null, false, null, null, "Trần Minh Khoa", "TMK"
        );

        ClassGradesResponse response = new ClassGradesResponse(
                "class-1",
                "NT118.Q11",
                "Phát triển ứng dụng",
                "ThS. Trần Minh Khoa",
                9.0,
                1,
                2,
                Arrays.asList(item1, item2)
        );

        String json = gson.toJson(response);
        ClassGradesResponse deserialized = gson.fromJson(json, ClassGradesResponse.class);

        assertNotNull(deserialized);
        assertEquals("class-1", deserialized.getClassId());
        assertEquals("NT118.Q11", deserialized.getClassCode());
        assertEquals("Phát triển ứng dụng", deserialized.getClassName());
        assertEquals("ThS. Trần Minh Khoa", deserialized.getLecturerName());
        assertEquals(9.0, deserialized.getAverageScore(), 0.001);
        assertEquals(1, deserialized.getGradedCount());
        assertEquals(2, deserialized.getTotalAssignments());
        assertEquals(2, deserialized.getGrades().size());

        ClassGradesResponse.GradeItem dItem1 = deserialized.getGrades().get(0);
        assertEquals("assign-1", dItem1.getAssignmentId());
        assertEquals("Lab 1", dItem1.getAssignmentTitle());
        assertTrue(dItem1.isSubmitted());
        assertEquals(Double.valueOf(9.0), dItem1.getScore());
        assertEquals("Tốt", dItem1.getFeedback());
        assertEquals("Trần Minh Khoa", dItem1.getLecturerName());
        assertEquals("TMK", dItem1.getLecturerInitial());

        ClassGradesResponse.GradeItem dItem2 = deserialized.getGrades().get(1);
        assertFalse(dItem2.isSubmitted());
        assertNull(dItem2.getScore());
        assertNull(dItem2.getFeedback());
    }

    @Test
    public void materialFiltering_allFilterReturnsAllItems() {
        List<MaterialItem> items = Arrays.asList(
                new MaterialItem("1", "Tài liệu môn học.pdf", "https://example.com/1.pdf", "PDF", "2026-10-01"),
                new MaterialItem("2", "Đề cương chi tiết.docx", "https://example.com/2.docx", "WORD", "2026-10-02"),
                new MaterialItem("3", "Slide bài giảng chương 1.pptx", "https://example.com/3.pptx", "PPTX", "2026-10-03")
        );

        List<MaterialItem> result = ClassMaterialsTabFragment.filterMaterials(items, MaterialFilter.ALL);
        assertEquals(3, result.size());
    }

    @Test
    public void materialFiltering_pdfFilterMatchesOnlyPdf() {
        List<MaterialItem> items = Arrays.asList(
                new MaterialItem("1", "Tài liệu môn học.pdf", "https://example.com/1.pdf", "PDF", "2026-10-01"),
                new MaterialItem("2", "Tài liệu 2.pdf", "https://example.com/2.pdf", "pdf", "2026-10-02"),
                new MaterialItem("3", "Đề cương chi tiết.docx", "https://example.com/3.docx", "doc", "2026-10-03"),
                new MaterialItem("4", "Slide bài giảng.pptx", "https://example.com/4.pptx", "pptx", "2026-10-04")
        );

        List<MaterialItem> result = ClassMaterialsTabFragment.filterMaterials(items, MaterialFilter.PDF);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
    }

    @Test
    public void materialFiltering_wordFilterMatchesDocAndWord() {
        List<MaterialItem> items = Arrays.asList(
                new MaterialItem("1", "Tài liệu môn học.pdf", "https://example.com/1.pdf", "PDF", "2026-10-01"),
                new MaterialItem("2", "Đề cương Word.doc", "https://example.com/2.doc", "DOC", "2026-10-02"),
                new MaterialItem("3", "Kế hoạch giảng dạy.docx", "https://example.com/3.docx", "docx", "2026-10-03"),
                new MaterialItem("4", "Biểu mẫu.word", "https://example.com/4.word", "word", "2026-10-04"),
                new MaterialItem("5", "Slide bài giảng.pptx", "https://example.com/5.pptx", "presentation", "2026-10-05")
        );

        List<MaterialItem> result = ClassMaterialsTabFragment.filterMaterials(items, MaterialFilter.WORD);
        assertEquals(3, result.size());
        assertEquals("2", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
        assertEquals("4", result.get(2).getId());
    }

    @Test
    public void materialFiltering_powerPointFilterMatchesPptAndSlides() {
        List<MaterialItem> items = Arrays.asList(
                new MaterialItem("1", "Tài liệu môn học.pdf", "https://example.com/1.pdf", "PDF", "2026-10-01"),
                new MaterialItem("2", "Đề cương Word.docx", "https://example.com/2.docx", "DOCX", "2026-10-02"),
                new MaterialItem("3", "Chương 1.ppt", "https://example.com/3.ppt", "PPT", "2026-10-03"),
                new MaterialItem("4", "Chương 2.pptx", "https://example.com/4.pptx", "pptx", "2026-10-04"),
                new MaterialItem("5", "Bài thuyết trình.powerpoint", "https://example.com/5.ppt", "powerpoint", "2026-10-05"),
                new MaterialItem("6", "Slide tham khảo", "https://example.com/6.ppt", "slide", "2026-10-06")
        );

        List<MaterialItem> result = ClassMaterialsTabFragment.filterMaterials(items, MaterialFilter.POWERPOINT);
        assertEquals(4, result.size());
        assertEquals("3", result.get(0).getId());
        assertEquals("4", result.get(1).getId());
        assertEquals("5", result.get(2).getId());
        assertEquals("6", result.get(3).getId());
    }

    @Test
    public void materialFiltering_handlesNullAndEmptyCollectionsGracefully() {
        assertEquals(0, ClassMaterialsTabFragment.filterMaterials(null, MaterialFilter.PDF).size());
        assertEquals(0, ClassMaterialsTabFragment.filterMaterials(Collections.emptyList(), MaterialFilter.ALL).size());

        List<MaterialItem> itemsWithNulls = new ArrayList<>();
        itemsWithNulls.add(null);
        itemsWithNulls.add(new MaterialItem("1", "No Ext", "https://example.com/file", null, "2026-10-01"));
        itemsWithNulls.add(new MaterialItem("2", "Valid PDF", "https://example.com/file.pdf", "pdf", "2026-10-02"));

        List<MaterialItem> filtered = ClassMaterialsTabFragment.filterMaterials(itemsWithNulls, MaterialFilter.PDF);
        assertEquals(1, filtered.size());
        assertEquals("2", filtered.get(0).getId());
    }
}
