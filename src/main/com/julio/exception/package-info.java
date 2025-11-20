/**
 * Package contenant les exceptions personnalisées de l'application.
 * <p>
 * Ce package regroupe toutes les exceptions métier et techniques utilisées
 * pour gérer les cas d'erreur spécifiques à l'application de gestion de sociétés.
 * </p>
 *
 * <h2>Architecture des exceptions</h2>
 * <p>
 * L'application utilise deux types d'exceptions selon la nature de l'erreur :
 * </p>
 *
 * <h3>Exceptions vérifiées (Checked Exceptions)</h3>
 * <ul>
 *   <li>{@link main.com.julio.exception.ValidationException} - Pour les erreurs
 *       de validation des données saisies par l'utilisateur. Ces erreurs sont
 *       prévisibles et récupérables (ex: format invalide, champ obligatoire manquant).
 *       L'utilisateur peut corriger sa saisie.</li>
 * </ul>
 *
 * <h3>Exceptions non vérifiées (Unchecked Exceptions)</h3>
 * <ul>
 *   <li>{@link main.com.julio.exception.NotFoundException} - Pour les erreurs
 *       système lorsqu'une ressource demandée n'existe pas (ex: client introuvable).
 *       Ces erreurs sont exceptionnelles et indiquent généralement un problème
 *       de logique applicative.</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
package main.com.julio.exception;
