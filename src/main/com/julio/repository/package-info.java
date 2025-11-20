/**
 * Package contenant les repositories pour la gestion de la persistance des données.
 * <p>
 * Ce package implémente le pattern Repository pour isoler la logique d'accès
 * aux données du reste de l'application. Chaque classe de repository gère
 * la persistance d'un type d'entité spécifique en utilisant des collections
 * en mémoire.
 * </p>
 *
 * <h2>Repositories disponibles</h2>
 * <ul>
 *   <li>{@link main.com.julio.repository.ClientRepository} - Gestion des clients
 *       avec suppression en cascade des contrats associés</li>
 *   <li>{@link main.com.julio.repository.ProspectRepository} - Gestion des prospects
 *       avec tri par raison sociale via comparateur statique</li>
 *   <li>{@link main.com.julio.repository.ContratRepository} - Gestion des contrats
 *       avec recherche par client via findByClientId()</li>
 * </ul>
 *
 * <h2>Pattern Repository</h2>
 * <p>
 * Les repositories fournissent une abstraction pour l'accès aux données,
 * permettant de :
 * </p>
 * <ul>
 *   <li>Centraliser les opérations CRUD (Create, Read, Update, Delete)</li>
 *   <li>Isoler la logique métier de la logique de persistance</li>
 *   <li>Faciliter les tests unitaires via le mock des repositories</li>
 *   <li>Permettre un changement futur de technologie de persistance</li>
 * </ul>
 *
 * <h2>Implémentation actuelle</h2>
 * <p>
 * Les repositories utilisent des collections en mémoire ({@link java.util.List})
 * pour stocker les données. Cette approche simple permet de développer
 * et tester l'application sans dépendance à une base de données externe.
 * </p>
 *
 * <h2>Opérations CRUD communes</h2>
 * <p>
 * Toutes les repositories implémentent les méthodes suivantes :
 * </p>
 * <ul>
 *   <li><b>add(T entity)</b> - Ajoute une nouvelle entité</li>
 *   <li><b>update(T entity)</b> - Met à jour une entité existante</li>
 *   <li><b>delete(int id)</b> - Supprime une entité par ID (retourne boolean)</li>
 *   <li><b>findById(int id)</b> - Recherche une entité par ID</li>
 *   <li><b>findAll()</b> - Retourne toutes les entités (triées)</li>
 * </ul>
 *
 * <h2>Spécificités par repository</h2>
 *
 * <h2>Gestion des identifiants</h2>
 * <p>
 * Les entités utilisent des compteurs statiques auto-incrémentés :
 * </p>
 * <ul>
 *   <li>Client : compteurId initialisé à 1, incrémenté dans constructeur</li>
 *   <li>Prospect : compteurId initialisé à 1, incrémenté dans constructeur</li>
 *   <li>Contrat : compteurId initialisé à 1, incrémenté dans constructeur</li>
 *   <li>Adresse : compteurId initialisé à 1, incrémenté dans constructeur</li>
 * </ul>
 * <p>
 * Méthodes resetCompteur() disponibles pour tests et réinitialisation.
 * </p>
 *
 * <h2>Technologies utilisées</h2>
 * <ul>
 *   <li>{@link java.util.ArrayList} - Collection principale pour stockage</li>
 *   <li>{@link java.util.stream.Stream} - API Stream pour recherches et filtres</li>
 *   <li>{@link java.util.Comparator} - Tri personnalisé des entités</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see main.com.julio.model
 * @see main.com.julio.viewmodel
 */
package main.com.julio.repository;
