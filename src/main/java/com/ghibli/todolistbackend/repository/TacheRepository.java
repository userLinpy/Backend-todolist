package com.ghibli.todolistbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ghibli.todolistbackend.model.Tache;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    
    // Spring Boot va automatiquement générer la requête SQL : 
    // "SELECT * FROM tache WHERE tableau_id = ?"
    List<Tache> findByTableauId(Long tableauId);
}