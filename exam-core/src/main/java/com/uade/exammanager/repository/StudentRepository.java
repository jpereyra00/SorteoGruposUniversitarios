package com.uade.exammanager.repository;

import com.uade.exammanager.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByLegajo(String legajo);
}
