package com.uade.exammanager.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Test
    void allArgsConstructor_setsFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Grade g = new Grade(1L, 10L, 5L, 3L, 8.0, 9.0, 8.7, StudentStatus.APPROVED, "Buen trabajo", now);
        assertEquals(1L, g.getId());
        assertEquals(10L, g.getStudentId());
        assertEquals(5L, g.getExamId());
        assertEquals(8.0, g.getGroupGrade());
        assertEquals(9.0, g.getIndividualGrade());
        assertEquals(8.7, g.getFinalGrade(), 0.001);
        assertEquals(StudentStatus.APPROVED, g.getStatus());
        assertEquals("Buen trabajo", g.getObservations());
    }

    @Test
    void isGraded_withFinalGrade_returnsTrue() {
        Grade g = new Grade();
        g.setFinalGrade(7.5);
        assertTrue(g.isGraded());
    }

    @Test
    void isGraded_withoutFinalGrade_returnsFalse() {
        Grade g = new Grade();
        assertFalse(g.isGraded());
    }

    @Test
    void studentStatus_allValues_exist() {
        assertEquals(6, StudentStatus.values().length);
        assertNotNull(StudentStatus.ABSENT);
        assertNotNull(StudentStatus.FAILED);
        assertNotNull(StudentStatus.AT_RISK);
        assertNotNull(StudentStatus.REGULAR);
        assertNotNull(StudentStatus.APPROVED);
        assertNotNull(StudentStatus.PROMOTED);
    }

    @Test
    void attendance_absentCheck_works() {
        Attendance present = new Attendance(1L, 10L, 5L, AttendanceStatus.PRESENT);
        Attendance absent = new Attendance(2L, 11L, 5L, AttendanceStatus.ABSENT);
        assertFalse(present.isAbsent());
        assertTrue(absent.isAbsent());
    }

    @Test
    void examGroup_memberCount_returnsCorrectSize() {
        ExamGroup group = new ExamGroup(1L, "Grupo 1");
        group.getMemberStudentIds().add(10L);
        group.getMemberStudentIds().add(20L);
        group.getMemberStudentIds().add(30L);
        assertEquals(3, group.memberCount());
    }

    @Test
    void unit_constructorAndGetters_work() {
        Unit unit = new Unit(1L, "Unidad 1", "Introducción al cálculo", 1);
        assertEquals("Unidad 1", unit.getName());
        assertEquals(1, unit.getOrder());
    }

    @Test
    void topic_withUnitId_linksCorrectly() {
        Topic topic = new Topic(1L, "Derivadas", 3L);
        assertEquals("Derivadas", topic.getName());
        assertEquals(3L, topic.getUnitId());
    }
}
