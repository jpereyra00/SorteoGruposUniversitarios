package com.uade.exammanager.domain.port.out;

import com.uade.exammanager.domain.model.Student;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de estudiantes.
 * El dominio depende de esta abstracción, no de JPA.
 */
public interface StudentRepositoryPort {

    List<Student> findAll();

    Optional<Student> findById(Long id);

    Optional<Student> findByLegajo(String legajo);

    List<Student> findAllById(Collection<Long> ids);

    boolean existsById(Long id);

    Student save(Student student);

    void deleteById(Long id);
}
