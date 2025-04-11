package com.casestudy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.casestudy.entities.Company;
import com.casestudy.payload.ApiResponse;
import com.casestudy.service.CompanyServiceImpl;

@RestController
@RequestMapping("/companies")
public class CompanyController {
	
	private final CompanyServiceImpl companyServiceImpl;
	
	public CompanyController(CompanyServiceImpl companyServiceImpl) {
		this.companyServiceImpl = companyServiceImpl;
	}
	
	@GetMapping
	public ResponseEntity<List<Company>> getAllCompanies(){
		return new ResponseEntity<>(companyServiceImpl.findAllCompanies(),HttpStatus.OK);	
	}
	
	@PostMapping
	public ResponseEntity<String> createCompany(@RequestBody List<Company> company){
		companyServiceImpl.addCompany(company);
		return new ResponseEntity<>("Company Created Successfully",HttpStatus.CREATED);
	}
	
	@GetMapping("/{companyId}")
	public ResponseEntity<Company> findCompany(@PathVariable String companyId){
		return new ResponseEntity<>(companyServiceImpl.findCompanyById(companyId),HttpStatus.OK);
	}
	
	@PutMapping("/{companyId}")
	public ResponseEntity<Company> updateCompany(@PathVariable String companyId, @RequestBody Company company){
		return new ResponseEntity<>(companyServiceImpl.updateCompany(companyId, company),HttpStatus.OK);
	}
	
	@DeleteMapping("/{companyId}")
	public ResponseEntity<ApiResponse> deleteCompany(@PathVariable String companyId){
		
		return companyServiceImpl.deleteCompany(companyId);
	}
	
	//get /jobs/{id}/company
}
