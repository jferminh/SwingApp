package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.util.DateUtils;

import java.time.LocalDate;

/**
 * Classe représentant un prospect dans le système de gestion.
 * <p>
 * Un prospect est une société potentiellement intéressée par les services
 * de l'entreprise. Cette classe gère les informations spécifiques comme
 * la date de prospection et le niveau d'intérêt manifesté.
 * Les identifiants des prospects sont générés automatiquement via un compteur
 * statique incrémental.
 * </p>
 * <p>
 * Contraintes métier :
 * </p>
 * <ul>
 *   <li>La date de prospection est obligatoire</li>
 *   <li>Le niveau d'intérêt (intéressé) est obligatoire</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Societe
 * @see Interesse
 */
public class Prospect extends Societe {

    /** Compteur statique pour générer automatiquement les identifiants des prospects */
    private static int compteurId = 1;

    /** Date à laquelle le prospect a été contacté ou identifié (obligatoire) */
    private LocalDate dateProspection;

    /** Intérêt manifesté par le prospect (obligatoire) */
    private Interesse interesse;

    /**
     * Constructeur principal de la classe Prospect.
     * <p>
     * Crée un nouveau prospect avec un identifiant auto-généré et valide
     * toutes les données métier. Le compteur d'identifiant est automatiquement
     * incrémenté après la création.
     * </p>
     *
     * @param raisonSociale raison sociale du prospect (ne peut pas être vide)
     * @param adresse adresse complète du prospect (ne peut pas être null)
     * @param telephone numéro de téléphone (doit respecter le format validé)
     * @param email adresse email (doit respecter le format validé)
     * @param commentaires notes additionnelles (peut être null ou vide)
     * @param dateProspection date de première prospection (ne peut pas être null)
     * @param interesse niveau d'intérêt du prospect (ne peut pas être null)
     * @throws ValidationException si une des validations échoue
     */
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

    /**
     * Retourne la date de prospection formatée selon le format défini dans DateUtils.
     * <p>
     * Cette méthode utilise le formatter configuré dans {@link DateUtils#FORMATTER}
     * pour convertir la date en chaîne de caractères lisible.
     * </p>
     *
     * @return la date de prospection formatée (ex: "19/11/2025")
     * @see DateUtils#FORMATTER
     */
    public String getDateProspectionFormatee() {
        return dateProspection.format(DateUtils.FORMATTER);
    }

    /**
     * Modifie la date de prospection avec validation métier.
     * La date de prospection est obligatoire et ne peut pas être null.
     *
     * @param dateProspection la nouvelle date de prospection
     * @throws ValidationException si la date est null
     */
    public void setDateProspection(LocalDate dateProspection) throws ValidationException {
        if (dateProspection == null) {
            throw new ValidationException("La date de prospection est obligatoire.");
        }
        this.dateProspection = dateProspection;
    }

    public Interesse getInteresse() {
        return interesse;
    }

    /**
     * Modifie l'intérêt du prospect avec validation métier.
     * Le champ 'intéressé' est obligatoire et ne peut pas être null.
     *
     * @param interesse Oui / Non
     * @throws ValidationException si Interesse est null
     * @see Interesse
     */
    public void setInteresse(Interesse interesse) throws ValidationException {
        if (interesse == null) {
            throw new ValidationException("Le champ 'intéressé' est obligatoire.");
        }
        this.interesse = interesse;
    }

    /**
     * Retourne le type de cette société.
     *
     * @return la chaîne "Prospect"
     */
    @Override
    public String getTypeSociete() {
        return "Prospect";
    }

    /**
     * Retourne une représentation textuelle du prospect.
     *
     * @return une chaîne au format "Raison Sociale (Prospect)"
     */
    @Override
    public String toString() {
        return getRaisonSociale() + " (Prospect)";
    }

    /**
     * Réinitialise le compteur d'identifiants des prospects à 1.
     * <p>
     * Cette méthode statique est généralement utilisée pour les tests.
     * </p>
     */
    public static void resetCompteur() {
        compteurId = 1;
    }
}
