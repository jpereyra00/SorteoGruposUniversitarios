package com.uade.exammanager.infrastructure.adapter.out.persistence;

import com.uade.exammanager.domain.port.out.ExamGroupRepositoryPort;
import com.uade.exammanager.entity.ExamGroup;
import com.uade.exammanager.entity.GroupMember;
import com.uade.exammanager.entity.GroupTopicAssignment;
import com.uade.exammanager.entity.Student;
import com.uade.exammanager.entity.Topic;
import com.uade.exammanager.repository.ExamGroupRepository;
import com.uade.exammanager.repository.GroupMemberRepository;
import com.uade.exammanager.repository.GroupTopicAssignmentRepository;
import com.uade.exammanager.repository.StudentRepository;
import com.uade.exammanager.repository.TopicRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador de salida para grupos de examen, integrantes y asignaciones de temas.
 * Mapea el modelo de dominio {@link com.uade.exammanager.domain.model.ExamGroup}
 * (que usa IDs) a las entidades JPA y sus relaciones.
 */
@Component
public class ExamGroupPersistenceAdapter implements ExamGroupRepositoryPort {

    private final ExamGroupRepository examGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupTopicAssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;

    public ExamGroupPersistenceAdapter(ExamGroupRepository examGroupRepository,
                                       GroupMemberRepository groupMemberRepository,
                                       GroupTopicAssignmentRepository assignmentRepository,
                                       StudentRepository studentRepository,
                                       TopicRepository topicRepository) {
        this.examGroupRepository = examGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    public List<com.uade.exammanager.domain.model.ExamGroup> findAll() {
        return examGroupRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<com.uade.exammanager.domain.model.ExamGroup> findById(Long id) {
        return examGroupRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void clearGroups() {
        assignmentRepository.deleteAllInBatch();
        groupMemberRepository.deleteAllInBatch();
        examGroupRepository.deleteAllInBatch();
        examGroupRepository.flush();
        groupMemberRepository.flush();
    }

    @Override
    public void saveNewGroups(List<com.uade.exammanager.domain.model.ExamGroup> groups) {
        List<ExamGroup> entities = groups.stream()
                .map(this::toNewEntity)
                .toList();
        examGroupRepository.saveAll(entities);
    }

    @Override
    public void replaceTopicAssignments(List<com.uade.exammanager.domain.model.ExamGroup> groups) {
        for (com.uade.exammanager.domain.model.ExamGroup group : groups) {
            assignmentRepository.deleteByExamGroupId(group.getId());
            ExamGroup groupRef = examGroupRepository.getReferenceById(group.getId());
            for (Long topicId : group.getTopicIds()) {
                Topic topicRef = topicRepository.getReferenceById(topicId);
                GroupTopicAssignment assignment = new GroupTopicAssignment();
                assignment.setExamGroup(groupRef);
                assignment.setTopic(topicRef);
                assignmentRepository.save(assignment);
            }
        }
    }

    @Override
    public Set<Long> findAssignedStudentIds(Collection<Long> studentIds) {
        return groupMemberRepository.findAssignedStudentIds(studentIds);
    }

    @Override
    public boolean isStudentAssigned(Long studentId) {
        return groupMemberRepository.existsByStudentId(studentId);
    }

    @Override
    public void releaseStudent(Long studentId) {
        groupMemberRepository.deleteByStudentId(studentId);
        groupMemberRepository.flush();
    }

    private com.uade.exammanager.domain.model.ExamGroup toDomain(ExamGroup entity) {
        com.uade.exammanager.domain.model.ExamGroup domain =
                new com.uade.exammanager.domain.model.ExamGroup(entity.getId(), entity.getName());
        entity.getMembers().forEach(m -> domain.getMemberStudentIds().add(m.getStudent().getId()));
        entity.getTopicAssignments().forEach(a -> domain.getTopicIds().add(a.getTopic().getId()));
        return domain;
    }

    private ExamGroup toNewEntity(com.uade.exammanager.domain.model.ExamGroup domain) {
        ExamGroup entity = new ExamGroup();
        entity.setName(domain.getName());
        for (Long studentId : domain.getMemberStudentIds()) {
            Student studentRef = studentRepository.getReferenceById(studentId);
            GroupMember member = new GroupMember();
            member.setExamGroup(entity);
            member.setStudent(studentRef);
            entity.getMembers().add(member);
        }
        return entity;
    }
}
