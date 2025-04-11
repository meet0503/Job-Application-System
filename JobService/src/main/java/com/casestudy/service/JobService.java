package com.casestudy.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.payload.ApiResponse;

public interface JobService {
	//create
	void addJob(List<Job> job);
	
	//find All Jobs
	List<JobDTO> findAllJobs();
	
	//find by Job by id
	JobDTO findJobById(String id);
	
	//delete 
	ResponseEntity<ApiResponse> deleteJob(String id);
	
	//update
	Job updateJob(String id, Job job);
	
	//find all jobs listed by a company
	List<JobDTO> findJobsByCompanyId(String companyId);
	
}
