package Proconnect.backProconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.JobDto;
import Proconnect.backProconnect.service.JobService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/job")
public class JobController {
    
    private final JobService jobService;

    public JobController(JobService jobService){
        this.jobService = jobService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddJob(@RequestBody @Valid JobDto job){
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.AddJob(job));
    }

    @DeleteMapping("/delete/{Id}")
    public ResponseEntity<?> DeleteJobById(@PathVariable Long Id){
        return ResponseEntity.ok().body(jobService.DeleteJobById(Id));
    }

    @GetMapping
    public ResponseEntity<?> getAllJob(){
        return ResponseEntity.ok().body(jobService.getAllJob());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(@RequestParam String keyword){
        return ResponseEntity.ok().body(jobService.findByKeyWord(keyword));
    }
}
