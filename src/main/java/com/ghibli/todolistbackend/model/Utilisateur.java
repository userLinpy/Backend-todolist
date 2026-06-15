package com.ghibli.todolistbackend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username; // Le pseudo (plus besoin de unique=true si plusieurs peuvent avoir le même)

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false) // L'email devient l'identifiant unique obligatoire
    private String email;

    @Column(nullable = false)
    private boolean actif = false; // Faux par défaut, le compte s'active uniquement après validation du mail

    @ManyToMany(mappedBy = "utilisateurs")
    private List<Tableau> tableaux;

    public Utilisateur() {}

    public Utilisateur(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.actif = false; // Bloqué tant que le code mail n'est pas validé
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public List<Tableau> getTableaux() { return tableaux; }
    public void setTableaux(List<Tableau> tableaux) { this.tableaux = tableaux; }
}