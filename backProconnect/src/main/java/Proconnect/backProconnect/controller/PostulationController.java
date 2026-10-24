package Proconnect.backProconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.model.PostulationDto;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.PostulationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/postulation")
public class PostulationController {
    
    private final PostulationService postulationService;

    public PostulationController(PostulationService postulationService){
        this.postulationService = postulationService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> AddPostulation(@RequestBody @Valid PostulationDto postulationDto){
        postulationDto.setUserId(CurrentUser.id());
        return ResponseEntity.ok().body(postulationService.AddPostulation(postulationDto));
    }

    @GetMapping
    public ResponseEntity<?> GetAllPostulations(){
        return ResponseEntity.ok().body(postulationService.GetAllPostulations());
    }

    /** Postulaciones recibidas en un empleo (solo el propietario de la empresa o ADMIN). */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> GetPostulationsByJob(@PathVariable Long jobId){
        return ResponseEntity.ok().body(postulationService.GetPostulationsByJob(jobId));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> GetPostulationById(@PathVariable Long id){
        Postulation postulation = postulationService.GetPostulationById(id);
        return ResponseEntity.ok().body(postulation);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> UpdatePostulationById(@PathVariable Long id, @RequestBody @Valid PostulationDto postulationDto){
        postulationDto.setUserId(CurrentUser.id());
        boolean isUpdate = postulationService.UpdatePostulationById(id, postulationDto);

        if(isUpdate){
            return ResponseEntity.ok().body("Postulation update successufly");
        }else{
            return ResponseEntity.badRequest().body("Does not update");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> DeleteProstulationById(@PathVariable Long id){
        postulationService.DeleteProstulationById(id);
        return ResponseEntity.noContent().build();
    }
}
