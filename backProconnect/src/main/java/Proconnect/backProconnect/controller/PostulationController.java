package Proconnect.backProconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import Proconnect.backProconnect.service.PostulationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/postulation")
@CrossOrigin(origins = "*")
public class PostulationController {
    
    private final PostulationService postulationService;

    public PostulationController(PostulationService postulationService){
        this.postulationService = postulationService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> AddPostulation(@RequestBody @Valid PostulationDto postulationDto){
        return ResponseEntity.ok().body(postulationService.AddPostulation(postulationDto));
    }

    @GetMapping
    public ResponseEntity<?> GetAllPostulations(){
        return ResponseEntity.ok().body(postulationService.GetAllPostulations());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> GetPostulationById(@PathVariable Long id){
        Postulation postulation = postulationService.GetPostulationById(id);

        if(postulation == null){
            return ResponseEntity.badRequest().body("Does not exist Postulations with this Id");
        }
        return ResponseEntity.ok().body(postulation);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> UpdatePostulationById(@PathVariable Long id, @RequestBody @Valid PostulationDto postulationDto){
        boolean isUpdate = postulationService.UpdatePostulationById(id, postulationDto);

        if(isUpdate){
            return ResponseEntity.ok().body("Postulation update successufly");
        }else{
            return ResponseEntity.badRequest().body("Does not update");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> DeleteProstulationById(@PathVariable Long id){
        boolean isDelete = postulationService.DeleteProstulationById(id);

        if(isDelete){
            return ResponseEntity.ok().body("Postulation delete successufly");
        }else{
            return ResponseEntity.badRequest().body("Does not delete");
        }
    }
}
