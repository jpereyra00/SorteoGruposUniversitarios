package com.uade.exammanager.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void allArgsConstructor_setsFieldsCorrectly() {
        Student s = new Student(1L, "Ana García", "12345");
        assertEquals(1L, s.getId());
        assertEquals("Ana García", s.getFullName());
        assertEquals("12345", s.getLegajo());
    }

    @Test
    void defaultConstructor_leavesFieldsNull() {
        Student s = new Student();
        assertNull(s.getId());
        assertNull(s.getFullName());
        assertNull(s.getLegajo());
    }

    @Test
    void equals_sameId_returnsTrue() {
        Student a = new Student(1L, "Ana García", "12345");
        Student b = new Student(1L, "Nombre Distinto", "99999");
        assertEquals(a, b);
    }

    @Test
    void equals_differentId_returnsFalse() {
        Student a = new Student(1L, "Ana García", "12345");
        Student b = new Student(2L, "Ana García", "12345");
        assertNotEquals(a, b);
    }

    @Test
    void equals_nullId_returnsFalse() {
        Student a = new Student();
        Student b = new Student();
        assertNotEquals(a, b);
    }

    @Test
    void setters_updateFields() {
        Student s = new Student();
        s.setId(5L);
        s.setFullName("Carlos López");
        s.setLegajo("67890");
        assertEquals(5L, s.getId());
        assertEquals("Carlos López", s.getFullName());
        assertEquals("67890", s.getLegajo());
    }
}
