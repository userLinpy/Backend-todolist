package com.ghibli.todolistbackend.service;

import com.ghibli.todolistbackend.model.Utilisateur;
import com.ghibli.todolistbackend.model.PasswordResetToken;
import com.ghibli.todolistbackend.repository.UtilisateurRepository;
import com.ghibli.todolistbackend.repository.PasswordResetTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${mail.from}")
    private String mailFrom;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void envoyerCodeVerificationInscription(Utilisateur utilisateur) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken verificationToken = new PasswordResetToken(token, utilisateur);
        tokenRepository.save(verificationToken);

        String sujet = "Validation de votre compte Totoro ToDoList";
        String contenu = "Bienvenue dans l'univers Ghibli !<br><br>"
                + "Voici votre code de validation pour activer votre compte ToDoList :<br><br>"
                + "<strong>" + token + "</strong><br><br>"
                + "Ce code expire dans 15 minutes.";

        envoyerEmail(utilisateur.getEmail(), sujet, contenu);
    }

    @Transactional
    public boolean validerInscriptionAvecCode(String code) {
        return tokenRepository.findByToken(code)
            .filter(resetToken -> !resetToken.isExpired())
            .map(resetToken -> {
                Utilisateur utilisateur = resetToken.getUtilisateur();
                utilisateur.setActif(true);
                utilisateurRepository.save(utilisateur);
                tokenRepository.delete(resetToken);
                return true;
            }).orElse(false);
    }

    @Transactional
    public boolean genererJetonReinitialisation(String email) {
        return utilisateurRepository.findByEmail(email).map(utilisateur -> {
            tokenRepository.deleteByUtilisateurId(utilisateur.getId());

            String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            PasswordResetToken resetToken = new PasswordResetToken(token, utilisateur);
            tokenRepository.save(resetToken);

            String contenu = "Bonjour " + utilisateur.getUsername() + ",<br><br>"
                    + "Vous avez demandé à réinitialiser votre mot de passe.<br>"
                    + "Voici votre code de secours à copier dans l'application :<br><br>"
                    + "<strong>" + token + "</strong><br><br>"
                    + "Ce code expire dans 15 minutes.";

            envoyerEmail(email, "Réinitialisation de votre mot de passe", contenu);
            return true;
        }).orElse(false);
    }

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

    @Transactional
    public boolean validerCompte(String email, String codeRecu) {
        return utilisateurRepository.findByEmail(email)
            .flatMap(user -> tokenRepository.findByToken(codeRecu)
                .filter(t -> t.getUtilisateur().getEmail().equals(email))
                .map(t -> {
                    user.setActif(true);
                    utilisateurRepository.save(user);
                    tokenRepository.delete(t);
                    return true;
                })
            ).orElse(false);
    }

    private void envoyerEmail(String destinataire, String sujet, String htmlContent) {
        try {
            String body = "{"
                + "\"sender\":{\"name\":\"Totoro ToDoList\",\"email\":\"" + mailFrom + "\"},"
                + "\"to\":[{\"email\":\"" + destinataire + "\"}],"
                + "\"subject\":\"" + sujet + "\","
                + "\"htmlContent\":\"" + htmlContent.replace("\"", "\\\"") + "\""
                + "}";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("api-key", brevoApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                System.out.println("📧 E-mail envoyé avec succès à : " + destinataire);
            } else {
                System.err.println("❌ Erreur Brevo API (" + response.statusCode() + ") : " + response.body());
                System.out.println("CODE DE SECOURS (Console) pour [" + destinataire + "] -> " + sujet);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur d'envoi de l'e-mail : " + e.getMessage());
            System.out.println("CODE DE SECOURS (Console) pour [" + destinataire + "] -> " + sujet);
        }
    }
}
