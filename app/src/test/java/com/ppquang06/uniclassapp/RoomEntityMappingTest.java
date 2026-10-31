package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.ppquang06.uniclassapp.data.local.entity.AssignmentEntity;
import com.ppquang06.uniclassapp.data.local.entity.ClassEntity;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;

import org.junit.Test;

public class RoomEntityMappingTest {

    @Test
    public void classEntity_fromAndToCourseClassItem_roundTrip() {
        CourseClassItem original = new CourseClassItem(
                "cls-101",
                "NT118",
                "Mobile App Development",
                "HK1 2025-2026",
                "C302",
                "Dr. Quang",
                "INVITE99",
                "ACTIVE",
                75,
                42
        );

        long before = System.currentTimeMillis();
        ClassEntity entity = ClassEntity.fromCourseClassItem(original);
        long after = System.currentTimeMillis();

        assertNotNull(entity);
        assertEquals("cls-101", entity.getId());
        assertEquals("NT118", entity.getCode());
        assertEquals("Mobile App Development", entity.getName());
        assertEquals("HK1 2025-2026", entity.getSemester());
        assertEquals("C302", entity.getRoom());
        assertEquals("Dr. Quang", entity.getLecturerName());
        assertEquals("INVITE99", entity.getInviteCode());
        assertEquals("ACTIVE", entity.getStatus());
        assertEquals(75, entity.getProgress());
        assertEquals(42, entity.getEnrolledCount());
        assertTrue(entity.getUpdatedAt() >= before && entity.getUpdatedAt() <= after);

        CourseClassItem mappedBack = entity.toCourseClassItem();
        assertNotNull(mappedBack);
        assertEquals("cls-101", mappedBack.getId());
        assertEquals("NT118", mappedBack.getCode());
        assertEquals("Mobile App Development", mappedBack.getName());
        assertEquals("HK1 2025-2026", mappedBack.getSemester());
        assertEquals("C302", mappedBack.getRoom());
        assertEquals("Dr. Quang", mappedBack.getLecturerName());
        assertEquals("INVITE99", mappedBack.getInviteCode());
        assertEquals("ACTIVE", mappedBack.getStatus());
        assertEquals(75, mappedBack.getProgress());
        assertEquals(42, mappedBack.getEnrolledCount());
    }

    @Test
    public void classEntity_nullHandling() {
        assertNull(ClassEntity.fromCourseClassItem(null));

        CourseClassItem sparseItem = new CourseClassItem();
        ClassEntity entity = ClassEntity.fromCourseClassItem(sparseItem);

        assertNotNull(entity);
        assertEquals("", entity.getId());
        assertNull(entity.getCode());
        assertNull(entity.getName());
        assertNull(entity.getSemester());
        assertNull(entity.getRoom());
        assertNull(entity.getLecturerName());
        assertNull(entity.getInviteCode());
        assertEquals("ACTIVE", entity.getStatus());
        assertEquals(0, entity.getProgress());
        assertEquals(0, entity.getEnrolledCount());

        CourseClassItem roundTripped = entity.toCourseClassItem();
        assertNotNull(roundTripped);
        assertEquals("", roundTripped.getId());
        assertEquals("ACTIVE", roundTripped.getStatus());
    }

    @Test
    public void assignmentEntity_fromAndToAssignmentItem_roundTrip() {
        AssignmentItem original = new AssignmentItem();
        original.setId("asg-001");
        original.setTitle("Lab 1: Room Database");
        original.setClassName("NT118.P11");
        original.setDueDate("2026-11-15T23:59:00Z");
        original.setSubmitted(true);

        long before = System.currentTimeMillis();
        AssignmentEntity entity = AssignmentEntity.fromAssignmentItem(original);
        long after = System.currentTimeMillis();

        assertNotNull(entity);
        assertEquals("asg-001", entity.getId());
        assertEquals("Lab 1: Room Database", entity.getTitle());
        assertEquals("NT118.P11", entity.getClassName());
        assertEquals("2026-11-15T23:59:00Z", entity.getDueDate());
        assertTrue(entity.isSubmitted());
        assertTrue(entity.getUpdatedAt() >= before && entity.getUpdatedAt() <= after);

        AssignmentItem mappedBack = entity.toAssignmentItem();
        assertNotNull(mappedBack);
        assertEquals("asg-001", mappedBack.getId());
        assertEquals("Lab 1: Room Database", mappedBack.getTitle());
        assertEquals("NT118.P11", mappedBack.getClassName());
        assertEquals("2026-11-15T23:59:00Z", mappedBack.getDueDate());
        assertTrue(mappedBack.isSubmitted());
    }

    @Test
    public void assignmentEntity_fromAndToClassAssignmentItem_roundTrip() {
        ClassDetailResponse.ClassAssignmentItem original =
                new ClassDetailResponse.ClassAssignmentItem(
                        "asg-002",
                        "Final Project Milestone",
                        "Implement offline caching with Room",
                        "2026-12-01T17:00:00Z",
                        "https://example.com/spec.pdf",
                        true,
                        9.5,
                        "Great job!"
                );

        AssignmentEntity entity = AssignmentEntity.fromClassAssignmentItem(
                "cls-101",
                "NT118.P11",
                original
        );

        assertNotNull(entity);
        assertEquals("asg-002", entity.getId());
        assertEquals("cls-101", entity.getClassId());
        assertEquals("NT118.P11", entity.getClassName());
        assertEquals("Final Project Milestone", entity.getTitle());
        assertEquals("Implement offline caching with Room", entity.getDescription());
        assertEquals("2026-12-01T17:00:00Z", entity.getDueDate());
        assertEquals("https://example.com/spec.pdf", entity.getAttachmentUrl());
        assertTrue(entity.isSubmitted());
        assertEquals(Double.valueOf(9.5), entity.getScore());
        assertEquals("Great job!", entity.getFeedback());

        ClassDetailResponse.ClassAssignmentItem mappedBack = entity.toClassAssignmentItem();
        assertNotNull(mappedBack);
        assertEquals("asg-002", mappedBack.getId());
        assertEquals("Final Project Milestone", mappedBack.getTitle());
        assertEquals("Implement offline caching with Room", mappedBack.getDescription());
        assertEquals("2026-12-01T17:00:00Z", mappedBack.getDueDate());
        assertEquals("https://example.com/spec.pdf", mappedBack.getAttachmentUrl());
        assertTrue(mappedBack.isSubmitted());
        assertEquals(Double.valueOf(9.5), mappedBack.getScore());
        assertEquals("Great job!", mappedBack.getFeedback());
    }

    @Test
    public void assignmentEntity_nullHandling() {
        assertNull(AssignmentEntity.fromAssignmentItem(null));
        assertNull(AssignmentEntity.fromClassAssignmentItem("cls-1", "Class 1", null));

        AssignmentItem emptyAssignmentItem = new AssignmentItem();
        AssignmentEntity entityFromItem = AssignmentEntity.fromAssignmentItem(emptyAssignmentItem);
        assertNotNull(entityFromItem);
        assertEquals("", entityFromItem.getId());
        assertNull(entityFromItem.getTitle());
        assertNull(entityFromItem.getClassName());
        assertNull(entityFromItem.getDueDate());
        assertFalse(entityFromItem.isSubmitted());

        ClassDetailResponse.ClassAssignmentItem ungradedItem =
                new ClassDetailResponse.ClassAssignmentItem(
                        null,
                        "Homework 1",
                        null,
                        null,
                        null,
                        false,
                        null,
                        null
                );
        AssignmentEntity entityFromClassItem =
                AssignmentEntity.fromClassAssignmentItem(null, null, ungradedItem);
        assertNotNull(entityFromClassItem);
        assertEquals("", entityFromClassItem.getId());
        assertNull(entityFromClassItem.getClassId());
        assertNull(entityFromClassItem.getClassName());
        assertEquals("Homework 1", entityFromClassItem.getTitle());
        assertNull(entityFromClassItem.getDescription());
        assertNull(entityFromClassItem.getDueDate());
        assertNull(entityFromClassItem.getAttachmentUrl());
        assertFalse(entityFromClassItem.isSubmitted());
        assertNull(entityFromClassItem.getScore());
        assertNull(entityFromClassItem.getFeedback());

        ClassDetailResponse.ClassAssignmentItem roundTripped =
                entityFromClassItem.toClassAssignmentItem();
        assertNotNull(roundTripped);
        assertEquals("", roundTripped.getId());
        assertEquals("Homework 1", roundTripped.getTitle());
        assertNull(roundTripped.getScore());
        assertNull(roundTripped.getFeedback());
    }
}
