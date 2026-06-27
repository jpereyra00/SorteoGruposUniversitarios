package com.uade.exammanager.application.usecase;

import com.uade.exammanager.application.view.StudentView;
import com.uade.exammanager.domain.model.Student;
import com.uade.exammanager.domain.port.in.StudentUseCase;
import com.uade.exammanager.domain.port.out.ExamGroupRepositoryPort;
import com.uade.exammanager.domain.port.out.StudentRepositoryPort;
import com.uade.exammanager.exception.BusinessException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Implementación del puerto de entrada {@link StudentUseCase}.
 * Orquesta los puertos de salida; no contiene lógica de persistencia.
 */
@Service
@Transactional
public class StudentApplicationService implements StudentUseCase {

    private final StudentRepositoryPort studentRepository;
    private final ExamGroupRepositoryPort examGroupRepository;

    public StudentApplicationService(StudentRepositoryPort studentRepository,
                                     ExamGroupRepositoryPort examGroupRepository) {
        this.studentRepository = studentRepository;
        this.examGroupRepository = examGroupRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentView> findAll() {
        return studentRepository.findAll().stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public StudentView create(String fullName, String legajo) {
        validate(fullName, legajo, null);
        Student student = new Student(null, fullName.trim(), legajo.trim());
        return toView(studentRepository.save(student));
    }

    @Override
    public StudentView update(Long id, String fullName, String legajo) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado."));
        validate(fullName, legajo, id);
        existing.setFullName(fullName.trim());
        existing.setLegajo(legajo.trim());
        return toView(studentRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new BusinessException("No existe el estudiante a eliminar.");
        }
        examGroupRepository.releaseStudent(id);
        studentRepository.deleteById(id);
    }

    @Override
    public int importFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Debe ingresar al menos una línea con formato Nombre Apellido,Legajo");
        }

        int imported = 0;
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(",");
            if (parts.length != 2) {
                throw new BusinessException("Formato inválido en línea: " + trimmed + " (debe ser Nombre Apellido,Legajo)");
            }
            upsert(parts[0], parts[1]);
            imported++;
        }
        return imported;
    }

    @Override
    public int importFromCsv(InputStream csvStream) {
        if (csvStream == null) {
            throw new BusinessException("Debe seleccionar un archivo CSV válido.");
        }

        int imported = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
            for (CSVRecord record : records) {
                if (record.size() < 2) {
                    continue;
                }
                upsert(record.get(0), record.get(1));
                imported++;
            }
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo CSV.");
        }
        return imported;
    }

    private void upsert(String fullName, String legajo) {
        validate(fullName, legajo, null);
        String normalizedLegajo = legajo.trim();
        Student student = studentRepository.findByLegajo(normalizedLegajo).orElseGet(Student::new);
        student.setFullName(fullName.trim());
        student.setLegajo(normalizedLegajo);
        studentRepository.save(student);
    }

    private void validate(String fullName, String legajo, Long currentId) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BusinessException("El nombre y apellido es obligatorio.");
        }
        if (legajo == null || legajo.trim().isEmpty()) {
            throw new BusinessException("El legajo es obligatorio.");
        }

        studentRepository.findByLegajo(legajo.trim()).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessException("Ya existe un estudiante con ese legajo.");
            }
        });
    }

    private StudentView toView(Student student) {
        return new StudentView(student.getId(), student.getFullName(), student.getLegajo());
    }
}
