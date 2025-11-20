/**
 * Package contenant les ViewModels de l'application.
 * <p>
 * Implémente le pattern MVVM en servant d'intermédiaire entre les vues (UI)
 * et les repositories (données). Les ViewModels orchestrent la logique de
 * présentation, les validations et la préparation des données pour l'affichage.
 * </p>
 *
 * <h2>ViewModels disponibles</h2>
 * <ul>
 *   <li>{@link main.com.julio.viewmodel.ClientViewModel} - Gestion des clients</li>
 *   <li>{@link main.com.julio.viewmodel.ProspectViewModel} - Gestion des prospects</li>
 *   <li>{@link main.com.julio.viewmodel.ContratViewModel} - Gestion des contrats</li>
 * </ul>
 *
 * <h2>Responsabilités des ViewModels</h2>
 * <ul>
 *   <li>Orchestrer les validations métier (unicité, formats, règles)</li>
 *   <li>Préparer les données pour affichage (DefaultTableModel, formatage)</li>
 *   <li>Gérer les associations bidirectionnelles (Client ↔ Contrat)</li>
 *   <li>Logger les erreurs et propager les exceptions</li>
 *   <li>Coordonner les appels aux repositories et services</li>
 * </ul>
 *
 * <h2>Pattern MVVM</h2>
 * <pre>
 * View (Swing) → ViewModel → Repository → Base de données
 *                    ↓
 *                 Service (validation, unicité, logging)
 * </pre>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see main.com.julio.view
 * @see main.com.julio.repository
 */
package main.com.julio.viewmodel;
