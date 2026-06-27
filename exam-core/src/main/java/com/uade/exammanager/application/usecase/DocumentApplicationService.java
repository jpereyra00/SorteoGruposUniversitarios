package com.uade.exammanager.application.usecase;

import com.uade.exammanager.application.view.GroupExamDocument;
import com.uade.exammanager.domain.model.Exam;
import com.uade.exammanager.domain.model.ExamGroup;
import com.uade.exammanager.domain.model.Student;
import com.uade.exammanager.domain.model.Topic;
import com.uade.exammanager.domain.port.in.GenerateExamDocumentUseCase;
import com.uade.exammanager.domain.port.out.DocumentGeneratorPort;
import com.uade.exammanager.domain.port.out.ExamConfigRepositoryPort;
import com.uade.exammanager.domain.port.out.ExamGroupRepositoryPort;
import com.uade.exammanager.domain.port.out.StudentRepositoryPort;
import com.uade.exammanager.domain.port.out.TopicRepositoryPort;
import com.uade.exammanager.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementación del puerto de entrada {@link GenerateExamDocumentUseCase}.
 * Compone los datos del grupo y delega el renderizado en {@link DocumentGeneratorPort}.
 */
@Service
@Transactional(readOnly = true)
public class DocumentApplicationService implements GenerateExamDocumentUseCase {

    private final ExamGroupRepositoryPort examGroupRepository;
    private final StudentRepositoryPort studentRepository;
    private final TopicRepositoryPort topicRepository;
    private final ExamConfigRepositoryPort examConfigRepository;
    private final DocumentGeneratorPort documentGenerator;

    public DocumentApplicationService(ExamGroupRepositoryPort examGroupRepository,
                                      StudentRepositoryPort studentRepository,
                                      TopicRepositoryPort topicRepository,
                                      ExamConfigRepositoryPort examConfigRepository,
                                      DocumentGeneratorPort documentGenerator) {
        this.examGroupRepository = examGroupRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
        this.examConfigRepository = examConfigRepository;
        this.documentGenerator = documentGenerator;
    }

    @Override
    public byte[] generateGroupExamPdf(Long groupId) {
        ExamGroup group = examGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Grupo no encontrado."));
        Exam config = examConfigRepository.load();

        Map<Long, Student> studentsById = studentRepository.findAllById(group.getMemberStudentIds()).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Map<Long, Topic> topicsById = topicRepository.findAll().stream()
                .collect(Collectors.toMap(Topic::getId, Function.identity()));

        List<GroupExamDocument.Member> members = group.getMemberStudentIds().stream()
                .map(studentsById::get)
                .filter(s -> s != null)
                .map(s -> new GroupExamDocument.Member(s.getFullName(), s.getLegajo()))
                .toList();

        List<String> topics = group.getTopicIds().stream()
                .map(topicsById::get)
                .filter(t -> t != null)
                .map(Topic::getName)
                .toList();

        GroupExamDocument document = new GroupExamDocument(
                group.getName(),
                members,
                topics,
                config.getSubjectName(),
                config.getTeachers(),
                config.getExamDate(),
                config.getPageCount()
        );

        return documentGenerator.generateGroupExam(document);
    }
}
