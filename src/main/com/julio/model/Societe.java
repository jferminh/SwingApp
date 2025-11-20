package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.ValidationService;

/**
 * Classe abstraite représentant une société dans le système de gestion.
 * Cette classe encapsule les informations communes à toutes les sociétés
 * (clients et prospects) et assure la validation des données métier.
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public abstract class Societe {

    private int id;
    private String raisonSociale;
    private Adresse adresse;
    private String telephone;
    private String email;
    private String commentaires;

    /**
     * Constructeur principal de la classe Societe.
     * Initialise une société avec validation des données obligatoires.
     *
     * @param id identifiant unique de la société
     * @param raisonSociale raison sociale de la société (ne peut pas être vide)
     * @param adresse adresse complète de la société (ne peut pas être null)
     * @param telephone numéro de téléphone (doit respecter le format validé)
     * @param email adresse email (doit respecter le format validé)
     * @param commentaires notes additionnelles (peut être null ou vide)
     * @throws ValidationException si une des validations échoue
     */
    public Societe(int id, String raisonSociale, Adresse adresse, String telephone,
                   String email, String commentaires) throws ValidationException {
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

    /**
     * Modifie l'identifiant de la société.
     *
     * @param id le nouvel identifiant
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Modifie la raison sociale de la société avec validation.
     * La raison sociale ne peut pas être nulle ou vide.
     *
     * @param raisonSociale la nouvelle raison sociale
     * @throws ValidationException si la raison sociale est nulle ou vide
     */
    public void setRaisonSociale(String raisonSociale) throws ValidationException {
        if (ValidationService.isNullOrEmpty(raisonSociale)) {
            throw new ValidationException("La raison sociale est obligatoire.");
        }
        this.raisonSociale = raisonSociale;
    }

    /**
     * Modifie l'adresse de la société avec validation.
     * L'adresse ne peut pas être nulle.
     *
     * @param adresse la nouvelle adresse
     * @throws ValidationException si l'adresse est nulle
     */
    public void setAdresse(Adresse adresse) throws ValidationException {
        if (adresse == null) {
            throw new ValidationException("La adresse est obligatoire.");
        }
        this.adresse = adresse;
    }

    /**
     * Modifie le numéro de téléphone de la société avec validation du format.
     * Le format du téléphone est vérifié par le service de validation.
     *
     * @param telephone le nouveau numéro de téléphone
     * @throws ValidationException si le format du téléphone est invalide
     * @see ValidationService#isValidTelephone(String)
     */
    public void setTelephone(String telephone) throws ValidationException {
        if (!ValidationService.isValidTelephone(telephone)) {
            throw new ValidationException("Le format du téléphone est invalide.");
        }
        this.telephone = telephone;
    }

    /**
     * Modifie l'adresse email de la société avec validation du format.
     * Le format de l'email est vérifié par le service de validation.
     *
     * @param email la nouvelle adresse email
     * @throws ValidationException si le format de l'email est invalide
     * @see ValidationService#isValidEmail(String)
     */
    public void setEmail(String email) throws ValidationException {
        if (!ValidationService.isValidEmail(email)) {
            throw new ValidationException("Le format de l'email est invalide.");
        }
        this.email = email;
    }

    /**
     * Modifie les commentaires associés à la société.
     *
     * @param commentaires les nouveaux commentaires (peut être null)
     */
    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
    }

    /**
     * Retourne le type spécifique de société.
     * Cette méthode abstraite doit être implémentée par les sous-classes
     * pour identifier leur nature (Client, Prospect).
     *
     * @return une chaîne de caractères représentant le type de société
     */
    public abstract String getTypeSociete();

}
