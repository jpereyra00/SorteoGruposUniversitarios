package com.uade.exammanager.infrastructure.adapter.out.persistence;

import com.uade.exammanager.domain.model.Exam;
import com.uade.exammanager.domain.port.out.ExamConfigRepositoryPort;
import com.uade.exammanager.entity.ExamConfig;
import com.uade.exammanager.infrastructure.adapter.out.persistence.mapper.ExamConfigMapper;
import com.uade.exammanager.repository.ExamConfigRepository;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida para la configuración del examen (singleton id=1).
 */
@Component
public class ExamConfigPersistenceAdapter implements ExamConfigRepositoryPort {

    private static final Long SINGLETON_ID = 1L;

    private final ExamConfigRepository examConfigRepository;

    public ExamConfigPersistenceAdapter(ExamConfigRepository examConfigRepository) {
        this.examConfigRepository = examConfigRepository;
    }

    @Override
    public Exam load() {
        ExamConfig entity = examConfigRepository.findById(SINGLETON_ID).orElseGet(this::createDefault);
        return ExamConfigMapper.toDomain(entity);
    }

    @Override
    public Exam save(Exam exam) {
        ExamConfig entity = examConfigRepository.findById(SINGLETON_ID).orElseGet(this::newSingleton);
        ExamConfigMapper.applyToEntity(exam, entity);
        return ExamConfigMapper.toDomain(examConfigRepository.save(entity));
    }

    private ExamConfig createDefault() {
        ExamConfig config = newSingleton();
        config.setSubjectName("Materia");
        config.setTeachers("Docentes");
        config.setPageCount(1);
        config.setTopicsPerGroup(1);
        config.setAllowTopicRepetition(false);
        return examConfigRepository.save(config);
    }

    private ExamConfig newSingleton() {
        ExamConfig config = new ExamConfig();
        config.setId(SINGLETON_ID);
        return config;
    }
}
