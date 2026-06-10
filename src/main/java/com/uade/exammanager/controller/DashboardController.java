package com.uade.exammanager.controller;

import com.uade.exammanager.entity.ExamConfig;
import com.uade.exammanager.entity.ExamGroup;
import com.uade.exammanager.entity.GroupMember;
import com.uade.exammanager.entity.GroupTopicAssignment;
import com.uade.exammanager.entity.Student;
import com.uade.exammanager.entity.Topic;
import com.uade.exammanager.service.ExamConfigService;
import com.uade.exammanager.service.GroupService;
import com.uade.exammanager.service.StudentService;
import com.uade.exammanager.service.TopicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StudentService studentService;
    private final GroupService groupService;
    private final TopicService topicService;
    private final ExamConfigService examConfigService;

    public DashboardController(StudentService studentService,
                               GroupService groupService,
                               TopicService topicService,
                               ExamConfigService examConfigService) {
        this.studentService = studentService;
        this.groupService = groupService;
        this.topicService = topicService;
        this.examConfigService = examConfigService;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        List<StudentResponse> students = studentService.findAll().stream()
                .map(this::toStudentResponse)
                .toList();

        List<GroupResponse> groups = groupService.findAll().stream()
                .map(this::toGroupResponse)
                .toList();

        List<TopicResponse> topics = topicService.findAll().stream()
                .map(t -> new TopicResponse(t.getId(), t.getName()))
                .toList();

        ExamConfig config = examConfigService.getConfig();

        return new DashboardResponse(
                students,
                groups,
                topics,
                new ConfigResponse(
                        config.getSubjectName(),
                        config.getTeachers(),
                        config.getExamDate(),
                        config.getPageCount(),
                        config.getTopicsPerGroup(),
                        config.isAllowTopicRepetition(),
                        toUploadedPath(config.getHeaderImagePath())
                )
        );
    }

    private StudentResponse toStudentResponse(Student student) {
        return new StudentResponse(student.getId(), student.getFullName(), student.getLegajo());
    }

    private GroupResponse toGroupResponse(ExamGroup group) {
        List<GroupMemberResponse> members = group.getMembers().stream()
                .map(this::toGroupMemberResponse)
                .toList();

        List<GroupTopicResponse> assignedTopics = group.getTopicAssignments().stream()
                .map(this::toGroupTopicResponse)
                .toList();

        return new GroupResponse(group.getId(), group.getName(), members, assignedTopics);
    }

    private GroupMemberResponse toGroupMemberResponse(GroupMember member) {
        return new GroupMemberResponse(member.getStudent().getId(), member.getStudent().getFullName(), member.getStudent().getLegajo());
    }

    private GroupTopicResponse toGroupTopicResponse(GroupTopicAssignment assignment) {
        return new GroupTopicResponse(assignment.getTopic().getId(), assignment.getTopic().getName());
    }

    private String toUploadedPath(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            return null;
        }
        try {
            return "/uploaded/" + Path.of(fullPath).getFileName();
        } catch (Exception ignored) {
            return null;
        }
    }

    public record DashboardResponse(List<StudentResponse> students,
                                    List<GroupResponse> groups,
                                    List<TopicResponse> topics,
                                    ConfigResponse config) {
    }

    public record StudentResponse(Long id, String fullName, String legajo) {
    }

    public record TopicResponse(Long id, String name) {
    }

    public record GroupMemberResponse(Long studentId, String fullName, String legajo) {
    }

    public record GroupTopicResponse(Long topicId, String topicName) {
    }

    public record GroupResponse(Long id, String name, List<GroupMemberResponse> members, List<GroupTopicResponse> topics) {
    }

    public record ConfigResponse(String subjectName,
                                 String teachers,
                                 LocalDate examDate,
                                 Integer pageCount,
                                 Integer topicsPerGroup,
                                 boolean allowTopicRepetition,
                                 String headerImageUrl) {
    }
}
