package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.WorkStation;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.PageResponse;
import Proconnect.backProconnect.model.WorkStationDto;
import Proconnect.backProconnect.model.WorkStationRequest;
import Proconnect.backProconnect.repository.WorkStationRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkStationService {

    private final WorkStationRepository workStationRepository;
    private final OwnershipGuard guard;

    public WorkStationService(WorkStationRepository workStationRepository, OwnershipGuard guard) {
        this.workStationRepository = workStationRepository;
        this.guard = guard;
    }

    @Transactional
    public WorkStationDto create(Long ownerId, WorkStationRequest request) {
        return toDto(
                workStationRepository.save(
                        new WorkStation(null, ownerId, request.workStation().trim())));
    }

    @Transactional(readOnly = true)
    public PageResponse<WorkStationDto> list(Optional<String> query, Integer page, Integer size) {
        Pageable pageable = Paging.page(page, size);
        Slice<WorkStation> result =
                query.filter(q -> !q.isBlank())
                        .map(
                                q ->
                                        workStationRepository.search(
                                                UserService.likePattern(q), pageable))
                        .orElseGet(() -> workStationRepository.findAllBy(pageable));
        return PageResponse.of(result, WorkStationService::toDto);
    }

    @Transactional(readOnly = true)
    public WorkStationDto get(Long id) {
        return toDto(findOr404(id));
    }

    /**
     * Solo el propietario (o ADMIN) la modifica (403); 404 si no existe. El propietario no cambia.
     */
    @Transactional
    public WorkStationDto update(Long id, WorkStationRequest request) {
        WorkStation ws = findOr404(id);
        guard.assertOwnerOrAdmin(ws.getUserId());
        ws.setWorkStation(request.workStation().trim());
        return toDto(workStationRepository.save(ws));
    }

    /** Solo el propietario (o ADMIN) la borra (403); 404 si no existe. */
    @Transactional
    public void delete(Long id) {
        WorkStation ws = findOr404(id);
        guard.assertOwnerOrAdmin(ws.getUserId());
        workStationRepository.delete(ws);
    }

    private WorkStation findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException("Puesto no encontrado");
        }
        return workStationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Puesto no encontrado"));
    }

    private static WorkStationDto toDto(WorkStation ws) {
        return new WorkStationDto(ws.getId(), ws.getUserId(), ws.getWorkStation());
    }
}
