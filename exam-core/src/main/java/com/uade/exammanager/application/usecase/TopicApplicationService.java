package com.uade.exammanager.application.usecase;

import com.uade.exammanager.application.view.TopicView;
import com.uade.exammanager.domain.model.Exam;
import com.uade.exammanager.domain.model.ExamGroup;
import com.uade.exammanager.domain.model.Topic;
import com.uade.exammanager.domain.port.in.TopicUseCase;
import com.uade.exammanager.domain.port.out.ExamConfigRepositoryPort;
import com.uade.exammanager.domain.port.out.ExamGroupRepositoryPort;
import com.uade.exammanager.domain.port.out.TopicRepositoryPort;
import com.uade.exammanager.domain.service.TopicAssignmentDomainService;
import com.uade.exammanager.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del puerto de entrada {@link TopicUseCase}.
 */
@Service
@Transactional
public class TopicApplicationService implements TopicUseCase {

    private final TopicRepositoryPort topicRepository;
    private final ExamGroupRepositoryPort examGroupRepository;
    private final ExamConfigRepositoryPort examConfigRepository;
    private final TopicAssignmentDomainService topicAssignmentDomainService;

    public TopicApplicationService(TopicRepositoryPort topicRepository,
                                   ExamGroupRepositoryPort examGroupRepository,
                                   ExamConfigRepositoryPort examConfigRepository,
                                   TopicAssignmentDomainService topicAssignmentDomainService) {
        this.topicRepository = topicRepository;
        this.examGroupRepository = examGroupRepository;
        this.examConfigRepository = examConfigRepository;
        this.topicAssignmentDomainService = topicAssignmentDomainService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicView> findAll() {
        return topicRepository.findAll().stream()
                .map(t -> new TopicView(t.getId(), t.getName()))
                .toList();
    }

    @Override
    public TopicView addTopic(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("El tema no puede estar vacío.");
        }
        Topic topic = topicRepository.save(new Topic(null, name.trim()));
        return new TopicView(topic.getId(), topic.getName());
    }

    @Override
    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }

    @Override
    public void assignTopicsToGroups(int topicsPerGroup, boolean allowRepetition) {
        List<ExamGroup> groups = examGroupRepository.findAll();
        List<Long> topicIds = topicRepository.findAll().stream()
                .map(Topic::getId)
                .toList();

        // Valida y sortea los temas (lógica de dominio pura).
        topicAssignmentDomainService.assign(groups, topicIds, topicsPerGroup, allowRepetition);

        // Persiste las asignaciones y la configuración del examen.
        examGroupRepository.replaceTopicAssignments(groups);
        persistTopicSettings(topicsPerGroup, allowRepetition);
    }

    private void persistTopicSettings(int topicsPerGroup, boolean allowRepetition) {
        Exam exam = examConfigRepository.load();
        exam.setTopicsPerGroup(topicsPerGroup);
        exam.setAllowTopicRepetition(allowRepetition);
        examConfigRepository.save(exam);
    }
}
