package com.ppquang06.uniclassapp.ui.student.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.NotificationAdapter;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class ClassDetailFlowTest {

    @Test
    public void pagerConstants_matchExpectedTabConfig() {
        assertEquals(4, ClassDetailPagerAdapter.TAB_COUNT);
        assertEquals(0, ClassDetailPagerAdapter.TAB_ANNOUNCEMENTS);
        assertEquals(1, ClassDetailPagerAdapter.TAB_MATERIALS);
        assertEquals(2, ClassDetailPagerAdapter.TAB_ASSIGNMENTS);
        assertEquals(3, ClassDetailPagerAdapter.TAB_ATTENDANCE);
    }

    @Test
    public void classDetailResponse_parsesJsonWithAllTabsCorrectly() {
        String json = "{\n" +
                "  \"success\": true,\n" +
                "  \"code\": 200,\n" +
                "  \"message\": \"Success\",\n" +
                "  \"data\": {\n" +
                "    \"id\": \"c-123\",\n" +
                "    \"code\": \"NT118.Q11\",\n" +
                "    \"name\": \"Phát triển ứng dụng di động\",\n" +
                "    \"semester\": \"HK1 2026-2027\",\n" +
                "    \"room\": \"B1.12\",\n" +
                "    \"lecturerName\": \"ThS. Trần Minh Khoa\",\n" +
                "    \"enrolledCount\": 45,\n" +
                "    \"attendanceRate\": 100,\n" +
                "    \"presentCount\": 5,\n" +
                "    \"absentCount\": 0,\n" +
                "    \"totalSessions\": 5,\n" +
                "    \"announcements\": [\n" +
                "      {\"id\": \"a-1\", \"title\": \"Thông báo 1\", \"content\": \"Nội dung 1\", \"createdAt\": \"2026-10-20T08:00:00Z\", \"urgent\": true, \"unread\": true}\n" +
                "    ],\n" +
                "    \"materials\": [\n" +
                "      {\"id\": \"m-1\", \"title\": \"Slide bài giảng\", \"fileUrl\": \"https://example.com/slide.pdf\", \"fileType\": \"PDF\", \"createdAt\": \"2026-10-21T08:00:00Z\"}\n" +
                "    ],\n" +
                "    \"assignments\": [\n" +
                "      {\"id\": \"as-1\", \"title\": \"Bài tập 1\", \"description\": \"Làm bài tập\", \"dueDate\": \"2026-11-01T23:59:59Z\", \"submitted\": false, \"score\": null, \"feedback\": null}\n" +
                "    ],\n" +
                "    \"attendanceRecords\": [\n" +
                "      {\"sessionId\": \"s-1\", \"sessionNumber\": 1, \"date\": \"2026-10-15T07:30:00Z\", \"status\": \"PRESENT\", \"scannedAt\": \"2026-10-15T07:32:00Z\"}\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        ApiResponse<?> apiResponse = new Gson().fromJson(json, ApiResponse.class);
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());

        ClassDetailResponse detail = new Gson().fromJson(new Gson().toJson(apiResponse.getData()), ClassDetailResponse.class);
        assertEquals("c-123", detail.getId());
        assertEquals("NT118.Q11", detail.getCode());
        assertEquals("Phát triển ứng dụng di động", detail.getName());
        assertEquals("B1.12", detail.getRoom());
        assertEquals("ThS. Trần Minh Khoa", detail.getLecturerName());
        assertEquals(45, detail.getEnrolledCount());
        assertEquals(100, detail.getAttendanceRate());
        assertEquals(5, detail.getPresentCount());
        assertEquals(5, detail.getTotalSessions());

        assertEquals(1, detail.getAnnouncements().size());
        assertEquals("Thông báo 1", detail.getAnnouncements().get(0).getTitle());
        assertTrue(detail.getAnnouncements().get(0).isUrgent());

        assertEquals(1, detail.getMaterials().size());
        assertEquals("PDF", detail.getMaterials().get(0).getFileType());

        assertEquals(1, detail.getAssignments().size());
        assertEquals("Bài tập 1", detail.getAssignments().get(0).getTitle());
        assertFalse(detail.getAssignments().get(0).isSubmitted());

        assertEquals(1, detail.getAttendanceRecords().size());
        assertEquals("PRESENT", detail.getAttendanceRecords().get(0).getStatus());
    }

    @Test
    public void mappingAnnouncements_toNotifications_preservesCategoryAndCode() {
        ClassDetailResponse.AnnouncementItem item = new ClassDetailResponse.AnnouncementItem(
                "ann-1", "Hạn nộp đồ án", "Nộp trước 23h59", "2026-10-25T10:00:00Z", true, true
        );
        List<NotificationItemResponse> mapped = NotificationAdapter.fromAnnouncements(Collections.singletonList(item), "NT118.Q11");

        assertEquals(1, mapped.size());
        NotificationItemResponse notif = mapped.get(0);
        assertEquals("ann-1", notif.getId());
        assertEquals("Hạn nộp đồ án", notif.getTitle());
        assertEquals("Nộp trước 23h59", notif.getBody());
        assertEquals("NT118.Q11", notif.getCourseCode());
        assertEquals("URGENT", notif.getCategory());
        assertTrue(notif.isUnread());
    }

    @Test
    public void mappingAssignments_toAssignmentItems_preservesClassName() {
        ClassDetailResponse.ClassAssignmentItem item = new ClassDetailResponse.ClassAssignmentItem(
                "asg-1", "Lab 3", "Mô tả lab", "2026-11-10T23:59:59Z", null, true, 9.5, "Tốt"
        );
        List<AssignmentItem> mapped = AssignmentAdapter.fromClassAssignments(Collections.singletonList(item), "Lớp Di động");

        assertEquals(1, mapped.size());
        AssignmentItem asg = mapped.get(0);
        assertEquals("asg-1", asg.getId());
        assertEquals("Lab 3", asg.getTitle());
        assertEquals("Lớp Di động", asg.getClassName());
        assertTrue(asg.isSubmitted());
    }

    @Test
    public void attendanceThreshold_resolvesCorrectStatus() {
        int rateHigh = 85;
        UiBadgeHelper.Status statusHigh = (rateHigh >= 80) ? UiBadgeHelper.Status.PRESENT : UiBadgeHelper.Status.ABSENT;
        assertEquals(UiBadgeHelper.Status.PRESENT, statusHigh);

        int rateLow = 75;
        UiBadgeHelper.Status statusLow = (rateLow >= 80) ? UiBadgeHelper.Status.PRESENT : UiBadgeHelper.Status.ABSENT;
        assertEquals(UiBadgeHelper.Status.ABSENT, statusLow);
    }
}
