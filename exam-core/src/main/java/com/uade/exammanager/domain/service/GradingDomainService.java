package com.uade.exammanager.domain.service;

import com.uade.exammanager.domain.model.StudentStatus;

/**
 * Lógica de negocio pura para el cálculo de la nota final y la asignación de estado del alumno.
 * No depende de Spring ni de JPA. Aún no se expone por endpoints (se usará en la Fase 5).
 */
public class GradingDomainService {

    /**
     * Calcula la nota final ponderada entre la nota grupal y la individual.
     *
     * @throws IllegalArgumentException si los pesos no suman 1.0
     */
    public double calculateFinalGrade(double groupGrade, double individualGrade,
                                      double groupWeight, double individualWeight) {
        if (Math.abs(groupWeight + individualWeight - 1.0) > 0.0001) {
            throw new IllegalArgumentException("Los pesos grupal e individual deben sumar 1.0");
        }
        return groupGrade * groupWeight + individualGrade * individualWeight;
    }

    /**
     * Determina el estado del alumno según la nota final y los umbrales del examen.
     * Si el alumno estuvo ausente, el estado es {@link StudentStatus#ABSENT} sin importar la nota.
     *
     * <ul>
     *   <li>FAILED:   nota &lt; riskThreshold</li>
     *   <li>AT_RISK:  riskThreshold &le; nota &lt; passingGrade</li>
     *   <li>REGULAR:  passingGrade &le; nota &lt; promotionGrade</li>
     *   <li>PROMOTED: nota &ge; promotionGrade</li>
     * </ul>
     */
    public StudentStatus determineStatus(Double finalGrade, double passingGrade,
                                         double promotionGrade, double riskThreshold, boolean absent) {
        if (absent || finalGrade == null) {
            return StudentStatus.ABSENT;
        }
        if (finalGrade < riskThreshold) {
            return StudentStatus.FAILED;
        }
        if (finalGrade < passingGrade) {
            return StudentStatus.AT_RISK;
        }
        if (finalGrade < promotionGrade) {
            return StudentStatus.REGULAR;
        }
        return StudentStatus.PROMOTED;
    }
}
