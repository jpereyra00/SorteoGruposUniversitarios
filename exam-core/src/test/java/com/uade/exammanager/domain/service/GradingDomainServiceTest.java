package com.uade.exammanager.domain.service;

import com.uade.exammanager.domain.model.StudentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradingDomainServiceTest {

    private final GradingDomainService service = new GradingDomainService();

    @Test
    void calculateFinalGrade_weightsApplied() {
        // 0.30 * 8 + 0.70 * 10 = 2.4 + 7.0 = 9.4
        double result = service.calculateFinalGrade(8.0, 10.0, 0.30, 0.70);
        assertEquals(9.4, result, 0.0001);
    }

    @Test
    void calculateFinalGrade_fullIndividualWeight() {
        double result = service.calculateFinalGrade(0.0, 7.0, 0.0, 1.0);
        assertEquals(7.0, result, 0.0001);
    }

    @Test
    void calculateFinalGrade_invalidWeights_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateFinalGrade(8.0, 9.0, 0.5, 0.6));
    }

    @Test
    void determineStatus_absent_overridesGrade() {
        assertEquals(StudentStatus.ABSENT,
                service.determineStatus(9.0, 4.0, 7.0, 4.0, true));
    }

    @Test
    void determineStatus_nullGrade_isAbsent() {
        assertEquals(StudentStatus.ABSENT,
                service.determineStatus(null, 4.0, 7.0, 4.0, false));
    }

    @Test
    void determineStatus_belowRisk_isFailed() {
        assertEquals(StudentStatus.FAILED,
                service.determineStatus(3.9, 4.0, 7.0, 4.0, false));
    }

    @Test
    void determineStatus_atRiskBoundary_isAtRisk() {
        // riskThreshold (4.0) <= nota < passingGrade (4.0)? Con passing=4 no hay rango AT_RISK.
        // Usamos passing=6 para verificar el rango.
        assertEquals(StudentStatus.AT_RISK,
                service.determineStatus(4.0, 6.0, 8.0, 4.0, false));
    }

    @Test
    void determineStatus_regularRange() {
        assertEquals(StudentStatus.REGULAR,
                service.determineStatus(6.0, 6.0, 8.0, 4.0, false));
    }

    @Test
    void determineStatus_promotedBoundary() {
        assertEquals(StudentStatus.PROMOTED,
                service.determineStatus(8.0, 6.0, 8.0, 4.0, false));
    }
}
