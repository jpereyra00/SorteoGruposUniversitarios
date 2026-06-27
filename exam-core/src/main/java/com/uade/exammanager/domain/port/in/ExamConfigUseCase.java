package com.uade.exammanager.domain.port.in;

import com.uade.exammanager.application.view.ExamConfigView;

import java.time.LocalDate;

/**
 * Puerto de entrada para la configuración del examen.
 */
public interface ExamConfigUseCase {

    ExamConfigView getConfig();

    /**
     * Guarda la configuración del examen.
     *
     * @param headerImageContent  contenido de la imagen de cabecera (puede ser null)
     * @param headerImageFilename nombre original de la imagen (puede ser null)
     */
    ExamConfigView saveConfig(String subjectName,
                              String teachers,
                              LocalDate examDate,
                              Integer pageCount,
                              byte[] headerImageContent,
                              String headerImageFilename);

    void updateTopicSettings(int topicsPerGroup, boolean allowRepetition);
}
