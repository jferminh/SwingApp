/**
 * Package contenant les services métier et utilitaires de l'application.
 * <p>
 * Ce package regroupe les classes de services qui implémentent la logique
 * métier de l'application, ainsi que les utilitaires transverses comme
 * la validation des données et le formatage des logs.
 * </p>
 *
 * <h2>Services inclus</h2>
 * <ul>
 *   <li>{@link main.com.julio.service.ValidationService} - Validation des données métier
 *       (formats email, téléphone, code postal, etc.)</li>
 *   <li>{@link main.com.julio.service.FormatterLog} - Formatage personnalisé des logs
 *       pour la traçabilité applicative</li>
 *   <li>{@link main.com.julio.service.UnicityService} - Vérification de l'unicité des raisons
 *       sociales à travers clients et prospects</li>
 *   <li>{@link main.com.julio.service.LoggingService} - Configuration centralisée du système
 *       de logging avec écriture dans fichier</li>
 * </ul>
 *
 * <h2>Architecture en couches</h2>
 * <p>
 * Les services constituent la couche métier de l'application, située entre :
 * </p>
 * <ul>
 *   <li>La couche présentation (interfaces utilisateur)</li>
 *   <li>La couche persistance (repositories)</li>
 * </ul>
 * <p>
 * Ils orchestrent les opérations métier, valident les données et gèrent
 * les transactions entre les différents repositories.
 * </p>
 *
 *
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
package main.com.julio.service;
