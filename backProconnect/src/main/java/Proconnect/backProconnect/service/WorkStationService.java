package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.model.WorkStationDto;
import Proconnect.backProconnect.repository.WorkStationRepository;
import Proconnect.backProconnect.specification.WorkStationSpecification;

@Service
public class WorkStationService {
    private final WorkStationRepository workStationRepository;

    public WorkStationService(WorkStationRepository workStationRepository) {
        this.workStationRepository = workStationRepository;
    }

    public String AddWorkStation(WorkStationDto workStation) {
        try {
            if (workStation == null) {
                return "WorkStation cannot be null";
            }
            workStationRepository.save(workStation);
            return "WorkStation add successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<WorkStationDto> GetAllWorkStation() {
        return workStationRepository.findAll();
    }

    public String UpdateWorkStationById(Long Id, WorkStationDto newWorkStation) {
        try {
            if (Long.valueOf(Id) == null) {
                return "Id cannot be null";
            }
            if (newWorkStation == null) {
                return "new WorkStation cannot be null";
            }

            WorkStationDto existWorkStation = workStationRepository.findById(Id).orElse(null);

            if (existWorkStation == null) {
                return "The WorkStation with " + Id + " does not exist";
            }
            existWorkStation.setUserId(newWorkStation.getUserId());
            existWorkStation.setWorkStation(newWorkStation.getWorkStation());
            return "WorkSation Update successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public boolean DeleteWorkStation(Long Id) {
        try {
            if (Long.valueOf(Id) == null) {
                return false;
            }
            if (!workStationRepository.existsById(Id)) {
                return false;
            }
            workStationRepository.deleteById(Id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public WorkStationDto GetWorkStationById(Long Id){
        try {
            if(Long.valueOf(Id) == null){
                return null;
            }
            Optional<WorkStationDto> workStation = workStationRepository.findById(Id);

            return workStation.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public List<WorkStationDto> SearchByWorkStation(String workStation){
        return workStationRepository.findAll(WorkStationSpecification.hasWorkStationLike(workStation));
    }
}
