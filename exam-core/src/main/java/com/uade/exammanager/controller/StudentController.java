package com.uade.exammanager.controller;

import com.uade.exammanager.entity.Student;
import com.uade.exammanager.service.StudentService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Validated
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> findAll() {
        return studentService.findAll().stream()
                .map(StudentController::toResponse)
                .toList();
    }

    @PostMapping
    public MessageResponse add(@jakarta.validation.Valid @RequestBody StudentRequest request) {
        Student created = studentService.create(request.fullName(), request.legajo());
        return new MessageResponse("Estudiante agregado correctamente.", toResponse(created));
    }

    @PutMapping("/{id}")
    public MessageResponse update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody StudentRequest request) {
        Student updated = studentService.update(id, request.fullName(), request.legajo());
        return new MessageResponse("Estudiante actualizado correctamente.", toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        studentService.delete(id);
        return new MessageResponse("Estudiante eliminado.", null);
    }

    @PostMapping("/import-text")
    public ImportResponse importText(@jakarta.validation.Valid @RequestBody ImportTextRequest request) {
        int count = studentService.importFromText(request.bulkText());
        return new ImportResponse("Importación desde texto completada.", count);
    }

    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResponse importCsv(@RequestPart("csvFile") MultipartFile csvFile) {
        int count = studentService.importFromCsv(csvFile);
        return new ImportResponse("Importación desde CSV completada.", count);
    }

    private static StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getFullName(), student.getLegajo());
    }

    public record StudentRequest(@NotBlank String fullName, @NotBlank String legajo) {
    }

    public record ImportTextRequest(@NotBlank String bulkText) {
    }

    public record StudentResponse(Long id, String fullName, String legajo) {
    }

    public record MessageResponse(String message, Object data) {
    }

    public record ImportResponse(String message, int importedCount) {
    }
}
