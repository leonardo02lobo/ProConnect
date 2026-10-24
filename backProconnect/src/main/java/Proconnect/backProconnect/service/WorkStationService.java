package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.WorkStation;
import Proconnect.backProconnect.model.WorkStationDto;
import Proconnect.backProconnect.repository.WorkStationRepository;
import Proconnect.backProconnect.specification.WorkStationSpecification;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class WorkStationService {
    private final WorkStationRepository workStationRepository;
    private final OwnershipGuard guard;

    public WorkStationService(WorkStationRepository workStationRepository, OwnershipGuard guard) {
        this.workStationRepository = workStationRepository;
        this.guard = guard;
    }

    public String AddWorkStation(WorkStationDto workStation) {
        try {
            if (workStation == null) {
                return "WorkStation cannot be null";
            }
            WorkStation workStationEntity = new WorkStation(null, workStation.getUserId(), workStation.getWorkStation());
            workStationRepository.save(workStationEntity);
            return "WorkStation add successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<WorkStation> GetAllWorkStation() {
        return workStationRepository.findAll();
    }

    /** Solo el propietario (o ADMIN) la modifica (403); null si no existe. El propietario no cambia. */
    public String UpdateWorkStationById(Long Id, WorkStationDto newWorkStation) {
        if (Id == null || newWorkStation == null) {
            return null;
        }
        WorkStation existWorkStation = workStationRepository.findById(Id).orElse(null);
        if (existWorkStation == null) {
            return null;
        }
        guard.assertOwnerOrAdmin(existWorkStation.getUserId());
        existWorkStation.setWorkStation(newWorkStation.getWorkStation());
        workStationRepository.save(existWorkStation);
        return "WorkSation Update successfully";
    }

    /** Solo el propietario (o ADMIN) la borra (403); false si no existe. */
    public boolean DeleteWorkStation(Long Id) {
        WorkStation existing = Id == null ? null : workStationRepository.findById(Id).orElse(null);
        if (existing == null) {
            return false;
        }
        guard.assertOwnerOrAdmin(existing.getUserId());
        workStationRepository.delete(existing);
        return true;
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
