package com.casestudy.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.service.JobServiceImpl;


@RestController
@RequestMapping("/jobs")
public class JobController {
	
	private final JobServiceImpl jobServiceImpl;
	
	private static final Logger log = LoggerFactory.getLogger(JobController.class);

    // Constructor Injection
    public JobController(JobServiceImpl jobServiceImpl) {
        this.jobServiceImpl = jobServiceImpl;
    }
	
    @GetMapping
	public ResponseEntity<List<JobDTO>> getAllJobs(){
		
		List<JobDTO> jobs = jobServiceImpl.findAllJobs();
		
		return new ResponseEntity<>(jobs,HttpStatus.OK);	
	}
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@PostMapping
	public ResponseEntity<String> createJob(@RequestBody List<Job> job){
		
		if (job == null || job.isEmpty()) {
			log.warn("Job list is empty or null");
	        return new ResponseEntity<>("Job list cannot be empty", HttpStatus.BAD_REQUEST);
	    }
		
		jobServiceImpl.addJob(job);
		
		return new ResponseEntity<>("Job Created Successfully",HttpStatus.CREATED);
	}
	
	@GetMapping("/{jobId}")
	public ResponseEntity<JobDTO> findJob(@PathVariable String jobId){
		
		JobDTO jobDTO = jobServiceImpl.findJobById(jobId);
		
		return new ResponseEntity<>(jobDTO,HttpStatus.OK);
	}
	
	@GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobDTO>> getJobsByCompany(@PathVariable String companyId) {
        
		List<JobDTO> jobs = jobServiceImpl.findJobsByCompanyId(companyId);
		
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@PutMapping("/{jobId}")
	public ResponseEntity<Job> updateJob(@PathVariable String jobId, @RequestBody Job job){
		
		Job updatedJob = jobServiceImpl.updateJob(jobId, job);
		
		return new ResponseEntity<>(updatedJob,HttpStatus.OK);
	}
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@DeleteMapping("/{jobId}")
	public ResponseEntity<String> deleteJob(@PathVariable String jobId){
		
		Job deletedJob = jobServiceImpl.deleteJob(jobId);
		
		String message = "Job with Title " + deletedJob.getTitle()+ " is deleted successfully";
		return new ResponseEntity<>(message,HttpStatus.OK);
	}
}
