package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.ValidationService;

/**
 * Classe représentant un contrat commercial dans le système de gestion.
 * <p>
 * Un contrat est associé à un client et contient les informations essentielles
 * telles que le nom du contrat et son montant financier. Les identifiants
 * des contrats sont générés automatiquement via un compteur statique incrémental.
 * </p>
 * <p>
 * Contraintes métier :
 * <ul>
 *   <li>L'ID du client doit être strictement positif</li>
 *   <li>Le nom du contrat est obligatoire (non vide)</li>
 *   <li>Le montant doit être strictement positif</li>
 * </ul>
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Client
 */
public class Contrat {

    /** Compteur statique pour générer automatiquement les identifiants des contrats */
    private static int compteurId = 1;

    /** Identifiant unique du contrat */
    private int id;

    /** Identifiant du client auquel le contrat est associé (doit être > 0) */
    private int clientId;

    /** Nom ou désignation du contrat (obligatoire) */
    private String nomContrat;

    /** Montant financier du contrat en euros (doit être > 0) */
    private double montant;

    /**
     * Constructeur principal de la classe Contrat.
     * <p>
     * Crée un nouveau contrat avec un identifiant auto-généré et valide
     * toutes les données métier. Le compteur d'identifiant est automatiquement
     * incrémenté après la création.
     * </p>
     *
     * @param clientId identifiant du client propriétaire du contrat (doit être > 0)
     * @param nomContrat nom ou désignation du contrat (ne peut pas être vide)
     * @param montant montant financier du contrat en euros (doit être > 0)
     * @throws ValidationException si une des validations échoue
     */
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

    /**
     * Modifie l'identifiant du client associé au contrat avec validation métier.
     * L'ID du client doit être strictement positif (supérieur à zéro).
     *
     * @param clientId le nouvel identifiant du client
     * @throws ValidationException si l'ID du client est inférieur ou égal à 0
     */
    public void setClientId(int clientId) throws ValidationException {
        if (clientId <= 0) {
            throw new ValidationException("L'ID du client est obligatoire.");
        }
        this.clientId = clientId;
    }

    public String getNomContrat() {
        return nomContrat;
    }

    /**
     * Modifie le nom du contrat avec validation métier.
     * Le nom du contrat est obligatoire et ne peut pas être null ou vide.
     *
     * @param nomContrat le nouveau nom du contrat
     * @throws ValidationException si le nom est null ou vide
     * @see ValidationService#isNullOrEmpty(String)
     */
    public void setNomContrat(String nomContrat) throws ValidationException {
        if (ValidationService.isNullOrEmpty(nomContrat)) {
            throw new ValidationException("Le nom du contrat est obligatoire.");
        }
        this.nomContrat = nomContrat;
    }

    public double getMontant() {
        return montant;
    }

    /**
     * Modifie le montant du contrat avec validation métier.
     * Le montant doit être strictement positif (supérieur à zéro).
     *
     * @param montant le nouveau montant en euros
     * @throws ValidationException si le montant est inférieur ou égal à 0
     */
    public void setMontant(double montant) throws ValidationException {
        if (montant <= 0) {
            throw new ValidationException("Le montant doit être positif.");
        }
        this.montant = montant;
    }

    /**
     * Retourne une représentation textuelle du contrat.
     * <p>
     * Le format inclut le nom du contrat suivi du montant en euros.
     * </p>
     *
     * @return une chaîne au format "Nom Contrat (montant€)"
     */
    public String toString() {
        return nomContrat + " (" + montant + "€)";
    }
}
