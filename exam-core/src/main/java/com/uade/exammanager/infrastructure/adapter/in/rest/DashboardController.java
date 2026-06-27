package com.uade.exammanager.infrastructure.adapter.in.rest;

import com.uade.exammanager.application.view.ExamConfigView;
import com.uade.exammanager.application.view.GroupView;
import com.uade.exammanager.application.view.StudentView;
import com.uade.exammanager.application.view.TopicView;
import com.uade.exammanager.domain.port.in.ExamConfigUseCase;
import com.uade.exammanager.domain.port.in.GroupUseCase;
import com.uade.exammanager.domain.port.in.StudentUseCase;
import com.uade.exammanager.domain.port.in.TopicUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StudentUseCase studentUseCase;
    private final GroupUseCase groupUseCase;
    private final TopicUseCase topicUseCase;
    private final ExamConfigUseCase examConfigUseCase;

    public DashboardController(StudentUseCase studentUseCase,
                              GroupUseCase groupUseCase,
                              TopicUseCase topicUseCase,
                              ExamConfigUseCase examConfigUseCase) {
        this.studentUseCase = studentUseCase;
        this.groupUseCase = groupUseCase;
        this.topicUseCase = topicUseCase;
        this.examConfigUseCase = examConfigUseCase;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        List<StudentView> students = studentUseCase.findAll();
        List<GroupView> groups = groupUseCase.findAll();
        List<TopicView> topics = topicUseCase.findAll();
        ExamConfigView config = examConfigUseCase.getConfig();

        return new DashboardResponse(
                students,
                groups,
                topics,
                new ConfigResponse(
                        config.subjectName(),
                        config.teachers(),
                        config.examDate(),
                        config.pageCount(),
                        config.topicsPerGroup(),
                        config.allowTopicRepetition(),
                        toUploadedPath(config.headerImagePath())
                )
        );
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

    public record DashboardResponse(List<StudentView> students,
                                    List<GroupView> groups,
                                    List<TopicView> topics,
                                    ConfigResponse config) {
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
