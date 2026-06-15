package com.ghibli.todolistbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = Utilisateur.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, Utilisateur utilisateur) {
        this.token = token;
        this.utilisateur = utilisateur;
        // Le jeton expire après 15 minutes
        this.expirationDate = LocalDateTime.now().plusMinutes(15);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expirationDate);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public Utilisateur getUtilisateur() { return utilisateur; }
}