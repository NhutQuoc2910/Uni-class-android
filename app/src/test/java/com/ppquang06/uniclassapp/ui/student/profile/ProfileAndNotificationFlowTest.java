package com.ppquang06.uniclassapp.ui.student.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.ChangePasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper;
import com.ppquang06.uniclassapp.ui.common.notifications.NotificationFilterHelper.NotificationFilter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileAndNotificationFlowTest {

    private List<NotificationItemResponse> sampleList;

    @Before
    public void setUp() {
        sampleList = new ArrayList<>();

        // 1. All & Urgent & Unread
        sampleList.add(new NotificationItemResponse(
                "n-1", "Bảo trì hệ thống", "Hệ thống sẽ bảo trì từ 22h",
                "SYSTEM", "2026-10-28T08:00:00Z", "URGENT", true
        ));

        // 2. Assignment & Read
        sampleList.add(new NotificationItemResponse(
                "n-2", "Bài tập lớn tuần 5", "Nộp báo cáo tiến độ",
                "NT118", "2026-10-27T08:00:00Z", "ASSIGNMENT", false
        ));

        // 3. Assignment & Unread
        sampleList.add(new NotificationItemResponse(
                "n-3", "Bài tập thực hành 3", "Đã mở cổng nộp",
                "NT118", "2026-10-26T08:00:00Z", "ASSIGNMENT", true
        ));

        // 4. Grade & Unread
        sampleList.add(new NotificationItemResponse(
                "n-4", "Điểm quá trình", "Giảng viên đã công bố điểm",
                "NT531", "2026-10-25T08:00:00Z", "GRADE", true
        ));

        // 5. General & Read
        sampleList.add(new NotificationItemResponse(
                "n-5", "Nghỉ lễ", "Thông báo lịch nghỉ lễ 2/9",
                "ALL", "2026-10-24T08:00:00Z", "GENERAL", false
        ));
    }

    @Test
    public void filterNotifications_allReturnsEntireList() {
        List<NotificationItemResponse> result =
                NotificationFilterHelper.filterNotifications(sampleList, NotificationFilter.ALL);
        assertEquals(5, result.size());
    }

    @Test
    public void filterNotifications_unreadFiltersOnlyUnreadItems() {
        List<NotificationItemResponse> result =
                NotificationFilterHelper.filterNotifications(sampleList, NotificationFilter.UNREAD);
        assertEquals(3, result.size());
        for (NotificationItemResponse item : result) {
            assertTrue(item.isUnread());
        }
    }

    @Test
    public void filterNotifications_urgentFiltersOnlyUrgentCategory() {
        List<NotificationItemResponse> result =
                NotificationFilterHelper.filterNotifications(sampleList, NotificationFilter.URGENT);
        assertEquals(1, result.size());
        assertEquals("n-1", result.get(0).getId());
        assertEquals("URGENT", result.get(0).getCategory());
    }

    @Test
    public void filterNotifications_assignmentFiltersOnlyAssignmentCategory() {
        List<NotificationItemResponse> result =
                NotificationFilterHelper.filterNotifications(sampleList, NotificationFilter.ASSIGNMENTS);
        assertEquals(2, result.size());
        for (NotificationItemResponse item : result) {
            assertEquals("ASSIGNMENT", item.getCategory());
        }
    }

    @Test
    public void filterNotifications_gradeFiltersOnlyGradeCategory() {
        List<NotificationItemResponse> result =
                NotificationFilterHelper.filterNotifications(sampleList, NotificationFilter.GRADES);
        assertEquals(1, result.size());
        assertEquals("n-4", result.get(0).getId());
        assertEquals("GRADE", result.get(0).getCategory());
    }

    @Test
    public void filterNotifications_handlesNullAndEmptyGracefully() {
        List<NotificationItemResponse> nullResult =
                NotificationFilterHelper.filterNotifications(null, NotificationFilter.ALL);
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());

        List<NotificationItemResponse> emptyResult =
                NotificationFilterHelper.filterNotifications(Collections.emptyList(), NotificationFilter.URGENT);
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    public void synthesizeFromAssignments_correctlyMapsProperties() {
        AssignmentItem assignment = new AssignmentItem();
        assignment.setId("as-99");
        assignment.setTitle("Báo cáo đồ án");
        assignment.setClassName("NT118.Q11");
        assignment.setDueDate("2026-11-15T23:59:59Z");
        assignment.setSubmitted(false);

        List<NotificationItemResponse> synthesized =
                NotificationFilterHelper.synthesizeFromAssignments(Collections.singletonList(assignment));

        assertEquals(1, synthesized.size());
        NotificationItemResponse item = synthesized.get(0);
        assertEquals("as-99", item.getId());
        assertEquals("Nhắc nhở: Báo cáo đồ án", item.getTitle());
        assertEquals("NT118.Q11", item.getCourseCode());
        assertEquals("ASSIGNMENT", item.getCategory());
        assertTrue(item.isUnread());
        assertTrue(item.getBody().contains("Chưa nộp"));
    }

    @Test
    public void updateProfileRequest_holdsPropertiesAndSerializesJson() {
        UpdateProfileRequest req = new UpdateProfileRequest("Nguyễn Văn B", "0987654321");
        assertEquals("Nguyễn Văn B", req.getFullName());
        assertEquals("0987654321", req.getPhone());

        req.setAvatarUrl("https://example.com/avatar.png");
        req.setFcmToken("fcm-token-123");

        String json = new Gson().toJson(req);
        assertTrue(json.contains("Nguyễn Văn B"));
        assertTrue(json.contains("0987654321"));
        assertTrue(json.contains("fcm-token-123"));
    }

    @Test
    public void changePasswordRequest_holdsPropertiesAndSerializesJson() {
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass123!", "NewPass123!");
        assertEquals("OldPass123!", req.getCurrentPassword());
        assertEquals("NewPass123!", req.getNewPassword());

        String json = new Gson().toJson(req);
        assertTrue(json.contains("OldPass123!"));
        assertTrue(json.contains("NewPass123!"));
    }

    @Test
    public void resolveChangePasswordErrorMessage_mapsKnownCodes() {
        assertEquals("Mật khẩu hiện tại không chính xác",
                NotificationFilterHelper.resolveChangePasswordErrorMessage(40006, ""));
        assertEquals("Mật khẩu mới không được trùng mật khẩu cũ",
                NotificationFilterHelper.resolveChangePasswordErrorMessage(40007, ""));

        // Fallback for custom message
        assertEquals("Lỗi máy chủ",
                NotificationFilterHelper.resolveChangePasswordErrorMessage(50000, "Lỗi máy chủ"));

        // Default fallback
        assertEquals("Không thể đổi mật khẩu. Vui lòng thử lại.",
                NotificationFilterHelper.resolveChangePasswordErrorMessage(-1, null));
    }

    @Test
    public void userResponse_parsesJsonAndMapsRolesCorrectly() {
        String json = "{\n" +
                "  \"success\": true,\n" +
                "  \"code\": 200,\n" +
                "  \"message\": \"Success\",\n" +
                "  \"data\": {\n" +
                "    \"id\": \"u-101\",\n" +
                "    \"email\": \"student1@uit.edu.vn\",\n" +
                "    \"fullName\": \"Đặng Văn Học\",\n" +
                "    \"phone\": \"0911223344\",\n" +
                "    \"role\": \"STUDENT\",\n" +
                "    \"studentId\": \"22520001\",\n" +
                "    \"isActive\": true\n" +
                "  }\n" +
                "}";

        ApiResponse<?> apiResponse = new Gson().fromJson(json, ApiResponse.class);
        assertTrue(apiResponse.isSuccess());
        assertEquals(200, apiResponse.getCode());

        UserResponse user = new Gson().fromJson(new Gson().toJson(apiResponse.getData()), UserResponse.class);
        assertEquals("u-101", user.getId());
        assertEquals("student1@uit.edu.vn", user.getEmail());
        assertEquals("Đặng Văn Học", user.getFullName());
        assertEquals("0911223344", user.getPhone());
        assertEquals("STUDENT", user.getRole());
        assertEquals("22520001", user.getStudentId());
        assertTrue(user.getActive());
    }
}
