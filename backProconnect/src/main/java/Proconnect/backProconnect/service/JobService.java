package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.JobDto;
import Proconnect.backProconnect.model.JobRequest;
import Proconnect.backProconnect.repository.JobRepository;
import Proconnect.backProconnect.specification.JobSpecification;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyService companyService;

    public JobService(JobRepository jobRepository, CompanyService companyService) {
        this.jobRepository = jobRepository;
        this.companyService = companyService;
    }

    /**
     * Solo el propietario de la empresa (o ADMIN) publica empleos en ella (403; 404 si no existe).
     */
    @Transactional
    public JobDto create(JobRequest request) {
        companyService.assertCanManage(request.companyId());
        Job job =
                new Job(
                        null,
                        request.companyId(),
                        request.title().trim(),
                        request.description().trim(),
                        request.workStation().trim(),
                        request.date() != null ? request.date() : new Date());
        return toDto(jobRepository.save(job));
    }

    /** Todos los empleos, o los que contienen {@code query} en el título o la descripción. */
    @Transactional(readOnly = true)
    public List<JobDto> list(Optional<String> query) {
        List<Job> jobs =
                query.filter(q -> !q.isBlank())
                        .map(q -> jobRepository.findAll(JobSpecification.hasKeyWord(q.trim())))
                        .orElseGet(jobRepository::findAll);
        return jobs.stream().map(JobService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public JobDto get(Long id) {
        return toDto(findOr404(id));
    }

    /** Solo el propietario de la empresa del empleo (o ADMIN) lo borra (403; 404 si no existe). */
    @Transactional
    public void delete(Long id) {
        Job job = findOr404(id);
        companyService.assertCanManage(job.getCompanyId());
        jobRepository.delete(job);
    }

    public Job findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException("Empleo no encontrado");
        }
        return jobRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Empleo no encontrado"));
    }

    static JobDto toDto(Job j) {
        return new JobDto(
                j.getId(),
                j.getCompanyId(),
                j.getTitle(),
                j.getDescription(),
                j.getWorkStation(),
                j.getDate());
    }
}
