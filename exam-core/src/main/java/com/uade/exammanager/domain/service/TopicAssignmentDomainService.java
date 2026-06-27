package com.uade.exammanager.domain.service;

import com.uade.exammanager.domain.model.ExamGroup;
import com.uade.exammanager.exception.BusinessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lógica de negocio pura para el sorteo de temas entre grupos.
 */
public class TopicAssignmentDomainService {

    private final Random random;

    public TopicAssignmentDomainService() {
        this(new Random());
    }

    /** Constructor para tests deterministas. */
    public TopicAssignmentDomainService(Random random) {
        this.random = random;
    }

    /**
     * Asigna {@code topicsPerGroup} temas a cada grupo, mutando su lista de {@code topicIds}.
     * Si {@code allowRepetition} es false, no se repiten temas entre grupos.
     */
    public void assign(List<ExamGroup> groups, List<Long> topicIds, int topicsPerGroup, boolean allowRepetition) {
        if (topicsPerGroup <= 0) {
            throw new BusinessException("La cantidad de temas por grupo debe ser mayor a 0.");
        }
        if (groups == null || groups.isEmpty()) {
            throw new BusinessException("Debe generar grupos antes de asignar temas.");
        }
        if (topicIds == null || topicIds.isEmpty()) {
            throw new BusinessException("Debe cargar temas antes de asignarlos.");
        }
        if (!allowRepetition && topicIds.size() < groups.size() * topicsPerGroup) {
            throw new BusinessException("No hay suficientes temas para asignar sin repetición.");
        }

        List<Long> globalPool = new ArrayList<>(topicIds);
        Collections.shuffle(globalPool, random);

        int globalPointer = 0;
        for (ExamGroup group : groups) {
            group.getTopicIds().clear();

            List<Long> groupPool = new ArrayList<>(topicIds);
            Collections.shuffle(groupPool, random);

            for (int i = 0; i < topicsPerGroup; i++) {
                Long selected = allowRepetition
                        ? groupPool.get(i % groupPool.size())
                        : globalPool.get(globalPointer++);
                group.getTopicIds().add(selected);
            }
        }
    }
}
