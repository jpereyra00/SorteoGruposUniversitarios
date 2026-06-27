package com.uade.exammanager.domain.port.in;

import com.uade.exammanager.application.view.StudentView;

import java.io.InputStream;
import java.util.List;

/**
 * Puerto de entrada para la gestión de estudiantes.
 */
public interface StudentUseCase {

    List<StudentView> findAll();

    StudentView create(String fullName, String legajo);

    StudentView update(Long id, String fullName, String legajo);

    void delete(Long id);

    int importFromText(String text);

    int importFromCsv(InputStream csvStream);
}
