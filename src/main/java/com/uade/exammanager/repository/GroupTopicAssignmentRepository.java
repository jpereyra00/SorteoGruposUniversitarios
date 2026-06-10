package com.uade.exammanager.repository;

import com.uade.exammanager.entity.GroupTopicAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupTopicAssignmentRepository extends JpaRepository<GroupTopicAssignment, Long> {
    void deleteByExamGroupId(Long examGroupId);
}
