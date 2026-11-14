package com.ppquang06.uniclassapp.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ppquang06.uniclassapp.data.model.response.UserResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper managing real academic/faculty profile attributes, Vietnamese academic
 * dropdown catalogs, formatting, and digital ID QR payload construction.
 */
public final class AcademicProfileHelper {

    public static final String NOT_SET_LABEL = "Chưa cập nhật";
    public static final String NO_ACADEMIC_RANK = "(Không có)";

    public static final String[] FACULTY_OPTIONS = {
            "Mạng máy tính và Truyền thông",
            "Bộ môn Toán - Lý",
            "Công nghệ phần mềm",
            "Khoa học máy tính",
            "Kỹ thuật máy tính",
            "Khoa học và Kỹ thuật thông tin",
            "Hệ thống thông tin"
    };

    public static final String[] EDUCATION_PROGRAM_OPTIONS = {
            "Đại học Chính Quy",
            "Đại học Chương trình tiên tiến",
            "Đại học Chương trình tài năng",
            "Cao học",
            "Cao đẳng"
    };

    public static final String[] ACADEMIC_DEGREE_OPTIONS = {
            "Cử nhân / Kỹ sư",
            "Thạc sĩ",
            "Tiến sĩ",
            "Tiến sĩ khoa học"
    };

    public static final String[] ACADEMIC_RANK_OPTIONS = {
            NO_ACADEMIC_RANK,
            "Phó Giáo sư",
            "Giáo sư"
    };

    public static final String[] POSITION_OPTIONS = {
            "Giảng viên",
            "Giảng viên chính",
            "Trưởng bộ môn",
            "Phó trưởng bộ môn",
            "Trưởng khoa",
            "Phó trưởng khoa",
            "Cố vấn học tập",
            "Trợ giảng"
    };

    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^(\\d{2})\\d+");

    private AcademicProfileHelper() {
        // Utility class
    }

    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.trim().isEmpty();
    }

    @NonNull
    public static String resolveStudentId(@Nullable UserResponse user) {
        if (user == null) return NOT_SET_LABEL;
        if (!isEmpty(user.getStudentId())) {
            return user.getStudentId().trim();
        }
        if (!isEmpty(user.getEmail())) {
            String emailPrefix = user.getEmail().split("@")[0].trim();
            if (emailPrefix.matches("^\\d{7,10}$")) {
                return emailPrefix;
            }
        }
        return NOT_SET_LABEL;
    }

    @NonNull
    public static String resolveLecturerId(@Nullable UserResponse user) {
        if (user == null) return NOT_SET_LABEL;
        if (!isEmpty(user.getLecturerId())) {
            return user.getLecturerId().trim();
        }
        return NOT_SET_LABEL;
    }

    @NonNull
    public static String resolveFaculty(@Nullable UserResponse user) {
        if (user != null && !isEmpty(user.getFaculty())) {
            return user.getFaculty().trim();
        }
        return NOT_SET_LABEL;
    }

    @NonNull
    public static String resolveCohort(@Nullable UserResponse user) {
        if (user != null && !isEmpty(user.getCohort())) {
            return user.getCohort().trim();
        }
        String studentId = resolveStudentId(user);
        if (!NOT_SET_LABEL.equals(studentId)) {
            Matcher m = STUDENT_ID_PATTERN.matcher(studentId);
            if (m.find()) {
                try {
                    int prefix = Integer.parseInt(m.group(1));
                    int startYear = 2000 + prefix;
                    int endYear = startYear + 4;
                    return "Khóa K" + prefix + " (" + startYear + " - " + endYear + ")";
                } catch (NumberFormatException ignored) {
                    // Ignore and return NOT_SET_LABEL
                }
            }
        }
        return NOT_SET_LABEL;
    }

    @NonNull
    public static String resolveEducationProgram(@Nullable UserResponse user) {
        if (user != null && !isEmpty(user.getEducationProgram())) {
            return user.getEducationProgram().trim();
        }
        return NOT_SET_LABEL;
    }

    @NonNull
    public static String resolvePosition(@Nullable UserResponse user) {
        if (user != null && !isEmpty(user.getPosition())) {
            return user.getPosition().trim();
        }
        return NOT_SET_LABEL;
    }

    @NonNull
    public static String formatAcademicTitle(@Nullable UserResponse user) {
        if (user == null) return NOT_SET_LABEL;
        return formatAcademicTitle(user.getAcademicDegree(), user.getAcademicRank());
    }

    @NonNull
    public static String formatAcademicTitle(@Nullable String degree, @Nullable String rank) {
        String cleanRank = (!isEmpty(rank) && !NO_ACADEMIC_RANK.equals(rank.trim())) ? rank.trim() : "";
        String cleanDegree = !isEmpty(degree) ? degree.trim() : "";

        if (!cleanRank.isEmpty() && !cleanDegree.isEmpty()) {
            String rankPrefix;
            if ("Giáo sư".equalsIgnoreCase(cleanRank)) {
                rankPrefix = "GS.";
            } else if ("Phó Giáo sư".equalsIgnoreCase(cleanRank)) {
                rankPrefix = "PGS.";
            } else {
                rankPrefix = cleanRank + " ";
            }

            String degreeShort;
            if ("Tiến sĩ".equalsIgnoreCase(cleanDegree)) {
                degreeShort = "TS";
            } else if ("Tiến sĩ khoa học".equalsIgnoreCase(cleanDegree)) {
                degreeShort = "TSKH";
            } else if ("Thạc sĩ".equalsIgnoreCase(cleanDegree)) {
                degreeShort = "ThS";
            } else {
                degreeShort = cleanDegree;
            }
            return rankPrefix + degreeShort;
        }

        if (!cleanRank.isEmpty()) {
            return cleanRank;
        }
        if (!cleanDegree.isEmpty()) {
            return cleanDegree;
        }
        return NOT_SET_LABEL;
    }

    @NonNull
    public static String resolveStudentStatus(@Nullable UserResponse user) {
        if (user != null && Boolean.FALSE.equals(user.getActive())) {
            return "Tạm dừng";
        }
        return "Đang theo học";
    }

    @NonNull
    public static String resolveLecturerStatus(@Nullable UserResponse user) {
        if (user != null && Boolean.FALSE.equals(user.getActive())) {
            return "Tạm nghỉ";
        }
        return "Đang công tác";
    }

    @NonNull
    public static String buildDigitalIdQrPayload(@NonNull UserResponse user, boolean isLecturer) {
        String id = isLecturer ? resolveLecturerId(user) : resolveStudentId(user);
        if (NOT_SET_LABEL.equals(id)) {
            id = !isEmpty(user.getId()) ? user.getId() : (!isEmpty(user.getEmail()) ? user.getEmail() : "UNKNOWN");
        }
        String email = user.getEmail() != null ? user.getEmail() : "";
        String role = isLecturer ? "LECTURER" : "STUDENT";
        return "UNICLASS:" + role + ":" + id + ":" + email;
    }

    public static void mergeCachedAcademicFieldsIfNull(@Nullable UserResponse cached, @NonNull UserResponse live) {
        if (cached == null) return;
        if (cached.getId() != null && live.getId() != null && !cached.getId().equals(live.getId())) {
            return;
        }
        if (live.getStudentId() == null && cached.getStudentId() != null) live.setStudentId(cached.getStudentId());
        if (live.getLecturerId() == null && cached.getLecturerId() != null) live.setLecturerId(cached.getLecturerId());
        if (live.getFaculty() == null && cached.getFaculty() != null) live.setFaculty(cached.getFaculty());
        if (live.getCohort() == null && cached.getCohort() != null) live.setCohort(cached.getCohort());
        if (live.getEducationProgram() == null && cached.getEducationProgram() != null) live.setEducationProgram(cached.getEducationProgram());
        if (live.getAcademicDegree() == null && cached.getAcademicDegree() != null) live.setAcademicDegree(cached.getAcademicDegree());
        if (live.getAcademicRank() == null && cached.getAcademicRank() != null) live.setAcademicRank(cached.getAcademicRank());
        if (live.getPosition() == null && cached.getPosition() != null) live.setPosition(cached.getPosition());
    }
}
