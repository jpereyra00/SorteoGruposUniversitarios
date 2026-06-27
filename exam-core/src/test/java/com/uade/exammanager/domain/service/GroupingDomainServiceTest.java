package com.uade.exammanager.domain.service;

import com.uade.exammanager.domain.model.ExamGroup;
import com.uade.exammanager.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GroupingDomainServiceTest {

    // Random con semilla fija para resultados deterministas.
    private final GroupingDomainService service = new GroupingDomainService(new Random(42));

    @Test
    void generateRandom_distributesAllStudents() {
        List<Long> students = List.of(1L, 2L, 3L, 4L, 5L);
        List<ExamGroup> groups = service.generateRandom(students, 2, 2);

        assertEquals(2, groups.size());
        int total = groups.stream().mapToInt(ExamGroup::memberCount).sum();
        assertEquals(5, total);
    }

    @Test
    void generateRandom_remainderGivesFirstGroupsExtraMember() {
        List<Long> students = List.of(1L, 2L, 3L, 4L, 5L);
        List<ExamGroup> groups = service.generateRandom(students, 2, 2);

        // 5 estudiantes en 2 grupos -> tamaños 3 y 2
        assertEquals(3, groups.get(0).memberCount());
        assertEquals(2, groups.get(1).memberCount());
    }

    @Test
    void generateRandom_noStudents_throws() {
        assertThrows(BusinessException.class,
                () -> service.generateRandom(List.of(), 2, 2));
    }

    @Test
    void generateRandom_moreGroupsThanStudents_throws() {
        assertThrows(BusinessException.class,
                () -> service.generateRandom(List.of(1L, 2L), 3, 1));
    }

    @Test
    void generateRandom_zeroGroups_throws() {
        assertThrows(BusinessException.class,
                () -> service.generateRandom(List.of(1L, 2L), 0, 1));
    }

    @Test
    void generateRandom_noEmptyGroups() {
        List<Long> students = List.of(1L, 2L, 3L);
        List<ExamGroup> groups = service.generateRandom(students, 3, 1);
        assertTrue(groups.stream().allMatch(g -> g.memberCount() >= 1));
    }

    @Test
    void generateManual_assignsToRequestedGroups() {
        List<Long> students = List.of(1L, 2L, 3L);
        Map<Long, Integer> assignments = new HashMap<>();
        assignments.put(1L, 1);
        assignments.put(2L, 2);
        assignments.put(3L, 1);

        List<ExamGroup> groups = service.generateManual(students, 2, assignments);

        assertEquals(2, groups.get(0).memberCount());
        assertTrue(groups.get(0).getMemberStudentIds().containsAll(List.of(1L, 3L)));
        assertEquals(1, groups.get(1).memberCount());
        assertTrue(groups.get(1).getMemberStudentIds().contains(2L));
    }

    @Test
    void generateManual_incompleteAssignments_throws() {
        Map<Long, Integer> assignments = new HashMap<>();
        assignments.put(1L, 1);

        assertThrows(BusinessException.class,
                () -> service.generateManual(List.of(1L, 2L), 2, assignments));
    }

    @Test
    void generateManual_invalidGroupNumber_throws() {
        Map<Long, Integer> assignments = new HashMap<>();
        assignments.put(1L, 5);
        assignments.put(2L, 1);

        assertThrows(BusinessException.class,
                () -> service.generateManual(List.of(1L, 2L), 2, assignments));
    }

    @Test
    void generateManual_emptyGroup_throws() {
        // Todos al grupo 1, grupo 2 queda vacío.
        Map<Long, Integer> assignments = new HashMap<>();
        assignments.put(1L, 1);
        assignments.put(2L, 1);

        assertThrows(BusinessException.class,
                () -> service.generateManual(List.of(1L, 2L), 2, assignments));
    }
}
