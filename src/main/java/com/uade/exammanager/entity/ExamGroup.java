package com.uade.exammanager.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "exam_groups")
public class ExamGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "examGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "examGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupTopicAssignment> topicAssignments = new ArrayList<>();

    public ExamGroup() {
    }

    public ExamGroup(Long id, String name, List<GroupMember> members, List<GroupTopicAssignment> topicAssignments) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.topicAssignments = topicAssignments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }

    public List<GroupTopicAssignment> getTopicAssignments() {
        return topicAssignments;
    }

    public void setTopicAssignments(List<GroupTopicAssignment> topicAssignments) {
        this.topicAssignments = topicAssignments;
    }

    @Override
    public String toString() {
        return "ExamGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", membersCount=" + (members == null ? 0 : members.size()) +
                ", topicAssignmentsCount=" + (topicAssignments == null ? 0 : topicAssignments.size()) +
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
        ExamGroup examGroup = (ExamGroup) o;
        return id != null && Objects.equals(id, examGroup.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
