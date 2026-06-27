package com.uade.exammanager.infrastructure.adapter.out.persistence.mapper;

import com.uade.exammanager.entity.Topic;

/**
 * Mapeo entre la entidad JPA {@link Topic} y el modelo de dominio
 * {@link com.uade.exammanager.domain.model.Topic}.
 *
 * <p>La entidad aún no modela la jerarquía de unidades (Fase 7), por lo que
 * {@code unitId} queda en null al mapear a dominio.</p>
 */
public final class TopicMapper {

    private TopicMapper() {
    }

    public static com.uade.exammanager.domain.model.Topic toDomain(Topic entity) {
        if (entity == null) {
            return null;
        }
        return new com.uade.exammanager.domain.model.Topic(entity.getId(), entity.getName());
    }
}
