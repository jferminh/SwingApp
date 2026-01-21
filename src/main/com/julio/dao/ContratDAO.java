package main.com.julio.dao;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Contrat;
import main.com.julio.service.LoggerService;
import main.com.julio.util.SQLExceptionAnalyzer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour la gestion des contrats en base de données.
 * Implémente le pattern Data Access Object (DAO) pour l'entité Contrat.
 * <p>
 * Cette classe gère les opérations CRUD sur les contrats et permet
 * de récupérer les contrats associés à un client spécifique.
 * Les transactions sont utilisées pour garantir l'intégrité des données.
 *
 * @author Julio FERMIN
 * @version 2.1
 * @since 21/01/2026
 */
public class ContratDAO {

    private static final Logger LOGGER = LoggerService.getLogger(ContratDAO.class);
    private final DatabaseConnexion dbConnection;

    /**
     * Constructeur qui récupère l'instance Singleton de DatabaseConnection.
     *
     * @throws DAOException si la connexion à la base de données échoue
     */
    public ContratDAO() throws DAOException {
        try {
            this.dbConnection = DatabaseConnexion.getInstance();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Échec de l'initialisation de ContratDAO", e);
            throw new DAOException(
                    DAOException.ErrorCode.CONNECTION_ERROR,
                    "init",
                    null,
                    "Impossible d'initialiser ContratDAO : " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Récupère tous les contrats de la base de données.
     * <p>
     * Cette méthode retourne tous les contrats sans filtrage, triés par ID.
     * Pour récupérer les contrats d'un client spécifique, utilisez findByIdClient().
     *
     * @return une liste de tous les contrats
     * @throws DAOException si une erreur survient lors de la requête
     */
    public List<Contrat> findAll() throws DAOException {
        List<Contrat> contrats = new ArrayList<>();

        String sql = "SELECT id_contrat, client_id, nom_contrat, montant " +
                "FROM contrat " +
                "ORDER BY id_contrat";

        Statement stmt = null;
        ResultSet rs = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources sur connexion
            Connection connection = dbConnection.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                try {
                    Contrat contrat = mapResultSetToContrat(rs);
                    contrats.add(contrat);

                } catch (ValidationException e) {
                    Integer contratId = rs.getInt("id_contrat");
                    LOGGER.log(Level.WARNING,
                            "Contrat ID={0} ignoré : données invalides - {1}",
                            new Object[]{contratId, e.getMessage()});
                }
            }

            return contrats;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de findAll()", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findAll",
                    null,
                    "Erreur lors de la récupération de tous les contrats : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT ResultSet et Statement
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture ResultSet", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture Statement", e);
                }
            }
            // ❌ NE PAS fermer connection (Singleton)
        }
    }

    /**
     * Récupère un contrat par son identifiant.
     *
     * @param id l'identifiant du contrat
     * @return le contrat correspondant ou null si non trouvé
     * @throws DAOException si une erreur survient lors de la requête
     */
    public Contrat findById(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            LOGGER.log(Level.WARNING, "Tentative de findById avec un ID invalide : {0}", id);
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "findById",
                    id,
                    "L'ID doit être un entier positif non null"
            );
        }

        String sql = "SELECT id_contrat, client_id, nom_contrat, montant " +
                "FROM contrat " +
                "WHERE id_contrat = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources sur connexion
            Connection connection = dbConnection.getConnection();
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                try {
                    Contrat contrat = mapResultSetToContrat(rs);

                    return contrat;

                } catch (ValidationException e) {
                    LOGGER.log(Level.SEVERE,
                            "Erreur de validation lors du mapping du contrat ID={0}", id);
                    throw new DAOException(
                            DAOException.ErrorCode.INVALID_PARAMETER,
                            "findById",
                            id,
                            "Données invalides pour le contrat : " + e.getMessage(),
                            e
                    );
                }
            } else {
                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de findById avec ID=" + id, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findById",
                    id,
                    "Erreur lors de la recherche du contrat : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT ResultSet et PreparedStatement
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture ResultSet", e);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture PreparedStatement", e);
                }
            }
            // ❌ NE PAS fermer connection (Singleton)
        }
    }

    /**
     * Récupère tous les contrats associés à un client spécifique.
     * <p>
     * Cette méthode est essentielle pour afficher les contrats d'un client
     * dans l'interface utilisateur. Les contrats sont triés par ID.
     *
     * @param clientId l'identifiant du client
     * @return une liste des contrats du client (peut être vide)
     * @throws DAOException si une erreur survient lors de la requête
     */
    public List<Contrat> findByIdClient(Integer clientId) throws DAOException {
        if (clientId == null || clientId <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "findByIdClient",
                    clientId,
                    "L'ID du client doit être un entier positif non null"
            );
        }

        List<Contrat> contrats = new ArrayList<>();

        String sql = "SELECT id_contrat, client_id, nom_contrat, montant " +
                "FROM contrat " +
                "WHERE client_id = ? " +
                "ORDER BY id_contrat";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources sur connexion
            Connection connection = dbConnection.getConnection();
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, clientId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                try {
                    Contrat contrat = mapResultSetToContrat(rs);
                    contrats.add(contrat);

                } catch (ValidationException e) {
                    Integer contratId = rs.getInt("id_contrat");
                    LOGGER.log(Level.WARNING,
                            "Contrat ID={0} ignoré pour client ID={1} : {2}",
                            new Object[]{contratId, clientId, e.getMessage()});
                }
            }

            return contrats;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de findByIdClient avec clientId=" + clientId, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findByIdClient",
                    clientId,
                    "Erreur lors de la recherche des contrats du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT ResultSet et PreparedStatement
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture ResultSet", e);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture PreparedStatement", e);
                }
            }
            // ❌ NE PAS fermer connection (Singleton)
        }
    }

    /**
     * Insère un nouveau contrat dans la base de données avec transaction.
     * <p>
     * Le client référencé par client_id doit exister dans la base de données,
     * sinon une exception de type FOREIGN_KEY_VIOLATION sera levée.
     *
     * @param contrat le contrat à insérer
     * @return le contrat avec son ID généré
     * @throws DAOException si une erreur survient lors de l'insertion
     */
    public Contrat create(Contrat contrat) throws DAOException {
        if (contrat == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "create",
                    null,
                    "Le contrat ne peut pas être null"
            );
        }

        String sql = "INSERT INTO contrat (client_id, nom_contrat, montant) " +
                "VALUES (?, ?, ?)";

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            // ✅ CORRECTION : Récupérer la connexion sans try-with-resources
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, contrat.getClientId());
            pstmt.setString(2, contrat.getNomContrat());
            pstmt.setDouble(3, contrat.getMontant());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("L'insertion du contrat a échoué, aucune ligne affectée");
            }

            generatedKeys = pstmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                Integer contratId = generatedKeys.getInt(1);
                contrat.setId(contratId);

                // ✅ COMMIT : Transaction réussie
                connection.commit();

                return contrat;

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

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la création du contrat", e);

            // Analyse spécifique pour les violations de clés étrangères
            if (SQLExceptionAnalyzer.isForeignKeyViolation(e)) {
                String constraintName = SQLExceptionAnalyzer.extractConstraintName(e);
                throw new DAOException(
                        DAOException.ErrorCode.FOREIGN_KEY_VIOLATION,
                        "create",
                        contrat.getClientId(),
                        "Le client ID=" + contrat.getClientId() + " n'existe pas dans la base de données" +
                                (constraintName != null ? " (contrainte: " + constraintName + ")" : ""),
                        e
                );
            }

            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "create",
                    contrat.getId(),
                    "Erreur lors de la création du contrat : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer toutes les ressources et réactiver autoCommit
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture ResultSet generatedKeys", e);
                }
            }

            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture PreparedStatement", e);
                }
            }

            // ✅ CRUCIAL : Réactiver autoCommit
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur réactivation autoCommit", e);
                }
            }

            // ❌ NE PAS fermer connection (Singleton)
        }
    }

    /**
     * Met à jour un contrat existant dans la base de données avec transaction.
     * <p>
     * Note : Le client_id ne peut pas être modifié. Pour réaffecter un contrat
     * à un autre client, il faut le supprimer et le recréer.
     *
     * @param contrat le contrat à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi, false sinon
     * @throws DAOException si une erreur survient lors de la mise à jour
     */
    public boolean save(Contrat contrat) throws DAOException {
        if (contrat == null || contrat.getId() == null || contrat.getId() <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    contrat != null ? contrat.getId() : null,
                    "Le contrat doit avoir un ID valide pour être mis à jour"
            );
        }

        String sql = "UPDATE contrat " +
                "SET nom_contrat = ?, montant = ? " +
                "WHERE id_contrat = ?";

        Connection connection = null;
        PreparedStatement pstmt = null;

        try {
            // ✅ CORRECTION : Récupérer la connexion sans try-with-resources
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, contrat.getNomContrat());
            pstmt.setDouble(2, contrat.getMontant());
            pstmt.setInt(3, contrat.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // ✅ COMMIT : Transaction réussie
                connection.commit();

                return true;
            } else {
                connection.rollback();
                return false;
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

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la mise à jour du contrat ID=" + contrat.getId(), e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "save",
                    contrat.getId(),
                    "Erreur lors de la mise à jour du contrat : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer toutes les ressources et réactiver autoCommit
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture PreparedStatement", e);
                }
            }

            // ✅ CRUCIAL : Réactiver autoCommit
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur réactivation autoCommit", e);
                }
            }

            // ❌ NE PAS fermer connection (Singleton)
        }
    }

    /**
     * Supprime un contrat de la base de données avec transaction.
     * <p>
     * Cette opération supprime définitivement le contrat. Le client associé
     * n'est pas affecté par cette suppression.
     *
     * @param id l'identifiant du contrat à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws DAOException si une erreur survient lors de la suppression
     */
    public boolean delete(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "delete",
                    id,
                    "L'ID doit être un entier positif non null pour supprimer un contrat"
            );
        }

        Connection connection = null;
        PreparedStatement pstmt = null;

        try {
            // ✅ CORRECTION : Récupérer la connexion sans try-with-resources
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            String sql = "DELETE FROM contrat WHERE id_contrat = ?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // ✅ COMMIT : Transaction réussie
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
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

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression du contrat ID=" + id, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "delete",
                    id,
                    "Erreur lors de la suppression du contrat : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer toutes les ressources et réactiver autoCommit
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture PreparedStatement", e);
                }
            }

            // ✅ CRUCIAL : Réactiver autoCommit
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur réactivation autoCommit", e);
                }
            }

            // ❌ NE PAS fermer connection (Singleton)
        }
    }

    /**
     * Supprime tous les contrats associés à un client.
     * <p>
     * Cette méthode est utilisée en interne lors de la suppression d'un client
     * pour maintenir l'intégrité référentielle. Elle est appelée dans le cadre
     * d'une transaction gérée par ClientDAO.
     *
     * <p><strong>NOTE :</strong> Cette méthode n'est plus utilisée car
     * ClientDAO.delete() vérifie qu'il n'y a pas de contrats avant de supprimer.
     * Elle reste disponible pour usage futur si nécessaire.</p>
     *
     * @param clientId l'identifiant du client
     * @return le nombre de contrats supprimés
     * @throws DAOException si une erreur survient lors de la suppression
     * @deprecated Utilisez la vérification dans ClientDAO.delete() à la place
     */
    @Deprecated
    public int deleteByClientId(Integer clientId) throws DAOException {
        if (clientId == null || clientId <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteByClientId",
                    clientId,
                    "L'ID du client doit être un entier positif non null"
            );
        }

        String sql = "DELETE FROM contrat WHERE client_id = ?";

        PreparedStatement pstmt = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources
            // Cette méthode est appelée dans une transaction externe
            Connection connection = dbConnection.getConnection();

            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, clientId);

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur SQL lors de deleteByClientId avec clientId=" + clientId, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "deleteByClientId",
                    clientId,
                    "Erreur lors de la suppression des contrats du client : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT le PreparedStatement
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture PreparedStatement", e);
                }
            }
            // ❌ NE PAS fermer connection (Singleton)
            // ❌ NE PAS gérer transaction (responsabilité de l'appelant)
        }
    }

    /**
     * Méthode utilitaire privée pour mapper un ResultSet vers un objet Contrat.
     * <p>
     * Cette méthode reconstruit un objet Contrat à partir des données
     * d'une requête SQL sur la table contrat.
     *
     * @param rs le ResultSet contenant les données du contrat
     * @return un objet Contrat reconstitué
     * @throws SQLException si une erreur survient lors de la lecture du ResultSet
     * @throws ValidationException si les données ne respectent pas les contraintes métier
     */
    private Contrat mapResultSetToContrat(ResultSet rs) throws SQLException, ValidationException {
        Contrat contrat = new Contrat(
                rs.getInt("client_id"),
                rs.getString("nom_contrat"),
                rs.getDouble("montant")
        );
        contrat.setId(rs.getInt("id_contrat"));
        return contrat;
    }
}
