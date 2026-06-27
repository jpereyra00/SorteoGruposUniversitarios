package com.uade.exammanager.domain.port.out;

import com.uade.exammanager.domain.model.Exam;

/**
 * Puerto de salida para la configuración del examen (singleton actual).
 * Se modela con el agregado de dominio {@link Exam}.
 */
public interface ExamConfigRepositoryPort {

    /** Carga la configuración, creando una por defecto si no existe. */
    Exam load();

    Exam save(Exam exam);
}
