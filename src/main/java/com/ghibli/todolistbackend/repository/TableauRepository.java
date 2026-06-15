package com.ghibli.todolistbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ghibli.todolistbackend.model.Tableau;
import java.util.Optional; 

public interface TableauRepository extends JpaRepository<Tableau, Long> {
    // Permet de trouver un tableau collectif avec son code d'invitation
    Optional<Tableau> findByCodeGroupe(String codeGroupe);
}