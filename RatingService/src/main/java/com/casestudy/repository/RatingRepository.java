package com.casestudy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.casestudy.entities.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String>{

	List<Rating> findByCompanyId(String companyId);

}
