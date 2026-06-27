package com.uade.exammanager.infrastructure.adapter.out.persistence.mapper;

import com.uade.exammanager.entity.Student;

/**
 * Mapeo entre la entidad JPA {@link Student} y el modelo de dominio
 * {@link com.uade.exammanager.domain.model.Student}.
 */
public final class StudentMapper {

    private StudentMapper() {
    }

    public static com.uade.exammanager.domain.model.Student toDomain(Student entity) {
        if (entity == null) {
            return null;
        }
        return new com.uade.exammanager.domain.model.Student(entity.getId(), entity.getFullName(), entity.getLegajo());
    }

    public static Student toEntity(com.uade.exammanager.domain.model.Student domain) {
        if (domain == null) {
            return null;
        }
        return new Student(domain.getId(), domain.getFullName(), domain.getLegajo());
    }
}
