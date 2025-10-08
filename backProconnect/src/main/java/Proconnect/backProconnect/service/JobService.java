package Proconnect.backProconnect.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.model.JobDto;
import Proconnect.backProconnect.repository.JobRepository;
import Proconnect.backProconnect.specification.JobSpecification;

@Service
public class JobService {
    
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository){
        this.jobRepository = jobRepository;
    }

    public String AddJob(JobDto job){
        try {
            if(job == null){
                return "Job cannot be null";
            }
            Job jobEntity = new Job(job.getId(), job.getCompanyId() , job.getTitle(), job.getDescription(), job.getWorkStation(), job.getDate());
            jobRepository.save(jobEntity);
            return "Job Add Successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public String DeleteJobById(Long Id){
        try {
            if(Long.valueOf(Id) == null || Id <= 0){
                return "Id cannot be null or less zero";
            }
            jobRepository.deleteById(Id);
            return "Job Delete Successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public List<Job> getAllJob(){
        return jobRepository.findAll();
    }

    public List<Job> findByKeyWord(String keyWord){
        Specification<Job> jobs = JobSpecification.hasKeyWord(keyWord);
        return jobRepository.findAll(jobs);
    }
}
