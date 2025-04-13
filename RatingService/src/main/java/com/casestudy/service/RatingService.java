package com.casestudy.service;

import java.util.List;

import com.casestudy.entities.Rating;

public interface RatingService {
	//create
	void addRating(String companyId, Rating rating);
	
	List<Rating> getAllRatings(String companyId);
	
	//find by Job by id
	Rating findRatingById(String id);
	
	//delete 
	Rating deleteRating(String id);
	
	//update
	Rating updateRating(String id, Rating job);
	
}
