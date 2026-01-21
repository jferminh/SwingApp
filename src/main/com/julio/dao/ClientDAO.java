package main.com.julio.dao;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;
import main.com.julio.service.LoggerService;
import main.com.julio.util.SQLExceptionAnalyzer;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour la gestion des clients en base de données.
 * Implémente le pattern Data Access Object (DAO) pour l'entité Client.
 * <p>
 * Cette classe gère les opérations CRUD sur les clients et leurs relations
 * avec les adresses et les contrats, en utilisant des transactions pour
 * garantir l'intégrité des données.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 */
public class ClientDAO extends SocieteDAO {

    private static final Logger LOGGER = LoggerService.getLogger(ClientDAO.class);
    private final ContratDAO contratDAO;

    /**
     * Constructeur qui récupère l'instance de DatabaseConnection.
     * Initialise également le ContratDAO pour gérer les contrats associés.
     *
     * @throws DAOException si la connexion à la base de données échoue
     */
    public ClientDAO() throws DAOException {
        super();
        this.contratDAO = new ContratDAO();

    }

    /**
     * Récupère tous les clients de la base de données avec leurs adresses et contrats.
     * <p>
     * Effectue une jointure entre les tables societe, client, adresse et contrat
     * pour récupérer toutes les informations en une seule requête.
     *
     * @return une liste de tous les clients
     * @throws DAOException si une erreur survient lors de la requête
     */
    public List<Client> findAll() throws DAOException {
        Map<Integer, Client> clientsMap = new LinkedHashMap<>();

        String sql = "SELECT " +
                "    s.id_societe, s.raison_sociale, s.adresse_id, " +
                "    s.telephone, s.email, s.commentaires, " +
                "    c.id_client, c.chiffre_affaires, c.nb_employes, " +
                "    a.numero_rue, a.nom_rue, a.code_postal, a.ville, " +
                "    ct.id_contrat, ct.nom_contrat, ct.montant " +
                "FROM societe s " +
                "INNER JOIN client c ON s.id_societe = c.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse " +
                "LEFT JOIN contrat ct ON c.id_client = ct.client_id " +
                "ORDER BY s.raison_sociale ASC";

        // ✅ SOLUTION : Ne PAS utiliser try-with-resources sur la connexion
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Connection conn = dbConnexion.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Integer clientId = rs.getInt("id_client");

                Client client = clientsMap.get(clientId);

                if (client == null) {
                    try {
                        client = mapResultSetToClient(rs);
                        clientsMap.put(clientId, client);
                    } catch (ValidationException e) {
                        throw new DAOException(
                                DAOException.ErrorCode.INVALID_PARAMETER,
                                "findAll",
                                clientId,
                                "Données invalides : " + e.getMessage(),
                                e
                        );
                    }
                }

                // Ajouter le contrat si présent
                Integer contratId = rs.getInt("id_contrat");
                if (!rs.wasNull() && contratId != null && contratId > 0) {
                    try {
                        Contrat contrat = new Contrat(
                                clientId,
                                rs.getString("nom_contrat"),
                                rs.getDouble("montant")
                        );
                        contrat.setId(contratId);

                        if (!client.getContrats().contains(contrat)) {
                            client.ajouterContrat(contrat);
                        }
                    } catch (ValidationException e) {
                        LOGGER.log(Level.WARNING,
                                "Contrat invalide ignoré pour client ID={0}", clientId);
                    }
                }
            }

            LOGGER.log(Level.INFO, "{0} client(s) récupéré(s)", clientsMap.size());
            return new ArrayList<>(clientsMap.values());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL dans findAll", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findAll",
                    null,
                    "Erreur récupération clients : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT ResultSet et PreparedStatement
            // NE PAS FERMER la connexion (gérée par Singleton)
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture ResultSet", e);
                }
            }
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture PreparedStatement", e);
                }
            }
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

                    // Charger les contrats du client
                    try {
                        List<Contrat> contrats = contratDAO.findByIdClient(client.getId());
                        for (Contrat contrat : contrats) {
                            client.ajouterContrat(contrat);
                        }
                    } catch (DAOException e) {
                        LOGGER.log(Level.WARNING,
                                "Impossible de charger les contrats du client ID={0}", client.getId());
                    }
                    return client;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de findById avec ID=" + id, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findById",
                    id,
                    "Erreur lors de la recherche du client : " + SQLExceptionAnalyzer.analyze(e),
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
     * Insère un nouveau client dans la base de données avec transaction.
     * <p>
     * Processus :
     * 1. Démarre une transaction
     * 2. Insère la partie société (table societe) via SocieteDAO
     * 3. Insère la partie client (table client)
     * 4. Commit de la transaction
     * <p>
     * Note : Les contrats ne sont pas insérés automatiquement, ils doivent
     * être créés séparément via ContratDAO après la création du client.
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
            String query = "INSERT INTO client (" +
                    "id_societe, " +
                    "chiffre_affaires, " +
                    "nb_employes) " +
                    " VALUES (?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, societeId);
                statement.setLong(2, client.getChiffreAffaires());
                statement.setInt(3, client.getNbEmployes());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("L'insértion du client a échoué, aucune ligne affectée");
                }

                // Récupérer l'ID généré pour le client
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer clientId = generatedKeys.getInt(1);
                        client.setId(clientId); // ID de la table client

                        // Charger les contrats du client (vide pour un nouveau client)
                        List<Contrat> contrats = contratDAO.findByIdClient(clientId);
                        for (Contrat contrat : contrats) {
                            client.ajouterContrat(contrat);
                        }

                        connection.commit();
                        LOGGER.log(Level.INFO,
                                "Client créé avec succès : ID client={0}, ID société={1}, Raison sociale={2}, CA={3}, Nb employés={4}",
                                new Object[]{clientId, societeId, client.getRaisonSociale(),
                                        client.getChiffreAffaires(), client.getNbEmployes()});
                    } else {
                        throw new SQLException("L'insertion a échoué, aucun ID généré");
                    }

                }
//                LOGGER.log(Level.INFO, "Client crée avec l'ID {0}", societeId);
                return client;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Erreur lors de la création de client, rollback effectué", e);

            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }
//            String detailedMessage = analyzeSQLException(e);
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du client : ");
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "create",
                    client.getId(),
                    "Erreur : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            try {
                connection.setAutoCommit(true);

            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
        }
    }

    /**
     * Met à jour un client existant dans la base de données avec transaction.
     * <p>
     * Met à jour :
     * Les informations de la société (raison sociale, téléphone, email, etc.)
     * Les informations spécifiques du client (chiffre d'affaires, nb employés)
     * L'adresse associée
     * <p>
     * Note : Les contrats ne sont pas mis à jour par cette méthode.
     * Utilisez ContratDAO.save() pour modifier les contrats.
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
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
//            connection.setAutoCommit(false);
            if (originalAutoCommit) {
                connection.setAutoCommit(false);
            }
            // 1. Récupérer id_societe depuis la table client
            Integer societeId = null;
            String getSocieteIdSQL = "SELECT id_societe FROM client WHERE id_client = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getSocieteIdSQL)) {
                pstmt.setInt(1, client.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        societeId = rs.getInt("id_societe");
                    } else {
                        if (originalAutoCommit) {
                            connection.rollback();
                        }
                        LOGGER.log(Level.WARNING, "Aucun client trouvé avec l'ID {0}", client.getId());
                        return false;
                    }
                }
            }

            // 2. Mettre à jour la partie adresse
            if (client.getAdresse() != null && client.getAdresse().getId() != null) {
                adresseDAO.save(client.getAdresse(), connection);
            }
            // 3. Mettre à jour la partie société
            saveSociete(client, societeId, connection);

            String query = "UPDATE client " +
                    "SET chiffre_affaires = ?, " +
                    "nb_employes = ? " +
                    "WHERE id_client = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, client.getChiffreAffaires());
                statement.setInt(2, client.getNbEmployes());
                statement.setInt(3, client.getId());

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    if (!connection.getAutoCommit()) {
                        connection.commit();
                    }
                    return true;
                } else {
                    if (!connection.getAutoCommit()) {
                        connection.rollback();
                    }
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
                LOGGER.log(Level.WARNING, "Erreur lors de la mis à jour, rollback effectué", e);

            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }

            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du client ID= " + client.getId(), e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "save",
                    client.getId(),
                    "Erreur lors de la mise à jour du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } catch(DAOException e) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur DAO", e);
                }
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }
            throw e;
        } finally {
            try {
                if (originalAutoCommit && !connection.getAutoCommit()) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
        }
    }

    /**
     * Supprime un client de la base de données avec transaction.
     *
     * <p><strong>⚠️ IMPORTANT :</strong> Cette méthode refuse de supprimer un client
     * qui possède des contrats associés. Les contrats doivent être supprimés
     * manuellement au préalable.</p>
     *
     * <p><strong>Étapes :</strong></p>
     * <ol>
     *   <li>Vérifier l'existence du client</li>
     *   <li>Vérifier qu'il n'a AUCUN contrat associé</li>
     *   <li>Supprimer le client de la table {@code client}</li>
     *   <li>Supprimer la société de la table {@code societe}</li>
     *   <li>Supprimer l'adresse si non référencée par d'autres sociétés</li>
     *   <li>Commit de la transaction</li>
     * </ol>
     *
     * @param id l'identifiant du client à supprimer
     * @return true si la suppression a réussi, false si le client n'existe pas
     * @throws DAOException si une erreur survient ou si le client possède des contrats
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

        Connection connection = null;
        Integer societeId = null;
        Integer adresseId = null;

        try {
            connection = dbConnexion.getConnection();
            connection.setAutoCommit(false);

            // ========== ÉTAPE 1 : VÉRIFIER L'EXISTENCE DU CLIENT ==========
            String getIdsSQL = "SELECT c.id_societe, s.adresse_id " +
                    "FROM client c " +
                    "INNER JOIN societe s ON c.id_societe = s.id_societe " +
                    "WHERE c.id_client = ?";

            try (PreparedStatement statement = connection.prepareStatement(getIdsSQL)) {
                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        societeId = rs.getInt("id_societe");
                        adresseId = rs.getInt("adresse_id");
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            // ========== ÉTAPE 2 : VÉRIFIER QU'IL N'A PAS DE CONTRATS ==========
            int nbContrats = countContratsByClientId(connection, id);

            if (nbContrats > 0) {
                connection.rollback();

                LOGGER.log(Level.WARNING,
                        "Impossible de supprimer le client ID={0} : {1} contrat(s) associé(s)",
                        new Object[]{id, nbContrats});

                throw new DAOException(
                        DAOException.ErrorCode.FOREIGN_KEY_VIOLATION,
                        "delete",
                        id,
                        String.format(
                                "Impossible de supprimer le client : %d contrat(s) associé(s) trouvé(s). " +
                                        "Veuillez d'abord supprimer les contrats.",
                                nbContrats
                        )
                );
            }

            // ========== ÉTAPE 3 : SUPPRIMER LE CLIENT ==========
            String deleteClientSQL = "DELETE FROM client WHERE id_client = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteClientSQL)) {
                statement.setInt(1, id);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Aucune ligne supprimée dans la table client pour ID=" + id);
                }

            }

            // ========== ÉTAPE 4 : SUPPRIMER LA SOCIÉTÉ ==========
            deleteSocieteInTransaction(connection, societeId);

            // ========== ÉTAPE 5 : VÉRIFIER SI L'ADRESSE EST RÉFÉRENCÉE ==========
            boolean adresseEstReferenciee = isAdresseReferencee(connection, adresseId);

            if (adresseEstReferenciee) {
                LOGGER.log(Level.INFO,
                        "Adresse conservée car référencée par d''autres sociétés : ID={0}",
                        adresseId);
            } else {
                // Supprimer l'adresse si elle n'est plus référencée
                try {
                    deleteAdresseInTransaction(connection, adresseId);
                    LOGGER.log(Level.INFO, "Adresse supprimée : ID={0}", adresseId);
                } catch (DAOException e) {
                    // Si la suppression échoue, on log mais on continue
                    LOGGER.log(Level.WARNING,
                            "Impossible de supprimer l''adresse ID={0} : {1}",
                            new Object[]{adresseId, e.getMessage()});
                }
            }

            // ========== COMMIT ==========
            connection.commit();

            LOGGER.log(Level.INFO,
                    "Client supprimé avec succès : ID client={0}, ID société={1}, Adresse {2}",
                    new Object[]{
                            id,
                            societeId,
                            adresseEstReferenciee ? "conservée (ID=" + adresseId + ")" : "supprimée (ID=" + adresseId + ")"
                    });

            return true;

        } catch (SQLException e) {
            rollback(connection, "delete", id);

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression du client ID=" + id, e);

            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "delete",
                    id,
                    "Erreur lors de la suppression du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            resetAutoCommit(connection);
        }
    }

// ========== MÉTHODES PRIVÉES UTILITAIRES ==========

    /**
     * Compte le nombre de contrats associés à un client.
     *
     * <p>Cette méthode est utilisée pour vérifier si un client peut être supprimé.</p>
     *
     * @param connection la connexion à utiliser (transaction en cours)
     * @param clientId l'ID du client
     * @return le nombre de contrats associés au client
     * @throws SQLException si une erreur survient
     */
    private int countContratsByClientId(Connection connection, Integer clientId) throws SQLException {
        String sql = "SELECT COUNT(*) AS nb FROM contrat WHERE client_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nb");
                }
            }
        }

        return 0;
    }

    /**
     * Supprime une société dans la transaction en cours.
     *
     * @param connection la connexion à utiliser (transaction en cours)
     * @param societeId l'ID de la société à supprimer
     * @throws SQLException si une erreur survient
     * @throws DAOException si la société n'existe pas
     */
    private void deleteSocieteInTransaction(Connection connection, Integer societeId)
            throws SQLException, DAOException {

        if (societeId == null || societeId <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteSocieteInTransaction",
                    societeId,
                    "L'ID de la société est invalide"
            );
        }

        String sql = "DELETE FROM societe WHERE id_societe = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, societeId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DAOException(
                        DAOException.ErrorCode.ENTITY_NOT_FOUND,
                        "deleteSocieteInTransaction",
                        societeId,
                        "Aucune société trouvée avec l'ID " + societeId
                );
            }
        }
    }

    /**
     * Vérifie si une adresse est encore référencée par d'autres sociétés.
     *
     * @param connection la connexion à utiliser (transaction en cours)
     * @param adresseId l'ID de l'adresse
     * @return true si l'adresse est encore référencée, false sinon
     * @throws SQLException si une erreur survient
     */
    private boolean isAdresseReferencee(Connection connection, Integer adresseId)
            throws SQLException {

        if (adresseId == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) AS nb FROM societe WHERE adresse_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, adresseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int nbReferences = rs.getInt("nb");
                    LOGGER.log(Level.FINE,
                            "Adresse ID={0} : {1} référence(s) trouvée(s)",
                            new Object[]{adresseId, nbReferences});
                    return nbReferences > 0;
                }
            }
        }

        return false;
    }

    /**
     * Supprime une adresse dans la transaction en cours.
     *
     * @param connection la connexion à utiliser (transaction en cours)
     * @param adresseId l'ID de l'adresse à supprimer
     * @throws SQLException si une erreur survient
     * @throws DAOException si l'adresse n'existe pas ou est encore référencée
     */
    private void deleteAdresseInTransaction(Connection connection, Integer adresseId)
            throws SQLException, DAOException {

        if (adresseId == null || adresseId <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteAdresseInTransaction",
                    adresseId,
                    "L'ID de l'adresse est invalide"
            );
        }

        String sql = "DELETE FROM adresse WHERE id_adresse = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, adresseId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                LOGGER.log(Level.WARNING, "Aucune adresse trouvée avec l''ID {0}", adresseId);
            }
        }
    }

    /**
     * Effectue un rollback sur une connexion.
     *
     * @param connection la connexion sur laquelle effectuer le rollback
     * @param operation le nom de l'opération en cours (pour les logs)
     * @param entityId l'ID de l'entité concernée (pour les logs)
     */
    private void rollback(Connection connection, String operation, Integer entityId) {
        if (connection != null) {
            try {
                connection.rollback();
                LOGGER.log(Level.WARNING,
                        "Rollback effectué pour l''opération {0} sur l''entité ID={1}",
                        new Object[]{operation, entityId});
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", e);
            }
        }
    }

    /**
     * Réactive l'auto-commit sur une connexion.
     *
     * @param connection la connexion sur laquelle réactiver l'auto-commit
     */
    private void resetAutoCommit(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l''autoCommit", e);
            }
        }
    }


    /**
     * Méthode utilitaire pour mapper un ResultSet vers un objet Client.
     *
     * @param rs le ResultSet contenant les données
     * @return l'objet Client créé
     * @throws SQLException        si une erreur survient lors de la lecture du ResultSet
     * @throws ValidationException si les données ne respectent pas les règles métier
     */
    private Client mapResultSetToClient(ResultSet rs) throws SQLException, ValidationException {
        // Reconstituer l'adresse
        Adresse adresse = new Adresse(
                rs.getString("numero_rue"),
                rs.getString("nom_rue"),
                rs.getString("code_postal"),
                rs.getString("ville")
        );
        adresse.setId(rs.getInt("id_societe"));

        // Reconstituer le client
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
