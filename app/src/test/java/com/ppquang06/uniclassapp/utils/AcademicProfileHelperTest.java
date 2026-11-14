package com.ppquang06.uniclassapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ppquang06.uniclassapp.data.model.response.UserResponse;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AcademicProfileHelperTest {

    @Test
    public void dropdownCatalogs_containRequiredVietnameseOptions() {
        List<String> faculties = Arrays.asList(AcademicProfileHelper.FACULTY_OPTIONS);
        assertEquals(7, faculties.size());
        assertTrue(faculties.contains("Mạng máy tính và Truyền thông"));
        assertTrue(faculties.contains("Bộ môn Toán - Lý"));
        assertTrue(faculties.contains("Công nghệ phần mềm"));
        assertTrue(faculties.contains("Khoa học máy tính"));
        assertTrue(faculties.contains("Kỹ thuật máy tính"));
        assertTrue(faculties.contains("Khoa học và Kỹ thuật thông tin"));
        assertTrue(faculties.contains("Hệ thống thông tin"));

        List<String> eduPrograms = Arrays.asList(AcademicProfileHelper.EDUCATION_PROGRAM_OPTIONS);
        assertEquals(5, eduPrograms.size());
        assertTrue(eduPrograms.contains("Đại học Chính Quy"));
        assertTrue(eduPrograms.contains("Đại học Chương trình tiên tiến"));
        assertTrue(eduPrograms.contains("Đại học Chương trình tài năng"));
        assertTrue(eduPrograms.contains("Cao học"));
        assertTrue(eduPrograms.contains("Cao đẳng"));

        List<String> degrees = Arrays.asList(AcademicProfileHelper.ACADEMIC_DEGREE_OPTIONS);
        assertTrue(degrees.contains("Thạc sĩ"));
        assertTrue(degrees.contains("Tiến sĩ"));

        List<String> ranks = Arrays.asList(AcademicProfileHelper.ACADEMIC_RANK_OPTIONS);
        assertTrue(ranks.contains(AcademicProfileHelper.NO_ACADEMIC_RANK));
        assertTrue(ranks.contains("Phó Giáo sư"));
        assertTrue(ranks.contains("Giáo sư"));
    }

    @Test
    public void resolveStudentFields_readsRealDataOrNotSet() {
        UserResponse emptyUser = new UserResponse();
        emptyUser.setEmail("student@gmail.com");

        assertEquals(AcademicProfileHelper.NOT_SET_LABEL, AcademicProfileHelper.resolveStudentId(emptyUser));
        assertEquals(AcademicProfileHelper.NOT_SET_LABEL, AcademicProfileHelper.resolveFaculty(emptyUser));
        assertEquals(AcademicProfileHelper.NOT_SET_LABEL, AcademicProfileHelper.resolveCohort(emptyUser));
        assertEquals(AcademicProfileHelper.NOT_SET_LABEL, AcademicProfileHelper.resolveEducationProgram(emptyUser));

        UserResponse realStudent = new UserResponse();
        realStudent.setStudentId("22521190");
        realStudent.setFaculty("Mạng máy tính và Truyền thông");
        realStudent.setEducationProgram("Đại học Chương trình tài năng");

        assertEquals("22521190", AcademicProfileHelper.resolveStudentId(realStudent));
        assertEquals("Mạng máy tính và Truyền thông", AcademicProfileHelper.resolveFaculty(realStudent));
        assertEquals("Khóa K22 (2022 - 2026)", AcademicProfileHelper.resolveCohort(realStudent));
        assertEquals("Đại học Chương trình tài năng", AcademicProfileHelper.resolveEducationProgram(realStudent));

        realStudent.setCohort("K2022 (2022-2026)");
        assertEquals("K2022 (2022-2026)", AcademicProfileHelper.resolveCohort(realStudent));
    }

    @Test
    public void resolveStudentId_extractsFromNumericEmailIfStudentIdUnset() {
        UserResponse user = new UserResponse();
        user.setEmail("23520001@gm.uit.edu.vn");

        assertEquals("23520001", AcademicProfileHelper.resolveStudentId(user));
        assertEquals("Khóa K23 (2023 - 2027)", AcademicProfileHelper.resolveCohort(user));
    }

    @Test
    public void formatAcademicTitle_formatsVietnameseDegreesAndOptionalRanks() {
        assertEquals("PGS.TS", AcademicProfileHelper.formatAcademicTitle("Tiến sĩ", "Phó Giáo sư"));
        assertEquals("GS.TS", AcademicProfileHelper.formatAcademicTitle("Tiến sĩ", "Giáo sư"));
        assertEquals("GS.TSKH", AcademicProfileHelper.formatAcademicTitle("Tiến sĩ khoa học", "Giáo sư"));
        assertEquals("Thạc sĩ", AcademicProfileHelper.formatAcademicTitle("Thạc sĩ", ""));
        assertEquals("Tiến sĩ", AcademicProfileHelper.formatAcademicTitle("Tiến sĩ", AcademicProfileHelper.NO_ACADEMIC_RANK));
        assertEquals(AcademicProfileHelper.NOT_SET_LABEL, AcademicProfileHelper.formatAcademicTitle("", ""));

        UserResponse lecturer = new UserResponse();
        lecturer.setLecturerId("GV101");
        lecturer.setFaculty("Công nghệ phần mềm");
        lecturer.setAcademicDegree("Tiến sĩ");
        lecturer.setAcademicRank("Phó Giáo sư");
        lecturer.setPosition("Trưởng bộ môn");

        assertEquals("GV101", AcademicProfileHelper.resolveLecturerId(lecturer));
        assertEquals("Công nghệ phần mềm", AcademicProfileHelper.resolveFaculty(lecturer));
        assertEquals("PGS.TS", AcademicProfileHelper.formatAcademicTitle(lecturer));
        assertEquals("Trưởng bộ môn", AcademicProfileHelper.resolvePosition(lecturer));
    }

    @Test
    public void buildDigitalIdQrPayload_formatsCorrectlyWithOrWithoutStudentId() {
        UserResponse student = new UserResponse();
        student.setStudentId("22521190");
        student.setEmail("22521190@gm.uit.edu.vn");
        assertEquals("UNICLASS:STUDENT:22521190:22521190@gm.uit.edu.vn",
                AcademicProfileHelper.buildDigitalIdQrPayload(student, false));

        UserResponse unsetStudent = new UserResponse();
        unsetStudent.setId("uuid-123");
        unsetStudent.setEmail("student@gmail.com");
        assertEquals("UNICLASS:STUDENT:uuid-123:student@gmail.com",
                AcademicProfileHelper.buildDigitalIdQrPayload(unsetStudent, false));
    }

    @Test
    public void mergeCachedAcademicFieldsIfNull_preservesCachedOnlyWhenLiveIsNull() {
        UserResponse cached = new UserResponse();
        cached.setId("u1");
        cached.setFaculty("Hệ thống thông tin");
        cached.setEducationProgram("Cao học");

        UserResponse liveFromOldServer = new UserResponse();
        liveFromOldServer.setId("u1");
        liveFromOldServer.setFullName("New Name");

        AcademicProfileHelper.mergeCachedAcademicFieldsIfNull(cached, liveFromOldServer);
        assertEquals("Hệ thống thông tin", liveFromOldServer.getFaculty());
        assertEquals("Cao học", liveFromOldServer.getEducationProgram());

        UserResponse liveFromNewServer = new UserResponse();
        liveFromNewServer.setId("u1");
        liveFromNewServer.setFaculty("Khoa học máy tính");
        liveFromNewServer.setEducationProgram("Đại học Chính Quy");

        AcademicProfileHelper.mergeCachedAcademicFieldsIfNull(cached, liveFromNewServer);
        assertEquals("Khoa học máy tính", liveFromNewServer.getFaculty());
        assertEquals("Đại học Chính Quy", liveFromNewServer.getEducationProgram());
    }
}
