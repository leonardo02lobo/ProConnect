package Proconnect.backProconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.models.WorkStation;
import Proconnect.backProconnect.service.WorkStationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/workstation")
public class WorkStationController {
    
    private final WorkStationService workStationService;

    public WorkStationController(WorkStationService workStationService){
        this.workStationService = workStationService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddWorkStation(@RequestBody @Valid WorkStation workStation){
        return ResponseEntity.status(HttpStatus.CREATED).body(workStationService.AddWorkStation(workStation));
    }

    @GetMapping
    public ResponseEntity<?> GetAllWorkStation(){
        return ResponseEntity.ok().body(workStationService.GetAllWorkStation());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> UpdateWorkStationById(@PathVariable Long Id, @RequestBody @Valid WorkStation workStation){
        return ResponseEntity.status(HttpStatus.OK).body(workStationService.UpdateWorkStationById(Id, workStation));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> DeleteWorkStation(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.UPGRADE_REQUIRED).body(workStationService.DeleteWorkStation(id));
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<?> GetWorkStationById(Long id){
        return ResponseEntity.ok().body(workStationService.GetWorkStationById(id));
    }

    @GetMapping("/search/workStation/{workStation}")
    public ResponseEntity<?> SearchByWorkStation(@PathVariable String workStation){
        return ResponseEntity.status(HttpStatus.OK).body(workStationService.SearchByWorkStation(workStation));
    }
}
