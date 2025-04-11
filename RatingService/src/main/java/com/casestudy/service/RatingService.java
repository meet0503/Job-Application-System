package com.casestudy.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.casestudy.entities.Rating;
import com.casestudy.payload.ApiResponse;

public interface RatingService {
	//create
	void addRating(String companyId, Rating rating);
	
	List<Rating> getAllRatings(String companyId);
	
	//find by Job by id
	Rating findRatingById(String id);
	
	//delete 
	ResponseEntity<ApiResponse> deleteRating(String id);
	
	//update
	Rating updateRating(String id, Rating job);
	
}
