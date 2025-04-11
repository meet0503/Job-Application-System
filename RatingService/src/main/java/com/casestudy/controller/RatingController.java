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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.casestudy.entities.Rating;
import com.casestudy.payload.ApiResponse;
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
	
	@PostMapping
	public ResponseEntity<String> createRating(@RequestParam String companyId, @RequestBody Rating rating){
		ratingServiceImpl.addRating(companyId,rating);
		return new ResponseEntity<>("Rating Created Successfully",HttpStatus.CREATED);
	}
	
	@GetMapping("/{ratingId}")
	public ResponseEntity<Rating> findRating(@PathVariable String ratingId){
		return new ResponseEntity<>(ratingServiceImpl.findRatingById(ratingId),HttpStatus.OK);
	}
	
	@PutMapping("/{ratingId}")
	public ResponseEntity<Rating> updateRating(@PathVariable String ratingId, @RequestBody Rating rating){
		return new ResponseEntity<>(ratingServiceImpl.updateRating(ratingId, rating),HttpStatus.OK);
	}
	
	@DeleteMapping("/{ratingId}")
	public ResponseEntity<ApiResponse> deleteRating(@PathVariable String ratingId){
		
		return ratingServiceImpl.deleteRating(ratingId);
	}
	
}
