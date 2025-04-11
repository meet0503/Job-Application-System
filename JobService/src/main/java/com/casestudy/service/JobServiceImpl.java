package com.casestudy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.entities.external.Company;
import com.casestudy.entities.external.Rating;
import com.casestudy.exception.JobNotFoundException;
import com.casestudy.feign.CompanyClient;
import com.casestudy.feign.RatingClient;
import com.casestudy.mapper.JobMapper;
import com.casestudy.payload.ApiResponse;
import com.casestudy.repository.JobRepository;

@Service
public class JobServiceImpl implements JobService {

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
	public void addJob(List<Job> jobs) {
	    jobs.forEach(job -> {
	        if (job.getCompanyId() == null || job.getCompanyId().isEmpty()) {
	            throw new IllegalArgumentException("Company ID is required");
	        }
	        job.setId(UUID.randomUUID().toString());
	    });
	    jobRepository.saveAll(jobs);
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

		Job job = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(JOB_NOT_FOUND));
		
		Company company = companyClient.getCompanyById(job.getCompanyId());
		List<Rating> ratings = ratingClient.getRatingsByCompanyId(job.getCompanyId());
		
		return JobMapper.mapToJobWithCompanyDTO(job, company, ratings);
	}
	
	@Override
    public List<JobDTO> findJobsByCompanyId(String companyId) {
        List<Job> jobs = jobRepository.findByCompanyId(companyId);
        List<JobDTO> jobDTOs = new ArrayList<>();
        
        if (jobs.isEmpty()) {
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
	public ResponseEntity<ApiResponse> deleteJob(String id) {
	    Job job = jobRepository.findById(id)
	        .orElseThrow(() -> new JobNotFoundException(JOB_NOT_FOUND));

	    jobRepository.delete(job);

	    String message = "Job Deleted Successfully: " + job.getTitle() + " (" + job.getId() + ")";
	    ApiResponse response = ApiResponse.builder()
	        .message(message)
	        .success(true)
	        .build();

	    return ResponseEntity.ok(response);
	}


	@Override
	public Job updateJob(String id, Job job) {

		Job existingJob = jobRepository.findById(id)
				.orElseThrow(() -> new JobNotFoundException(JOB_NOT_FOUND));

		existingJob.setTitle(job.getTitle());
		existingJob.setDescription(job.getDescription());
		existingJob.setMaxSalary(job.getMaxSalary());
		existingJob.setMinSalary(job.getMinSalary());
		existingJob.setLocation(job.getLocation());
		existingJob.setCompanyId(job.getCompanyId());

		return jobRepository.save(existingJob);
	}

}
