package com.uade.exammanager.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExamTest {

    @Test
    void defaultConstructor_setsDefaultValues() {
        Exam exam = new Exam();
        assertEquals(ExamStatus.DRAFT, exam.getStatus());
        assertEquals(1, exam.getPageCount());
        assertEquals(1, exam.getTopicsPerGroup());
        assertFalse(exam.isAllowTopicRepetition());
        assertEquals(0.30, exam.getGroupWeight(), 0.001);
        assertEquals(0.70, exam.getIndividualWeight(), 0.001);
        assertEquals(4.0, exam.getPassingGrade(), 0.001);
        assertEquals(7.0, exam.getPromotionGrade(), 0.001);
        assertEquals(4.0, exam.getRiskThreshold(), 0.001);
    }

    @Test
    void isWeightsValid_defaultWeights_returnsTrue() {
        Exam exam = new Exam();
        assertTrue(exam.isWeightsValid());
    }

    @Test
    void isWeightsValid_invalidWeights_returnsFalse() {
        Exam exam = new Exam();
        exam.setGroupWeight(0.50);
        exam.setIndividualWeight(0.60);
        assertFalse(exam.isWeightsValid());
    }

    @Test
    void isWeightsValid_customValidWeights_returnsTrue() {
        Exam exam = new Exam();
        exam.setGroupWeight(0.40);
        exam.setIndividualWeight(0.60);
        assertTrue(exam.isWeightsValid());
    }

    @Test
    void mainConstructor_setsFieldsCorrectly() {
        LocalDate date = LocalDate.of(2026, 7, 15);
        Exam exam = new Exam(1L, "Cálculo I", "Prof. López", date, 2, false, 1, null);
        assertEquals(1L, exam.getId());
        assertEquals("Cálculo I", exam.getSubjectName());
        assertEquals("Prof. López", exam.getTeachers());
        assertEquals(date, exam.getExamDate());
        assertEquals(2, exam.getPageCount());
    }

    @Test
    void examStatus_allValues_exist() {
        assertEquals(3, ExamStatus.values().length);
        assertNotNull(ExamStatus.DRAFT);
        assertNotNull(ExamStatus.ACTIVE);
        assertNotNull(ExamStatus.CLOSED);
    }

    @Test
    void examType_allValues_exist() {
        assertEquals(7, ExamType.values().length);
    }

    @Test
    void equals_sameId_returnsTrue() {
        Exam a = new Exam(1L, "Cálculo I", "Prof. López", null, 1, false, 1, null);
        Exam b = new Exam(1L, "Álgebra", "Prof. García", null, 1, false, 1, null);
        assertEquals(a, b);
    }
}
