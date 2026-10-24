package Proconnect.backProconnect.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.model.PostulationDto;
import Proconnect.backProconnect.repository.CompanyRepository;
import Proconnect.backProconnect.repository.JobRepository;
import Proconnect.backProconnect.repository.PostulationRepository;
import Proconnect.backProconnect.security.OwnershipGuard;

/**
 * Postulaciones: recurso privado. Las ve el postulante, el propietario de la empresa del empleo y un ADMIN.
 * Cualquier otro recibe 404 (igual que un id inexistente).
 */
@Service
public class PostulationService {

    private final PostulationRepository postulationRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final OwnershipGuard guard;

    public PostulationService(PostulationRepository postulationRepository, JobRepository jobRepository,
            CompanyRepository companyRepository, CompanyService companyService, OwnershipGuard guard) {
        this.postulationRepository = postulationRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.companyService = companyService;
        this.guard = guard;
    }

    /** El postulante sale del token (lo fija el controller); el empleo tiene que existir (404). */
    public String AddPostulation(PostulationDto postulationDto) {
        if (postulationDto == null) {
            return "Postulation cannot be null";
        }
        if (postulationDto.getJobId() == null || !jobRepository.existsById(postulationDto.getJobId())) {
            throw new ResourceNotFoundException();
        }
        Postulation postulation = new Postulation(null, postulationDto.getUserId(), postulationDto.getJobId(),
                postulationDto.getDate() != null ? postulationDto.getDate() : new java.util.Date());
        postulationRepository.save(postulation);
        return "Postulation Add successufly";
    }

    /** ADMIN: todas. Resto: las propias más las recibidas en empleos de sus empresas. */
    public List<Postulation> GetAllPostulations() {
        if (guard.isAdmin()) {
            return postulationRepository.findAll();
        }
        Long me = guard.currentUserId();
        Map<Long, Postulation> visible = new LinkedHashMap<>();
        postulationRepository.findByUserId(me).forEach(p -> visible.put(p.getId(), p));
        List<Long> companyIds = companyRepository.findByUserId(me).stream().map(Company::getId).toList();
        if (!companyIds.isEmpty()) {
            List<Long> jobIds = jobRepository.findByCompanyIdIn(companyIds).stream().map(Job::getId).toList();
            if (!jobIds.isEmpty()) {
                postulationRepository.findByJobIdIn(jobIds).forEach(p -> visible.put(p.getId(), p));
            }
        }
        List<Postulation> result = new ArrayList<>(visible.values());
        result.sort(Comparator.comparing(Postulation::getId));
        return result;
    }

    /** Postulaciones de un empleo: solo el propietario de la empresa o un ADMIN (403; 404 si no existe). */
    public List<Postulation> GetPostulationsByJob(Long jobId) {
        Job job = jobId == null ? null : jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new ResourceNotFoundException();
        }
        companyService.assertCanManage(job.getCompanyId());
        return postulationRepository.findByJobId(jobId);
    }

    /** 404 si no existe o si el actor no es postulante, propietario de la empresa ni ADMIN. */
    public Postulation GetPostulationById(Long Id) {
        return findVisibleOr404(Id);
    }

    /** Solo el postulante (o ADMIN) modifica su postulación; el postulante no cambia. */
    public boolean UpdatePostulationById(Long Id, PostulationDto postulation) {
        Postulation existing = findVisibleOr404(Id);
        guard.assertParticipantOrNotFound(existing.getUserId());
        if (postulation == null) {
            return false;
        }
        if (postulation.getJobId() != null && !Objects.equals(postulation.getJobId(), existing.getJobId())) {
            if (!jobRepository.existsById(postulation.getJobId())) {
                throw new ResourceNotFoundException();
            }
            existing.setJobId(postulation.getJobId());
        }
        if (postulation.getDate() != null) {
            existing.setDate(postulation.getDate());
        }
        postulationRepository.save(existing);
        return true;
    }

    /** La retira el postulante o la resuelve el propietario de la empresa (o ADMIN); el resto, 404. */
    public boolean DeleteProstulationById(Long Id) {
        Postulation existing = findVisibleOr404(Id);
        postulationRepository.delete(existing);
        return true;
    }

    private Postulation findVisibleOr404(Long id) {
        Postulation postulation = (id == null || id <= 0) ? null : postulationRepository.findById(id).orElse(null);
        if (postulation == null) {
            throw new ResourceNotFoundException();
        }
        guard.assertParticipantOrNotFound(postulation.getUserId(), companyOwnerOf(postulation.getJobId()));
        return postulation;
    }

    private Long companyOwnerOf(Long jobId) {
        if (jobId == null) {
            return null;
        }
        return jobRepository.findById(jobId)
                .flatMap(job -> companyRepository.findById(job.getCompanyId()))
                .map(Company::getUserId)
                .orElse(null);
    }
}
