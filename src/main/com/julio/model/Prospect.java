package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.LoggingService;
import main.com.julio.util.DateUtils;

import java.time.LocalDate;

public class Prospect extends Societe {
    private LocalDate dateProspection;
    private Interesse interesse;

    public Prospect(String raisonSociale, Adresse adresse, String telephone,
                    String email, String commentaires, LocalDate dateProspection,
                    Interesse interesse) {
        super(raisonSociale, adresse, telephone, email, commentaires);
        setDateProspection(dateProspection);
        setInteresse(interesse);
        LoggingService.log("Prospect créé : " + raisonSociale);
    }

    public LocalDate getDateProspection() {
        return dateProspection;
    }

    public String getDateProspectionFormatee() {
        return dateProspection.format(DateUtils.FORMATTER);
    }

    public void setDateProspection(LocalDate dateProspection) {
        if (dateProspection == null) {
            throw new ValidationException("dateProspection", "La date de prospection est obligatoire.");
        }
        this.dateProspection = dateProspection;
    }

    public Interesse getInteresse() {
        return interesse;
    }

    public void setInteresse(Interesse interesse) {
        if (interesse == null) {
            throw new ValidationException("intéressé", "Le champ 'intéressé' est obligatoire.");
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
}
