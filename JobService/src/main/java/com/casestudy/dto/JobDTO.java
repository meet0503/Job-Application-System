package com.casestudy.dto;

import java.util.List;

import com.casestudy.entities.external.Company;
import com.casestudy.entities.external.Rating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobDTO {
	private String id;
	private String title;
	private String description;
	private String minSalary;
	private String maxSalary;
	private String location;
	private Company company;
	private List<Rating> ratings;
}
