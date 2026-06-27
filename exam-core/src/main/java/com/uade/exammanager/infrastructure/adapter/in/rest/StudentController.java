package com.uade.exammanager.infrastructure.adapter.in.rest;

import com.uade.exammanager.application.view.StudentView;
import com.uade.exammanager.domain.port.in.StudentUseCase;
import com.uade.exammanager.exception.BusinessException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@Validated
public class StudentController {

    private final StudentUseCase studentUseCase;

    public StudentController(StudentUseCase studentUseCase) {
        this.studentUseCase = studentUseCase;
    }

    @GetMapping
    public List<StudentView> findAll() {
        return studentUseCase.findAll();
    }

    @PostMapping
    public MessageResponse add(@jakarta.validation.Valid @RequestBody StudentRequest request) {
        StudentView created = studentUseCase.create(request.fullName(), request.legajo());
        return new MessageResponse("Estudiante agregado correctamente.", created);
    }

    @PutMapping("/{id}")
    public MessageResponse update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody StudentRequest request) {
        StudentView updated = studentUseCase.update(id, request.fullName(), request.legajo());
        return new MessageResponse("Estudiante actualizado correctamente.", updated);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        studentUseCase.delete(id);
        return new MessageResponse("Estudiante eliminado.", null);
    }

    @PostMapping("/import-text")
    public ImportResponse importText(@jakarta.validation.Valid @RequestBody ImportTextRequest request) {
        int count = studentUseCase.importFromText(request.bulkText());
        return new ImportResponse("Importación desde texto completada.", count);
    }

    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResponse importCsv(@RequestPart("csvFile") MultipartFile csvFile) {
        if (csvFile == null || csvFile.isEmpty()) {
            throw new BusinessException("Debe seleccionar un archivo CSV válido.");
        }
        try {
            int count = studentUseCase.importFromCsv(csvFile.getInputStream());
            return new ImportResponse("Importación desde CSV completada.", count);
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo CSV.");
        }
    }

    public record StudentRequest(@NotBlank String fullName, @NotBlank String legajo) {
    }

    public record ImportTextRequest(@NotBlank String bulkText) {
    }

    public record MessageResponse(String message, Object data) {
    }

    public record ImportResponse(String message, int importedCount) {
    }
}
