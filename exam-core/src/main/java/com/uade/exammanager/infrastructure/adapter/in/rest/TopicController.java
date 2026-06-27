package com.uade.exammanager.infrastructure.adapter.in.rest;

import com.uade.exammanager.application.view.TopicView;
import com.uade.exammanager.domain.port.in.TopicUseCase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicUseCase topicUseCase;

    public TopicController(TopicUseCase topicUseCase) {
        this.topicUseCase = topicUseCase;
    }

    @GetMapping
    public List<TopicView> findAll() {
        return topicUseCase.findAll();
    }

    @PostMapping
    public MessageResponse add(@jakarta.validation.Valid @RequestBody AddTopicRequest request) {
        TopicView topic = topicUseCase.addTopic(request.topicName());
        return new MessageResponse("Tema agregado correctamente.", topic);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        topicUseCase.deleteTopic(id);
        return new MessageResponse("Tema eliminado.", null);
    }

    @PostMapping("/assign")
    public MessageResponse assign(@jakarta.validation.Valid @RequestBody AssignTopicRequest request) {
        topicUseCase.assignTopicsToGroups(request.topicsPerGroup(), request.allowRepetition());
        return new MessageResponse("Temas asignados a los grupos.", null);
    }

    public record AddTopicRequest(@NotBlank String topicName) {
    }

    public record AssignTopicRequest(@Min(1) int topicsPerGroup, boolean allowRepetition) {
    }

    public record MessageResponse(String message, Object data) {
    }
}
