package com.ppquang06.uniclassapp.ui.student.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.JoinClassRequest;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;

import org.junit.Test;

public class JoinClassPayloadTest {

    @Test
    public void joinClassRequest_normalizesInviteCode() {
        String input = " nt118demo ";
        JoinClassRequest req = new JoinClassRequest(input.trim().toUpperCase());
        assertEquals("NT118DEMO", req.getInviteCode());
    }

    @Test
    public void apiResponse_deserializesCodeAndMessageCorrectly() {
        String json = "{\"success\":false,\"code\":40008,\"message\":\"Bạn đã tham gia lớp học này rồi\",\"data\":null}";
        ApiResponse<?> response = new Gson().fromJson(json, ApiResponse.class);

        assertFalse(response.isSuccess());
        assertEquals(40008, response.getCode());
        assertEquals("Bạn đã tham gia lớp học này rồi", response.getMessage());
    }

    @Test
    public void courseClassItem_holdsExpectedProperties() {
        CourseClassItem item = new CourseClassItem();
        item.setId("class-1");
        item.setCode("NT118");
        item.setName("Phát triển ứng dụng trên thiết bị di động");
        item.setSemester("HK1 2026-2027");
        item.setRoom("B4.02");
        item.setLecturerName("ThS. Nguyễn Văn A");
        item.setProgress(85);
        item.setEnrolledCount(45);

        assertEquals("class-1", item.getId());
        assertEquals("NT118", item.getCode());
        assertEquals("Phát triển ứng dụng trên thiết bị di động", item.getName());
        assertEquals("HK1 2026-2027", item.getSemester());
        assertEquals("B4.02", item.getRoom());
        assertEquals("ThS. Nguyễn Văn A", item.getLecturerName());
        assertEquals(85, item.getProgress());
        assertEquals(45, item.getEnrolledCount());
    }
}
