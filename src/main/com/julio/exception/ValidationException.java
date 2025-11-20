package main.com.julio.exception;

/**
 * Exception vérifiée levée lors d'une erreur de validation des données métier.
 * <p>
 * Cette exception hérite de {@link Exception} (exception vérifiée) et est utilisée
 * pour signaler que des données fournies par l'utilisateur ne respectent pas
 * les règles de validation métier de l'application (formats invalides, valeurs
 * manquantes ou hors limites, etc.).
 * </p>
 * <p>
 * En tant qu'exception vérifiée, elle doit être déclarée explicitement dans
 * les signatures de méthodes avec throws, forçant ainsi les développeurs à
 * gérer ces cas d'erreur prévisibles et récupérables.
 * </p>
 *
 * <p><b>Exemples d'utilisation :</b></p>
 * <pre>
 * // Validation d'une raison sociale vide
 * throw new ValidationException("La raison sociale est obligatoire.");
 *
 * // Validation d'un format d'email invalide
 * throw new ValidationException("Le format de l'email est invalide.");
 *
 * // Validation d'un chiffre d'affaires insuffisant
 * throw new ValidationException("Le chiffre d'affaires doit être >= 200.");
 * </pre>
 *
 * <p><b>Contextes d'utilisation :</b></p>
 * <ul>
 *   <li>Validation des champs obligatoires (raison sociale, adresse, etc.)</li>
 *   <li>Validation des formats (email, téléphone, code postal)</li>
 *   <li>Validation des contraintes métier (montants, quantités, dates)</li>
 *   <li>Validation des références entre entités (IDs valides)</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Exception
 * @see main.com.julio.service.ValidationService
 */
public class ValidationException extends Exception {

    /**
     * Constructeur créant une exception avec un message descriptif.
     * <p>
     * Le message devrait clairement identifier la règle de validation
     * non respectée pour permettre à l'utilisateur de corriger sa saisie.
     * Il est recommandé d'utiliser des messages explicites et en français
     * pour faciliter la compréhension par l'utilisateur final.
     * </p>
     *
     * @param message message décrivant l'erreur de validation rencontrée
     */
    public ValidationException(String message) {
        super(message);
    }
}

