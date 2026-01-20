package main.com.julio.viewmodel;

import main.com.julio.dao.AdresseDAO;
import main.com.julio.dao.ClientDAO;
import main.com.julio.dao.ContratDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.exception.NotFoundException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;
import main.com.julio.service.LoggerService;
import main.com.julio.service.UnicityService;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.com.julio.exception.DAOException.ErrorCode.*;
import static main.com.julio.service.LoggingService.LOGGER;

/**
 * ViewModel pour la gestion des clients.
 * Fait le lien entre la Vue et les DAO (pattern MVVM).
 * <p>
 * Cette classe orchestre les opérations CRUD sur les clients en utilisant
 * les DAO (ClientDAO, ContratDAO, AdresseDAO) et en gérant les exceptions
 * de manière appropriée pour la couche présentation.
 * <p>
 * Responsabilités :
 * 1 Validation des données d'entrée de l'utilisateur
 * 2 Orchestration des opérations DAO
 * 3 Transformation des exceptions DAO en messages utilisateur
 * 4 Préparation des données pour l'affichage (TableModel)
 * 5 Logging des opérations métier
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 20/01/2026
 */
public class ClientViewModel {
    private static final Logger LOGGER = LoggerService.getLogger(ClientViewModel.class);

    private final ClientDAO clientDAO;
    private final ContratDAO contratDAO;
    private final AdresseDAO adresseDAO;

    /**
     * Constructeur avec injection des DAO.
     *
     * @throws DAOException si l'initialisation des DAO échoue
     */
    public ClientViewModel() throws DAOException {
        this.clientDAO = new ClientDAO();
        this.contratDAO = new ContratDAO();
        this.adresseDAO = new AdresseDAO();
    }

    /**
     * Crée un nouveau client avec son adresse.
     * <p>
     * Processus :
     * 1. Valide les données d'entrée
     * 2. Crée l'adresse via AdresseDAO
     * 3. Crée le client via ClientDAO
     * 4. Logue l'opération
     *
     * @param raisonSociale raison sociale du client
     * @param numeroRue numéro de rue
     * @param nomRue nom de rue
     * @param codePostal code postal (5 chiffres)
     * @param ville ville
     * @param telephone téléphone (format validé)
     * @param email email (format validé)
     * @param commentaires commentaires optionnels
     * @param chiffreAffaires chiffre d'affaires (>= 200)
     * @param nbEmployes nombre d'employés (>= 1)
     * @return le client créé avec son ID généré
     * @throws IllegalArgumentException si les données sont invalides
     * @throws RuntimeException si une erreur DAO survient
     */
    public Client creerClient(String raisonSociale,
                            String numeroRue,
                            String nomRue,
                            String codePostal,
                            String ville,
                            String telephone,
                            String email,
                            String commentaires,
                            long chiffreAffaires,
                            int nbEmployes) {
        try {
            // 1. Créer et persister l'adresse
            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);
            adresse = adresseDAO.create(adresse);
            LOGGER.log(Level.FINE, "Adresse créée avec ID={0}", adresse.getId());

            // 2. Créer et persister le client
            Client client = new Client(
                    raisonSociale,
                    adresse,
                    telephone,
                    email,
                    commentaires,
                    chiffreAffaires,
                    nbEmployes
            );
            client = clientDAO.create(client);

            LOGGER.log(Level.INFO,
                    "Client créé avec succès : ID={0}, Raison sociale={1}",
                    new Object[]{client.getId(), raisonSociale});

            return client;

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation lors de la création du client", e);
            throw new IllegalArgumentException(
                    "Données invalides : " + e.getMessage(), e
            );
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur DAO lors de la création du client", e);

            // Analyser le type d'erreur pour un message utilisateur approprié
            String messageUtilisateur = switch (e.getErrorCode()) {
                case UNIQUE_CONSTRAINT_VIOLATION ->
                        "Cette raison sociale existe déjà.";
                case FOREIGN_KEY_VIOLATION ->
                        "Erreur de référence dans la base de données.";
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                default ->
                        "Erreur lors de la création du client : " + e.getMessage();
            };

            throw new RuntimeException(messageUtilisateur, e);
        }
    }

    /**
     * Modifie un client existant.
     * <p>
     * Processus :
     * 1. Récupère le client existant
     * 2. Met à jour les données
     * 3. Met à jour l'adresse via AdresseDAO
     * 4. Met à jour le client via ClientDAO
     *
     * @param id identifiant du client
     * @param raisonSociale nouvelle raison sociale
     * @param numeroRue nouveau numéro de rue
     * @param nomRue nouveau nom de rue
     * @param codePostal nouveau code postal
     * @param ville nouvelle ville
     * @param telephone nouveau téléphone
     * @param email nouvel email
     * @param commentaires nouveaux commentaires
     * @param chiffreAffaires nouveau chiffre d'affaires
     * @param nbEmployes nouveau nombre d'employés
     * @return true si la modification a réussi
     * @throws IllegalArgumentException si les données sont invalides ou si le client n'existe pas
     * @throws RuntimeException si une erreur DAO survient
     */
    public boolean modifierClient(Integer id,
                               String raisonSociale,
                               String numeroRue,
                               String nomRue,
                               String codePostal,
                               String ville,
                               String telephone,
                               String email,
                               String commentaires,
                               long chiffreAffaires,
                               int nbEmployes) throws ValidationException, NotFoundException {
        try {
            // 1. Récupérer le client existant
            Client client = clientDAO.findById(id);

            if (client == null) {
                LOGGER.log(Level.WARNING, "Client ID={0} introuvable pour modification", id);
                throw new IllegalArgumentException("Client introuvable avec l'ID " + id);
            }

            // 2. Mettre à jour les données du client
            client.setRaisonSociale(raisonSociale);
            client.setTelephone(telephone);
            client.setEmail(email);
            client.setCommentaires(commentaires);
            client.setChiffreAffaires(chiffreAffaires);
            client.setNbEmployes(nbEmployes);

            // 3. Mettre à jour l'adresse
            Adresse adresse = client.getAdresse();
            adresse.setNumeroRue(numeroRue);
            adresse.setNomRue(nomRue);
            adresse.setCodePostal(codePostal);
            adresse.setVille(ville);

            adresseDAO.save(adresse);

            // 4. Sauvegarder le client
            boolean success = clientDAO.save(client);

            if (success) {
                LOGGER.log(Level.INFO, "Client modifié avec succès : ID={0}", id);
            }

            return success;



        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation lors de la modification du client ID=" + id, e);
            throw new IllegalArgumentException(
                    "Données invalides : " + e.getMessage(), e
            );
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur DAO lors de la modification du client ID=" + id, e);

            String messageUtilisateur = switch (e.getErrorCode()) {
                case ENTITY_NOT_FOUND ->
                        "Client introuvable.";
                case UNIQUE_CONSTRAINT_VIOLATION ->
                        "Cette raison sociale existe déjà.";
                default ->
                        "Erreur lors de la modification du client : " + e.getMessage();
            };

            throw new RuntimeException(messageUtilisateur, e);
        }
    }

    /**
     * Supprime un client.
     * <p>
     * IMPORTANT : La suppression est bloquée si le client possède des contrats.
     *
     * @param id identifiant du client à supprimer
     * @return true si la suppression a réussi
     * @throws IllegalArgumentException si le client possède des contrats
     * @throws RuntimeException si une erreur DAO survient
     */
    public boolean supprimerClient(Integer id) {
        try {
            boolean success = clientDAO.delete(id);

            if (success) {
                LOGGER.log(Level.INFO, "Client supprimé avec succès : ID={0}", id);
            } else {
                LOGGER.log(Level.WARNING, "Aucun client trouvé avec l'ID {0} pour suppression", id);
            }

            return success;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du client ID=" + id, e);

            String messageUtilisateur = switch (e.getErrorCode()) {
                case FOREIGN_KEY_VIOLATION -> {
                    // Extraire le nombre de contrats du message d'erreur
                    String msg = e.getMessage();
                    if (msg.contains("contrat(s)")) {
                        yield msg; // Message déjà formaté par ClientDAO
                    }
                    yield "Impossible de supprimer le client : des contrats sont associés.";
                }
                case ENTITY_NOT_FOUND ->
                        "Client introuvable.";
                default ->
                        "Erreur lors de la suppression : " + e.getMessage();
            };

            throw new RuntimeException(messageUtilisateur, e);
        }
    }

    /**
     * Récupère un client par son ID avec ses contrats chargés.
     *
     * @param id identifiant du client
     * @return le client ou null si non trouvé
     * @throws RuntimeException si une erreur DAO survient
     */
    public Client getClientById(Integer id) {
        try {
            Client client = clientDAO.findById(id);

            if (client != null) {
                LOGGER.log(Level.FINE,
                        "Client trouvé : ID={0}, {1} contrat(s)",
                        new Object[]{id, client.getContrats().size()});
            }

            return client;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du client ID=" + id, e);
            throw new RuntimeException(
                    "Erreur lors de la récupération du client : " + e.getMessage(), e
            );
        }
    }

    /**
     * Récupère tous les clients triés par raison sociale.
     *
     * @return liste de tous les clients (peut être vide)
     * @throws RuntimeException si une erreur DAO survient
     */
    public List<Client> getTousLesClients() {

        try {
            List<Client> clients = clientDAO.findAll();

            LOGGER.log(Level.INFO, "{0} client(s) récupéré(s)", clients.size());

            return clients;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des clients", e);
            throw new RuntimeException(
                    "Erreur lors de la récupération de la liste des clients : " + e.getMessage(), e
            );
        }
    }

    /**
     * Construit un modèle de table Swing pour affichage des clients.
     * <p>
     * Crée un DefaultTableModel non-éditable avec colonnes :
     * ID, Raison Sociale, Adresse, Téléphone, Email, CA (€), Nb Employés
     * </p>
     *
     * @return modèle de table prêt pour JTable
     */
    public DefaultTableModel construireTableModel() {
        String[] colonnes = {"ID", "Raison Sociale", "Adresse", "Téléphone",
                "Email", "CA (€)", "Nb Employés"};

        // Modèle non-éditable via override isCellEditable
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Toutes cellules en lecture seule
            }
        };

        // Remplissage avec données clients
        List<Client> clients = getTousLesClients();
        for (Client client : clients) {
            Object[] row = {
                    client.getId(),
                    client.getRaisonSociale(),
                    client.getAdresse().toString(),  // Formatage adresse
                    client.getTelephone(),
                    client.getEmail(),
                    String.format("%,d €", client.getChiffreAffaires()),
                    client.getNbEmployes()
            };
            model.addRow(row);
        }
        return model;
    }

    /**
     * Retourne un tableau de clients pour utilisation dans un JComboBox.
     * <p>
     * Les clients sont triés par raison sociale.
     * La méthode toString() de Client retourne "Raison Sociale (Client)".
     *
     * @return un tableau de clients (peut être vide)
     */
    public Client[] getClientsForComboBox() {
        List<Client> clients = getTousLesClients();
        return clients.toArray(new Client[0]);
    }
}
