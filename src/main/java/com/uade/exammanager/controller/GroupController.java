package com.uade.exammanager.controller;

import com.uade.exammanager.entity.ExamGroup;
import com.uade.exammanager.entity.GroupMember;
import com.uade.exammanager.entity.GroupTopicAssignment;
import com.uade.exammanager.service.GroupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<GroupResponse> findAll() {
        return groupService.findAll().stream()
                .map(GroupController::toResponse)
                .toList();
    }

    @PostMapping("/random")
    public MessageResponse random(@Valid @RequestBody RandomGroupRequest request) {
        groupService.generateRandomGroups(request.groupsCount(), request.membersPerGroup());
        return new MessageResponse("Grupos aleatorios generados correctamente.");
    }

    @PostMapping("/manual")
    public MessageResponse manual(@Valid @RequestBody ManualGroupRequest request) {
        Map<Long, Integer> assignments = request.assignments().stream()
                .collect(Collectors.toMap(ManualAssignment::studentId, ManualAssignment::groupNumber, (a, b) -> a));

        groupService.generateManualGroups(request.groupsCount(), assignments);
        return new MessageResponse("Asignación manual guardada correctamente.");
    }

    @PostMapping("/clear")
    public MessageResponse clear() {
        groupService.clearGroups();
        return new MessageResponse("Se eliminaron los grupos generados.");
    }

    @PostMapping("/release-student/{studentId}")
    public MessageResponse releaseStudent(@PathVariable Long studentId) {
        groupService.releaseStudentFromGroup(studentId);
        return new MessageResponse("Estudiante liberado del grupo correctamente.");
    }

    private static GroupResponse toResponse(ExamGroup group) {
        List<GroupMemberResponse> members = group.getMembers().stream()
                .map(GroupController::toMemberResponse)
                .toList();

        List<GroupTopicResponse> topics = group.getTopicAssignments().stream()
                .map(GroupController::toTopicResponse)
                .toList();

        return new GroupResponse(group.getId(), group.getName(), members, topics);
    }

    private static GroupMemberResponse toMemberResponse(GroupMember member) {
        return new GroupMemberResponse(
                member.getStudent().getId(),
                member.getStudent().getFullName(),
                member.getStudent().getLegajo()
        );
    }

    private static GroupTopicResponse toTopicResponse(GroupTopicAssignment assignment) {
        return new GroupTopicResponse(assignment.getTopic().getId(), assignment.getTopic().getName());
    }

    public record RandomGroupRequest(@Min(1) int groupsCount, @Min(1) int membersPerGroup) {
    }

    public record ManualGroupRequest(@Min(1) int groupsCount, @NotNull List<@Valid ManualAssignment> assignments) {
    }

    public record ManualAssignment(@NotNull Long studentId, @Min(1) int groupNumber) {
    }

    public record GroupResponse(Long id, String name, List<GroupMemberResponse> members, List<GroupTopicResponse> topics) {
    }

    public record GroupMemberResponse(Long studentId, String fullName, String legajo) {
    }

    public record GroupTopicResponse(Long topicId, String topicName) {
    }

    public record MessageResponse(String message) {
    }
}
