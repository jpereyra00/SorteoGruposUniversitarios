package com.uade.exammanager.domain.service;

import com.uade.exammanager.domain.model.ExamGroup;
import com.uade.exammanager.exception.BusinessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Lógica de negocio pura para la distribución de estudiantes en grupos.
 * No depende de Spring ni de JPA: opera sobre IDs y modelos de dominio.
 */
public class GroupingDomainService {

    private final Random random;

    public GroupingDomainService() {
        this(new Random());
    }

    /** Constructor para tests deterministas. */
    public GroupingDomainService(Random random) {
        this.random = random;
    }

    /**
     * Distribuye aleatoriamente los estudiantes en {@code groupsCount} grupos,
     * repartiendo el resto de forma equitativa (los primeros grupos reciben uno extra).
     */
    public List<ExamGroup> generateRandom(List<Long> studentIds, int groupsCount, int membersPerGroup) {
        if (groupsCount <= 0) {
            throw new BusinessException("La cantidad de grupos debe ser mayor a 0.");
        }
        if (membersPerGroup <= 0) {
            throw new BusinessException("La cantidad de integrantes por grupo debe ser mayor a 0.");
        }
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BusinessException("No hay estudiantes cargados para generar grupos.");
        }

        int totalStudents = studentIds.size();
        if (groupsCount > totalStudents) {
            throw new BusinessException("No podés crear " + groupsCount + " grupos con solo " + totalStudents
                    + " estudiantes. Sugerencia: usá entre 1 y " + totalStudents + " grupos.");
        }
        if (membersPerGroup > totalStudents) {
            throw new BusinessException("La sugerencia de integrantes por grupo (" + membersPerGroup
                    + ") no puede superar el total de estudiantes cargados (" + totalStudents + ").");
        }

        List<ExamGroup> groups = createGroups(groupsCount);
        List<Long> shuffled = new ArrayList<>(studentIds);
        Collections.shuffle(shuffled, random);

        int baseSize = totalStudents / groupsCount;
        int groupsWithExtraMember = totalStudents % groupsCount;

        int index = 0;
        for (int i = 0; i < groups.size(); i++) {
            int groupSize = baseSize + (i < groupsWithExtraMember ? 1 : 0);
            for (int j = 0; j < groupSize; j++) {
                groups.get(i).getMemberStudentIds().add(shuffled.get(index++));
            }
        }

        ensureNoEmptyGroups(groups);
        return groups;
    }

    /**
     * Crea los grupos a partir de una asignación manual estudiante → número de grupo (1..groupsCount).
     */
    public List<ExamGroup> generateManual(List<Long> studentIds, int groupsCount, Map<Long, Integer> assignments) {
        if (groupsCount <= 0) {
            throw new BusinessException("La cantidad de grupos debe ser mayor a 0.");
        }
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BusinessException("Debe cargar estudiantes antes de generar grupos manuales.");
        }
        if (studentIds.size() < groupsCount) {
            throw new BusinessException("No se pueden generar grupos vacíos: debe haber al menos un estudiante por grupo.");
        }
        if (assignments == null || assignments.size() != studentIds.size()) {
            throw new BusinessException("Debe asignar un grupo a todos los estudiantes.");
        }

        List<ExamGroup> groups = createGroups(groupsCount);
        Map<Integer, ExamGroup> byNumber = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            byNumber.put(i + 1, groups.get(i));
        }

        Set<Long> seen = new HashSet<>();
        Map<Integer, Integer> groupSizes = new HashMap<>();

        for (Long studentId : studentIds) {
            if (!seen.add(studentId)) {
                throw new BusinessException("Se detectó un estudiante duplicado en la asignación manual: " + studentId);
            }

            Integer targetGroup = assignments.get(studentId);
            if (targetGroup == null || targetGroup <= 0 || targetGroup > groupsCount) {
                throw new BusinessException("Asignación inválida para el estudiante: " + studentId);
            }

            byNumber.get(targetGroup).getMemberStudentIds().add(studentId);
            groupSizes.merge(targetGroup, 1, Integer::sum);
        }

        for (int groupNumber = 1; groupNumber <= groupsCount; groupNumber++) {
            if (groupSizes.getOrDefault(groupNumber, 0) == 0) {
                throw new BusinessException("No se permiten grupos vacíos. El grupo " + groupNumber + " no tiene integrantes.");
            }
        }

        return groups;
    }

    private List<ExamGroup> createGroups(int groupsCount) {
        List<ExamGroup> groups = new ArrayList<>();
        for (int i = 1; i <= groupsCount; i++) {
            groups.add(new ExamGroup(null, "Grupo " + i));
        }
        return groups;
    }

    private void ensureNoEmptyGroups(List<ExamGroup> groups) {
        boolean hasEmpty = groups.stream().anyMatch(g -> g.getMemberStudentIds().isEmpty());
        if (hasEmpty) {
            throw new BusinessException("No se permiten grupos vacíos. Revise la configuración de generación.");
        }
    }
}
