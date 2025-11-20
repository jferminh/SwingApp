package main.com.julio.repository;

import main.com.julio.model.Contrat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository gérant la persistance et l'accès aux données des contrats.
 * <p>
 * Cette classe implémente le pattern Repository pour centraliser toutes
 * les opérations CRUD (Create, Read, Update, Delete) sur les contrats.
 * Elle maintient une collection en mémoire de tous les contrats et fournit
 * des méthodes de recherche spécialisées pour retrouver les contrats associés
 * à un client particulier.
 * </p>
 * <p>
 * Contrairement aux autres repositories, celui-ci ne contient pas de données
 * de démonstration initiales. Les contrats sont créés et associés lors de
 * l'initialisation du {@link ClientRepository}.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Contrat
 * @see ClientRepository
 */
public class ContratRepository {

    /** Collection en mémoire contenant tous les contrats */
    private List<Contrat> contrats;

    /**
     * Constructeur initialisant le repository avec une collection vide.
     * <p>
     * Aucune donnée de démonstration n'est chargée. Les contrats sont ajoutés
     * lors de l'initialisation des clients via {@link ClientRepository#initialiserDonneesDemo()}.
     * </p>
     */
    public ContratRepository() {
        this.contrats = new ArrayList<>();
    }

    /**
     * Ajoute un nouveau contrat au repository.
     * <p>
     * Le contrat est ajouté à la fin de la collection.
     * </p>
     *
     * @param contrat le contrat à ajouter (ne devrait pas être null)
     */
    public void add(Contrat contrat) {
        contrats.add(contrat);
    }

    /**
     * Met à jour un contrat existant dans le repository.
     * <p>
     * Recherche le contrat par son identifiant et remplace l'ancienne instance
     * par la nouvelle.
     * </p>
     *
     * @param contrat le contrat avec les nouvelles données (doit avoir un ID valide)
     */
    public void update(Contrat contrat) {
        for (int i = 0; i < this.contrats.size(); i++) {
            if (this.contrats.get(i).getId() == contrat.getId()) {
                this.contrats.set(i, contrat);
                return;
            }
        }
    }

    /**
     * Supprime un contrat du repository par son identifiant.
     * <p>
     * Utilise {@link List#removeIf} pour supprimer le contrat correspondant.
     * Cette méthode est notamment utilisée lors de la suppression en cascade
     * d'un client via {@link ClientRepository#delete(int)}.
     * </p>
     *
     * @param id identifiant du contrat à supprimer
     * @return true si un contrat a été supprimé, false si aucun contrat ne correspond
     * @see ClientRepository#delete(int)
     */
    public boolean delete(int id) {
        return contrats.removeIf(contrat -> contrat.getId() == id);
    }

    /**
     * Recherche un contrat par son identifiant.
     * <p>
     * Utilise les Streams Java 8 pour rechercher efficacement le contrat.
     * </p>
     *
     * @param id identifiant du contrat recherché
     * @return le contrat trouvé ou null si aucun contrat ne correspond
     */
    public Contrat findById(int id) {
        return contrats.stream()
                .filter(contrat -> contrat.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Recherche tous les contrats associés à un client spécifique.
     * <p>
     * Filtre les contrats par l'identifiant du client et retourne une nouvelle
     * liste contenant uniquement les contrats correspondants. Cette méthode est
     * essentielle pour :
     * <ul>
     *   <li>Afficher les contrats d'un client dans l'interface utilisateur</li>
     *   <li>Effectuer la suppression en cascade lors de la suppression d'un client</li>
     *   <li>Calculer des statistiques par client (montants totaux, nombre de contrats)</li>
     * </ul>
     * </p>
     *
     * @param clientId identifiant du client dont on recherche les contrats
     * @return liste des contrats associés au client (vide si aucun contrat trouvé)
     * @see ClientRepository#delete(int)
     */
    public List<Contrat> findByClientId(int clientId) {
        return contrats.stream()
                .filter(contrat -> contrat.getClientId() == clientId)
                .collect(Collectors.toList());
    }
}
