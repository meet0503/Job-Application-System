package com.casestudy.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.casestudy.entities.Rating;
import com.casestudy.exception.RatingNotFoundException;
import com.casestudy.payload.ApiResponse;
import com.casestudy.repository.RatingRepository;

@Service
public class RatingServiceImpl implements RatingService {

	@Autowired
	private RatingRepository ratingRepository;
	
	@Override
	public List<Rating> getAllRatings(String companyId) {
		
		return ratingRepository.findByCompanyId(companyId);
	}

	@Override
	public void addRating(String companyId, Rating rating) {
		// TODO Auto-generated method stub
		
		rating.setId(UUID.randomUUID().toString());
		
		//add companyId not null check
		rating.setCompanyId(companyId);
		
		
		ratingRepository.save(rating);
	}


	@Override
	public Rating findRatingById(String id) {
		// TODO Auto-generated method stub
		return ratingRepository.findById(id).orElseThrow(() -> new RatingNotFoundException("No Rating found with this id"));
	}

	@Override
	public ResponseEntity<ApiResponse> deleteRating(String id) {
	    Rating rating = ratingRepository.findById(id)
	        .orElseThrow(() -> new RatingNotFoundException("No Rating found with this id"));

	    ratingRepository.delete(rating);

	    String message = "Rating Deleted Successfully: " + rating.getTitle();
	    ApiResponse response = ApiResponse.builder()
	        .message(message)
	        .success(true)
	        .build();

	    return ResponseEntity.ok(response);
	}


	@Override
	public Rating updateRating(String id, Rating updatedRating) {
		// TODO Auto-generated method stub
		Rating existingRating = ratingRepository.findById(id)
				.orElseThrow(() -> new RatingNotFoundException("No Ratings found with this id"));

		existingRating.setTitle(updatedRating.getTitle());
		existingRating.setFeedback(updatedRating.getFeedback());
		existingRating.setRatings(updatedRating.getRatings());
		existingRating.setCompanyId(updatedRating.getCompanyId());

		return ratingRepository.save(existingRating);
	}

}
