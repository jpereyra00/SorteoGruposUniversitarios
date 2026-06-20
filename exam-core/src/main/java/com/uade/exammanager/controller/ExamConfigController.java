package com.uade.exammanager.controller;

import com.uade.exammanager.entity.ExamConfig;
import com.uade.exammanager.service.ExamConfigService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/exam-config")
public class ExamConfigController {

    private final ExamConfigService examConfigService;

    public ExamConfigController(ExamConfigService examConfigService) {
        this.examConfigService = examConfigService;
    }

    @GetMapping
    public ExamConfigResponse get() {
        return toResponse(examConfigService.getConfig());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageResponse save(@RequestParam String subjectName,
                                @RequestParam String teachers,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
                                @RequestParam Integer pageCount,
                                @RequestParam(required = false) MultipartFile headerImage) {
        ExamConfig config = examConfigService.saveConfig(subjectName, teachers, examDate, pageCount, headerImage);
        return new MessageResponse("Configuración de examen guardada.", toResponse(config));
    }

    private ExamConfigResponse toResponse(ExamConfig config) {
        return new ExamConfigResponse(
                config.getSubjectName(),
                config.getTeachers(),
                config.getExamDate(),
                config.getPageCount(),
                config.isAllowTopicRepetition(),
                config.getTopicsPerGroup(),
                toUploadedPath(config.getHeaderImagePath())
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
