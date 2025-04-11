package com.security.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.security.entities.RefreshToken;
import com.security.entities.User;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer>{

	Optional<RefreshToken> findByToken(String token);

	Optional<RefreshToken> findByUser(User user);

}
