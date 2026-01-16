package main.com.julio.dao;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour la gestion des clients en base de données.
 * Implémente le pattern Data Access Object (DAO) pour l'entité Client.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 14/01/2026
 */
public class ClientDAO extends SocieteDAO {

    private static final Logger LOGGER = LoggerUtil.getLogger(ClientDAO.class);
    private static final String ENTITY_NAME = "Client";

    /**
     * Constructeur qui récupère l'instance de DatabaseConnection.
     *
     * @throws DAOException si la connexion à la base de données échoue
     */
    public ClientDAO() throws DAOException {
//        try {
            super();
//            LOGGER.fine("ClientDAO initialisé avec succès");
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Échec de l'initialisation de ClientDAO", e);
//            throw new DAOException(
//                    DAOException.ErrorCode.CONNECTION_ERROR,
//                    "init",
//                    null,
//                    "Imposible d'initialiser ClientDAO : " + e.getMessage(),
//                    e
//            );
//        }
    }

    /**
     * Récupère tous les clients de la base de données.
     *
     * @return une liste de tous les clients
     * @throws DAOException si une erreur survient lors de la requête
     */
    public List<Client> findAll() throws DAOException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT s.id_societe, s.raison_sociale, s.adresse_id, s.telephone, " +
                "s.email, s.commentaires, " +
                "c.id_client, c.chiffre_affaires, c.nb_employes, " +
                "a.numero_rue, a.nom_rue, a.code_postal, a.ville " +
                "FROM Societe s " +
                "INNER JOIN client c ON s.id_societe = c.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse";

        try (Statement statement = dbConnexion.getConnection().createStatement();
        ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Client client = mapResultSetToClient(rs);
                clients.add(client);
            }
            LOGGER.log(Level.INFO, "Récupération de {0} clients" + clients.size());
            return clients;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de tous les clients", e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findAll",
                    null,
                    "Erreur lors de la récupération de tous les clients : " + e.getMessage(),
                    e
            );
        } catch (ValidationException ex) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping dans findAll", ex);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findAll",
                    null,
                    "Erreur de validation des données : " + ex.getMessage(),
                    ex
            );
        }

    }

    /**
     * Récupère un client par son identifiant.
     *
     * @param id l'identifiant du client
     * @return le client correspondant ou null si non trouvé
     * @throws DAOException si une erreur survient lors de la requête
     */
    public Client findById(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "findById",
                    id,
                    "L'ID doit être une entier positif non null"
            );
        }

        String query = "SELECT s.id_societe, s.raison_sociale, s.adresse_id, s.telephone, " +
                "s.email, s.commentaires, " +
                "c.id_client, c.chiffre_affaires, c.nb_employes, " +
                "a.numero_rue, a.nom_rue, a.code_postal, a.ville " +
                "FROM societe s " +
                "INNER JOIN client c ON s.id_societe = c.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse " +
                "WHERE c.id_client = ?";

        try (PreparedStatement statement = dbConnexion.getConnection().prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Client client = mapResultSetToClient(rs);
                    return client;
                } else {
                    LOGGER.log(Level.INFO, "Aucun client trouvé avec l'ID {0}", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de findById avec ID=" + id, e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findById",
                    id,
                    "Erreur lors de la recherche du client : " + e.getMessage(),
                    e
            );
        } catch (ValidationException ex) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping", ex);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findById",
                    id,
                    "Erreur de validation des données : " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Insère un nouveau client dans la base de données.
     * L'ID est généré automatiquement et affecté à l'objet.
     *
     * @param client le client à insérer
     * @return le client avec son ID généré
     * @throws DAOException si une erreur survient lors de l'insertion
     */
    public Client create(Client client) throws DAOException {
        if (client == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "create",
                    null,
                    "Le client ne peut pas être null"
            );
        }

        Connection connection = dbConnexion.getConnection();

        try {
            connection.setAutoCommit(false);

            // 1. Insérer la partie société
            Integer societeId = createSociete(client);

            // 2. Insérer la partie client
            String query = "INSERT INTO client (id_societe, chiffre_affaires, nb_employes) " +
                    " VALUES (?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, societeId);
                statement.setLong(2, client.getChiffreAffaires());
                statement.setInt(3, client.getNbEmployes());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("L'insértion du client a échoué, aucune ligne affectée");
                }

                connection.commit();
//                LOGGER.log(Level.INFO, "Client crée avec l'ID {0}", societeId);
                return client;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Erreur lors de la création de client, rollback effectué", e);

            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback",  rollbackEx);
            }
            String detailedMessage = analyzeSQLException(e);
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du client : " + detailedMessage);
            throw new DAOException(
                    categorizeSQLException(e),
                    "create",
                    client.getId(),
                    "Erreur lors de la création du client : " + detailedMessage,
                    e
            );
        } finally {
            try {
                connection.setAutoCommit(true);

            }catch (SQLException e){
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
        }
    }

    /**
     * Met à jour un client existant dans la base de données.
     *
     * @param client le client à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi, false sinon
     * @throws DAOException si une erreur survient lors de la mise à jour
     */
    public boolean save(Client client) throws DAOException {
        if (client == null || client.getId() == null || client.getId() <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    client != null ? client.getId() : null,
                    "Le client doit avoir un ID valide"
            );
        }
        Connection connection = dbConnexion.getConnection();

        try {
            connection.setAutoCommit(false);
            // 1. Mettre à jour la partie sociéte
            saveSociete(client);

            // 2. Mettre à jour la partie client
            String query = "UPDATE client SET chiffre_affaires = ?, nb_employes = ? " +
                    "WHERE id_client = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, client.getChiffreAffaires());
                statement.setInt(2, client.getNbEmployes());
                statement.setInt(3, client.getId());

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    LOGGER.log(Level.INFO, "Client mis à jour avec l'ID {0}", client.getId());
                    return true;
                } else {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Aucun client trouvé avec l'ID {0}", client.getId());
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Erreur lors de la mis à jour, rollback effectué", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback",  rollbackEx);
            }

            String detailedMessage = analyzeSQLException(e);
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du client ID= " + client.getId(), e);
            throw new DAOException(
                    categorizeSQLException(e),
                    "save",
                    client.getId(),
                    "Erreur lors de la mise à jour du client : " + detailedMessage,
                    e
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e){
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
        }
    }

    /**
     * Supprime un client de la base de données avec transaction.
     *
     * @param id l'identifiant du client à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws DAOException si une erreur survient ou si des contrats sont liés
     */
    public boolean delete(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "delete",
                    id,
                    "L'ID doit être un entier positif non null"
            );
        }

        Connection connection = dbConnexion.getConnection();
        try {
            connection.setAutoCommit(false);

            // Vérifier s'il existe des contrats liés à ce client
            String query = "SELECT COUNT(*) FROM contrat WHERE client_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        int count = rs.getInt(1);
                        connection.rollback();
                        String errorMsg = String.format(
                                "Impossible de supprimer le client ID %d : %d contrat(s) y sont liés. " +
                                "Supprimez d'abord les contrats associés.", id, count);
                        LOGGER.log(Level.SEVERE, errorMsg);
                        throw new DAOException(
                                DAOException.ErrorCode.FOREIGN_KEY_VIOLATION,
                                "delete",
                                id,
                                errorMsg + ". Supprimez d'abord les contrats associés."
                        );
                    }
                }
            }

            // Récupérer l'adresse_id avant de supprimer la société
            Integer adresseId = null;
            String getAdresseSQL = "SELECT s.adresse_id " +
                    "FROM client c " +
                    "INNER JOIN societe s ON c.id_societe = s.id_societe " +
                    "WHERE c.id_client = ?";
            try (PreparedStatement statement = connection.prepareStatement(getAdresseSQL)) {
                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        adresseId = rs.getInt("adresse_id");
                    }
                }
            }

            //Supprimer l'enregistrement client
            String deleteClientSql = "DELETE FROM client WHERE id_client = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteClientSql)) {
                statement.setInt(1, id);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected == 0) {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Aucun client trouvé avec l'ID {0}", id);
                    return false;
                }
            }


            // Supprimer l'enregistrement société
            String deleteSocieteSql = "DELETE FROM client WHERE id_client = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSocieteSql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }

            // Supprimer l'enregistrement adresse
            String deleteAdresseSql = "DELETE FROM adresse WHERE id_adresse = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteAdresseSql)) {
                statement.setInt(1, adresseId);
                statement.executeUpdate();
            }

            connection.commit();
            LOGGER.log(Level.INFO, "Client supprimé avec l'ID {0}", id);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Erreur lord de la suppression du client ID " + id, e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback",  rollbackEx);
            }

            String detailedMessage = analyzeSQLException(e);
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du client ID=" + id, e);

            throw new DAOException(
                    categorizeSQLException(e),
                    "delete",
                    id,
                    "Erreur lors de la suppression du client : " + detailedMessage,
                    e
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e){
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit",  e);
            }
        }
    }

    /**
     * Analyse une SQLException pour fournir un message d'erreur détaillé.
     */
    private String analyzeSQLException(SQLException e) {
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();

        if (sqlState != null) {
            // Codes SQL standard
            if (sqlState.startsWith("23")) {
                if (sqlState.equals("23000")) {
                    return "Violation de contrainte d'intégrité (vérifiez les clés étrangères)";
                } else if (sqlState.equals("23505")) {
                    return "Violation de contrainte d'unicité (valeur déjà existante)";
                }
                return "Violation de contrainte d'intégrité";
            } else if (sqlState.startsWith("42")) {
                return "Erreur de syntaxe SQL ou objet non trouvé";
            } else if (sqlState.startsWith("08")) {
                return "Problème de connexion à la base de données";
            }
        }

        return e.getMessage();
    }

    /**
     * Catégorise une SQLException en ErrorCode.
     */
    private DAOException.ErrorCode categorizeSQLException(SQLException e) {
        String sqlState = e.getSQLState();

        if (sqlState != null) {
            if (sqlState.startsWith("23")) {
                if (sqlState.contains("foreign")) {
                    return DAOException.ErrorCode.FOREIGN_KEY_VIOLATION;
                } else if (sqlState.contains("unique")) {
                    return DAOException.ErrorCode.UNIQUE_CONSTRAINT_VIOLATION;
                } else if (sqlState.contains("null")) {
                    return DAOException.ErrorCode.NOT_NULL_VIOLATION;
                }
                return DAOException.ErrorCode.CHECK_CONSTRAINT_VIOLATION;
            } else if (sqlState.startsWith("08")) {
                return DAOException.ErrorCode.CONNECTION_ERROR;
            }
        }

        return DAOException.ErrorCode.GENERAL_ERROR;
    }

    /**
     * Méthode utilitaire pour mapper un ResultSet vers un objet Client.
     *
     * @param rs le ResultSet contenant les données
     * @return l'objet Client créé
     * @throws SQLException si une erreur survient lors de la lecture du ResultSet
     * @throws ValidationException si les données ne respectent pas les règles métier
     */
    private Client mapResultSetToClient(ResultSet rs) throws SQLException, ValidationException {
        // Créer l'adresse
        Adresse adresse = new Adresse(
                rs.getString("numero_rue"),
                rs.getString("nom_rue"),
                rs.getString("code_postal"),
                rs.getString("ville")
                );
        adresse.setId(rs.getInt("id_societe"));

        // Créer le client
        Client client = new Client(
                rs.getString("raison_sociale"),
                adresse,
                rs.getString("telephone"),
                rs.getString("email"),
                rs.getString("commentaires"),
                rs.getLong("chiffre_affaires"),
                rs.getInt("nb_employes")
        );
        client.setId(rs.getInt("id_client"));
        return client;
    }
}
