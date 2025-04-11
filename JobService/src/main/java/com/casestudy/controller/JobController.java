package com.casestudy.controller;

import java.util.List;

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
import com.casestudy.payload.ApiResponse;
import com.casestudy.service.JobServiceImpl;


@RestController
@RequestMapping("/jobs")
public class JobController {
	
	private final JobServiceImpl jobServiceImpl;

    // Constructor Injection
    public JobController(JobServiceImpl jobServiceImpl) {
        this.jobServiceImpl = jobServiceImpl;
    }
	
	@GetMapping
	public ResponseEntity<List<JobDTO>> getAllJobs(){
		return new ResponseEntity<>(jobServiceImpl.findAllJobs(),HttpStatus.OK);	
	}
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@PostMapping
	public ResponseEntity<String> createJob(@RequestBody List<Job> job){
		jobServiceImpl.addJob(job);
		return new ResponseEntity<>("Job Created Successfully",HttpStatus.CREATED);
	}
	
	@GetMapping("/{jobId}")
	public ResponseEntity<JobDTO> findJob(@PathVariable String jobId){
		return new ResponseEntity<>(jobServiceImpl.findJobById(jobId),HttpStatus.OK);
	}
	
	@GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobDTO>> getJobsByCompany(@PathVariable String companyId) {
        List<JobDTO> jobs = jobServiceImpl.findJobsByCompanyId(companyId);
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@PutMapping("/{jobId}")
	public ResponseEntity<Job> updateJob(@PathVariable String jobId, @RequestBody Job job){
		return new ResponseEntity<>(jobServiceImpl.updateJob(jobId, job),HttpStatus.OK);
	}
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@DeleteMapping("/{jobId}")
	public ResponseEntity<ApiResponse> deleteJob(@PathVariable String jobId){
		
		return jobServiceImpl.deleteJob(jobId);
	}
}
