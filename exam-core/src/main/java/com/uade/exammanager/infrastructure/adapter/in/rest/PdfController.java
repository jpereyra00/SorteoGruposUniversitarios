package com.uade.exammanager.infrastructure.adapter.in.rest;

import com.uade.exammanager.domain.port.in.GenerateExamDocumentUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final GenerateExamDocumentUseCase generateExamDocumentUseCase;

    public PdfController(GenerateExamDocumentUseCase generateExamDocumentUseCase) {
        this.generateExamDocumentUseCase = generateExamDocumentUseCase;
    }

    @GetMapping("/group/{id}")
    public ResponseEntity<byte[]> downloadGroupPdf(@PathVariable Long id) {
        byte[] pdfBytes = generateExamDocumentUseCase.generateGroupExamPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grupo-" + id + "-examen.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
