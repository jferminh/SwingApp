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
 *
 * Cette classe gère les opérations CRUD sur les contrats et permet
 * de récupérer les contrats associés à un client spécifique.
 * Les transactions sont utilisées pour garantir l'intégrité des données.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
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
     *
     * Cette méthode retourne tous les contrats sans filtrage, triés par ID.
     * Pour récupérer les contrats d'un client spécifique, utilisez findByIdClient().
     *
     * @return une liste de tous les contrats
     * @throws DAOException si une erreur survient lors de la requête
     */
    public List<Contrat> findAll() throws DAOException {

        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT id_contrat, " +
                "client_id, " +
                "nom_contrat, " +
                "montant " +
                "FROM contrat " +
                "ORDER BY id";

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Contrat contrat = mapResultSetToContrat(rs);
                contrats.add(contrat);
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
        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping dans findAll()", e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findAll",
                    null,
                    "Erreur de validation des données du contrat : " + e.getMessage(),
                    e
            );
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

        String sql = "SELECT id_contrat, " +
                "client_id, " +
                "nom_contrat, " +
                "montant " +
                "FROM contrat " +
                "WHERE id_contrat = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Contrat contrat = mapResultSetToContrat(rs);

                    return contrat;
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
                    "Erreur lors de la recherche du contrat : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping dans findById, ID=" + id, e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findById",
                    id,
                    "Erreur de validation des données du contrat : " + e.getMessage(),
                    e
            );
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
        String sql = "SELECT id_contrat, " +
                "client_id, " +
                "nom_contrat, " +
                "montant " +
                "FROM contrat " +
                "WHERE client_id = ? " +
                "ORDER BY id_contrat";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, clientId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Contrat contrat = mapResultSetToContrat(rs);
                    contrats.add(contrat);
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
        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur de validation lors du mapping dans findByIdClient, clientId=" + clientId, e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findByIdClient",
                    clientId,
                    "Erreur de validation des données du contrat : " + e.getMessage(),
                    e
            );
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

        String sql = "INSERT INTO contrat (client_id, " +
                "nom_contrat, " +
                "montant) " +
                "VALUES (?, ?, ?)";
        Connection conn = dbConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setInt(1, contrat.getClientId());
                pstmt.setString(2, contrat.getNomContrat());
                pstmt.setDouble(3, contrat.getMontant());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("L'insertion du contrat a échoué, aucune ligne affectée");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        contrat.setId(generatedKeys.getInt(1));
                        conn.commit();

                    } else {
                        throw new SQLException("L'insertion a échoué, aucun ID généré");
                    }
                }

                return contrat;
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de création", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
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
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
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
                "SET nom_contrat = ?, " +
                "montant = ? " +
                "WHERE id_contrat = ?";
        Connection conn = dbConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, contrat.getNomContrat());
                pstmt.setDouble(2, contrat.getMontant());
                pstmt.setInt(3, contrat.getId());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de mise à jour", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
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
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
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

        Connection conn = dbConnection.getConnection();

        try {
            conn.setAutoCommit(false);
            String sql = "DELETE FROM contrat WHERE id_contrat = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de suppression", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
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
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
        }
    }

    /**
     * Supprime tous les contrats associés à un client.
     * <p>
     * Cette méthode est utilisée en interne lors de la suppression d'un client
     * pour maintenir l'intégrité référentielle. Elle est appelée dans le cadre
     * d'une transaction gérée par ClientDAO.
     *
     * @param clientId l'identifiant du client
     * @return le nombre de contrats supprimés
     * @throws DAOException si une erreur survient lors de la suppression
     */
    public int deleteByClientId(Integer clientId) throws DAOException {
        if (clientId == null || clientId <= 0) {
            LOGGER.log(Level.WARNING, "Tentative de deleteByClientId avec un ID invalide : {0}", clientId);
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteByClientId",
                    clientId,
                    "L'ID du client doit être un entier positif non null"
            );
        }

        String sql = "DELETE FROM contrat " +
                "WHERE client_id = ?";
        Connection conn = dbConnection.getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
