package com.casestudy.entities;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "RATINGS")
public class Rating {
	
	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "FEEDBACK")
	private String feedback;

	@Column(name = "RATINGS")
	private double ratings;

	@Column(name = "COMPANY_ID", nullable = false)
	private String companyId;  


}
