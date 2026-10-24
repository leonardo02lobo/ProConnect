package Proconnect.backProconnect.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.model.JobDto;
import Proconnect.backProconnect.repository.JobRepository;
import Proconnect.backProconnect.specification.JobSpecification;
import Proconnect.backProconnect.exception.ResourceNotFoundException;

@Service
public class JobService {
    
    private final JobRepository jobRepository;
    private final CompanyService companyService;

    public JobService(JobRepository jobRepository, CompanyService companyService){
        this.jobRepository = jobRepository;
        this.companyService = companyService;
    }

    /** Solo el propietario de la empresa (o ADMIN) publica empleos en ella (403; 404 si no existe). */
    public String AddJob(JobDto job){
        if(job == null){
            return "Job cannot be null";
        }
        companyService.assertCanManage(job.getCompanyId());
        try {
            Job jobEntity = new Job(null, job.getCompanyId() , job.getTitle(), job.getDescription(), job.getWorkStation(), job.getDate());
            jobRepository.save(jobEntity);
            return "Job Add Successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    /** Solo el propietario de la empresa del empleo (o ADMIN) lo borra (403; 404 si no existe). */
    public String DeleteJobById(Long Id){
        Job job = findOr404(Id);
        companyService.assertCanManage(job.getCompanyId());
        jobRepository.delete(job);
        return "Job Delete Successufly";
    }

    public Job findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new ResourceNotFoundException();
        }
        return jobRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Job> getAllJob(){
        return jobRepository.findAll();
    }

    public List<Job> findByKeyWord(String keyWord){
        Specification<Job> jobs = JobSpecification.hasKeyWord(keyWord);
        return jobRepository.findAll(jobs);
    }
}
