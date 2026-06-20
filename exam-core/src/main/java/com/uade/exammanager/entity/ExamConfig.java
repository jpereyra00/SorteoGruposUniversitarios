package com.uade.exammanager.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "exam_config")
public class ExamConfig {

    @Id
    private Long id;

    @Column(nullable = false)
    private String subjectName = "";

    @Column(nullable = false)
    private String teachers = "";

    private LocalDate examDate;

    @Column(nullable = false)
    private Integer pageCount = 1;

    @Column(nullable = false)
    private boolean allowTopicRepetition = false;

    @Column(nullable = false)
    private Integer topicsPerGroup = 1;

    private String headerImagePath;

    public ExamConfig() {
    }

    public ExamConfig(Long id, String subjectName, String teachers, LocalDate examDate, Integer pageCount,
                      boolean allowTopicRepetition, Integer topicsPerGroup, String headerImagePath) {
        this.id = id;
        this.subjectName = subjectName;
        this.teachers = teachers;
        this.examDate = examDate;
        this.pageCount = pageCount;
        this.allowTopicRepetition = allowTopicRepetition;
        this.topicsPerGroup = topicsPerGroup;
        this.headerImagePath = headerImagePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTeachers() {
        return teachers;
    }

    public void setTeachers(String teachers) {
        this.teachers = teachers;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public boolean isAllowTopicRepetition() {
        return allowTopicRepetition;
    }

    public void setAllowTopicRepetition(boolean allowTopicRepetition) {
        this.allowTopicRepetition = allowTopicRepetition;
    }

    public Integer getTopicsPerGroup() {
        return topicsPerGroup;
    }

    public void setTopicsPerGroup(Integer topicsPerGroup) {
        this.topicsPerGroup = topicsPerGroup;
    }

    public String getHeaderImagePath() {
        return headerImagePath;
    }

    public void setHeaderImagePath(String headerImagePath) {
        this.headerImagePath = headerImagePath;
    }

    @Override
    public String toString() {
        return "ExamConfig{" +
                "id=" + id +
                ", subjectName='" + subjectName + '\'' +
                ", teachers='" + teachers + '\'' +
                ", examDate=" + examDate +
                ", pageCount=" + pageCount +
                ", allowTopicRepetition=" + allowTopicRepetition +
                ", topicsPerGroup=" + topicsPerGroup +
                ", headerImagePath='" + headerImagePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExamConfig that = (ExamConfig) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
