package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.model.PostulationDto;
import Proconnect.backProconnect.repository.PostulationRepository;

@Service
public class PostulationService {
    
    private final PostulationRepository postulationRepository;

    public PostulationService(PostulationRepository postulationRepository){
        this.postulationRepository = postulationRepository;
    }

    public String AddPostulation(PostulationDto postulationDto){
        try {
            if(postulationDto == null){
                return "Postulation cannot be null";
            }
            Postulation postulation = new Postulation(postulationDto.getId(), postulationDto.getUserId(), postulationDto.getJobId(), postulationDto.getDate());
            postulationRepository.save(postulation);
            return "Postulation Add successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }
    
    public List<Postulation> GetAllPostulations(){
        return postulationRepository.findAll();
    }

    public Postulation GetPostulationById(Long Id){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return null;
            }
            Optional<Postulation> postulation = postulationRepository.findById(Id);
            return postulation.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean UpdatePostulationById(Long Id,PostulationDto postulation){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return false;
            }
            if(postulation == null){
                return false;
            }
            if(!postulationRepository.existsById(Id)){
                return false;
            }   
            Postulation exitsPostulation = postulationRepository.findById(Id).get();
            exitsPostulation.setJobId(postulation.getJobId());
            exitsPostulation.setUserId(postulation.getUserId());
            exitsPostulation.setDate(postulation.getDate());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean DeleteProstulationById(Long Id){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return false;
            }
            postulationRepository.deleteById(Id);
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
