package com.uade.exammanager.domain.port.in;

/**
 * Puerto de entrada para la generación de documentos de examen.
 */
public interface GenerateExamDocumentUseCase {

    byte[] generateGroupExamPdf(Long groupId);
}
