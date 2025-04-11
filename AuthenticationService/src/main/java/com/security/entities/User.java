package com.security.entities;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	
	@NotBlank(message = "The username field can't be blank")
	@Column(unique = true)
	private String username;
	
	@NotBlank(message="The password field can't be blank")
	@Size(min = 5, message = "The password must have at least 5 characters")
	private String password;
	
	@Enumerated(EnumType.STRING)
	private UserRole role;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonManagedReference
	private RefreshToken refreshToken;


	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public boolean isAccountNonExpired() {
	    return true; 
	}

	@Override
	public boolean isAccountNonLocked() {
	    return true; 
	}

	@Override
	public boolean isCredentialsNonExpired() {
	    return true; 
	}

	@Override
	public boolean isEnabled() {
	    return true;
	}
}
