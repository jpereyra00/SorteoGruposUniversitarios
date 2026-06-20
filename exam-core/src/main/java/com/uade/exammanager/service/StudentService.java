package com.uade.exammanager.service;

import com.uade.exammanager.entity.Student;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentService {
    List<Student> findAll();
    Student create(String fullName, String legajo);
    Student update(Long id, String fullName, String legajo);
    void delete(Long id);
    int importFromText(String text);
    int importFromCsv(MultipartFile file);
}
