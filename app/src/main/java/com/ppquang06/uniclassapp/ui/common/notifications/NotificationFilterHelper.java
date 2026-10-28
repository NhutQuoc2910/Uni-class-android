package com.ppquang06.uniclassapp.ui.common.notifications;

import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;

import java.util.ArrayList;
import java.util.List;

public class NotificationFilterHelper {

    public enum NotificationFilter {
        ALL,
        UNREAD,
        URGENT,
        ASSIGNMENTS,
        GRADES
    }

    public static List<NotificationItemResponse> filterNotifications(
            List<NotificationItemResponse> source,
            NotificationFilter filter
    ) {
        List<NotificationItemResponse> result = new ArrayList<>();
        if (source == null) {
            return result;
        }

        if (filter == null || filter == NotificationFilter.ALL) {
            result.addAll(source);
            return result;
        }

        for (NotificationItemResponse item : source) {
            if (item == null) continue;

            switch (filter) {
                case UNREAD:
                    if (item.isUnread()) {
                        result.add(item);
                    }
                    break;
                case URGENT:
                    if ("URGENT".equalsIgnoreCase(item.getCategory())) {
                        result.add(item);
                    }
                    break;
                case ASSIGNMENTS:
                    if ("ASSIGNMENT".equalsIgnoreCase(item.getCategory())) {
                        result.add(item);
                    }
                    break;
                case GRADES:
                    if ("GRADE".equalsIgnoreCase(item.getCategory())) {
                        result.add(item);
                    }
                    break;
                default:
                    result.add(item);
                    break;
            }
        }
        return result;
    }

    public static List<NotificationItemResponse> synthesizeFromAssignments(List<AssignmentItem> assignments) {
        List<NotificationItemResponse> result = new ArrayList<>();
        if (assignments == null) {
            return result;
        }

        for (AssignmentItem item : assignments) {
            if (item == null) continue;
            NotificationItemResponse notif = new NotificationItemResponse();
            notif.setId(item.getId() != null ? item.getId() : "");
            String title = item.getTitle() != null ? item.getTitle() : "Bài tập";
            notif.setTitle("Nhắc nhở: " + title);
            notif.setCourseCode(item.getClassName() != null ? item.getClassName() : "");
            String dueDate = item.getDueDate() != null ? item.getDueDate() : "";
            notif.setBody("Hạn nộp: " + dueDate + (item.isSubmitted() ? " · Đã nộp" : " · Chưa nộp"));
            notif.setCreatedAt(dueDate);
            notif.setCategory("ASSIGNMENT");
            notif.setUnread(!item.isSubmitted());
            result.add(notif);
        }
        return result;
    }

    public static String resolveChangePasswordErrorMessage(int errorCode, String fallback) {
        if (errorCode == 40006) {
            return "Mật khẩu hiện tại không chính xác";
        } else if (errorCode == 40007) {
            return "Mật khẩu mới không được trùng mật khẩu cũ";
        }
        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback;
        }
        return "Không thể đổi mật khẩu. Vui lòng thử lại.";
    }
}
