package com.uade.exammanager.service.impl;

import com.uade.exammanager.entity.Student;
import com.uade.exammanager.exception.BusinessException;
import com.uade.exammanager.repository.GroupMemberRepository;
import com.uade.exammanager.repository.StudentRepository;
import com.uade.exammanager.service.StudentService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final GroupMemberRepository groupMemberRepository;

    public StudentServiceImpl(StudentRepository studentRepository, GroupMemberRepository groupMemberRepository) {
        this.studentRepository = studentRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public Student create(String fullName, String legajo) {
        validateStudent(fullName, legajo, null);
        Student student = new Student();
        student.setFullName(fullName.trim());
        student.setLegajo(legajo.trim());
        return studentRepository.save(student);
    }

    @Override
    public Student update(Long id, String fullName, String legajo) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Estudiante no encontrado."));
        validateStudent(fullName, legajo, id);
        existing.setFullName(fullName.trim());
        existing.setLegajo(legajo.trim());
        return studentRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new BusinessException("No existe el estudiante a eliminar.");
        }
        groupMemberRepository.deleteByStudentId(id);
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
            if (upsertStudent(parts[0], parts[1])) {
                imported++;
            }
        }
        return imported;
    }

    @Override
    public int importFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Debe seleccionar un archivo CSV válido.");
        }

        int imported = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
            for (CSVRecord record : records) {
                if (record.size() < 2) {
                    continue;
                }
                if (upsertStudent(record.get(0), record.get(1))) {
                    imported++;
                }
            }
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo CSV.");
        }
        return imported;
    }

    private boolean upsertStudent(String fullName, String legajo) {
        validateStudent(fullName, legajo, null);

        String normalizedLegajo = legajo.trim();
        Student student = studentRepository.findByLegajo(normalizedLegajo).orElseGet(Student::new);
        student.setFullName(fullName.trim());
        student.setLegajo(normalizedLegajo);
        studentRepository.save(student);
        return true;
    }

    private void validateStudent(String fullName, String legajo, Long currentId) {
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
}
