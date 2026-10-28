package com.ppquang06.uniclassapp.ui.student.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AttendanceHistoryAndGradesFlowTest {

    private final Gson gson = new Gson();

    @Test
    public void absenceWarning_shownWhenAbsentCountAtOrAboveThreshold() {
        assertFalse("0 absences should not show warning",
                StudentAttendanceHistoryFragment.shouldShowAbsenceWarning(0));
        assertFalse("1 absence should not show warning",
                StudentAttendanceHistoryFragment.shouldShowAbsenceWarning(1));
        assertTrue("2 absences must trigger warning banner",
                StudentAttendanceHistoryFragment.shouldShowAbsenceWarning(2));
        assertTrue("3 absences must trigger warning banner",
                StudentAttendanceHistoryFragment.shouldShowAbsenceWarning(3));
        assertTrue("4 absences must trigger warning banner",
                StudentAttendanceHistoryFragment.shouldShowAbsenceWarning(4));
    }

    @Test
    public void absenceWarning_buildWarningSubtitle() {
        String msg2 = StudentAttendanceHistoryFragment.buildWarningSubtitle(2);
        assertTrue("Message for 2 absences should warn about 1 more absence",
                msg2.contains("vắng 2 buổi") && msg2.contains("1 buổi nữa"));

        String msg3 = StudentAttendanceHistoryFragment.buildWarningSubtitle(3);
        assertTrue("Message for 3 absences should indicate threshold exceeded",
                msg3.contains("vắng 3 buổi") && msg3.contains("cấm thi"));

        String msg1 = StudentAttendanceHistoryFragment.buildWarningSubtitle(1);
        assertEquals("", msg1);
    }

    @Test
    public void calculateRemainingSessions_computesCorrectly() {
        assertEquals("6 of 15 completed should leave 9 sessions",
                9, StudentAttendanceHistoryFragment.calculateRemainingSessions(6));
        assertEquals("0 completed should leave 15 sessions",
                15, StudentAttendanceHistoryFragment.calculateRemainingSessions(0));
        assertEquals("15 completed should leave 0 sessions",
                0, StudentAttendanceHistoryFragment.calculateRemainingSessions(15));
        assertEquals("Exceeding 15 should clamp to 0",
                0, StudentAttendanceHistoryFragment.calculateRemainingSessions(16));
    }

    @Test
    public void gradeBadgeStatusAndLabel_resolvesCorrectColorAndLabel() {
        // >= 8.0: green (Status.PRESENT)
        assertEquals(UiBadgeHelper.Status.PRESENT, UiBadgeHelper.resolveGradeStatus(9.5));
        assertEquals("9.5 / 10", UiBadgeHelper.resolveGradeLabel(9.5));

        assertEquals(UiBadgeHelper.Status.PRESENT, UiBadgeHelper.resolveGradeStatus(8.0));
        assertEquals("8.0 / 10", UiBadgeHelper.resolveGradeLabel(8.0));

        // >= 5.0 and < 8.0: primary (Status.OPEN)
        assertEquals(UiBadgeHelper.Status.OPEN, UiBadgeHelper.resolveGradeStatus(7.5));
        assertEquals("7.5 / 10", UiBadgeHelper.resolveGradeLabel(7.5));

        assertEquals(UiBadgeHelper.Status.OPEN, UiBadgeHelper.resolveGradeStatus(5.0));
        assertEquals("5.0 / 10", UiBadgeHelper.resolveGradeLabel(5.0));

        // < 5.0: error (Status.OVERDUE)
        assertEquals(UiBadgeHelper.Status.OVERDUE, UiBadgeHelper.resolveGradeStatus(4.5));
        assertEquals("4.5 / 10", UiBadgeHelper.resolveGradeLabel(4.5));

        // null: warning/gray (Status.DUE_SOON)
        assertEquals(UiBadgeHelper.Status.DUE_SOON, UiBadgeHelper.resolveGradeStatus(null));
        assertEquals("Chưa chấm", UiBadgeHelper.resolveGradeLabel(null));
    }

    @Test
    public void averageScoreAndOverview_formatsCorrectly() {
        assertEquals("8.5", StudentGradesFragment.formatAverageScore(8.5));
        assertEquals("7.0", StudentGradesFragment.formatAverageScore(7.0));
        assertEquals("10.0", StudentGradesFragment.formatAverageScore(10.0));

        String desc = StudentGradesFragment.buildGradesOverviewDesc(3, 2);
        assertEquals("Đã hoàn thành 3 bài tập · 2 bài đã có điểm", desc);
    }

    @Test
    public void dtoRoundtripping_classGradesResponse() {
        List<ClassGradesResponse.GradeItem> grades = new ArrayList<>();
        grades.add(new ClassGradesResponse.GradeItem(
                "assign-1",
                "Bài tập 1: Layout XML",
                "2026-10-24T14:30:00Z",
                true,
                8.5,
                "Bài làm rất tốt",
                "ThS. Trần Minh Khoa",
                "K"
        ));
        grades.add(new ClassGradesResponse.GradeItem(
                "assign-2",
                "Bài tập 2: Retrofit API",
                null,
                false,
                null,
                null,
                "ThS. Trần Minh Khoa",
                "K"
        ));

        ClassGradesResponse response = new ClassGradesResponse(
                "cls-100",
                "NT118.Q11",
                "Phát triển ứng dụng di động",
                "ThS. Trần Minh Khoa",
                8.5,
                1,
                2,
                grades
        );

        String json = gson.toJson(response);
        ClassGradesResponse deserialized = gson.fromJson(json, ClassGradesResponse.class);

        assertNotNull(deserialized);
        assertEquals("cls-100", deserialized.getClassId());
        assertEquals("NT118.Q11", deserialized.getClassCode());
        assertEquals("Phát triển ứng dụng di động", deserialized.getClassName());
        assertEquals(8.5, deserialized.getAverageScore(), 0.001);
        assertEquals(1, deserialized.getGradedCount());
        assertEquals(2, deserialized.getTotalAssignments());
        assertEquals(2, deserialized.getGrades().size());

        ClassGradesResponse.GradeItem item1 = deserialized.getGrades().get(0);
        assertEquals("assign-1", item1.getAssignmentId());
        assertTrue(item1.isSubmitted());
        assertEquals(Double.valueOf(8.5), item1.getScore());
        assertEquals("Bài làm rất tốt", item1.getFeedback());

        ClassGradesResponse.GradeItem item2 = deserialized.getGrades().get(1);
        assertEquals("assign-2", item2.getAssignmentId());
        assertFalse(item2.isSubmitted());
    }

    @Test
    public void dtoRoundtripping_classDetailAttendanceRecords() {
        ClassDetailResponse response = new ClassDetailResponse();
        response.setId("cls-100");
        response.setName("Lập trình Di động");
        response.setPresentCount(4);
        response.setAbsentCount(2);
        response.setTotalSessions(6);

        List<ClassDetailResponse.AttendanceSessionRecordItem> records = new ArrayList<>();
        records.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-1", 1, "2026-10-01", "PRESENT", "2026-10-01T07:31:00Z"));
        records.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-2", 2, "2026-10-08", "ABSENT", null));
        response.setAttendanceRecords(records);

        String json = gson.toJson(response);
        ClassDetailResponse deserialized = gson.fromJson(json, ClassDetailResponse.class);

        assertNotNull(deserialized);
        assertEquals(4, deserialized.getPresentCount());
        assertEquals(2, deserialized.getAbsentCount());
        assertEquals(6, deserialized.getTotalSessions());
        assertEquals(2, deserialized.getAttendanceRecords().size());
        assertEquals("PRESENT", deserialized.getAttendanceRecords().get(0).getStatus());
        assertEquals("ABSENT", deserialized.getAttendanceRecords().get(1).getStatus());
    }
}
