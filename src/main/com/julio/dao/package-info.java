/**
 * Classes DAO (Data Access Object) pour la persistance MySQL.
 * <p>
 * Implémente le pattern DAO avec transactions ACID, gestion d'erreurs typées
 * et analyse avancée des exceptions SQL.
 * </p>
 *
 * <h2>Classes Principales</h2>
 * <ul>
 *   <li>{@link main.com.julio.dao.ClientDAO} - CRUD clients</li>
 *   <li>{@link main.com.julio.dao.ProspectDAO} - CRUD prospects</li>
 *   <li>{@link main.com.julio.dao.ContratDAO} - CRUD contrats</li>
 *   <li>{@link main.com.julio.dao.AdresseDAO} - Opérations adresses (protected)</li>
 *   <li>{@link main.com.julio.dao.SocieteDAO} - Opérations sociétés (protected)</li>
 * </ul>
 *
 * <h2>Gestion des Transactions</h2>
 * <p>
 * Les opérations d'écriture utilisent des transactions pour garantir l'intégrité :
 * </p>
 * <pre>
 * connection.setAutoCommit(false);
 * try {
 *     // Opérations multiples (INSERT adresse, societe, client)
 *     connection.commit();
 * } catch (SQLException e) {
 *     connection.rollback();
 *     throw new DAOException(...);
 * }
 * </pre>
 *
 * <h2>Exceptions</h2>
 * <p>
 * Toutes les méthodes lancent {@link main.com.julio.exception.DAOException}
 * avec codes d'erreur spécifiques :
 * </p>
 * <ul>
 *   <li><b>CONNECTION_ERROR</b> - Erreur connexion MySQL</li>
 *   <li><b>UNIQUE_CONSTRAINT_VIOLATION</b> - Doublon (raison sociale)</li>
 *   <li><b>FOREIGN_KEY_VIOLATION</b> - Contrainte FK (contrats associés)</li>
 *   <li><b>ENTITY_NOT_FOUND</b> - Entité introuvable</li>
 *   <li><b>INVALID_DATA</b> - Données BDD invalides</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 * @see main.com.julio.exception.DAOException
 */
package main.com.julio.dao;