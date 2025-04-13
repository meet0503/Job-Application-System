package com.casestudy.service;

import java.util.List;

import com.casestudy.entities.Company;

public interface CompanyService {
	//create
	void addCompany(List<Company> companies);
	
	//find All Companies
	List<Company> findAllCompanies();
	
	//find by Company by id
	Company findCompanyById(String id);
	
	//delete 
	Company deleteCompany(String id);
	
	//update
	Company updateCompany(String id, Company company);
	
}
