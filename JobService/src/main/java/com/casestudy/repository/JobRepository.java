package com.casestudy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.casestudy.entities.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, String>{
	List<Job> findByCompanyId(String companyId);
}
