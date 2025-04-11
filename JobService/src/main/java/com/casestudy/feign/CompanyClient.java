package com.casestudy.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.casestudy.entities.external.Company;


@FeignClient(name = "COMPANYSERVICE")
public interface CompanyClient {
	@GetMapping("/companies/{companyId}")
	Company getCompanyById(@PathVariable String companyId);

}
