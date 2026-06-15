package com.ghibli.todolistbackend.model;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "tache")
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto incrémentation
    private Long id; // Notre clé primaire

    private String titre; 
    private String description;
    private LocalDate dateCreation;
    private LocalDate dateFinTache;
    private String priorite;    // Urgent, Important, Secondaire
    private double avancement;
    private boolean terminee; // pour indiquer l'état de la tache

    // Clé étrangère obligatoire vers un Tableau (Qu'il soit perso ou collectif)
    @ManyToOne
    @JoinColumn(name = "tableau_id", nullable = false)
    private Tableau tableau;


    public Tache() {} // Constructeur vide pour Spring Boot

    // Constructeur Tache pour notre test 
    public Tache(String titre, String description, LocalDate dateFinTache, String priorite, Tableau tableau) {
            this.titre = titre;
            this.description = description;
            this.dateCreation = LocalDate.now();
            this.dateFinTache = dateFinTache;
            this.priorite = priorite;
            this.avancement = 0.0;
            this.terminee = false;
            this.tableau = tableau;
    }

    // Getters et Setters des différents attributs
    public Long getId() { return id;}
    public void setId(Long ident) { this.id = ident;}
    
    public String getTitre(){ return titre;}
    public void setTitre(String titre) { this.titre = titre;}

    public String getDescription(){ return description;}
    public void setDescription(String description) { this.description = description;}

    public LocalDate getDateCreation() { return dateCreation;}
    public void setDateCreation(LocalDate date) { this.dateCreation = date;}

    public LocalDate getDateFinTache() { return dateFinTache;}
    public void setDateFinTache(LocalDate date2) { this.dateFinTache = date2;}

    public String getPriorite() { return priorite;}
    public void setPriorite(String priorite_new) { this.priorite = priorite_new;}
    
    public double getAvancement() { return avancement;}
    public void setAvancement(double avanc) { this.avancement = avanc;} 

    public boolean getTerminee() { 
        if (avancement == 100.0) {
            terminee = true;
        }else {terminee = false;}
        return terminee;}

    public void setTerminee(boolean bool) { this.terminee = bool;}

    public Tableau getTableau() { return tableau; }
    public void setTableau(Tableau tableau) { this.tableau = tableau; }

}
