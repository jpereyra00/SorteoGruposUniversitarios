package com.uade.exammanager.service.impl;

import com.uade.exammanager.entity.ExamGroup;
import com.uade.exammanager.entity.GroupMember;
import com.uade.exammanager.entity.Student;
import com.uade.exammanager.exception.BusinessException;
import com.uade.exammanager.repository.ExamGroupRepository;
import com.uade.exammanager.repository.GroupMemberRepository;
import com.uade.exammanager.repository.GroupTopicAssignmentRepository;
import com.uade.exammanager.repository.StudentRepository;
import com.uade.exammanager.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    private final ExamGroupRepository examGroupRepository;
    private final StudentRepository studentRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupTopicAssignmentRepository groupTopicAssignmentRepository;

    public GroupServiceImpl(ExamGroupRepository examGroupRepository,
                            StudentRepository studentRepository,
                            GroupMemberRepository groupMemberRepository,
                            GroupTopicAssignmentRepository groupTopicAssignmentRepository) {
        this.examGroupRepository = examGroupRepository;
        this.studentRepository = studentRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupTopicAssignmentRepository = groupTopicAssignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamGroup> findAll() {
        return examGroupRepository.findAll();
    }

    @Override
    public void generateRandomGroups(int groupsCount, int membersPerGroup) {
        validateRandomGenerationParams(groupsCount, membersPerGroup);

        List<Student> students = studentRepository.findAll();
        validateStudentsForRandomGeneration(students, groupsCount, membersPerGroup);

        resetGroupsForGeneration();
        validateStudentsAvailability(students.stream().map(Student::getId).toList());

        List<ExamGroup> groups = createGroups(groupsCount);
        List<Student> shuffledStudents = new ArrayList<>(students);
        Collections.shuffle(shuffledStudents, new Random());

        int totalStudents = shuffledStudents.size();
        int baseSize = totalStudents / groupsCount;
        int groupsWithExtraMember = totalStudents % groupsCount;

        int studentIndex = 0;
        for (int i = 0; i < groups.size(); i++) {
            ExamGroup group = groups.get(i);
            int groupSize = baseSize + (i < groupsWithExtraMember ? 1 : 0);

            for (int j = 0; j < groupSize; j++) {
                addStudentToGroup(group, shuffledStudents.get(studentIndex++));
            }
        }

        ensureNoEmptyGroups(groups);
        examGroupRepository.saveAll(groups);
    }

    @Override
    public void generateManualGroups(int groupsCount, Map<Long, Integer> assignments) {
        if (groupsCount <= 0) {
            throw new BusinessException("La cantidad de grupos debe ser mayor a 0.");
        }

        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            throw new BusinessException("Debe cargar estudiantes antes de generar grupos manuales.");
        }
        if (students.size() < groupsCount) {
            throw new BusinessException("No se pueden generar grupos vacíos: debe haber al menos un estudiante por grupo.");
        }
        if (assignments == null || assignments.size() != students.size()) {
            throw new BusinessException("Debe asignar un grupo a todos los estudiantes.");
        }

        resetGroupsForGeneration();

        List<Long> studentIds = students.stream().map(Student::getId).toList();
        validateStudentsAvailability(studentIds);

        List<ExamGroup> groups = createGroups(groupsCount);
        Map<Integer, ExamGroup> byIndex = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            byIndex.put(i + 1, groups.get(i));
        }

        Set<Long> seenStudents = new HashSet<>();
        Map<Integer, Integer> groupSizes = new HashMap<>();

        for (Student student : students) {
            Long studentId = student.getId();
            if (!seenStudents.add(studentId)) {
                throw new BusinessException("Se detectó un estudiante duplicado en la asignación manual: " + student.getFullName());
            }

            Integer targetGroup = assignments.get(studentId);
            if (targetGroup == null || targetGroup <= 0 || targetGroup > groupsCount) {
                throw new BusinessException("Asignación inválida para el estudiante: " + student.getFullName());
            }

            ExamGroup selectedGroup = byIndex.get(targetGroup);
            addStudentToGroup(selectedGroup, student);
            groupSizes.merge(targetGroup, 1, Integer::sum);
        }

        for (int groupNumber = 1; groupNumber <= groupsCount; groupNumber++) {
            if (groupSizes.getOrDefault(groupNumber, 0) == 0) {
                throw new BusinessException("No se permiten grupos vacíos. El grupo " + groupNumber + " no tiene integrantes.");
            }
        }

        examGroupRepository.saveAll(groups);
    }

    @Override
    public void clearGroups() {
        resetGroupsForGeneration();
    }

    @Override
    public void releaseStudentFromGroup(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("Debe indicar un estudiante válido para liberar.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado."));

        if (!groupMemberRepository.existsByStudentId(studentId)) {
            throw new BusinessException("El estudiante " + student.getFullName() + " no está asignado a ningún grupo.");
        }

        groupMemberRepository.deleteByStudentId(studentId);
        groupMemberRepository.flush();
    }

    private void validateRandomGenerationParams(int groupsCount, int membersPerGroup) {
        if (groupsCount <= 0) {
            throw new BusinessException("La cantidad de grupos debe ser mayor a 0.");
        }
        if (membersPerGroup <= 0) {
            throw new BusinessException("La cantidad de integrantes por grupo debe ser mayor a 0.");
        }
    }

    private void validateStudentsForRandomGeneration(List<Student> students, int groupsCount, int membersPerGroup) {
        if (students.isEmpty()) {
            throw new BusinessException("No hay estudiantes cargados para generar grupos.");
        }

        int totalStudents = students.size();
        if (groupsCount > totalStudents) {
            throw new BusinessException("No podés crear " + groupsCount + " grupos con solo " + totalStudents
                    + " estudiantes. Sugerencia: usá entre 1 y " + totalStudents + " grupos.");
        }

        if (membersPerGroup > totalStudents) {
            throw new BusinessException("La sugerencia de integrantes por grupo (" + membersPerGroup
                    + ") no puede superar el total de estudiantes cargados (" + totalStudents + ").");
        }
    }

    private void resetGroupsForGeneration() {
        groupTopicAssignmentRepository.deleteAllInBatch();
        groupMemberRepository.deleteAllInBatch();
        examGroupRepository.deleteAllInBatch();
        examGroupRepository.flush();
        groupMemberRepository.flush();
    }

    private void validateStudentsAvailability(Collection<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BusinessException("No se recibieron estudiantes para asignar a grupos.");
        }

        Set<Long> assignedIds = groupMemberRepository.findAssignedStudentIds(studentIds);
        if (!assignedIds.isEmpty()) {
            List<Student> busyStudents = studentRepository.findAllById(assignedIds);
            String names = busyStudents.stream()
                    .map(s -> s.getFullName() + " (" + s.getLegajo() + ")")
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new BusinessException("Hay estudiantes ya asignados a grupos: " + names + ". Libérelos o limpie grupos antes de continuar.");
        }
    }

    private void addStudentToGroup(ExamGroup group, Student student) {
        GroupMember member = new GroupMember();
        member.setExamGroup(group);
        member.setStudent(student);
        group.getMembers().add(member);
    }

    private void ensureNoEmptyGroups(List<ExamGroup> groups) {
        Optional<ExamGroup> emptyGroup = groups.stream()
                .filter(g -> g.getMembers() == null || g.getMembers().isEmpty())
                .findFirst();

        if (emptyGroup.isPresent()) {
            throw new BusinessException("No se permiten grupos vacíos. Revise la configuración de generación.");
        }
    }

    private List<ExamGroup> createGroups(int groupsCount) {
        List<ExamGroup> groups = new ArrayList<>();
        for (int i = 1; i <= groupsCount; i++) {
            ExamGroup group = new ExamGroup();
            group.setName("Grupo " + i);
            groups.add(group);
        }
        return groups;
    }
}
