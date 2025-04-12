package com.casestudy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.entities.external.Company;
import com.casestudy.entities.external.Rating;
import com.casestudy.exception.JobNotFoundException;
import com.casestudy.feign.CompanyClient;
import com.casestudy.feign.RatingClient;
import com.casestudy.mapper.JobMapper;
import com.casestudy.repository.JobRepository;

@Service
public class JobServiceImpl implements JobService {
    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
    
    private final JobRepository jobRepository;
    private final CompanyClient companyClient;
    private final RatingClient ratingClient;
    
    public static final String JOB_NOT_FOUND ="No Job found with this id";
    
    //Constructor Injection
    public JobServiceImpl(JobRepository jobRepository, CompanyClient companyClient, RatingClient ratingClient) {
        this.jobRepository=jobRepository;
        this.companyClient=companyClient;
        this.ratingClient=ratingClient;
    }
    
    @Override
    public List<Job> addJob(List<Job> jobs) {
        
        jobs.forEach(job -> {
            if (job.getCompanyId() == null || job.getCompanyId().isEmpty()) {
                log.error("Job creation failed - Company ID is missing");
                throw new IllegalArgumentException("Company ID is required");
            }
            job.setId(UUID.randomUUID().toString());
        });
        return jobRepository.saveAll(jobs);
    }
    
    @Override
    public List<JobDTO> findAllJobs() {
       
        List<Job> jobs = jobRepository.findAll();
        List<JobDTO> jobDTOs = new ArrayList<>();
        
        jobs.forEach(job->{
            Company company = companyClient.getCompanyById(job.getCompanyId());
            List<Rating> ratings = ratingClient.getRatingsByCompanyId(job.getCompanyId());
            
            JobDTO jobDTO = JobMapper.mapToJobWithCompanyDTO(job, company, ratings);
            
            jobDTOs.add(jobDTO);
        });        
        return jobDTOs;
    }
    
    @Override
    public JobDTO findJobById(String id) {
        
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job with ID {} not found", id);
                    return new JobNotFoundException(JOB_NOT_FOUND);
                });
        
        Company company = companyClient.getCompanyById(job.getCompanyId());
        List<Rating> ratings = ratingClient.getRatingsByCompanyId(job.getCompanyId());
        
        return JobMapper.mapToJobWithCompanyDTO(job, company, ratings);
    }
    
    @Override
    public List<JobDTO> findJobsByCompanyId(String companyId) {
        
        List<Job> jobs = jobRepository.findByCompanyId(companyId);
        List<JobDTO> jobDTOs = new ArrayList<>();
        
        if (jobs.isEmpty()) {
        	log.warn("There are no jobs for the specific companyId");
            return jobDTOs;
        }
        
        Company company = companyClient.getCompanyById(companyId);
        List<Rating> ratings = ratingClient.getRatingsByCompanyId(companyId);
        
        jobs.forEach(job -> {
            JobDTO jobDTO = JobMapper.mapToJobWithCompanyDTO(job, company, ratings);
            jobDTOs.add(jobDTO);
        });
        
        return jobDTOs;
    }
    
    @Override
    public Job deleteJob(String id) {
        
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Delete failed - Job with ID {} not found", id);
                return new JobNotFoundException(JOB_NOT_FOUND);
            });
        
        jobRepository.delete(job);
        return job;
    }
    
    @Override
    public Job updateJob(String id, Job job) {
        
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed - Job with ID {} not found", id);
                    return new JobNotFoundException(JOB_NOT_FOUND);
                });
        
        existingJob.setTitle(job.getTitle());
        existingJob.setDescription(job.getDescription());
        existingJob.setMaxSalary(job.getMaxSalary());
        existingJob.setMinSalary(job.getMinSalary());
        existingJob.setLocation(job.getLocation());
        existingJob.setCompanyId(job.getCompanyId());
        
        return jobRepository.save(existingJob);
    }
}