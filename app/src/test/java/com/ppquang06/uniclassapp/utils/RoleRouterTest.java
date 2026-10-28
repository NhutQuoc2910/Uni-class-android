package com.ppquang06.uniclassapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ppquang06.uniclassapp.ui.lecturer.LecturerMainActivity;
import com.ppquang06.uniclassapp.ui.student.StudentMainActivity;

import org.junit.Test;

public class RoleRouterTest {

    @Test
    public void isLecturerRole_recognizesLecturerVariants() {
        assertTrue(RoleRouter.isLecturerRole("ROLE_LECTURER"));
        assertTrue(RoleRouter.isLecturerRole("LECTURER"));
        assertTrue(RoleRouter.isLecturerRole("lecturer"));
        assertFalse(RoleRouter.isLecturerRole("ROLE_STUDENT"));
        assertFalse(RoleRouter.isLecturerRole("STUDENT"));
        assertFalse(RoleRouter.isLecturerRole(null));
    }

    @Test
    public void resolveTargetActivity_returnsCorrectHostClass() {
        assertEquals(LecturerMainActivity.class, RoleRouter.resolveTargetActivity("ROLE_LECTURER"));
        assertEquals(LecturerMainActivity.class, RoleRouter.resolveTargetActivity("LECTURER"));
        assertEquals(StudentMainActivity.class, RoleRouter.resolveTargetActivity("ROLE_STUDENT"));
        assertEquals(StudentMainActivity.class, RoleRouter.resolveTargetActivity(null));
    }
}
