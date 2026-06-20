package com.uade.exammanager.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_student", columnNames = {"exam_group_id", "student_id"}),
        @UniqueConstraint(name = "uk_student_once", columnNames = {"student_id"})
})
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_group_id", nullable = false)
    private ExamGroup examGroup;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public GroupMember() {
    }

    public GroupMember(Long id, ExamGroup examGroup, Student student) {
        this.id = id;
        this.examGroup = examGroup;
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamGroup getExamGroup() {
        return examGroup;
    }

    public void setExamGroup(ExamGroup examGroup) {
        this.examGroup = examGroup;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "GroupMember{" +
                "id=" + id +
                ", examGroupId=" + (examGroup != null ? examGroup.getId() : null) +
                ", studentId=" + (student != null ? student.getId() : null) +
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
        GroupMember that = (GroupMember) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
