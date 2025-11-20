/**
 * Package contenant les vues (interfaces graphiques Swing) de l'application.
 * <p>
 * Ce package implémente la couche présentation selon le pattern MVVM (Model-View-ViewModel).
 * Chaque vue est responsable uniquement de l'affichage et de la capture des interactions
 * utilisateur, déléguant la logique métier aux ViewModels.
 * </p>
 *
 * <h2>Vues disponibles</h2>
 * <ul>
 *   <li>{@link main.com.julio.view.AccueilView} - Écran principal avec sélection
 *       du type d'entité (Client/Prospect) et accès aux opérations</li>
 *   <li>{@link main.com.julio.view.ListeView} - Affichage tabulaire des clients
 *       ou prospects avec actions CRUD</li>
 *   <li>{@link main.com.julio.view.FormulaireView} - Formulaire universel pour
 *       création/modification/suppression de clients et prospects</li>
 *   <li>{@link main.com.julio.view.ListeContratsView} - Affichage et gestion
 *       des contrats d'un client spécifique</li>
 * </ul>
 *
 * <h2>Architecture MVVM</h2>
 * <p>
 * Les vues suivent le pattern MVVM :
 * </p>
 * <ul>
 *   <li><b>View</b> : classes de ce package (interfaces Swing)</li>
 *   <li><b>ViewModel</b> : {@link main.com.julio.viewmodel} (logique présentation)</li>
 *   <li><b>Model</b> : {@link main.com.julio.model} (entités métier)</li>
 * </ul>
 *
 * <h2>Caractéristiques communes</h2>
 * <p>
 * Toutes les vues partagent ces caractéristiques :
 * </p>
 * <ul>
 *   <li>Héritent de {@link javax.swing.JFrame}</li>
 *   <li>Reçoivent les ViewModels via injection constructeur</li>
 *   <li>Utilisent {@link main.com.julio.util.DisplayDialog} pour messages utilisateur</li>
 *   <li>Gèrent la navigation avec dispose() et création nouvelle vue</li>
 *   <li>Intègrent bouton "Quitter" avec log avant System.exit()</li>
 *   <li>Fenêtres non redimensionnables et centrées</li>
 * </ul>
 *
 * <h2>Navigation entre vues</h2>
 * <pre>
 * AccueilView (hub central)
 *     ├─> ListeView (liste clients ou prospects)
 *     │       ├─> FormulaireView (CRUD)
 *     │       └─> ListeContratsView (contrats client)
 *     └─> FormulaireView (CRUD direct)
 *             └─> ListeContratsView (depuis modification client)
 * </pre>
 *
 * <h2>Gestion des formulaires</h2>
 * <p>
 * {@link main.com.julio.view.FormulaireView} est un formulaire universel qui s'adapte :
 * </p>
 * <ul>
 *   <li><b>Type entité</b> : affiche champs Client ou Prospect</li>
 *   <li><b>Action</b> : Créer (champs vides), Modifier (pré-remplis), Supprimer (lecture seule)</li>
 *   <li><b>Origin</b> : retourne à la vue d'origine après validation</li>
 * </ul>
 *
 * <h2>Gestion des erreurs</h2>
 * <p>
 * Les vues capturent et affichent les exceptions :
 * </p>
 * <ul>
 *   <li>{@link main.com.julio.exception.ValidationException} - Erreurs de validation</li>
 *   <li>{@link java.lang.NumberFormatException} - Erreurs de parsing numérique</li>
 *   <li>{@link java.time.DateTimeException} - Erreurs de format date</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see main.com.julio.viewmodel
 * @see main.com.julio.util.DisplayDialog
 */
package main.com.julio.view;
