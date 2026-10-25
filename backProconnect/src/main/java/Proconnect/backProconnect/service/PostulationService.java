package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.Entity.PostulationStatus;
import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.mail.PostulationResolvedEvent;
import Proconnect.backProconnect.model.PostulationDto;
import Proconnect.backProconnect.model.PostulationRequest;
import Proconnect.backProconnect.repository.CompanyRepository;
import Proconnect.backProconnect.repository.JobRepository;
import Proconnect.backProconnect.repository.PostulationRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Postulaciones: recurso privado. Las ve el postulante, el propietario de la empresa del empleo y
 * un ADMIN; cualquier otro recibe 404 (igual que un id inexistente). Solo el propietario de la
 * empresa (o un ADMIN) las resuelve, y al resolverlas se notifica por correo al postulante.
 */
@Service
public class PostulationService {

    private final PostulationRepository postulationRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final OwnershipGuard guard;
    private final ApplicationEventPublisher events;

    public PostulationService(
            PostulationRepository postulationRepository,
            JobRepository jobRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            CompanyService companyService,
            OwnershipGuard guard,
            ApplicationEventPublisher events) {
        this.postulationRepository = postulationRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyService = companyService;
        this.guard = guard;
        this.events = events;
    }

    /** El postulante sale de la sesión: 404 si el empleo no existe, 409 si ya se postuló a él. */
    @Transactional
    public PostulationDto create(Long applicantId, PostulationRequest request) {
        if (!jobRepository.existsById(request.jobId())) {
            throw new NotFoundException("Empleo no encontrado");
        }
        if (postulationRepository.existsByUserIdAndJobId(applicantId, request.jobId())) {
            throw new ConflictException("Ya te postulaste a este empleo");
        }
        Postulation postulation =
                new Postulation(null, applicantId, request.jobId(), Instant.now());
        return toDto(postulationRepository.save(postulation));
    }

    /** ADMIN: todas. Resto: las propias más las recibidas en empleos de sus empresas. */
    @Transactional(readOnly = true)
    public List<PostulationDto> listVisible() {
        if (guard.isAdmin()) {
            return postulationRepository.findAll().stream().map(PostulationService::toDto).toList();
        }
        Long me = guard.currentUserId();
        Map<Long, Postulation> visible = new LinkedHashMap<>();
        postulationRepository.findByUserId(me).forEach(p -> visible.put(p.getId(), p));
        List<Long> companyIds =
                companyRepository.findByUserId(me).stream().map(Company::getId).toList();
        if (!companyIds.isEmpty()) {
            List<Long> jobIds =
                    jobRepository.findByCompanyIdIn(companyIds).stream().map(Job::getId).toList();
            if (!jobIds.isEmpty()) {
                postulationRepository.findByJobIdIn(jobIds).forEach(p -> visible.put(p.getId(), p));
            }
        }
        List<Postulation> result = new ArrayList<>(visible.values());
        result.sort(Comparator.comparing(Postulation::getId));
        return result.stream().map(PostulationService::toDto).toList();
    }

    /**
     * Postulaciones de un empleo: solo el propietario de la empresa o un ADMIN (403; 404 si no
     * existe).
     */
    @Transactional(readOnly = true)
    public List<PostulationDto> listByJob(Long jobId) {
        Job job = jobId == null ? null : jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new NotFoundException("Empleo no encontrado");
        }
        companyService.assertCanManage(job.getCompanyId());
        return postulationRepository.findByJobId(jobId).stream()
                .map(PostulationService::toDto)
                .toList();
    }

    /** 404 si no existe o si el actor no es postulante, propietario de la empresa ni ADMIN. */
    @Transactional(readOnly = true)
    public PostulationDto get(Long id) {
        return toDto(findVisibleOr404(id));
    }

    /**
     * Solo el postulante (o ADMIN) cambia el empleo de su postulación pendiente; el postulante no
     * cambia.
     */
    @Transactional
    public PostulationDto update(Long id, PostulationRequest request) {
        Postulation existing = findVisibleOr404(id);
        guard.assertParticipantOrNotFound(existing.getUserId());
        if (existing.getStatus() != PostulationStatus.PENDING) {
            throw new ConflictException("La postulación ya fue resuelta");
        }
        if (!Objects.equals(request.jobId(), existing.getJobId())) {
            if (!jobRepository.existsById(request.jobId())) {
                throw new NotFoundException("Empleo no encontrado");
            }
            existing.setJobId(request.jobId());
        }
        return toDto(postulationRepository.save(existing));
    }

    /**
     * La retira el postulante o la descarta el propietario de la empresa (o ADMIN); el resto, 404.
     */
    @Transactional
    public void delete(Long id) {
        postulationRepository.delete(findVisibleOr404(id));
    }

    @Transactional
    public PostulationDto accept(Long id) {
        return resolve(id, PostulationStatus.ACCEPTED);
    }

    @Transactional
    public PostulationDto reject(Long id) {
        return resolve(id, PostulationStatus.REJECTED);
    }

    /**
     * Resolución: quien no puede ver la postulación recibe 404 (privacidad, igual que
     * verify-web-security); quien la ve pero no es propietario de la empresa ni ADMIN (p. ej. el
     * propio postulante) recibe 403; una ya resuelta, 409. El correo se publica como evento y sale
     * tras el commit con los datos de la BD (nunca del body).
     */
    private PostulationDto resolve(Long id, PostulationStatus status) {
        Postulation postulation = findVisibleOr404(id);
        Job job =
                jobRepository
                        .findById(postulation.getJobId())
                        .orElseThrow(() -> new NotFoundException("Empleo no encontrado"));
        Company company = companyService.findOr404(job.getCompanyId());
        guard.assertOwnerOrAdmin(company.getUserId());
        if (postulation.getStatus() != PostulationStatus.PENDING) {
            throw new ConflictException("La postulación ya fue resuelta");
        }
        postulation.setStatus(status);
        Postulation saved = postulationRepository.save(postulation);
        User applicant =
                userRepository
                        .findById(saved.getUserId())
                        .orElseThrow(() -> new NotFoundException("Postulante no encontrado"));
        events.publishEvent(
                new PostulationResolvedEvent(
                        saved.getId(),
                        status,
                        applicant.getEmail(),
                        applicant.getName(),
                        job.getTitle(),
                        company.getName()));
        return toDto(saved);
    }

    private Postulation findVisibleOr404(Long id) {
        Postulation postulation =
                (id == null || id <= 0) ? null : postulationRepository.findById(id).orElse(null);
        if (postulation == null) {
            throw new NotFoundException();
        }
        guard.assertParticipantOrNotFound(
                postulation.getUserId(), companyOwnerOf(postulation.getJobId()));
        return postulation;
    }

    private Long companyOwnerOf(Long jobId) {
        if (jobId == null) {
            return null;
        }
        return jobRepository
                .findById(jobId)
                .flatMap(job -> companyRepository.findById(job.getCompanyId()))
                .map(Company::getUserId)
                .orElse(null);
    }

    private static PostulationDto toDto(Postulation p) {
        return new PostulationDto(
                p.getId(), p.getUserId(), p.getJobId(), p.getCreatedAt(), p.getStatus());
    }
}
