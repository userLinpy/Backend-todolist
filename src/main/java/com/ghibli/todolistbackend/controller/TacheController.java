package com.ghibli.todolistbackend.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ghibli.todolistbackend.model.Tache;
import com.ghibli.todolistbackend.repository.TableauRepository;
import com.ghibli.todolistbackend.repository.TacheRepository;


@RestController // Dit à Spring Boot que cette classe va répondre à des requêtes sur Internet
@RequestMapping("/api") // Permet de standardisé l'adresse par /api/...
public class TacheController {

    private TacheRepository tacheRepository;
    private final com.ghibli.todolistbackend.repository.TableauRepository tableauRepository;

    public TacheController(TacheRepository tacheRepository, TableauRepository tableauRepository) {
        this.tacheRepository = tacheRepository;
        this.tableauRepository = tableauRepository; 
    }

    @PostMapping("/tableau/{tableauId}/tache")
    public ResponseEntity<Tache> createTachePourTableau(@PathVariable Long tableauId, @RequestBody Tache nouvelleTache) {
        return tableauRepository.findById(tableauId).map(tableau -> {
            nouvelleTache.setTableau(tableau); // Assigne le tableau à la tâche
            Tache savedTache = tacheRepository.save(nouvelleTache);
            return ResponseEntity.ok(savedTache);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Nouvelle route pour récupérer les tâches d'UN SEUL tableau spécifique
    @GetMapping("/tableau/{tableauId}/taches")
    public List<Tache> getTachesParTableau(@PathVariable Long tableauId) {
        // Le robot magasinier va chercher uniquement les tâches liées à cet ID de tableau
        return tacheRepository.findByTableauId(tableauId);
    }

    // Modifier une tâche existante
    @PutMapping("/tache/{id}")
    public ResponseEntity<Tache> modifierTache(@PathVariable Long id, @RequestBody Tache tacheModifiee) {
        return tacheRepository.findById(id).map(tache -> {
            tache.setTitre(tacheModifiee.getTitre());
            tache.setDescription(tacheModifiee.getDescription());
            tache.setDateFinTache(tacheModifiee.getDateFinTache());
            tache.setPriorite(tacheModifiee.getPriorite());
            tache.setAvancement(tacheModifiee.getAvancement());
            return ResponseEntity.ok(tacheRepository.save(tache));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Supprimer une tâche par son ID
    @DeleteMapping("/tache/{id}")
    public ResponseEntity<Void> supprimerTache(@PathVariable Long id) {
        if (!tacheRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tacheRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}