package com.uade.exammanager.infrastructure.adapter.in.rest;

import com.uade.exammanager.application.view.GroupView;
import com.uade.exammanager.domain.port.in.GroupUseCase;
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

    private final GroupUseCase groupUseCase;

    public GroupController(GroupUseCase groupUseCase) {
        this.groupUseCase = groupUseCase;
    }

    @GetMapping
    public List<GroupView> findAll() {
        return groupUseCase.findAll();
    }

    @PostMapping("/random")
    public MessageResponse random(@Valid @RequestBody RandomGroupRequest request) {
        groupUseCase.generateRandomGroups(request.groupsCount(), request.membersPerGroup());
        return new MessageResponse("Grupos aleatorios generados correctamente.");
    }

    @PostMapping("/manual")
    public MessageResponse manual(@Valid @RequestBody ManualGroupRequest request) {
        Map<Long, Integer> assignments = request.assignments().stream()
                .collect(Collectors.toMap(ManualAssignment::studentId, ManualAssignment::groupNumber, (a, b) -> a));

        groupUseCase.generateManualGroups(request.groupsCount(), assignments);
        return new MessageResponse("Asignación manual guardada correctamente.");
    }

    @PostMapping("/clear")
    public MessageResponse clear() {
        groupUseCase.clearGroups();
        return new MessageResponse("Se eliminaron los grupos generados.");
    }

    @PostMapping("/release-student/{studentId}")
    public MessageResponse releaseStudent(@PathVariable Long studentId) {
        groupUseCase.releaseStudentFromGroup(studentId);
        return new MessageResponse("Estudiante liberado del grupo correctamente.");
    }

    public record RandomGroupRequest(@Min(1) int groupsCount, @Min(1) int membersPerGroup) {
    }

    public record ManualGroupRequest(@Min(1) int groupsCount, @NotNull List<@Valid ManualAssignment> assignments) {
    }

    public record ManualAssignment(@NotNull Long studentId, @Min(1) int groupNumber) {
    }

    public record MessageResponse(String message) {
    }
}
