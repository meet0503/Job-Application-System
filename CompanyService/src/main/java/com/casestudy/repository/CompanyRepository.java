package com.casestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.casestudy.entities.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String>{

}
