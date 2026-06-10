package com.uade.exammanager.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "group_topic_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_topic", columnNames = {"exam_group_id", "topic_id"})
})
public class GroupTopicAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_group_id", nullable = false)
    private ExamGroup examGroup;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    public GroupTopicAssignment() {
    }

    public GroupTopicAssignment(Long id, ExamGroup examGroup, Topic topic) {
        this.id = id;
        this.examGroup = examGroup;
        this.topic = topic;
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

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "GroupTopicAssignment{" +
                "id=" + id +
                ", examGroupId=" + (examGroup != null ? examGroup.getId() : null) +
                ", topicId=" + (topic != null ? topic.getId() : null) +
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
        GroupTopicAssignment that = (GroupTopicAssignment) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
