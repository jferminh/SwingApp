package main.com.julio.dao;

import main.com.julio.exception.DAOException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Societe;
import main.com.julio.service.LoggerService;
import main.com.julio.util.SQLExceptionAnalyzer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.com.julio.util.JdbcUtil.closeResources;

/**
 * Classe DAO pour la gestion de la table société.
 * Gère les informations communes aux clients et prospects.
 *
 * Cette classe fournit des méthodes protégées pour les opérations CRUD
 * sur la partie commune des entités Client et Prospect.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 */
public abstract class SocieteDAO {

    private static final Logger LOGGER = LoggerService.getLogger(SocieteDAO.class);
    protected final DatabaseConnexion dbConnexion;
    protected final AdresseDAO adresseDAO;

    /**
     * Constructeur qui récupère l'instance de DatabaseConnection.
     *
     * @throws DAOException si la connexion à la base de données échoue
     */
    public SocieteDAO() throws DAOException {
        try {
            this.dbConnexion = DatabaseConnexion.getInstance();
            this.adresseDAO = new AdresseDAO();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Échec de l'initialisation de SocieteDAO", ex);
            throw new DAOException(
                    DAOException.ErrorCode.CONNECTION_ERROR,
                    "init",
                    null,
                    "Impossible d'initialiser SocieteDAO : " + ex.getMessage()
            );
        }
    }

    /**
     * Crée une société dans la base de données.
     *
     * <p><strong>IMPORTANT :</strong> Cette méthode est appelée dans le contexte
     * d'une transaction parent (depuis create()). Elle NE DOIT PAS gérer
     * commit/rollback ni setAutoCommit.</p>
     *
     * <p>La transaction doit être gérée par l'appelant.</p>
     *
     * @param societe la société à créer
     * @return l'ID de la société créée
     * @throws DAOException si une erreur survient lors de la création
     */
    protected Integer createSociete(Societe societe) throws DAOException {
        if (societe == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "createSociete",
                    null,
                    "La société ne peut pas être null"
            );
        }

        // ========== ÉTAPE 1 : Valider et créer/récupérer l'adresse ==========
        Adresse adresse = societe.getAdresse();
        if (adresse == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "createSociete",
                    null,
                    "L'adresse de la société ne peut pas être null"
            );
        }

        // Créer l'adresse si elle n'existe pas encore
        if (adresse.getId() == null) {
            try {
                // ⚠️ ATTENTION : adresseDAO.create() doit AUSSI ne pas gérer de transaction
                adresse = adresseDAO.create(adresse);

            } catch (DAOException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la création de l'adresse", ex);
                throw new DAOException(
                        DAOException.ErrorCode.CREATE_ERROR,
                        "createSociete",
                        null,
                        "Erreur lors de la création de l'adresse : " + ex.getMessage(),
                        ex
                );
            }
        }

        // ========== ÉTAPE 2 : Insérer la société ==========
        String sql = "INSERT INTO societe (raison_sociale, adresse_id, telephone, email, commentaires) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            // ✅ CORRECTION : Récupérer la connexion sans try-with-resources
            // La connexion est en mode transaction (autoCommit=false) depuis create()
            Connection connection = dbConnexion.getConnection();

            pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, societe.getRaisonSociale());
            pstmt.setInt(2, adresse.getId());
            pstmt.setString(3, societe.getTelephone());
            pstmt.setString(4, societe.getEmail());
            pstmt.setString(5, societe.getCommentaires());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("L'insertion de la société a échoué, aucune ligne affectée");
            }

            // ========== ÉTAPE 3 : Récupérer l'ID généré ==========
            generatedKeys = pstmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                Integer societeId = generatedKeys.getInt(1);
                societe.setId(societeId);

                return societeId;

            } else {
                throw new SQLException("L'insertion a échoué, aucun ID généré");
            }

        } catch (SQLException sqlEx) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la création de la société", sqlEx);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(sqlEx),
                    "createSociete",
                    null,
                    "Erreur lors de la création de la société : " + SQLExceptionAnalyzer.analyze(sqlEx),
                    sqlEx
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT ResultSet et PreparedStatement
            // NE PAS :
            // ._ Fermer la connexion (Singleton)
            // ._ Faire commit/rollback (géré par l'appelant)
            // ._ Modifier autoCommit (géré par l'appelant)

            closeResources(generatedKeys, pstmt, null);

            // NE PAS fermer connection
            // NE PAS toucher à setAutoCommit
            // NE PAS faire commit/rollback
        }
    }

    /**
     * Met à jour une société existante dans la base de données.
     *
     * <p><strong>IMPORTANT :</strong> Cette méthode est appelée dans le contexte
     * d'une transaction parent (depuis save()). Elle NE DOIT PAS gérer
     * commit/rollback ni setAutoCommit.</p>
     *
     * <p>La transaction doit être gérée par l'appelant.</p>
     *
     * @param societe la société avec les nouvelles données
     * @param societeId l'ID de la société à mettre à jour
     * @param connection la connexion à utiliser (en transaction)
     * @throws DAOException si une erreur survient lors de la mise à jour
     */
    protected void saveSociete(Societe societe, Integer societeId, Connection connection)
            throws DAOException {

        if (societe == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "saveSociete",
                    societeId,
                    "La société ne peut pas être null"
            );
        }

        if (societeId == null || societeId <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "saveSociete",
                    societeId,
                    "L'ID société doit être un entier positif non null"
            );
        }

        if (connection == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "saveSociete",
                    societeId,
                    "La connexion ne peut pas être null"
            );
        }

        String sql = "UPDATE societe " +
                "SET raison_sociale = ?, telephone = ?, email = ?, commentaires = ? " +
                "WHERE id_societe = ?";

        PreparedStatement pstmt = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources
            // La connexion est gérée par l'appelant (transaction parent)
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, societe.getRaisonSociale());
            pstmt.setString(2, societe.getTelephone());
            pstmt.setString(3, societe.getEmail());
            pstmt.setString(4, societe.getCommentaires());
            pstmt.setInt(5, societeId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException(
                        "La mise à jour de la société a échoué, aucune ligne affectée (ID=" + societeId + ")"
                );
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur SQL lors de la mise à jour de la société ID=" + societeId, e);

            // Vérifier si c'est une violation de contrainte d'unicité
            if (SQLExceptionAnalyzer.isUniqueConstraintViolation(e)) {
                String constraintName = SQLExceptionAnalyzer.extractConstraintName(e);
                throw new DAOException(
                        DAOException.ErrorCode.UNIQUE_CONSTRAINT_VIOLATION,
                        "saveSociete",
                        societeId,
                        "La raison sociale '" + societe.getRaisonSociale() + "' existe déjà" +
                                (constraintName != null ? " (contrainte: " + constraintName + ")" : ""),
                        e
                );
            }

            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "saveSociete",
                    societeId,
                    "Erreur lors de la mise à jour de la société : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT le PreparedStatement
            // NE PAS :
            // ._ Fermer la connexion (gérée par l'appelant)
            // ._ Faire commit/rollback (géré par l'appelant)
            // ._ Modifier autoCommit (géré par l'appelant)

            closeResources(null, pstmt, null);
        }
    }

    /**
     * Supprime une société dans le contexte d'une transaction parent.
     *
     * <p><strong>IMPORTANT :</strong> Cette méthode participe à une transaction
     * gérée par l'appelant. Elle NE DOIT PAS gérer commit/rollback.</p>
     *
     * @param connection la connexion en transaction
     * @param societeId l'ID de la société à supprimer
     * @throws DAOException si une erreur survient lors de la suppression
     */
    protected void deleteSociete(Connection connection, Integer societeId) throws DAOException {
        if (connection == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteSociete",
                    societeId,
                    "La connexion ne peut pas être null"
            );
        }

        if (societeId == null || societeId <= 0) {
            LOGGER.log(Level.WARNING, "Tentative de suppression avec ID société invalide : {0}", societeId);
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteSociete",
                    societeId,
                    "L'ID société doit être un entier positif non null"
            );
        }

        String sql = "DELETE FROM societe WHERE id_societe = ?";
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, societeId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.FINE, "Société supprimée : ID={0}", societeId);
            } else {
                LOGGER.log(Level.WARNING, "Aucune société trouvée avec l'ID {0}", societeId);
                throw new DAOException(
                        DAOException.ErrorCode.ENTITY_NOT_FOUND,
                        "deleteSociete",
                        societeId,
                        "Aucune société trouvée avec l'ID " + societeId
                );
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression de la société ID=" + societeId, e);

            if (SQLExceptionAnalyzer.isForeignKeyViolation(e)) {
                String constraintName = SQLExceptionAnalyzer.extractConstraintName(e);
                throw new DAOException(
                        DAOException.ErrorCode.FOREIGN_KEY_VIOLATION,
                        "deleteSociete",
                        societeId,
                        "Impossible de supprimer la société : elle est référencée par d'autres entités" +
                                (constraintName != null ? " (contrainte: " + constraintName + ")" : ""),
                        e
                );
            }

            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "deleteSociete",
                    societeId,
                    "Erreur lors de la suppression de la société : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            closeResources(null, pstmt,  null);
        }
    }


}
