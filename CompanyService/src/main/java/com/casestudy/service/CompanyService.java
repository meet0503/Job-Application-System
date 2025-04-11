package com.casestudy.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.casestudy.entities.Company;
import com.casestudy.payload.ApiResponse;

public interface CompanyService {
	//create
	void addCompany(List<Company> companies);
	
	//find All Companies
	List<Company> findAllCompanies();
	
	//find by Company by id
	Company findCompanyById(String id);
	
	//delete 
	ResponseEntity<ApiResponse> deleteCompany(String id);
	
	//update
	Company updateCompany(String id, Company company);
	
}
