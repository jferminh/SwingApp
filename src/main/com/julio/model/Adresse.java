package main.com.julio.model;

import main.com.julio.exception.ValidationException;
import main.com.julio.service.ValidationService;

/**
 * Classe représentant une adresse postale dans le système de gestion.
 * <p>
 * Une adresse contient tous les éléments nécessaires pour identifier
 * une localisation : numéro de rue, nom de rue, code postal et ville.
 * Les identifiants des adresses sont générés automatiquement via un compteur
 * statique incrémental.
 * </p>
 * <p>
 * Contraintes métier :
 * </p>
 * <ul>
 *   <li>Le numéro de rue est obligatoire (non vide)</li>
 *   <li>Le nom de rue est obligatoire (non vide)</li>
 *   <li>Le code postal doit contenir exactement 5 chiffres</li>
 *   <li>La ville est obligatoire (non vide)</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Societe
 */
public class Adresse {

    /** Compteur statique pour générer automatiquement les identifiants des adresses */
    private static int compteurId = 1;

    /** Identifiant unique de l'adresse */
    private int id;

    /** Numéro dans la rue (peut inclure bis, ter, etc.) - obligatoire */
    private String numeroRue;

    /** Nom de la voie (rue, avenue, boulevard, etc.) - obligatoire */
    private String nomRue;

    /** Code postal français à 5 chiffres - obligatoire et validé */
    private String codePostal;

    /** Nom de la ville - obligatoire */
    private String ville;

    /**
     * Constructeur principal de la classe Adresse.
     * <p>
     * Crée une nouvelle adresse avec un identifiant auto-généré et valide
     * tous les champs selon les règles métier. Le compteur d'identifiant est
     * automatiquement incrémenté après la création.
     * </p>
     *
     * @param numeroRue numéro de la rue (ne peut pas être vide)
     * @param nomRue nom de la voie (ne peut pas être vide)
     * @param codePostal code postal à 5 chiffres (doit respecter le format)
     * @param ville nom de la ville (ne peut pas être vide)
     * @throws ValidationException si une des validations échoue
     */
    public Adresse(String numeroRue, String nomRue, String codePostal, String ville) throws ValidationException {
        this.id = compteurId++;
        setNumeroRue(numeroRue);
        setNomRue(nomRue);
        setCodePostal(codePostal);
        setVille(ville);
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

    /**
     * Modifie le numéro de rue avec validation métier.
     * Le numéro de rue est obligatoire et ne peut pas être null ou vide.
     *
     * @param numeroRue le nouveau numéro de rue (ex: "12", "5 bis")
     * @throws ValidationException si le numéro de rue est null ou vide
     * @see ValidationService#isNullOrEmpty(String)
     */
    public void setNumeroRue(String numeroRue) throws ValidationException {
        if (ValidationService.isNullOrEmpty(numeroRue)) {
            throw new ValidationException("Le numéro de rue est obligatoire");
        }
        this.numeroRue = numeroRue;
    }

    public String getNomRue() {
        return nomRue;
    }

    /**
     * Modifie le nom de rue avec validation métier.
     * Le nom de rue est obligatoire et ne peut pas être null ou vide.
     *
     * @param nomRue le nouveau nom de rue (ex: "Rue de la Paix", "Avenue des Champs-Élysées")
     * @throws ValidationException si le nom de rue est null ou vide
     * @see ValidationService#isNullOrEmpty(String)
     */
    public void setNomRue(String nomRue) throws ValidationException {
        if (ValidationService.isNullOrEmpty(nomRue)) {
            throw new ValidationException("Le nom de rue est obligatoire");
        }
        this.nomRue = nomRue;
    }

    public String getCodePostal() {
        return codePostal;
    }

    /**
     * Modifie le code postal avec validation du format français.
     * <p>
     * Le code postal doit contenir exactement 5 chiffres consécutifs.
     * La validation est effectuée via {@link ValidationService#isValidCodePostal(String)}.
     * </p>
     *
     * @param codePostal le nouveau code postal (ex: "75001", "13008")
     * @throws ValidationException si le code postal ne contient pas exactement 5 chiffres
     * @see ValidationService#isValidCodePostal(String)
     */
    public void setCodePostal(String codePostal) throws ValidationException {
        if (!ValidationService.isValidCodePostal(codePostal)) {
            throw new ValidationException("Le code postal doit contenir exactement 5 chiffres");
        }
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    /**
     * Modifie le nom de la ville avec validation métier.
     * La ville est obligatoire et ne peut pas être null ou vide.
     *
     * @param ville le nouveau nom de la ville (ex: "Paris", "Marseille")
     * @throws ValidationException si la ville est null ou vide
     * @see ValidationService#isNullOrEmpty(String)
     */
    public void setVille(String ville) throws ValidationException {
        if (ValidationService.isNullOrEmpty(ville)) {
            throw new ValidationException("Le ville est obligatoire");
        }
        this.ville = ville;
    }

    /**
     * Retourne une représentation textuelle complète de l'adresse.
     * <p>
     * Le format retourné est standardisé pour l'affichage :
     * "numéro nom_rue code_postal ville"
     * </p>
     *
     * @return une chaîne représentant l'adresse complète (ex: "12 Rue de la Paix 75001 Paris")
     */
    @Override
    public String toString() {
        return numeroRue + " " + nomRue + " " + codePostal + " " + ville;
    }

    /**
     * Réinitialise le compteur d'identifiants des adresses à 1.
     * <p>
     * Cette méthode statique est généralement utilisée pour les tests.
     * </p>
     */
    public static void resetCompteur() {
        compteurId = 1;
    }
}
