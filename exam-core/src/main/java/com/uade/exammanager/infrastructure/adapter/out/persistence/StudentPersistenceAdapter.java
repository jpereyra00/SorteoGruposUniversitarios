package com.uade.exammanager.infrastructure.adapter.out.persistence;

import com.uade.exammanager.domain.model.Student;
import com.uade.exammanager.domain.port.out.StudentRepositoryPort;
import com.uade.exammanager.infrastructure.adapter.out.persistence.mapper.StudentMapper;
import com.uade.exammanager.repository.StudentRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link StudentRepositoryPort} sobre Spring Data JPA.
 */
@Component
public class StudentPersistenceAdapter implements StudentRepositoryPort {

    private final StudentRepository studentRepository;

    public StudentPersistenceAdapter(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll().stream()
                .map(StudentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id).map(StudentMapper::toDomain);
    }

    @Override
    public Optional<Student> findByLegajo(String legajo) {
        return studentRepository.findByLegajo(legajo).map(StudentMapper::toDomain);
    }

    @Override
    public List<Student> findAllById(Collection<Long> ids) {
        return studentRepository.findAllById(ids).stream()
                .map(StudentMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return studentRepository.existsById(id);
    }

    @Override
    public Student save(Student student) {
        com.uade.exammanager.entity.Student entity = student.getId() != null
                ? studentRepository.findById(student.getId()).orElseGet(com.uade.exammanager.entity.Student::new)
                : new com.uade.exammanager.entity.Student();
        entity.setFullName(student.getFullName());
        entity.setLegajo(student.getLegajo());
        return StudentMapper.toDomain(studentRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }
}
