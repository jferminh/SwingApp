package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.ValidationService;

public class Contrat {
    private static int compteurId = 1;

    private int id;
    private int clientId;
    private String nomContrat;
    private double montant;

    public Contrat(int clientId, String nomContrat, double montant) throws ValidationException {
        setClientId(clientId);
        setNomContrat(nomContrat);
        setMontant(montant);
        this.id = compteurId++;
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

    public void setClientId(int clientId) throws ValidationException {
        if (clientId <= 0) {
            throw new ValidationException("L'ID du client est obligatoire.");
        }
        this.clientId = clientId;
    }

    public String getNomContrat() {
        return nomContrat;
    }

    public void setNomContrat(String nomContrat) throws ValidationException {
        if (ValidationService.isNullOrEmpty(nomContrat)) {
            throw new ValidationException("Le nom du contrat est obligatoire.");
        }
        this.nomContrat = nomContrat;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) throws ValidationException {
        if (montant <= 0) {
            throw new ValidationException("Le montant doit être positif.");
        }
        this.montant = montant;
    }

    public String toString() {
        return nomContrat + " (" + montant + "€)";
    }
}
