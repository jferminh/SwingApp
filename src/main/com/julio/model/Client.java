package main.com.julio.model;

import main.com.julio.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant un client dans le système de gestion.
 * <p>
 * Un client est une société avec des informations spécifiques telles que
 * le chiffre d'affaires, le nombre d'employés et une liste de contrats associés.
 * Les identifiants des clients sont générés automatiquement via un compteur
 * statique incrémental.
 * </p>
 * <p>
 * Contraintes métier :
 * </p>
 * <ul>
 *   <li>Le chiffre d'affaires doit être supérieur ou égal à 200</li>
 *   <li>Le nombre d'employés doit être supérieur ou égal à 1</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Societe
 * @see Contrat
 */
public class Client extends Societe {

    /** Compteur statique pour générer automatiquement les identifiants des clients */
    private static int compteurId = 1;

    /** Chiffre d'affaires du client en euros (minimum 200) */
    private long chiffreAffaires;

    /** Nombre d'employés du client (minimum 1) */
    private int nbEmployes;

    /** Liste des contrats associés au client */
    private List<Contrat> contrats;

    /**
     * Constructeur principal de la classe Client.
     * <p>
     * Crée un nouveau client avec un identifiant auto-généré et valide
     * toutes les données métier. Le compteur d'identifiant est automatiquement
     * incrémenté après la création.
     * </p>
     *
     * @param raisonSociale raison sociale du client (ne peut pas être vide)
     * @param adresse adresse complète du client (ne peut pas être null)
     * @param telephone numéro de téléphone (doit respecter le format validé)
     * @param email adresse email (doit respecter le format validé)
     * @param commentaires notes additionnelles (peut être null ou vide)
     * @param chiffreAffaires chiffre d'affaires du client (doit être >= 200)
     * @param nbEmployes nombre d'employés du client (doit être >= 1)
     * @throws ValidationException si une des validations échoue
     */
    public Client(String raisonSociale, Adresse adresse, String telephone,
                  String email, String commentaires, long chiffreAffaires,
                  int nbEmployes) throws ValidationException {
        super(compteurId, raisonSociale, adresse, telephone, email, commentaires);
        setChiffreAffaires(chiffreAffaires);
        setNbEmployes(nbEmployes);
        compteurId = compteurId + 1;
        this.contrats = new ArrayList<>();
    }

    public long getChiffreAffaires() {
        return chiffreAffaires;
    }

    public int getNbEmployes() {
        return nbEmployes;
    }

    /**
     * Retourne une copie défensive de la liste des contrats du client.
     * Les modifications de la liste retournée n'affectent pas la liste interne.
     *
     * @return une nouvelle liste contenant les contrats du client
     */
    public List<Contrat> getContrats() {
        return new ArrayList<>(contrats);
    }

    /**
     * Modifie le chiffre d'affaires du client avec validation métier.
     * Le chiffre d'affaires doit être supérieur ou égal à 200.
     *
     * @param chiffreAffaires le nouveau chiffre d'affaires en euros
     * @throws ValidationException si le chiffre d'affaires est inférieur à 200
     */
    public void setChiffreAffaires(long chiffreAffaires) throws ValidationException {
        if (chiffreAffaires < 200) {
            throw new ValidationException("Le chiffre d'affaires doit être >= 200.");
        }
        this.chiffreAffaires = chiffreAffaires;
    }

    /**
     * Modifie le nombre d'employés du client avec validation métier.
     * Le nombre d'employés doit être supérieur ou égal à 1.
     *
     * @param nbEmployes le nouveau nombre d'employés
     * @throws ValidationException si le nombre d'employés est inférieur à 1
     */
    public void setNbEmployes(int nbEmployes) throws ValidationException {
        if (nbEmployes < 1) {
            throw new ValidationException("Le nombre d'employés doit être >= 1");
        }
        this.nbEmployes = nbEmployes;
    }

    /**
     * Ajoute un contrat à la liste des contrats du client.
     *
     * @param contrat le contrat à ajouter
     */
    public void ajouterContrat(Contrat contrat) {
        if (contrat != null && !contrats.contains(contrat)) {
            contrats.add(contrat);
        }
    }

    /**
     * Supprime un contrat de la liste des contrats du client.
     * <p>
     * Si le contrat n'existe pas dans la liste, la méthode n'a aucun effet.
     * </p>
     *
     * @param contrat le contrat à supprimer
     */
    public void supprimerContrat(Contrat contrat) {
        contrats.remove(contrat);
    }

    /**
     * Réinitialise le compteur d'identifiants des clients à 1.
     * <p>
     * Cette méthode statique est généralement utilisée pour les tests.
     * </p>
     */
    public static void resetCompteur() {
        compteurId = 1;
    }

    /**
     * Retourne une représentation textuelle du client.
     *
     * @return une chaîne au format "Raison Sociale (Client)"
     */
    @Override
    public String toString() {
        return getRaisonSociale() + " (Client)";
    }

    /**
     * Retourne le type de cette société.
     *
     * @return la chaîne "Client"
     */
    @Override
    public String getTypeSociete() {
        return "Client";
    }
}
