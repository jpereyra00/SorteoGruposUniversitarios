package com.uade.exammanager.domain.port.out;

import com.uade.exammanager.domain.model.ExamGroup;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida para la persistencia de grupos de examen y sus relaciones
 * (integrantes y asignaciones de temas).
 */
public interface ExamGroupRepositoryPort {

    List<ExamGroup> findAll();

    Optional<ExamGroup> findById(Long id);

    /** Elimina todas las asignaciones de temas, integrantes y grupos. */
    void clearGroups();

    /** Persiste grupos nuevos (con sus integrantes) luego de limpiar. */
    void saveNewGroups(List<ExamGroup> groups);

    /** Reemplaza las asignaciones de temas de los grupos indicados, preservando integrantes. */
    void replaceTopicAssignments(List<ExamGroup> groups);

    Set<Long> findAssignedStudentIds(Collection<Long> studentIds);

    boolean isStudentAssigned(Long studentId);

    void releaseStudent(Long studentId);
}
