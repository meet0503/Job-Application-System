package com.casestudy.service;

import java.util.List;

import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;

public interface JobService {
	//create
	List<Job> addJob(List<Job> job);
	
	//find All Jobs
	List<JobDTO> findAllJobs();
	
	//find by Job by id
	JobDTO findJobById(String id);
	
	//delete 
	Job deleteJob(String id);
	
	//update
	Job updateJob(String id, Job job);
	
	//find all jobs listed by a company
	List<JobDTO> findJobsByCompanyId(String companyId);
	
}
