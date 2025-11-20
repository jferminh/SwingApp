package main.com.julio.service;

import main.com.julio.util.RegexPatterns;

/**
 * Service utilitaire centralisé pour la validation des données métier.
 * <p>
 * Cette classe fournit un ensemble de méthodes statiques pour valider différents
 * types de données selon les règles métier et les formats standardisés de l'application.
 * Toutes les validations basées sur des expressions régulières utilisent les patterns
 * définis dans {@link RegexPatterns} pour garantir la cohérence et la maintenabilité.
 * </p>
 *
 * <p><b>Types de validations disponibles :</b></p>
 * <ul>
 *   <li>Champs obligatoires (non null et non vides)</li>
 *   <li>Codes postaux français (5 chiffres)</li>
 *   <li>Adresses email (format standard)</li>
 *   <li>Numéros de téléphone français (format mobile et fixe)</li>
 * </ul>
 *
 * <p><b>Architecture :</b></p>
 * <ul>
 *   <li>Classe utilitaire avec méthodes statiques uniquement</li>
 *   <li>Aucune instanciation nécessaire</li>
 *   <li>Délégation des regex à {@link RegexPatterns}</li>
 *   <li>Utilisée par les setters des classes du modèle pour valider les données</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see RegexPatterns
 * @see main.com.julio.exception.ValidationException
 */
public class ValidationService {

    /**
     * Vérifie si une chaîne de caractères est null ou vide.
     * <p>
     * Cette méthode utilise {@link String#isBlank()} (Java 11+) qui considère
     * une chaîne comme vide si elle ne contient que des espaces blancs
     * (espaces, tabulations, retours à la ligne, etc.).
     * </p>
     *
     * @param str la chaîne à vérifier
     * @return true si la chaîne est null ou ne contient que des espaces, false sinon
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Vérifie si un code postal respecte le format français.
     * <p>
     * Un code postal valide doit contenir exactement 5 chiffres consécutifs.
     * La validation utilise l'expression régulière définie dans
     * {@link RegexPatterns#CODE_POSTAL}.
     * </p>
     *
     * <p><b>Exemples valides :</b></p>
     * <ul>
     *   <li>"75001" (Paris)</li>
     *   <li>"54000" (Nancy)</li>
     *   <li>"13008" (Marseille)</li>
     * </ul>
     *
     * <p><b>Exemples invalides :</b></p>
     * <ul>
     *   <li>null → false</li>
     *   <li>"7500" (4 chiffres) → false</li>
     *   <li>"750001" (6 chiffres) → false</li>
     *   <li>"75O01" (lettre O au lieu de 0) → false</li>
     * </ul>
     *
     * @param codePostal le code postal à valider
     * @return true si le code postal est valide, false sinon
     * @see RegexPatterns#CODE_POSTAL
     */
    public static boolean isValidCodePostal(String codePostal) {
        return codePostal != null && codePostal.matches(RegexPatterns.CODE_POSTAL);
    }

    /**
     * Vérifie si une adresse email respecte le format standard.
     * <p>
     * Un email valide doit contenir un nom d'utilisateur, le symbole @,
     * un nom de domaine et une extension. La validation utilise l'expression
     * régulière définie dans {@link RegexPatterns#EMAIL}.
     * </p>
     *
     * <p><b>Exemples valides :</b></p>
     * <ul>
     *   <li>"utilisateur@exemple.com"</li>
     *   <li>"prenom.nom@entreprise.fr"</li>
     *   <li>"contact123@domaine.co.uk"</li>
     * </ul>
     *
     * <p><b>Exemples invalides :</b></p>
     * <ul>
     *   <li>null → false</li>
     *   <li>"utilisateur@" (domaine manquant) → false</li>
     *   <li>"@exemple.com" (nom manquant) → false</li>
     *   <li>"utilisateur.exemple.com" (@ manquant) → false</li>
     * </ul>
     *
     * @param email l'adresse email à valider
     * @return true si l'email est valide, false sinon
     * @see RegexPatterns#EMAIL
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches(RegexPatterns.EMAIL);
    }

    /**
     * Vérifie si un numéro de téléphone respecte le format français.
     * <p>
     * Un téléphone valide peut être au format mobile (06, 07) ou fixe (01-05, 09).
     * La validation utilise l'expression régulière définie dans
     * {@link RegexPatterns#TELEPHONE}.
     * </p>
     *
     * <p><b>Exemples valides :</b></p>
     * <ul>
     *   <li>"0612345678" (mobile, 10 chiffres)</li>
     *   <li>"0123456789" (fixe, 10 chiffres)</li>
     *   <li>Autres formats acceptés selon {@link RegexPatterns#TELEPHONE}</li>
     * </ul>
     *
     * <p><b>Exemples invalides :</b></p>
     * <ul>
     *   <li>null → false</li>
     *   <li>"061234567" (9 chiffres) → false</li>
     * </ul>
     *
     * <p><b>Note :</b> Le format exact accepté dépend de {@link RegexPatterns#TELEPHONE}.
     * Consultez cette classe pour connaître les variations acceptées (avec/sans espaces,
     * tirets, préfixe international, etc.).</p>
     *
     * @param telephone le numéro de téléphone à valider
     * @return true si le téléphone est valide, false sinon
     * @see RegexPatterns#TELEPHONE
     */
    public static boolean isValidTelephone(String telephone) {
        return telephone != null && telephone.matches(RegexPatterns.TELEPHONE);
    }
}
