package com.uade.exammanager.repository;

import com.uade.exammanager.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    void deleteByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    @Query("select gm.student.id from GroupMember gm where gm.student.id in :studentIds")
    Set<Long> findAssignedStudentIds(@Param("studentIds") Collection<Long> studentIds);
}
