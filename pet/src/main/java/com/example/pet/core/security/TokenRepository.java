package com.example.pet.core.security;

import com.example.pet.core.security.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByAccessToken(String jwt);
}
