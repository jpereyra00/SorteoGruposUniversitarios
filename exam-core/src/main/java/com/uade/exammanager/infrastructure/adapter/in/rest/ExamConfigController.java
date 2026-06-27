package com.uade.exammanager.infrastructure.adapter.in.rest;

import com.uade.exammanager.application.view.ExamConfigView;
import com.uade.exammanager.domain.port.in.ExamConfigUseCase;
import com.uade.exammanager.exception.BusinessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/exam-config")
public class ExamConfigController {

    private final ExamConfigUseCase examConfigUseCase;

    public ExamConfigController(ExamConfigUseCase examConfigUseCase) {
        this.examConfigUseCase = examConfigUseCase;
    }

    @GetMapping
    public ExamConfigResponse get() {
        return toResponse(examConfigUseCase.getConfig());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageResponse save(@RequestParam String subjectName,
                                @RequestParam String teachers,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
                                @RequestParam Integer pageCount,
                                @RequestParam(required = false) MultipartFile headerImage) {
        byte[] imageContent = null;
        String imageFilename = null;
        if (headerImage != null && !headerImage.isEmpty()) {
            try {
                imageContent = headerImage.getBytes();
                imageFilename = headerImage.getOriginalFilename();
            } catch (IOException e) {
                throw new BusinessException("No se pudo leer la imagen de cabecera.");
            }
        }

        ExamConfigView config = examConfigUseCase.saveConfig(subjectName, teachers, examDate, pageCount,
                imageContent, imageFilename);
        return new MessageResponse("Configuración de examen guardada.", toResponse(config));
    }

    private ExamConfigResponse toResponse(ExamConfigView config) {
        return new ExamConfigResponse(
                config.subjectName(),
                config.teachers(),
                config.examDate(),
                config.pageCount(),
                config.allowTopicRepetition(),
                config.topicsPerGroup(),
                toUploadedPath(config.headerImagePath())
        );
    }

    private String toUploadedPath(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            return null;
        }

        try {
            return "/uploaded/" + Path.of(fullPath).getFileName();
        } catch (Exception ignored) {
            return null;
        }
    }

    public record ExamConfigResponse(String subjectName,
                                     String teachers,
                                     LocalDate examDate,
                                     Integer pageCount,
                                     boolean allowTopicRepetition,
                                     Integer topicsPerGroup,
                                     String headerImageUrl) {
    }

    public record MessageResponse(String message, Object data) {
    }
}
