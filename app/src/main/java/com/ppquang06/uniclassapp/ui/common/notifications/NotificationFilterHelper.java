package com.ppquang06.uniclassapp.ui.common.notifications;

import androidx.annotation.Nullable;

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

    /**
     * Controlled by the 3 Bento Hero status boxes: All, Unread, Urgent.
     */
    public enum StatusFilter {
        ALL,
        UNREAD,
        URGENT
    }

    /**
     * Controlled by the horizontal Category Chip bar below Bento Hero.
     */
    public enum CategoryFilter {
        ALL,
        QR,
        ASSIGNMENTS,
        GRADES,
        GENERAL
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

    /**
     * 2D filter combining Bento Status (ALL/UNREAD/URGENT) and Chip Category (ALL/QR/ASSIGNMENTS/GRADES/GENERAL).
     */
    public static List<NotificationItemResponse> filterByStatusAndCategory(
            @Nullable List<NotificationItemResponse> source,
            @Nullable StatusFilter statusFilter,
            @Nullable CategoryFilter categoryFilter
    ) {
        List<NotificationItemResponse> result = new ArrayList<>();
        if (source == null) {
            return result;
        }

        StatusFilter effectiveStatus = statusFilter != null ? statusFilter : StatusFilter.ALL;
        CategoryFilter effectiveCategory = categoryFilter != null ? categoryFilter : CategoryFilter.ALL;

        for (NotificationItemResponse item : source) {
            if (item == null) continue;

            // 1. Check Status Filter
            if (effectiveStatus == StatusFilter.UNREAD && !item.isUnread()) {
                continue;
            }
            if (effectiveStatus == StatusFilter.URGENT && !isUrgentItem(item)) {
                continue;
            }

            // 2. Check Category Filter
            if (effectiveCategory != CategoryFilter.ALL && !matchesCategory(item, effectiveCategory)) {
                continue;
            }

            result.add(item);
        }
        return result;
    }

    public static boolean isUrgentItem(@Nullable NotificationItemResponse item) {
        if (item == null) return false;
        if ("URGENT".equalsIgnoreCase(item.getCategory())) {
            return true;
        }
        String id = item.getId();
        return id != null && (id.startsWith("att-") || id.startsWith("urgent-"));
    }

    public static boolean matchesCategory(@Nullable NotificationItemResponse item, @Nullable CategoryFilter categoryFilter) {
        if (item == null) return false;
        if (categoryFilter == null || categoryFilter == CategoryFilter.ALL) {
            return true;
        }
        String id = item.getId() != null ? item.getId() : "";
        String cat = item.getCategory() != null ? item.getCategory() : "";

        switch (categoryFilter) {
            case QR:
                return id.startsWith("att-") || "QR".equalsIgnoreCase(cat);
            case ASSIGNMENTS:
                return "ASSIGNMENT".equalsIgnoreCase(cat)
                        || id.startsWith("assign-")
                        || id.startsWith("urgent-")
                        || id.startsWith("as-");
            case GRADES:
                return "GRADE".equalsIgnoreCase(cat) || id.startsWith("grade-");
            case GENERAL:
                return "GENERAL".equalsIgnoreCase(cat) || id.startsWith("mat-");
            default:
                return true;
        }
    }

    public static int markAllAsRead(@Nullable List<NotificationItemResponse> source) {
        if (source == null) return 0;
        int updated = 0;
        for (NotificationItemResponse item : source) {
            if (item != null && item.isUnread()) {
                item.setUnread(false);
                updated++;
            }
        }
        return updated;
    }

    public static String resolveCategoryBadgeLabel(@Nullable NotificationItemResponse item) {
        if (item == null) {
            return "Thông báo lớp";
        }
        String id = item.getId() != null ? item.getId() : "";
        String cat = item.getCategory() != null ? item.getCategory() : "";

        if (id.startsWith("att-") || "QR".equalsIgnoreCase(cat)) {
            return "Điểm danh QR";
        }
        if ("GRADE".equalsIgnoreCase(cat) || id.startsWith("grade-")) {
            return "Bảng điểm";
        }
        if ("ASSIGNMENT".equalsIgnoreCase(cat)
                || id.startsWith("assign-")
                || id.startsWith("urgent-")
                || id.startsWith("as-")) {
            return "Bài tập & Hạn nộp";
        }
        if ("URGENT".equalsIgnoreCase(cat)) {
            return "Khẩn cấp";
        }
        return "Thông báo lớp";
    }

    @Nullable
    public static NotificationItemResponse findActiveQrNotification(@Nullable List<NotificationItemResponse> source) {
        if (source == null) {
            return null;
        }
        for (NotificationItemResponse item : source) {
            if (item != null && item.getId() != null && item.getId().startsWith("att-")) {
                return item;
            }
        }
        return null;
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
