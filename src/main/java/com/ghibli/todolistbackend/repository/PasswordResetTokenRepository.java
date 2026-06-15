package com.ghibli.todolistbackend.repository;

import com.ghibli.todolistbackend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUtilisateurId(Long utilisateurId);
}