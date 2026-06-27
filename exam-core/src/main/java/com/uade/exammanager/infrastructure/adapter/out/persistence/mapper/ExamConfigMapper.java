package com.uade.exammanager.infrastructure.adapter.out.persistence.mapper;

import com.uade.exammanager.domain.model.Exam;
import com.uade.exammanager.entity.ExamConfig;

/**
 * Mapeo entre la entidad JPA singleton {@link ExamConfig} y el agregado de dominio {@link Exam}.
 */
public final class ExamConfigMapper {

    private ExamConfigMapper() {
    }

    public static Exam toDomain(ExamConfig entity) {
        if (entity == null) {
            return null;
        }
        Exam exam = new Exam(
                entity.getId(),
                entity.getSubjectName(),
                entity.getTeachers(),
                entity.getExamDate(),
                entity.getPageCount() == null ? 1 : entity.getPageCount(),
                entity.isAllowTopicRepetition(),
                entity.getTopicsPerGroup() == null ? 1 : entity.getTopicsPerGroup(),
                entity.getHeaderImagePath()
        );
        return exam;
    }

    /** Vuelca los campos del dominio sobre una entidad existente (preserva el id singleton). */
    public static void applyToEntity(Exam exam, ExamConfig entity) {
        entity.setSubjectName(exam.getSubjectName());
        entity.setTeachers(exam.getTeachers());
        entity.setExamDate(exam.getExamDate());
        entity.setPageCount(exam.getPageCount());
        entity.setAllowTopicRepetition(exam.isAllowTopicRepetition());
        entity.setTopicsPerGroup(exam.getTopicsPerGroup());
        entity.setHeaderImagePath(exam.getHeaderImagePath());
    }
}
