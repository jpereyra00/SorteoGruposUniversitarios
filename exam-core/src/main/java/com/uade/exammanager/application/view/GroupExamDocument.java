package com.uade.exammanager.application.view;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos necesarios para renderizar el documento de examen de un grupo.
 * Es el contrato de entrada de {@link com.uade.exammanager.domain.port.out.DocumentGeneratorPort},
 * desacoplado de las entidades JPA.
 */
public record GroupExamDocument(String groupName,
                                List<Member> members,
                                List<String> topics,
                                String subjectName,
                                String teachers,
                                LocalDate examDate,
                                Integer pageCount) {

    public record Member(String fullName, String legajo) {
    }
}
