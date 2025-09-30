package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.models.WorkStation;
import Proconnect.backProconnect.repository.WorkStationRepository;
import Proconnect.backProconnect.specification.WorkStationSpecification;

@Service
public class WorkStationService {
    private final WorkStationRepository workStationRepository;

    public WorkStationService(WorkStationRepository workStationRepository) {
        this.workStationRepository = workStationRepository;
    }

    public String AddWorkStation(WorkStation workStation) {
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

    public List<WorkStation> GetAllWorkStation() {
        return workStationRepository.findAll();
    }

    public String UpdateWorkStationById(Long Id, WorkStation newWorkStation) {
        try {
            if (Long.valueOf(Id) == null) {
                return "Id cannot be null";
            }
            if (newWorkStation == null) {
                return "new WorkStation cannot be null";
            }

            WorkStation existWorkStation = workStationRepository.findById(Id).orElse(null);

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

    public String DeleteWorkStation(Long Id) {
        try {
            if (Long.valueOf(Id) == null) {
                return "Id cannot be null";
            }
            if (!workStationRepository.existsById(Id)) {
                return "WorkStation with Id " + Id + " not exist";
            }
            workStationRepository.deleteById(Id);
            return "WorkStation Delete successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public WorkStation GetWorkStationById(Long Id){
        try {
            if(Long.valueOf(Id) == null){
                return null;
            }
            Optional<WorkStation> workStation = workStationRepository.findById(Id);

            return workStation.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public List<WorkStation> SearchByWorkStation(String workStation){
        return workStationRepository.findAll(WorkStationSpecification.hasWorkStationLike(workStation));
    }
}
