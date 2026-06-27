package com.uade.exammanager.domain.port.out;

import com.uade.exammanager.application.view.GroupExamDocument;

/**
 * Puerto de salida para la generación de documentos de examen.
 * La implementación concreta (OpenPDF, Word, microservicio) es un adaptador.
 */
public interface DocumentGeneratorPort {

    byte[] generateGroupExam(GroupExamDocument data);
}
