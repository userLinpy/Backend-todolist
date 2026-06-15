package com.ghibli.todolistbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ghibli.todolistbackend.model.LoginRequest;
import com.ghibli.todolistbackend.model.Tableau;
import com.ghibli.todolistbackend.model.Utilisateur;
import com.ghibli.todolistbackend.repository.TableauRepository;
import com.ghibli.todolistbackend.repository.UtilisateurRepository;
import com.ghibli.todolistbackend.service.AuthService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final com.ghibli.todolistbackend.repository.TableauRepository tableauRepository;

    public AuthController(UtilisateurRepository utilisateurRepository, AuthService authService, PasswordEncoder passwordEncoder, TableauRepository tableauRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.tableauRepository = tableauRepository; 
    }

    // Inscription (Création du compte bloqué + Envoi du mail de vérification)
    @PostMapping("/inscription")
    public ResponseEntity<String> inscription(@RequestBody LoginRequest request) {
        if (request.getPassword() == null || request.getPassword().trim().length() < 5) {
            return ResponseEntity.badRequest().body("Erreur : Le mot de passe doit contenir au moins 5 caractères !");
        }

        // 1. Sauvegarde de l'utilisateur
        Utilisateur nouvelUtilisateur = new Utilisateur(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            request.getEmail()
        );
        utilisateurRepository.save(nouvelUtilisateur);

        // 2. CRÉATION AUTOMATIQUE DE SON TABLEAU PERSO
        Tableau tableauPerso = new Tableau("Mon Espace (" + request.getUsername() + ")", "PERSONNEL");
        tableauPerso.setUtilisateurs(new java.util.ArrayList<>(java.util.List.of(nouvelUtilisateur)));
        tableauRepository.save(tableauPerso); // Sauvegardé en BDD !

        // 3. Envoi du mail
        authService.envoyerCodeVerificationInscription(nouvelUtilisateur);

        return ResponseEntity.ok("Inscription réussie ! Un code de vérification a été envoyé.");
    }

    // Confirmer l'inscription avec le code reçu par mail
    @PostMapping("/verifier-compte")
    public ResponseEntity<String> verifierCompte(@RequestBody Map<String, String> request) {
        String email = request.get("email"); // 🌟 Il faut envoyer l'email depuis JavaFX !
        String code = request.get("code");
        
        // On appelle la méthode qui vérifie l'email ET le code
        boolean valide = authService.validerCompte(email, code); 
        
        if (valide) {
            return ResponseEntity.ok("Votre compte a été activé avec succès !");
        }
        return ResponseEntity.badRequest().body("Erreur : Code invalide ou expiré.");
    }

    // Connexion (Utilise l'EMAIL comme identifiant de connexion)
    @PostMapping("/connexion")
    public ResponseEntity<?> connecter(@RequestBody LoginRequest request) {
        // Étape 1 : Trouver l'utilisateur (le front envoie la saisie dans le champ "username")
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(request.getUsername());
        if (userOpt.isEmpty()) {
            userOpt = utilisateurRepository.findByUsername(request.getUsername());
        }

        if (userOpt.isPresent()) {
            Utilisateur utilisateur = userOpt.get();

            // Étape 2 : Vérifier si le compte est actif
            if (!utilisateur.isActif()) {
                return ResponseEntity.badRequest().body("Erreur : Compte non activé. Vérifie tes e-mails !");
            }

            // 🌟 Étape 3 : LA CORRECTION MAGIQUE ICI 🌟
            if (passwordEncoder.matches(request.getPassword(), utilisateur.getPassword())) {
                return ResponseEntity.ok(utilisateur); // Connexion réussie !
            } else {
                return ResponseEntity.badRequest().body("Erreur : Mot de passe incorrect.");
            }
        }
        return ResponseEntity.badRequest().body("Erreur : Utilisateur introuvable.");
    }

    // Demande de jeton (Mot de passe oublié)
    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<?> motDePasseOublie(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean succes = authService.genererJetonReinitialisation(email);
        if (succes) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Erreur : Aucun compte n'est associé à cet e-mail.");
    }
    
    // Soumission du nouveau mot de passe avec le jeton
    @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<?> reinitialiserMotDePasse(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String nouveauMotDePasse = request.get("nouveauMotDePasse");

        if (nouveauMotDePasse == null || nouveauMotDePasse.trim().length() < 5) {
            return ResponseEntity.badRequest().body("Erreur : Le nouveau mot de passe doit contenir au moins 5 caractères !");
        }
    
        boolean valide = authService.modifierMotDePasseAvecJeton(token, nouveauMotDePasse);
        if (valide) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Jeton invalide ou expiré.");
    }

    @PostMapping("/valider-compte")
    public ResponseEntity<String> validerCompte(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (authService.validerCompte(email, code)) {
            return ResponseEntity.ok("Compte activé avec succès !");
        } else {
            return ResponseEntity.badRequest().body("Code invalide ou expiré.");
        }
    }

    // Modifier le pseudo de l'utilisateur
    @PutMapping("/pseudo")
    public ResponseEntity<?> changerPseudo(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String nouveauPseudo = request.get("pseudo");

        if (email == null || nouveauPseudo == null || nouveauPseudo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Données invalides.");
        }

        return utilisateurRepository.findByEmail(email).map(user -> {
            user.setUsername(nouveauPseudo);
            utilisateurRepository.save(user);
            return ResponseEntity.ok("Pseudo mis à jour avec succès !");
        }).orElse(ResponseEntity.badRequest().body("Utilisateur introuvable."));
    }
}