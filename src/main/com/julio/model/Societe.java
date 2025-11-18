package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.ValidationService;

public abstract class Societe {
//    private static int compteurId = 1;
    private int id;
    private String raisonSociale;
    private Adresse adresse;
    private String telephone;
    private String email;
    private String commentaires;

    public Societe(int id, String raisonSociale, Adresse adresse, String telephone,
                   String email, String commentaires) throws ValidationException {
//        this.id = compteurId++;
        this.id = id;
        setRaisonSociale(raisonSociale);
        setAdresse(adresse);
        setTelephone(telephone);
        setEmail(email);
        this.commentaires = commentaires;
    }

    public int getId() {
        return id;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getCommentaires() {
        return commentaires;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRaisonSociale(String raisonSociale) throws ValidationException {
        if (ValidationService.isNullOrEmpty(raisonSociale)) {
            throw new ValidationException("La raison sociale est obligatoire.");
        }
        this.raisonSociale = raisonSociale;
    }

    public void setAdresse(Adresse adresse) throws ValidationException {
        if (adresse == null) {
            throw new ValidationException("La adresse est obligatoire.");
        }
        this.adresse = adresse;
    }

    public void setTelephone(String telephone) throws ValidationException {
        if (!ValidationService.isValidTelephone(telephone)) {
            throw new ValidationException("Le format du téléphone est invalide.");
        }
        this.telephone = telephone;
    }

    public void setEmail(String email) throws ValidationException {
        if (!ValidationService.isValidEmail(email)) {
            throw new ValidationException("Le format de l'email est invalide.");
        }
        this.email = email;
    }

    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
    }

    public abstract String getTypeSociete();

//    public static void resetCompteur() {
//        compteurId = 1;
//    }
}
