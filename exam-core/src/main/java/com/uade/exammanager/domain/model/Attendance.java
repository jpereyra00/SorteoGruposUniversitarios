package com.uade.exammanager.domain.model;

import java.util.Objects;

public class Attendance {

    private Long id;
    private Long studentId;
    private Long examId;
    private AttendanceStatus status;

    public Attendance() {}

    public Attendance(Long id, Long studentId, Long examId, AttendanceStatus status) {
        this.id = id;
        this.studentId = studentId;
        this.examId = examId;
        this.status = status;
    }

    public boolean isAbsent() {
        return AttendanceStatus.ABSENT.equals(status);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance a = (Attendance) o;
        return id != null && Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Attendance{id=" + id + ", studentId=" + studentId + ", examId=" + examId + ", status=" + status + "}";
    }
}
