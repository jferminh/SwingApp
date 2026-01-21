package main.com.julio.viewmodel;

import main.com.julio.dao.ClientDAO;
import main.com.julio.dao.ContratDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.service.LoggerService;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel pour la gestion des clients.
 * Fait le lien entre la Vue et les DAO (pattern MVVM).
 * <p>
 * Cette classe orchestre les opérations CRUD sur les clients en utilisant
 * les DAO (ClientDAO, ContratDAO) et en gérant les exceptions
 * de manière appropriée pour la couche présentation.
 * <p>
 * Responsabilités :
 * 1. Validation des données d'entrée de l'utilisateur
 * 2. Orchestration des opérations DAO
 * 3. Transformation des exceptions DAO en exceptions métier
 * 4. Préparation des données pour l'affichage (TableModel)
 * 5. Logging des erreurs critiques uniquement
 * <p>
 * <strong>Note :</strong> Ce ViewModel ne gère PAS l'affichage UI directement.
 * Les vues (panels) sont responsables d'afficher les messages d'erreur.
 *
 * @author Julio FERMIN
 * @version 2.1
 * @since 21/01/2026
 */
public class ClientViewModel {
    private static final Logger LOGGER = LoggerService.getLogger(ClientViewModel.class);

    private final ClientDAO clientDAO;
    private final ContratDAO contratDAO;

    /**
     * Constructeur avec injection des DAO.
     *
     * @throws DAOException si l'initialisation des DAO échoue
     */
    public ClientViewModel() throws DAOException {
        this.clientDAO = new ClientDAO();
        this.contratDAO = new ContratDAO();
    }

    /**
     * Crée un nouveau client avec son adresse.
     * <p>
     * <strong>IMPORTANT :</strong> L'adresse est créée dans la même transaction
     * que le client via ClientDAO.create(). Si la création échoue, aucune donnée
     * n'est persistée (rollback complet).
     * </p>
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
     * @throws ValidationException si les données ne respectent pas les contraintes métier
     * @throws DAOException si une erreur survient lors de la persistance
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
                              int nbEmployes) throws ValidationException, DAOException {

        try {
            // Créer l'entité Client avec Adresse
            Adresse adresse = new main.com.julio.model.Adresse(
                    numeroRue,
                    nomRue,
                    codePostal,
                    ville
            );

            Client client = new Client(
                    raisonSociale,
                    adresse,
                    telephone,
                    email,
                    commentaires,
                    chiffreAffaires,
                    nbEmployes
            );

            // ClientDAO.create() gère TOUTE la transaction
            client = clientDAO.create(client);

            return client;

        } catch (ValidationException e) {
            throw e;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO création client : {0}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Modifie un client existant.
     * <p>
     * <strong>IMPORTANT :</strong> La modification de l'adresse est incluse
     * dans la transaction de ClientDAO.save(). Rollback complet en cas d'erreur.
     * </p>
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
     * @return true si la modification a réussi, false si le client n'existe pas
     * @throws ValidationException si les données ne respectent pas les contraintes métier
     * @throws DAOException si une erreur survient lors de la persistance
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
                                  int nbEmployes) throws ValidationException, DAOException {

        try {
            // Récupérer le client existant
            Client client = clientDAO.findById(id);

            if (client == null) {
                return false;
            }

            // Mettre à jour les données
            client.setRaisonSociale(raisonSociale);
            client.setTelephone(telephone);
            client.setEmail(email);
            client.setCommentaires(commentaires);
            client.setChiffreAffaires(chiffreAffaires);
            client.setNbEmployes(nbEmployes);

            // Mettre à jour l'adresse
            Adresse adresse = client.getAdresse();
            adresse.setNumeroRue(numeroRue);
            adresse.setNomRue(nomRue);
            adresse.setCodePostal(codePostal);
            adresse.setVille(ville);

            // Persister les modifications
            return clientDAO.save(client);

        } catch (ValidationException e) {
            throw e;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO modification client ID={0} : {1}",
                    new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Supprime un client.
     * <p>
     * <strong>IMPORTANT :</strong> La suppression est bloquée si le client possède des contrats.
     * La vérification est faite par ClientDAO.delete() qui lève une DAOException
     * avec ErrorCode.FOREIGN_KEY_VIOLATION.
     * </p>
     *
     * @param id identifiant du client à supprimer
     * @return true si la suppression a réussi, false si le client n'existe pas
     * @throws DAOException si une erreur survient (notamment si le client a des contrats)
     */
    public boolean supprimerClient(Integer id) throws DAOException {
        try {
            return clientDAO.delete(id);

        } catch (DAOException e) {
            throw e;
        }
    }

    /**
     * Récupère un client par son ID avec ses contrats chargés.
     *
     * @param id identifiant du client
     * @return le client ou null si non trouvé
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public Client getClientById(Integer id) throws DAOException {
        try {
            return clientDAO.findById(id);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur récupération client ID={0} : {1}",
                    new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Récupère tous les clients triés par raison sociale.
     *
     * @return liste de tous les clients (peut être vide)
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public List<Client> getTousLesClients() throws DAOException {
        try {
            return clientDAO.findAll();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur récupération clients : {0}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Construit un modèle de table Swing pour affichage des clients.
     * <p>
     * Crée un DefaultTableModel non-éditable avec colonnes :
     * ID, Raison Sociale, Adresse, Téléphone, Email, CA (€), Nb Employés, Nb Contrats
     * </p>
     *
     * @return modèle de table prêt pour JTable
     * @throws DAOException si une erreur survient lors de la récupération des clients
     */
    public DefaultTableModel construireTableModel() throws DAOException {
        String[] colonnes = {"ID", "Raison Sociale", "Adresse", "Téléphone",
                "Email", "CA (€)", "Nb Employés", "Nb Contrats"};

        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Client> clients = getTousLesClients();

        for (Client client : clients) {
            Object[] row = {
                    client.getId(),
                    client.getRaisonSociale(),
                    client.getAdresse().toString(),
                    client.getTelephone(),
                    client.getEmail(),
                    String.format("%,d €", client.getChiffreAffaires()),
                    client.getNbEmployes(),
                    client.getContrats().size()
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
     * </p>
     *
     * @return un tableau de clients (peut être vide)
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public Client[] getClientsForComboBox() throws DAOException {
        List<Client> clients = getTousLesClients();
        return clients.toArray(new Client[0]);
    }

    /**
     * Récupère le nombre de contrats d'un client.
     * <p>
     * Méthode utilitaire pour afficher rapidement le nombre de contrats
     * sans charger le client complet.
     * </p>
     *
     * @param clientId identifiant du client
     * @return le nombre de contrats
     * @throws DAOException si une erreur survient
     */
    public int getNombreContrats(Integer clientId) throws DAOException {
        try {
            return contratDAO.findByIdClient(clientId).size();
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur comptage contrats client ID={0}",
                    clientId);
            throw e;
        }
    }
}
