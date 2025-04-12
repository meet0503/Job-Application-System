package com.casestudy.mapper;

import java.util.List;

import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.entities.external.Company;
import com.casestudy.entities.external.Rating;


public class JobMapper {
	public static JobDTO mapToJobWithCompanyDTO(Job job, Company company, List<Rating> ratings) {
		JobDTO jobDTO = new JobDTO();
		jobDTO.setId(job.getId());
		jobDTO.setDescription(job.getDescription());
		jobDTO.setMinSalary(job.getMinSalary());
		jobDTO.setMaxSalary(job.getMaxSalary());
		jobDTO.setLocation(job.getLocation());
		jobDTO.setTitle(job.getTitle());
		
		jobDTO.setCompany(company);
		jobDTO.setRatings(ratings);
		
		return jobDTO;
	}
	
	// so that no one can instantiate the JobMapper
	private JobMapper() {
		
	}
}
