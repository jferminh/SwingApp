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

import static main.com.julio.util.JdbcUtil.closeResources;

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
            closeResources(rs, pstmt, null);

        }
    }


    /**
     * Recherche un client par son identifiant avec ses contrats.
     *
     * <p>Cette méthode utilise un LEFT JOIN pour récupérer le client et ses contrats
     * en une seule requête SQL (optimisation N+1).</p>
     *
     * @param id l'identifiant du client à rechercher
     * @return le client trouvé avec ses contrats, ou null si aucun client ne correspond
     * @throws DAOException si une erreur survient lors de la recherche
     */
    public Client findById(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "findById",
                    id,
                    "L'ID doit être un entier positif non null"
            );
        }

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
                "WHERE c.id_client = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Client client = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources sur la connexion
            Connection conn = dbConnexion.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                // ========== ÉTAPE 1 : CRÉER LE CLIENT (une seule fois) ==========
                if (client == null) {
                    try {
                        client = mapResultSetToClient(rs);

                    } catch (ValidationException e) {
                        LOGGER.log(Level.SEVERE,
                                "Erreur de validation lors du mapping du client ID={0}", id);
                        throw new DAOException(
                                DAOException.ErrorCode.INVALID_PARAMETER,
                                "findById",
                                id,
                                "Données invalides pour le client : " + e.getMessage(),
                                e
                        );
                    }
                }

                // ========== ÉTAPE 2 : AJOUTER LE CONTRAT SI PRÉSENT ==========
                Integer contratId = rs.getInt("id_contrat");

                // Vérifier si un contrat existe (LEFT JOIN peut retourner NULL)
                if (!rs.wasNull() && contratId != null && contratId > 0) {
                    try {
                        String nomContrat = rs.getString("nom_contrat");
                        double montant = rs.getDouble("montant");

                        Contrat contrat = new Contrat(id, nomContrat, montant);
                        contrat.setId(contratId);

                        // Éviter les doublons
                        if (!client.getContrats().contains(contrat)) {
                            client.ajouterContrat(contrat);

                        }

                    } catch (ValidationException e) {
                        LOGGER.log(Level.WARNING,
                                "Contrat invalide ignoré pour le client ID={0} : {1}",
                                new Object[]{id, e.getMessage()});
                        // On continue sans bloquer le chargement du client
                    }
                }
            }

            if (client != null) {
                LOGGER.log(Level.INFO,
                        "Client ID={0} récupéré avec {1} contrat(s)",
                        new Object[]{id, client.getContrats().size()});
            } else {
                LOGGER.log(Level.FINE, "Aucun client trouvé avec l''ID {0}", id);
            }

            return client;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de findById avec ID=" + id, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findById",
                    id,
                    "Erreur lors de la recherche du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            closeResources(rs, pstmt, null);
        }
    }

    /**
     * Crée un nouveau client dans la base de données.
     *
     * <p>Cette méthode effectue une transaction qui :</p>
     * <ul>
     *   <li>Insère d'abord la société (via createSociete)</li>
     *   <li>Puis insère le client avec l'ID société généré</li>
     * </ul>
     *
     * @param client le client à créer (ne doit pas être null)
     * @return le client créé avec son ID généré
     * @throws DAOException si une erreur survient lors de la création
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

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            // ✅ CORRECTION : Récupérer la connexion sans try-with-resources
            connection = dbConnexion.getConnection();
            connection.setAutoCommit(false);

            // ========== ÉTAPE 1 : Insérer la partie société ==========
            Integer societeId = createSociete(client);

            // ========== ÉTAPE 2 : Insérer la partie client ==========
            String sql = "INSERT INTO client (id_societe, chiffre_affaires, nb_employes) " +
                    "VALUES (?, ?, ?)";

            pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, societeId);
            pstmt.setLong(2, client.getChiffreAffaires());
            pstmt.setInt(3, client.getNbEmployes());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("L'insertion du client a échoué, aucune ligne affectée");
            }

            // ========== ÉTAPE 3 : Récupérer l'ID généré ==========
            generatedKeys = pstmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                Integer clientId = generatedKeys.getInt(1);
                client.setId(clientId);  // ID de la table client

                // ✅ OPTIMISATION : Pas besoin de charger les contrats
                // Un client nouvellement créé n'a jamais de contrats
                // La liste est déjà vide par défaut dans le constructeur

                // ✅ COMMIT : Transaction réussie
                connection.commit();

                return client;

            } else {
                throw new SQLException("L'insertion a échoué, aucun ID généré");
            }

        } catch (SQLException e) {
            // ✅ ROLLBACK en cas d'erreur SQL
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur SQL", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
                }
            }

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la création du client", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "create",
                    client.getId(),
                    "Erreur lors de la création du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );

        } catch (DAOException e) {
            // ✅ ROLLBACK en cas d'erreur DAO (createSociete peut lever DAOException)
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur DAO", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
                }
            }
            throw e;

        } finally {
            closeResources(generatedKeys, pstmt, connection);

        }
    }

    public boolean save(Client client) throws DAOException {
        if (client == null || client.getId() == null || client.getId() <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    client != null ? client.getId() : null,
                    "Le client doit avoir un ID valide"
            );
        }

        Connection connection = null;
        PreparedStatement pstmtGetSociete = null;
        PreparedStatement pstmtUpdateClient = null;
        ResultSet rs = null;

        try {
            connection = dbConnexion.getConnection();
            connection.setAutoCommit(false);

            // ========== ÉTAPE 1 : Récupérer id_societe ==========
            Integer societeId = null;
            String getSocieteIdSQL = "SELECT id_societe FROM client WHERE id_client = ?";

            pstmtGetSociete = connection.prepareStatement(getSocieteIdSQL);
            pstmtGetSociete.setInt(1, client.getId());
            rs = pstmtGetSociete.executeQuery();

            if (rs.next()) {
                societeId = rs.getInt("id_societe");
            } else {
                connection.rollback();
                return false;
            }

            // On peut fermer ici ce couple rs / pstmtGetSociete
            closeResources(rs, pstmtGetSociete, null);
            rs = null;
            pstmtGetSociete = null;

            // ========== ÉTAPE 2 : Mettre à jour l'adresse ==========
            if (client.getAdresse() != null && client.getAdresse().getId() != null) {
                adresseDAO.save(client.getAdresse(), connection);
            }

            // ========== ÉTAPE 3 : Mettre à jour la société ==========
            saveSociete(client, societeId, connection);

            // ========== ÉTAPE 4 : Mettre à jour le client ==========
            String sql = "UPDATE client " +
                    "SET chiffre_affaires = ?, nb_employes = ? " +
                    "WHERE id_client = ?";

            pstmtUpdateClient = connection.prepareStatement(sql);
            pstmtUpdateClient.setLong(1, client.getChiffreAffaires());
            pstmtUpdateClient.setInt(2, client.getNbEmployes());
            pstmtUpdateClient.setInt(3, client.getId());

            int rowsAffected = pstmtUpdateClient.executeUpdate();

            if (rowsAffected > 0) {
                connection.commit();

                LOGGER.log(Level.INFO,
                        "Client mis à jour avec succès : ID={0}, Raison sociale={1}, CA={2}, Nb employés={3}",
                        new Object[]{client.getId(), client.getRaisonSociale(),
                                client.getChiffreAffaires(), client.getNbEmployes()});

                return true;
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Aucune ligne mise à jour pour le client ID={0}", client.getId());
                return false;
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur SQL", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
                }
            }

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la mise à jour du client ID=" + client.getId(), e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "save",
                    client.getId(),
                    "Erreur lors de la mise à jour du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );

        } catch (DAOException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur DAO", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
                }
            }
            throw e;

        } finally {
            if (pstmtGetSociete != null && pstmtUpdateClient == null) {
                closeResources(rs, pstmtGetSociete, connection);
            } else if (pstmtUpdateClient != null) {
                closeResources(null, pstmtUpdateClient, connection);
            } else {
                closeResources(rs, null, connection);
            }

        }
    }


    /**
     * Supprime un client de la base de données.
     *
     * <p>Cette méthode effectue une transaction qui :</p>
     * <ol>
     *   <li>Vérifie que le client existe</li>
     *   <li>Vérifie qu'il n'a pas de contrats (sinon erreur)</li>
     *   <li>Supprime le client</li>
     *   <li>Supprime la société associée</li>
     *   <li>Supprime l'adresse si elle n'est plus référencée</li>
     * </ol>
     *
     * @param id l'ID du client à supprimer
     * @return true si la suppression a réussi, false si le client n'existe pas
     * @throws DAOException si une erreur survient ou si le client a des contrats
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
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Integer societeId = null;
        Integer adresseId = null;

        try {
            // ✅ CORRECTION : Récupérer la connexion sans try-with-resources
            connection = dbConnexion.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.FINE, "Début transaction suppression client : ID={0}", id);

            // ========== ÉTAPE 1 : Récupérer id_societe et adresse_id ==========
            String getIdsSQL = "SELECT c.id_societe, s.adresse_id " +
                    "FROM client c " +
                    "INNER JOIN societe s ON c.id_societe = s.id_societe " +
                    "WHERE c.id_client = ?";

            pstmt = connection.prepareStatement(getIdsSQL);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                societeId = rs.getInt("id_societe");
                adresseId = rs.getInt("adresse_id");
                LOGGER.log(Level.FINE, "Client trouvé : société ID={0}, adresse ID={1}",
                        new Object[]{societeId, adresseId});
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Aucun client trouvé avec l'ID {0}", id);
                return false;
            }

            // Fermer rs et pstmt
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;

            // ========== ÉTAPE 2 : Vérifier qu'il n'a pas de contrats ==========
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
                                "Impossible de supprimer le client : %d contrat(s) associé(s). " +
                                        "Veuillez d'abord supprimer les contrats.",
                                nbContrats
                        )
                );
            }

            LOGGER.log(Level.FINE, "Client sans contrats, suppression autorisée");

            // ========== ÉTAPE 3 : Supprimer le client ==========
            String deleteClientSQL = "DELETE FROM client WHERE id_client = ?";
            pstmt = connection.prepareStatement(deleteClientSQL);
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Aucune ligne supprimée dans la table client pour ID=" + id);
            }

            LOGGER.log(Level.FINE, "Client supprimé : ID={0}", id);

            pstmt.close();
            pstmt = null;

            // ========== ÉTAPE 4 : Supprimer la société ==========
            deleteSociete(connection, societeId);
            LOGGER.log(Level.FINE, "Société supprimée : ID={0}", societeId);

            // ========== ÉTAPE 5 : Vérifier si l'adresse est référencée ==========
            boolean adresseEstReferenciee = isAdresseReferencee(connection, adresseId);

            if (adresseEstReferenciee) {
                LOGGER.log(Level.INFO,
                        "Adresse conservée car référencée par d'autres sociétés : ID={0}",
                        adresseId);
            } else {
                // Supprimer l'adresse si elle n'est plus référencée
                try {
                    adresseDAO.deleteAdresse(connection, adresseId);
                    LOGGER.log(Level.FINE, "Adresse supprimée : ID={0}", adresseId);
                } catch (DAOException e) {
                    // Si la suppression échoue, on log mais on continue
                    LOGGER.log(Level.WARNING,
                            "Impossible de supprimer l'adresse ID={0} : {1}",
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
            // ✅ ROLLBACK en cas d'erreur SQL
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur SQL", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
                }
            }

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression du client ID=" + id, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "delete",
                    id,
                    "Erreur lors de la suppression du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );

        } catch (DAOException e) {
            // ✅ ROLLBACK en cas d'erreur DAO
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur DAO", e);
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
                }
            }
            throw e;

        } finally {
            // ✅ IMPORTANT : Fermer toutes les ressources
            closeResources(rs, pstmt, connection);

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
