package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.LoggingService;
import main.com.julio.service.ValidationService;

public class Adresse {
    private static int compteurId = 1;
    private int id;
    private String numeroRue;
    private String nomRue;
    private String codePostal;
    private String ville;

    public Adresse(String numeroRue, String nomRue, String codePostal, String ville) {
        this.id = compteurId++;
        setNumeroRue(numeroRue);
        setNomRue(nomRue);
        setCodePostal(codePostal);
        setVille(ville);
        LoggingService.log("Adresse créé avec ID : " + this.id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroRue() {
        return numeroRue;
    }

    public void setNumeroRue(String numeroRue) {
        if (ValidationService.isNullOrEmpty(numeroRue)) {
            throw new ValidationException("numeroRue", "Le numéro de rue est obligatoire");
        }
        this.numeroRue = numeroRue;
    }

    public String getNomRue() {
        return nomRue;
    }

    public void setNomRue(String nomRue) {
        if (ValidationService.isNullOrEmpty(nomRue)) {
            throw new ValidationException("nomRue", "Le nome est obligatoire");
        }
        this.nomRue = nomRue;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        if (!ValidationService.isValidCodePostal(codePostal)) {
            throw new ValidationException("codePostal", "Le code doit contenir exactement 5 chiffres");
        }
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        if (ValidationService.isNullOrEmpty(ville)) {
            throw new ValidationException("ville", "Le ville est obligatoire");
        }
        this.ville = ville;
    }

    @Override
    public String toString() {
        return numeroRue + " " + nomRue + " " + codePostal + " " + ville;
    }

}
