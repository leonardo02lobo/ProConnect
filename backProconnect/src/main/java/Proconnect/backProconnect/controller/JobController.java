package Proconnect.backProconnect.controller;

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

import Proconnect.backProconnect.model.JobDto;
import Proconnect.backProconnect.model.JobRequest;
import Proconnect.backProconnect.model.PostulationDto;
import Proconnect.backProconnect.service.JobService;
import Proconnect.backProconnect.service.PostulationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final PostulationService postulationService;

    public JobController(JobService jobService, PostulationService postulationService) {
        this.jobService = jobService;
        this.postulationService = postulationService;
    }

    /** Todos los empleos, o los que contienen {@code q} en el título o la descripción. */
    @GetMapping
    public List<JobDto> list(@RequestParam(name = "q", required = false) String query) {
        return jobService.list(Optional.ofNullable(query));
    }

    @GetMapping("/{id}")
    public JobDto get(@PathVariable Long id) {
        return jobService.get(id);
    }

    @PostMapping
    public ResponseEntity<JobDto> create(@RequestBody @Valid JobRequest request) {
        JobDto created = jobService.create(request);
        return ResponseEntity.created(URI.create("/api/jobs/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobService.delete(id);
    }

    /** Postulaciones recibidas en el empleo (solo el propietario de la empresa o ADMIN). */
    @GetMapping("/{id}/postulations")
    public List<PostulationDto> postulations(@PathVariable Long id) {
        return postulationService.listByJob(id);
    }
}
