package com.uade.exammanager.application.usecase;

import com.uade.exammanager.application.view.ExamConfigView;
import com.uade.exammanager.domain.model.Exam;
import com.uade.exammanager.domain.port.in.ExamConfigUseCase;
import com.uade.exammanager.domain.port.out.ExamConfigRepositoryPort;
import com.uade.exammanager.domain.port.out.HeaderImageStoragePort;
import com.uade.exammanager.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implementación del puerto de entrada {@link ExamConfigUseCase}.
 */
@Service
@Transactional
public class ExamConfigApplicationService implements ExamConfigUseCase {

    private final ExamConfigRepositoryPort examConfigRepository;
    private final HeaderImageStoragePort headerImageStorage;

    public ExamConfigApplicationService(ExamConfigRepositoryPort examConfigRepository,
                                        HeaderImageStoragePort headerImageStorage) {
        this.examConfigRepository = examConfigRepository;
        this.headerImageStorage = headerImageStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public ExamConfigView getConfig() {
        return toView(examConfigRepository.load());
    }

    @Override
    public ExamConfigView saveConfig(String subjectName,
                                     String teachers,
                                     LocalDate examDate,
                                     Integer pageCount,
                                     byte[] headerImageContent,
                                     String headerImageFilename) {
        if (subjectName == null || subjectName.trim().isEmpty()) {
            throw new BusinessException("El nombre de la materia es obligatorio.");
        }
        if (teachers == null || teachers.trim().isEmpty()) {
            throw new BusinessException("Debe indicar al menos un docente.");
        }
        if (pageCount == null || pageCount <= 0) {
            throw new BusinessException("La cantidad de hojas debe ser mayor a 0.");
        }

        Exam exam = examConfigRepository.load();
        exam.setSubjectName(subjectName.trim());
        exam.setTeachers(teachers.trim());
        exam.setExamDate(examDate);
        exam.setPageCount(pageCount);

        if (headerImageContent != null && headerImageContent.length > 0) {
            exam.setHeaderImagePath(headerImageStorage.store(headerImageContent, headerImageFilename));
        }

        return toView(examConfigRepository.save(exam));
    }

    @Override
    public void updateTopicSettings(int topicsPerGroup, boolean allowRepetition) {
        if (topicsPerGroup <= 0) {
            throw new BusinessException("La cantidad de temas por grupo debe ser mayor a 0.");
        }
        Exam exam = examConfigRepository.load();
        exam.setTopicsPerGroup(topicsPerGroup);
        exam.setAllowTopicRepetition(allowRepetition);
        examConfigRepository.save(exam);
    }

    private ExamConfigView toView(Exam exam) {
        return new ExamConfigView(
                exam.getSubjectName(),
                exam.getTeachers(),
                exam.getExamDate(),
                exam.getPageCount(),
                exam.isAllowTopicRepetition(),
                exam.getTopicsPerGroup(),
                exam.getHeaderImagePath()
        );
    }
}
