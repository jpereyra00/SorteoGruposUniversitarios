package com.uade.exammanager.controller;

import com.uade.exammanager.entity.Topic;
import com.uade.exammanager.service.ExamConfigService;
import com.uade.exammanager.service.TopicService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;
    private final ExamConfigService examConfigService;

    public TopicController(TopicService topicService, ExamConfigService examConfigService) {
        this.topicService = topicService;
        this.examConfigService = examConfigService;
    }

    @GetMapping
    public List<TopicResponse> findAll() {
        return topicService.findAll().stream()
                .map(t -> new TopicResponse(t.getId(), t.getName()))
                .toList();
    }

    @PostMapping
    public MessageResponse add(@jakarta.validation.Valid @RequestBody AddTopicRequest request) {
        Topic topic = topicService.addTopic(request.topicName());
        return new MessageResponse("Tema agregado correctamente.", new TopicResponse(topic.getId(), topic.getName()));
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return new MessageResponse("Tema eliminado.", null);
    }

    @PostMapping("/assign")
    public MessageResponse assign(@jakarta.validation.Valid @RequestBody AssignTopicRequest request) {
        examConfigService.updateTopicSettings(request.topicsPerGroup(), request.allowRepetition());
        topicService.assignTopicsToGroups(request.topicsPerGroup(), request.allowRepetition());
        return new MessageResponse("Temas asignados a los grupos.", null);
    }

    public record AddTopicRequest(@NotBlank String topicName) {
    }

    public record AssignTopicRequest(@Min(1) int topicsPerGroup, boolean allowRepetition) {
    }

    public record TopicResponse(Long id, String name) {
    }

    public record MessageResponse(String message, Object data) {
    }
}
