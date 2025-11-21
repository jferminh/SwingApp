package main.com.julio.repository;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repository gérant la persistance et l'accès aux données des clients.
 * <p>
 * Cette classe implémente le pattern Repository pour centraliser toutes
 * les opérations CRUD (Create, Read, Update, Delete) sur les clients.
 * Elle maintient une collection en mémoire de tous les clients et assure
 * la cohérence avec les contrats associés via {@link ContratRepository}.
 * </p>
 * <p>
 * Le repository initialise automatiquement des données de démonstration
 * lors de sa création pour faciliter les tests et la démonstration de
 * l'application.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @see Client
 * @see ContratRepository
 * @since 19/11/2025
 */
public class ClientRepository {

    /**
     * Collection en mémoire contenant tous les clients
     */
    private List<Client> clients;

    /**
     * Référence vers le repository des contrats pour gérer les associations
     */
    private final ContratRepository contratRepo;

    /**
     * Comparateur statique pour trier les clients par raison sociale.
     * <p>
     * Utilise {@link Optional#ofNullable} pour gérer les raisons sociales nulles
     * en les traitant comme des chaînes vides, garantissant ainsi un tri stable
     * sans risque de {@link NullPointerException}.
     * </p>
     */
    public static final Comparator<Client> BY_RAISON_SOCIALE =
            Comparator.comparing((Client c) -> Optional.ofNullable(c.getRaisonSociale()).orElse(""));

    /**
     * Constructeur initialisant le repository avec des données de démonstration.
     * <p>
     * Crée un repository vide puis le peuple automatiquement avec 3 clients
     * de démonstration (IBM, Apple, Microsoft) et leurs contrats associés.
     * Cette initialisation facilite les tests et la démonstration de l'application.
     * </p>
     *
     * @param contratRepo repository des contrats pour gérer les associations
     * @throws ValidationException si les données de démonstration ne respectent pas les règles de validation
     */
    public ClientRepository(ContratRepository contratRepo) throws ValidationException {
        this.clients = new ArrayList<>();
        this.contratRepo = contratRepo;
        initialiserDonneesDemo();
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    /**
     * Ajoute un nouveau client au repository.
     * <p>
     * Le client est ajouté à la fin de la collection.
     * </p>
     *
     * @param client le client à ajouter (ne devrait pas être null)
     */
    public void add(Client client) {
        this.clients.add(client);
    }

    /**
     * Met à jour un client existant dans le repository.
     * <p>
     * Recherche le client par son identifiant et remplace l'ancienne instance
     * par la nouvelle. Les contrats associés ne sont pas automatiquement
     * mis à jour (gestion séparée via {@link ContratRepository}).
     * </p>
     *
     * @param client le client avec les nouvelles données (doit avoir un ID valide)
     */
    public void update(Client client) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == client.getId()) {
                this.clients.set(i, client);
                return;
            }
        }
    }

    /**
     * Supprime un client du repository ainsi que toutes ses dépendances.
     * <p>
     * Cette méthode effectue une suppression en cascade en trois étapes pour
     * garantir l'intégrité référentielle :
     * </p>
     * <ol>
     *   <li>Récupère tous les contrats associés au client via {@link ContratRepository#findByClientId(int)}</li>
     *   <li>Supprime chaque contrat du {@link ContratRepository}</li>
     *   <li>Nettoie la liste des contrats dans l'objet Client lui-même</li>
     *   <li>Supprime finalement le client de la collection</li>
     * </ol>
     * <p>
     * <b>Note importante :</b> L'utilisation de {@code new ArrayList<>(collection)} lors des
     * itérations évite les {@link java.util.ConcurrentModificationException} qui se produiraient
     * si on modifiait la collection pendant son parcours.
     * </p>
     *
     * @param id identifiant du client à supprimer
     * @return true si le client a été trouvé et supprimé (avec ses contrats), false si aucun client ne correspond
     * @see ContratRepository#findByClientId(int)
     * @see ContratRepository#delete(int)
     * @see Client#supprimerContrat (Contrat)
     */
    public boolean delete(int id) {
        Client existing = findById(id);

        // Nettoyage de la liste des contrats dans l'objet Client
        for (Contrat ct : new ArrayList<>(existing.getContrats())) {
            existing.supprimerContrat(ct);
        }

        // Suppression du client
        return clients.removeIf(client -> client.getId() == id);
    }


    /**
     * Recherche un client par son identifiant.
     * <p>
     * Utilise les Streams Java 8 pour rechercher efficacement le client.
     * </p>
     *
     * @param id identifiant du client recherché
     * @return le client trouvé ou null si aucun client ne correspond
     */
    public Client findById(int id) {
        return clients.stream()
                .filter(client -> client.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retourne tous les clients triés alphabétiquement par raison sociale.
     * <p>
     * Le tri est insensible à la casse (compareToIgnoreCase) pour garantir
     * un ordre cohérent indépendamment de la saisie utilisateur.
     * Retourne une nouvelle liste pour éviter les modifications non contrôlées.
     * </p>
     *
     * @return liste de tous les clients triés par raison sociale (A-Z)
     */
    public List<Client> findAll() {
        List<Client> copy = new ArrayList<>(clients);
        copy.sort(BY_RAISON_SOCIALE);
        return copy;
    }


    /**
     * Initialise le repository avec des données de démonstration.
     * <p>
     * Crée 3 clients fictifs avec leurs adresses et contrats associés :
     * </p>
     * <ul>
     *   <li><b>IBM</b> (Nancy) - CA: 5 000€, 10 employés, 2 contrats</li>
     *   <li><b>Apple</b> (Schoelcher) - CA: 50 000€, 100 employés, 3 contrats</li>
     *   <li><b>Microsoft</b> (Frouard) - CA: 500 000€, 1000 employés, 1 contrat</li>
     * </ul>
     * <p>
     * Cette méthode est appelée automatiquement par le constructeur.
     * </p>
     *
     * @throws ValidationException si les données de démonstration violent les règles métier
     */
    public void initialiserDonneesDemo() throws ValidationException {
        Adresse adresse1 = new Adresse(
                "10",
                "Victor Hugo",
                "54000",
                "Nancy"
        );
        Adresse adresse2 = new Adresse(
                "102",
                "Victor Duquesnay",
                "97233",
                "Schoelcher"
        );
        Adresse adresse3 = new Adresse(
                "25",
                "L'Esperance",
                "54390",
                "Frouard"
        );
        Client c1 = new Client(
                "IBM",
                adresse1,
                "0778663083",
                "ibm@ibm.com",
                "",
                5000,
                10
        );
        add(c1);

        Client c2 = new Client(
                "Apple",
                adresse2,
                "0778663083",
                "apple@apple.com",
                "",
                50000,
                100
        );
        add(c2);

        Client c3 = new Client(
                "Microsoft",
                adresse3,
                "0778663083",
                "microsoft@microsoft.com",
                "",
                500000,
                1000
        );
        add(c3);

        // Préchargement des contrats pour IBM
        prechargerContrats(c1.getId(),
                new Contrat(c1.getId(), "TMA ERP", 45000),
                new Contrat(c1.getId(), "Projet BI 2025", 82000)
        );

        // Préchargement des contrats pour Apple
        prechargerContrats(c2.getId(),
                new Contrat(c2.getId(), "Migration Cloud", 150000),
                new Contrat(c2.getId(), "Support Niveau 2", 36000),
                new Contrat(c2.getId(), "Audit Cybersécurité", 22000)
        );

        // Préchargement des contrats pour Microsoft
        prechargerContrats(c3.getId(),
                new Contrat(c3.getId(), "Refonte Site Web", 28000)
        );
    }

    /**
     * Méthode utilitaire privée pour précharger les contrats d'un client.
     * <p>
     * Pour chaque contrat fourni :
     * <ol>
     *   <li>Ajoute le contrat au {@link ContratRepository}</li>
     *   <li>Récupère le client correspondant</li>
     *   <li>Associe le contrat au client via {@link Client#ajouterContrat(Contrat)}</li>
     * </ol>
     * Cette méthode garantit la cohérence bidirectionnelle entre clients et contrats.
     * </p>
     *
     * @param clientId identifiant du client auquel associer les contrats
     * @param contrats varargs de contrats à précharger pour ce client
     */
    private void prechargerContrats(int clientId, Contrat... contrats) {
        for (Contrat ct : contrats) {
            contratRepo.add(ct);
            Client cli = findById(clientId);
            if (ct != null) {
                cli.ajouterContrat(ct);
            }
        }
    }
}
