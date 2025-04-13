package com.casestudy.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.casestudy.entities.Company;
import com.casestudy.exception.CompanyNotFoundException;
import com.casestudy.repository.CompanyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CompanyServiceImpl implements CompanyService {

	private final CompanyRepository companyRepository;
	
	public CompanyServiceImpl(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}
	
	public static final String COMPANY_NOT_FOUND ="No Company found with this id";
	
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

		return companyRepository.findById(id).orElseThrow(() -> {
			log.warn("Company with ID {} not found", id);
			throw new CompanyNotFoundException(COMPANY_NOT_FOUND);
		});
	}

	@Override
	public Company deleteCompany(String id) {
	    Company company = companyRepository.findById(id).orElseThrow(() -> {
			log.warn("Delete failed - Company with ID {} not found", id);
			throw new CompanyNotFoundException(COMPANY_NOT_FOUND);
		});

	    companyRepository.delete(company);
	    return company;
	}


	@Override
	public Company updateCompany(String id, Company updatedCompany) {

		Company existingCompany = companyRepository.findById(id).orElseThrow(() -> {
			log.warn("Update failed - Company with ID {} not found", id);
			throw new CompanyNotFoundException(COMPANY_NOT_FOUND);
		});

		existingCompany.setName(updatedCompany.getName());
		existingCompany.setDescription(updatedCompany.getDescription());
		

		return companyRepository.save(existingCompany);
	}

}
