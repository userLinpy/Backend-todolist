package com.ghibli.todolistbackend.service;

import com.ghibli.todolistbackend.model.Utilisateur;
import com.ghibli.todolistbackend.model.PasswordResetToken;
import com.ghibli.todolistbackend.repository.UtilisateurRepository;
import com.ghibli.todolistbackend.repository.PasswordResetTokenRepository;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder; 
    private final JavaMailSender mailSender;

    public AuthService(UtilisateurRepository utilisateurRepository, 
                       PasswordResetTokenRepository tokenRepository, 
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender) {
        this.utilisateurRepository = utilisateurRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // Envoyer le code de vérification pour l'inscription
    @Transactional
    public void envoyerCodeVerificationInscription(Utilisateur utilisateur) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken verificationToken = new PasswordResetToken(token, utilisateur);
        
        // Sauvegarder le jeton en base de données !
        tokenRepository.save(verificationToken);

        String messageText = "Bienvenue dans l'univers Ghibli !\n\n"
                + "Voici votre code de validation pour activer votre compte ToDoList :\n"
                + token + "\n\n"
                + "Ce code expire dans 15 minutes.";

        // On appelle l'outil d'envoi d'e-mail (qui gère son propre try-catch)
        envoyerEmail(utilisateur.getEmail(), "Validation de votre compte Totoro ToDoList", messageText);
    }

    // Valider le code d'inscription reçu par mail
    @Transactional
    public boolean validerInscriptionAvecCode(String code) {
        return tokenRepository.findByToken(code)
            .filter(resetToken -> !resetToken.isExpired())
            .map(resetToken -> {
                Utilisateur utilisateur = resetToken.getUtilisateur();
                utilisateur.setActif(true); // On active enfin le compte !
                utilisateurRepository.save(utilisateur);
                tokenRepository.delete(resetToken); // Code à usage unique
                return true;
            }).orElse(false);
    }

    // Envoyer un jeton pour le mot de passe oublié
    @Transactional
    public boolean genererJetonReinitialisation(String email) {
        return utilisateurRepository.findByEmail(email).map(utilisateur -> {
            tokenRepository.deleteByUtilisateurId(utilisateur.getId());

            String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // Code de réinitialisation propre
            PasswordResetToken resetToken = new PasswordResetToken(token, utilisateur);
            tokenRepository.save(resetToken);

            envoyerEmail(email, 
                "Réinitialisation de votre mot de passe", 
                "Bonjour " + utilisateur.getUsername() + ",\n\n"
                + "Vous avez demandé à réinitialiser votre mot de passe.\n"
                + "Voici votre code de secours à copier dans l'application :\n\n"
                + "👉 " + token + "\n\n"
                + "Ce code expire dans 15 minutes.");
            
            return true;
        }).orElse(false);
    }

    // Modifier le mot de passe
    @Transactional
    public boolean modifierMotDePasseAvecJeton(String token, String nouveauMotDePasse) {
        return tokenRepository.findByToken(token)
            .filter(resetToken -> !resetToken.isExpired())
            .map(resetToken -> {
                Utilisateur utilisateur = resetToken.getUtilisateur();
                utilisateur.setPassword(passwordEncoder.encode(nouveauMotDePasse));
                utilisateurRepository.save(utilisateur);
                tokenRepository.delete(resetToken); 
                return true;
            }).orElse(false);
    }

    // Outil interne d'envoi de mails pour éviter de répéter du code
    private void envoyerEmail(String destinataire, String sujet, String messageTexte) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(messageTexte);
            mailSender.send(message);
            System.out.println("📧 E-mail envoyé avec succès à : " + destinataire);
        } catch (Exception e) {
            System.err.println("❌ Erreur d'envoi de l'e-mail : " + e.getMessage());
            System.out.println("CODE DE SECOURS (Console) pour [" + destinataire + "] -> " + sujet);
        }
    }

    // Nouvelle méthode pour activer le compte via le code reçu par mail
    @Transactional
    public boolean validerCompte(String email, String codeRecu) {
        return utilisateurRepository.findByEmail(email)
            .flatMap(user -> tokenRepository.findByToken(codeRecu)
                .filter(t -> t.getUtilisateur().getEmail().equals(email)) // Sécurité : le code appartient à cet email
                .map(t -> {
                    user.setActif(true);
                    utilisateurRepository.save(user);
                    tokenRepository.delete(t);
                    return true;
                })
            ).orElse(false);
    }
}