package com.uade.exammanager.application.usecase;

import com.uade.exammanager.application.view.GroupMemberView;
import com.uade.exammanager.application.view.GroupTopicView;
import com.uade.exammanager.application.view.GroupView;
import com.uade.exammanager.domain.model.ExamGroup;
import com.uade.exammanager.domain.model.Student;
import com.uade.exammanager.domain.model.Topic;
import com.uade.exammanager.domain.port.in.GroupUseCase;
import com.uade.exammanager.domain.port.out.ExamGroupRepositoryPort;
import com.uade.exammanager.domain.port.out.StudentRepositoryPort;
import com.uade.exammanager.domain.port.out.TopicRepositoryPort;
import com.uade.exammanager.domain.service.GroupingDomainService;
import com.uade.exammanager.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementación del puerto de entrada {@link GroupUseCase}.
 * Carga datos vía puertos de salida, delega la distribución al servicio de dominio
 * {@link GroupingDomainService} y persiste el resultado.
 */
@Service
@Transactional
public class GroupApplicationService implements GroupUseCase {

    private final StudentRepositoryPort studentRepository;
    private final TopicRepositoryPort topicRepository;
    private final ExamGroupRepositoryPort examGroupRepository;
    private final GroupingDomainService groupingDomainService;

    public GroupApplicationService(StudentRepositoryPort studentRepository,
                                   TopicRepositoryPort topicRepository,
                                   ExamGroupRepositoryPort examGroupRepository,
                                   GroupingDomainService groupingDomainService) {
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
        this.examGroupRepository = examGroupRepository;
        this.groupingDomainService = groupingDomainService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupView> findAll() {
        List<ExamGroup> groups = examGroupRepository.findAll();

        Map<Long, Student> studentsById = studentRepository.findAll().stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Map<Long, Topic> topicsById = topicRepository.findAll().stream()
                .collect(Collectors.toMap(Topic::getId, Function.identity()));

        return groups.stream()
                .map(group -> toView(group, studentsById, topicsById))
                .toList();
    }

    @Override
    public void generateRandomGroups(int groupsCount, int membersPerGroup) {
        List<Long> studentIds = studentRepository.findAll().stream()
                .map(Student::getId)
                .toList();

        List<ExamGroup> groups = groupingDomainService.generateRandom(studentIds, groupsCount, membersPerGroup);
        examGroupRepository.clearGroups();
        examGroupRepository.saveNewGroups(groups);
    }

    @Override
    public void generateManualGroups(int groupsCount, Map<Long, Integer> assignments) {
        List<Long> studentIds = studentRepository.findAll().stream()
                .map(Student::getId)
                .toList();

        List<ExamGroup> groups = groupingDomainService.generateManual(studentIds, groupsCount, assignments);
        examGroupRepository.clearGroups();
        examGroupRepository.saveNewGroups(groups);
    }

    @Override
    public void clearGroups() {
        examGroupRepository.clearGroups();
    }

    @Override
    public void releaseStudentFromGroup(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("Debe indicar un estudiante válido para liberar.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado."));

        if (!examGroupRepository.isStudentAssigned(studentId)) {
            throw new BusinessException("El estudiante " + student.getFullName() + " no está asignado a ningún grupo.");
        }

        examGroupRepository.releaseStudent(studentId);
    }

    private GroupView toView(ExamGroup group, Map<Long, Student> studentsById, Map<Long, Topic> topicsById) {
        List<GroupMemberView> members = group.getMemberStudentIds().stream()
                .map(studentsById::get)
                .filter(s -> s != null)
                .map(s -> new GroupMemberView(s.getId(), s.getFullName(), s.getLegajo()))
                .toList();

        List<GroupTopicView> topics = group.getTopicIds().stream()
                .map(topicsById::get)
                .filter(t -> t != null)
                .map(t -> new GroupTopicView(t.getId(), t.getName()))
                .toList();

        return new GroupView(group.getId(), group.getName(), members, topics);
    }
}
