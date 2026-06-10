package com.uade.exammanager.service;

public interface PdfService {
    byte[] generateGroupExamPdf(Long groupId);
}
