package com.uade.exammanager.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Grade {

    private Long id;
    private Long studentId;
    private Long examId;
    private Long examGroupId;
    private Double groupGrade;
    private Double individualGrade;
    private Double finalGrade;
    private StudentStatus status;
    private String observations;
    private LocalDateTime gradedAt;

    public Grade() {}

    public Grade(Long id, Long studentId, Long examId, Long examGroupId,
                 Double groupGrade, Double individualGrade, Double finalGrade,
                 StudentStatus status, String observations, LocalDateTime gradedAt) {
        this.id = id;
        this.studentId = studentId;
        this.examId = examId;
        this.examGroupId = examGroupId;
        this.groupGrade = groupGrade;
        this.individualGrade = individualGrade;
        this.finalGrade = finalGrade;
        this.status = status;
        this.observations = observations;
        this.gradedAt = gradedAt;
    }

    public boolean isGraded() {
        return finalGrade != null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Long getExamGroupId() { return examGroupId; }
    public void setExamGroupId(Long examGroupId) { this.examGroupId = examGroupId; }

    public Double getGroupGrade() { return groupGrade; }
    public void setGroupGrade(Double groupGrade) { this.groupGrade = groupGrade; }

    public Double getIndividualGrade() { return individualGrade; }
    public void setIndividualGrade(Double individualGrade) { this.individualGrade = individualGrade; }

    public Double getFinalGrade() { return finalGrade; }
    public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }

    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade g = (Grade) o;
        return id != null && Objects.equals(id, g.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Grade{id=" + id + ", studentId=" + studentId + ", finalGrade=" + finalGrade + ", status=" + status + "}";
    }
}
