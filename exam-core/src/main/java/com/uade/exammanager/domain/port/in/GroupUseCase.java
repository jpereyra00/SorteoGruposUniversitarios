package com.uade.exammanager.domain.port.in;

import com.uade.exammanager.application.view.GroupView;

import java.util.List;
import java.util.Map;

/**
 * Puerto de entrada para la formación y gestión de grupos de examen.
 */
public interface GroupUseCase {

    List<GroupView> findAll();

    void generateRandomGroups(int groupsCount, int membersPerGroup);

    void generateManualGroups(int groupsCount, Map<Long, Integer> assignments);

    void clearGroups();

    void releaseStudentFromGroup(Long studentId);
}
