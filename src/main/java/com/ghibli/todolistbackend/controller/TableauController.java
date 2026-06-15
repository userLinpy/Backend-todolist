package com.ghibli.todolistbackend.controller;

import com.ghibli.todolistbackend.model.Tableau;
import com.ghibli.todolistbackend.model.Utilisateur;
import com.ghibli.todolistbackend.repository.TableauRepository;
import com.ghibli.todolistbackend.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tableaux")
public class TableauController {

    private final TableauRepository tableauRepository;
    private final UtilisateurRepository utilisateurRepository;

    public TableauController(TableauRepository tableauRepository, UtilisateurRepository utilisateurRepository) {
        this.tableauRepository = tableauRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // Créer un nouveau groupe collectif
    @PostMapping("/creer")
    public ResponseEntity<?> creerGroupe(@RequestBody Map<String, String> request) {
        String nomGroupe = request.get("nom");
        String emailCreateur = request.get("emailCreateur"); // On identifie le créateur

        if (nomGroupe == null || nomGroupe.isEmpty() || emailCreateur == null) {
            return ResponseEntity.badRequest().body("Nom du groupe ou identifiant manquant.");
        }

        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(emailCreateur);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur créateur introuvable.");
        }

        Utilisateur createur = userOpt.get();

        // Création du tableau collectif
        Tableau nouveauGroupe = new Tableau(nomGroupe, "COLLECTIF");
        
        // Génération d'un code unique à 6 caractères pour rejoindre le groupe
        String codeGenere = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        nouveauGroupe.setCodeGroupe(codeGenere);

        // Ajout du créateur à la liste des membres du groupe
        List<Utilisateur> membres = new ArrayList<>();
        membres.add(createur);
        nouveauGroupe.setUtilisateurs(membres);

        Tableau groupeSauvegarde = tableauRepository.save(nouveauGroupe);
        
        // On retourne le tableau créé (avec son code) pour que le Front-end puisse l'afficher
        return ResponseEntity.ok(groupeSauvegarde);
    }

    // Rejoindre un groupe existant avec un code
    @PostMapping("/rejoindre")
    public ResponseEntity<?> rejoindreGroupe(@RequestBody Map<String, String> request) {
        String codeSaisi = request.get("code");
        String emailUtilisateur = request.get("email");

        if (codeSaisi == null || codeSaisi.isEmpty() || emailUtilisateur == null) {
            return ResponseEntity.badRequest().body("Code ou identifiant manquant.");
        }

        Optional<Tableau> groupeOpt = tableauRepository.findByCodeGroupe(codeSaisi.toUpperCase());
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(emailUtilisateur);

        if (groupeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Code de groupe invalide ou inexistant.");
        }
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur introuvable.");
        }

        Tableau groupe = groupeOpt.get();
        Utilisateur utilisateur = userOpt.get();

        // Vérifier si l'utilisateur est déjà dans le groupe
        boolean dejaMembre = groupe.getUtilisateurs().stream()
                .anyMatch(u -> u.getId().equals(utilisateur.getId()));

        if (dejaMembre) {
            return ResponseEntity.badRequest().body("Vous êtes déjà membre de ce groupe.");
        }

        // Ajouter l'utilisateur au groupe
        groupe.getUtilisateurs().add(utilisateur);
        tableauRepository.save(groupe);

        return ResponseEntity.ok(groupe);
    }

    // Récupérer les groupes d'un utilisateur
    @GetMapping("/mes-groupes/{email}")
    public ResponseEntity<?> getMesGroupes(@PathVariable String email) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            // On filtre la liste pour ne renvoyer QUE les tableaux de type "COLLECTIF"
            List<Tableau> groupes = userOpt.get().getTableaux().stream()
                    .filter(t -> "COLLECTIF".equals(t.getType()))
                    .toList();
            return ResponseEntity.ok(groupes);
        }
        return ResponseEntity.badRequest().body("Utilisateur introuvable.");
    }

    // Récupérer la liste des pseudos des membres d'un tableau
    @GetMapping("/{tableauId}/membres")
    public ResponseEntity<?> getMembresTableau(@PathVariable Long tableauId) {
        return tableauRepository.findById(tableauId).map(tableau -> {
            // On extrait uniquement les pseudos des utilisateurs de ce tableau
            List<String> pseudos = tableau.getUtilisateurs().stream()
                    .map(Utilisateur::getUsername)
                    .toList();
            return ResponseEntity.ok(pseudos);
        }).orElse(ResponseEntity.notFound().build());
    }
}