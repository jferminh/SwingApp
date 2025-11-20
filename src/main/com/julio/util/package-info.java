/**
 * Package contenant les classes utilitaires de l'application.
 * <p>
 * Ce package regroupe des utilitaires transverses utilisés dans toute
 * l'application pour standardiser le traitement de certains types de données,
 * l'affichage des messages utilisateur et éviter la duplication de code.
 * </p>
 *
 * <h2>Utilitaires disponibles</h2>
 * <ul>
 *   <li>{@link main.com.julio.util.DateUtils} - Gestion et formatage standardisé
 *       des dates au format français (dd/MM/yyyy)</li>
 *   <li>{@link main.com.julio.util.RegexPatterns} - Centralisation des expressions
 *       régulières pour la validation des formats (email, téléphone, code postal)</li>
 *   <li>{@link main.com.julio.util.DisplayDialog} - Affichage simplifié de boîtes
 *       de dialogue Swing (info, warning, error)</li>
 * </ul>
 *
 * <h2>Séparation des responsabilités</h2>
 * <ul>
 *   <li><b>DateUtils</b> : manipulation temporelle (parsing, formatage)</li>
 *   <li><b>RegexPatterns</b> : validation de formats (règles métier)</li>
 *   <li><b>DisplayDialog</b> : interaction utilisateur (présentation)</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
package main.com.julio.util;