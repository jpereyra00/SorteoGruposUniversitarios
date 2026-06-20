package com.uade.exammanager.service;

import com.uade.exammanager.entity.ExamGroup;

import java.util.List;
import java.util.Map;

public interface GroupService {
    List<ExamGroup> findAll();

    void generateRandomGroups(int groupsCount, int membersPerGroup);

    void generateManualGroups(int groupsCount, Map<Long, Integer> assignments);

    void clearGroups();

    void releaseStudentFromGroup(Long studentId);
}
