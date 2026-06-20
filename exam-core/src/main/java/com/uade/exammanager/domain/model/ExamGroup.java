package com.uade.exammanager.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExamGroup {

    private Long id;
    private String name;
    private Long examId;
    private List<Long> memberStudentIds = new ArrayList<>();
    private List<Long> topicIds = new ArrayList<>();

    public ExamGroup() {}

    public ExamGroup(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public int memberCount() {
        return memberStudentIds.size();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public List<Long> getMemberStudentIds() { return memberStudentIds; }
    public void setMemberStudentIds(List<Long> memberStudentIds) { this.memberStudentIds = memberStudentIds; }

    public List<Long> getTopicIds() { return topicIds; }
    public void setTopicIds(List<Long> topicIds) { this.topicIds = topicIds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamGroup g = (ExamGroup) o;
        return id != null && Objects.equals(id, g.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "ExamGroup{id=" + id + ", name='" + name + "', members=" + memberStudentIds.size() + "}";
    }
}
