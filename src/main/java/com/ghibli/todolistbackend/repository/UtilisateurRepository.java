package com.ghibli.todolistbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ghibli.todolistbackend.model.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsername(String username);
    Optional<Utilisateur> findByEmail(String email);

}