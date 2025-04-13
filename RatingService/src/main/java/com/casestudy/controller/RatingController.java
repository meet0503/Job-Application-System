package com.casestudy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.casestudy.entities.Rating;
import com.casestudy.service.RatingServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ratings")
public class RatingController {
	
	
	private final RatingServiceImpl ratingServiceImpl;
	
	@GetMapping
	public ResponseEntity<List<Rating>> findAllRatings(@RequestParam String companyId){
		return new ResponseEntity<>(ratingServiceImpl.getAllRatings(companyId), HttpStatus.OK);
	}
	
	//no need to PreAuthorize because any user can create ratings against any company
	@PostMapping
	public ResponseEntity<String> createRating(@RequestParam String companyId, @RequestBody Rating rating){
		ratingServiceImpl.addRating(companyId,rating);
		return new ResponseEntity<>("Rating Created Successfully",HttpStatus.CREATED);
	}
	
	@GetMapping("/{ratingId}")
	public ResponseEntity<Rating> findRating(@PathVariable String ratingId){
		return new ResponseEntity<>(ratingServiceImpl.findRatingById(ratingId),HttpStatus.OK);
	}
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@PutMapping("/{ratingId}")
	public ResponseEntity<Rating> updateRating(@PathVariable String ratingId, @RequestBody Rating rating){
		return new ResponseEntity<>(ratingServiceImpl.updateRating(ratingId, rating),HttpStatus.OK);
	}
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@DeleteMapping("/{ratingId}")
	public ResponseEntity<String> deleteRating(@PathVariable String ratingId){
		Rating deletedRating = ratingServiceImpl.deleteRating(ratingId);
		String message = "Rating with Title " + deletedRating.getTitle()+ " is deleted successfully";
		return new ResponseEntity<>(message,HttpStatus.OK);
	}
	
}
