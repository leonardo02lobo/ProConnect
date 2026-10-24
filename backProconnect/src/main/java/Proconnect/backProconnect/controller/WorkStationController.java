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

import Proconnect.backProconnect.model.WorkStationDto;
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
    public ResponseEntity<?> AddWorkStation(@RequestBody @Valid WorkStationDto workStation){
        var result = workStationService.AddWorkStation(workStation);
        if (result != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se pudo crear la estación de trabajo.");
    }

    @GetMapping
    public ResponseEntity<?> GetAllWorkStation(){
        var result = workStationService.GetAllWorkStation();
        if (result != null && !result.isEmpty()) {
            return ResponseEntity.ok().body(result);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay estaciones de trabajo.");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> UpdateWorkStationById(@PathVariable Long id, @RequestBody @Valid WorkStationDto workStation){
        var result = workStationService.UpdateWorkStationById(id, workStation);
        if (result != null) {
            return ResponseEntity.ok().body(result);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Estación de trabajo no encontrada.");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> DeleteWorkStation(@PathVariable Long id){
        boolean deleted = workStationService.DeleteWorkStation(id);
        if (deleted) {
            return ResponseEntity.ok().body("Estación de trabajo eliminada correctamente.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Estación de trabajo no encontrada.");
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<?> GetWorkStationById(@PathVariable Long id){
        var result = workStationService.GetWorkStationById(id);
        if (result != null) {
            return ResponseEntity.ok().body(result);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Estación de trabajo no encontrada.");
    }

    @GetMapping("/search/workStation/{workStation}")
    public ResponseEntity<?> SearchByWorkStation(@PathVariable String workStation){
        var result = workStationService.SearchByWorkStation(workStation);
        if (result != null && !result.isEmpty()) {
            return ResponseEntity.ok().body(result);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No se encontraron estaciones de trabajo.");
    }
}
