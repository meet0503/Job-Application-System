package com.casestudy.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.casestudy.entities.external.Rating;

@FeignClient(name = "RATINGSERVICE")
public interface RatingClient {
	
	@GetMapping("/ratings")
	List<Rating> getRatingsByCompanyId(@RequestParam String companyId);
}
