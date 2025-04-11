package com.casestudy.entities.external;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Rating {
	private String id;
	private String title;
	private String feedback;
	private double ratings;
}
