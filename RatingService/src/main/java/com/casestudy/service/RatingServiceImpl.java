package com.casestudy.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.casestudy.entities.Rating;
import com.casestudy.exception.CompanyNotFoundException;
import com.casestudy.exception.RatingNotFoundException;
import com.casestudy.repository.RatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

	private final RatingRepository ratingRepository;
	
	public static final String RATING_NOT_FOUND ="No Ratings found with this id";
	
	@Override
	public List<Rating> getAllRatings(String companyId) {
		
		return ratingRepository.findByCompanyId(companyId);
	}

	@Override
	public void addRating(String companyId, Rating rating) {

		
		rating.setId(UUID.randomUUID().toString());
		
		//added companyId not null check
		if(companyId==null || companyId.isEmpty()) {
			throw new CompanyNotFoundException("CompanyId is either blank or null");
		}
		rating.setCompanyId(companyId);
		
		ratingRepository.save(rating);
	}


	@Override
	public Rating findRatingById(String id) {

		return ratingRepository.findById(id).orElseThrow(() -> new RatingNotFoundException(RATING_NOT_FOUND));
	}

	@Override
	public Rating deleteRating(String id) {
	    Rating rating = ratingRepository.findById(id)
	        .orElseThrow(() -> new RatingNotFoundException(RATING_NOT_FOUND));

	    ratingRepository.delete(rating);
	    return rating;
	}


	@Override
	public Rating updateRating(String id, Rating updatedRating) {
		Rating existingRating = ratingRepository.findById(id)
				.orElseThrow(() -> new RatingNotFoundException(RATING_NOT_FOUND));

		existingRating.setTitle(updatedRating.getTitle());
		existingRating.setFeedback(updatedRating.getFeedback());
		existingRating.setRatings(updatedRating.getRatings());
		existingRating.setCompanyId(updatedRating.getCompanyId());

		return ratingRepository.save(existingRating);
	}

}
