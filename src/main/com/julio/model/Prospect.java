package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.util.DateUtils;

import java.time.LocalDate;

public class Prospect extends Societe {
    private static int compteurId = 1;
    private LocalDate dateProspection;
    private Interesse interesse;

    public Prospect(String raisonSociale, Adresse adresse, String telephone,
                    String email, String commentaires, LocalDate dateProspection,
                    Interesse interesse) throws ValidationException {
        super(compteurId, raisonSociale, adresse, telephone, email, commentaires);
        setDateProspection(dateProspection);
        setInteresse(interesse);
        compteurId++;
    }

    public LocalDate getDateProspection() {
        return dateProspection;
    }

    public String getDateProspectionFormatee() {
        return dateProspection.format(DateUtils.FORMATTER);
    }

    public void setDateProspection(LocalDate dateProspection) throws ValidationException {
        if (dateProspection == null) {
            throw new ValidationException("La date de prospection est obligatoire.");
        }
        this.dateProspection = dateProspection;
    }

    public Interesse getInteresse() {
        return interesse;
    }

    public void setInteresse(Interesse interesse) throws ValidationException {
        if (interesse == null) {
            throw new ValidationException("Le champ 'intéressé' est obligatoire.");
        }
        this.interesse = interesse;
    }

    @Override
    public String getTypeSociete() {
        return "Prospect";
    }

    @Override
    public String toString() {
        return getTypeSociete() + " (Prospect)";
    }

    public static void resetCompteur() {
        compteurId = 1;
    }
}
