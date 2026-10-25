package Proconnect.backProconnect.controller;

import Proconnect.backProconnect.model.WorkStationDto;
import Proconnect.backProconnect.model.WorkStationRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.WorkStationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workstations")
public class WorkStationController {

    private final WorkStationService workStationService;

    public WorkStationController(WorkStationService workStationService) {
        this.workStationService = workStationService;
    }

    @GetMapping
    public List<WorkStationDto> list(@RequestParam(name = "q", required = false) String query) {
        return workStationService.list(Optional.ofNullable(query));
    }

    @GetMapping("/{id}")
    public WorkStationDto get(@PathVariable Long id) {
        return workStationService.get(id);
    }

    @PostMapping
    public ResponseEntity<WorkStationDto> create(@RequestBody @Valid WorkStationRequest request) {
        WorkStationDto created = workStationService.create(CurrentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/workstations/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public WorkStationDto update(
            @PathVariable Long id, @RequestBody @Valid WorkStationRequest request) {
        return workStationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workStationService.delete(id);
    }
}
