package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.LoggingService;
import main.com.julio.service.ValidationService;

public class Contrat {
    private static int compteurId = 1;

    private int id;
    private int clientId;
    private String nomContrat;
    private double montant;

    public Contrat(int clientId, String nomContrat, double montant) {
        this.id = compteurId++;
        setClientId(clientId);
        setNomContrat(nomContrat);
        setMontant(montant);
        LoggingService.log("Contrat créé avec ID: " + this.id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        if (clientId <= 0) {
            throw new ValidationException("clientId","L'ID du client est obligatoire.");
        }
        this.clientId = clientId;
    }

    public String getNomContrat() {
        return nomContrat;
    }

    public void setNomContrat(String nomContrat) {
        if (ValidationService.isNullOrEmpty(nomContrat)) {
            throw new ValidationException("nomContrat", "Le nom du client est obligatoire.");
        }
        this.nomContrat = nomContrat;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        if (montant <= 0) {
            throw new ValidationException("montant", "Le montant doit être positif.");
        }
        this.montant = montant;
    }

    public static void resetCompteur(){
        compteurId = 1;
    }

    public String toString(){
        return nomContrat + " (" + montant + "€)";
    }
}
