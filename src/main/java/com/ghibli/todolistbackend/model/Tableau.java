package com.ghibli.todolistbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tableau")
public class Tableau {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String type; // "PERSONNEL" ou "COLLECTIF"

    @Column(unique = true, nullable = true) // Nullable car les tableaux personnels n'en ont pas besoin
    private String codeGroupe;

    // La relation Many-to-Many 
    // Hibernate va créer automatiquement la table d'association "utilisateur_tableau"
    @ManyToMany
    @JoinTable(
        name = "utilisateur_tableau",
        joinColumns = @JoinColumn(name = "tableau_id"),
        inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    @JsonIgnoreProperties("tableaux")
    private List<Utilisateur> utilisateurs;

    public Tableau() {}
    public Tableau(String nom, String type) {
        this.nom = nom;
        this.type = type;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Utilisateur> getUtilisateurs() { return utilisateurs; }
    public void setUtilisateurs(List<Utilisateur> utilisateurs) { this.utilisateurs = utilisateurs; }

    public String getCodeGroupe() { return codeGroupe; }
    public void setCodeGroupe(String nouvCodeGroupe) { this.codeGroupe = nouvCodeGroupe; }
}
