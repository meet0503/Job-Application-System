package com.casestudy.entities;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "JOB")
public class Job {
	
	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "TITLE", nullable = false)
	private String title;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "MIN_SALARY")
	private String minSalary;

	@Column(name = "MAX_SALARY")
	private String maxSalary;

	@Column(name = "LOCATION")
	private String location;

	@Column(name = "COMPANY_ID", nullable = false)
	private String companyId; 

}
