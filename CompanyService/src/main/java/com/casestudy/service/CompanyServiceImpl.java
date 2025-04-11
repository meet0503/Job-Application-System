package com.casestudy.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.casestudy.entities.Company;
import com.casestudy.exception.CompanyNotFoundException;
import com.casestudy.payload.ApiResponse;
import com.casestudy.repository.CompanyRepository;

@Service
public class CompanyServiceImpl implements CompanyService {

	private final CompanyRepository companyRepository;
	
	public CompanyServiceImpl(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	@Override
	public void addCompany(List<Company> companies) {

		companies.forEach(company -> company.setId(UUID.randomUUID().toString()));
		
		companyRepository.saveAll(companies);
	}

	@Override
	public List<Company> findAllCompanies() {

		return companyRepository.findAll();
	}

	@Override
	public Company findCompanyById(String id) {

		return companyRepository.findById(id).orElseThrow(() -> new CompanyNotFoundException("No Company found with this id"));
	}

	@Override
	public ResponseEntity<ApiResponse> deleteCompany(String id) {
	    Company company = companyRepository.findById(id)
	        .orElseThrow(() -> new CompanyNotFoundException("No Company found with this id"));

	    companyRepository.delete(company);

	    String message = "Job Deleted Successfully: " + company.getName();
	    ApiResponse response = ApiResponse.builder()
	        .message(message)
	        .success(true)
	        .build();

	    return ResponseEntity.ok(response);
	}


	@Override
	public Company updateCompany(String id, Company updatedCompany) {

		Company existingCompany = companyRepository.findById(id)
				.orElseThrow(() -> new CompanyNotFoundException("No Company found with this id"));

		existingCompany.setName(updatedCompany.getName());
		existingCompany.setDescription(updatedCompany.getDescription());
		

		return companyRepository.save(existingCompany);
	}

}
