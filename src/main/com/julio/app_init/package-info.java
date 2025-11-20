/**
 * Package d’amorçage de l’application (initialisation).
 * <p>
 * Contient le point d’entrée et la configuration de démarrage :
 * </p>
 * <ul>
 *   <li>Initialisation du logging (fichier, formatter, handlers)</li>
 *   <li>Initialisation du Look and Feel Swing</li>
 *   <li>Construction des repositories (données en mémoire)</li>
 *   <li>Création des services (unicité, etc.)</li>
 *   <li>Injection des ViewModels (MVVM)</li>
 *   <li>Lancement de la vue d’accueil sur l’EDT</li>
 * </ul>
 *
 * @author Julio
 * @version 1.0
 * @since 19/11/2025
 * @see main.com.julio.repository
 * @see main.com.julio.service
 * @see main.com.julio.viewmodel
 * @see main.com.julio.view
 */
package main.com.julio.app_init;
