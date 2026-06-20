package com.uade.exammanager.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public class Exam {

    private Long id;
    private String subjectName;
    private String teachers;
    private LocalDate examDate;
    private int pageCount = 1;
    private boolean allowTopicRepetition = false;
    private int topicsPerGroup = 1;
    private String headerImagePath;
    private ExamStatus status = ExamStatus.DRAFT;
    private ExamType type;

    // Pesos para nota final (groupWeight + individualWeight == 1.0)
    private double groupWeight = 0.30;
    private double individualWeight = 0.70;

    // Umbrales de calificación
    private double passingGrade = 4.0;
    private double promotionGrade = 7.0;
    private double riskThreshold = 4.0;

    // Referencia a CourseEdition (Fase 2)
    private Long courseEditionId;

    public Exam() {}

    public Exam(Long id, String subjectName, String teachers, LocalDate examDate,
                int pageCount, boolean allowTopicRepetition, int topicsPerGroup,
                String headerImagePath) {
        this.id = id;
        this.subjectName = subjectName;
        this.teachers = teachers;
        this.examDate = examDate;
        this.pageCount = pageCount;
        this.allowTopicRepetition = allowTopicRepetition;
        this.topicsPerGroup = topicsPerGroup;
        this.headerImagePath = headerImagePath;
    }

    public boolean isWeightsValid() {
        return Math.abs(groupWeight + individualWeight - 1.0) < 0.0001;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeachers() { return teachers; }
    public void setTeachers(String teachers) { this.teachers = teachers; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public boolean isAllowTopicRepetition() { return allowTopicRepetition; }
    public void setAllowTopicRepetition(boolean allowTopicRepetition) { this.allowTopicRepetition = allowTopicRepetition; }

    public int getTopicsPerGroup() { return topicsPerGroup; }
    public void setTopicsPerGroup(int topicsPerGroup) { this.topicsPerGroup = topicsPerGroup; }

    public String getHeaderImagePath() { return headerImagePath; }
    public void setHeaderImagePath(String headerImagePath) { this.headerImagePath = headerImagePath; }

    public ExamStatus getStatus() { return status; }
    public void setStatus(ExamStatus status) { this.status = status; }

    public ExamType getType() { return type; }
    public void setType(ExamType type) { this.type = type; }

    public double getGroupWeight() { return groupWeight; }
    public void setGroupWeight(double groupWeight) { this.groupWeight = groupWeight; }

    public double getIndividualWeight() { return individualWeight; }
    public void setIndividualWeight(double individualWeight) { this.individualWeight = individualWeight; }

    public double getPassingGrade() { return passingGrade; }
    public void setPassingGrade(double passingGrade) { this.passingGrade = passingGrade; }

    public double getPromotionGrade() { return promotionGrade; }
    public void setPromotionGrade(double promotionGrade) { this.promotionGrade = promotionGrade; }

    public double getRiskThreshold() { return riskThreshold; }
    public void setRiskThreshold(double riskThreshold) { this.riskThreshold = riskThreshold; }

    public Long getCourseEditionId() { return courseEditionId; }
    public void setCourseEditionId(Long courseEditionId) { this.courseEditionId = courseEditionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam e = (Exam) o;
        return id != null && Objects.equals(id, e.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Exam{id=" + id + ", subjectName='" + subjectName + "', status=" + status + ", type=" + type + "}";
    }
}
