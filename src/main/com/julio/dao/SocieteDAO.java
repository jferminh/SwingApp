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
     * Insère une société (partie commune) dans la base de données.
     * Cette méthode est utilisée par ClientDAO et ProspectDAO.
     *
     * @param societe la société à insérer
     * @return l'ID généré pour la société
     * @throws DAOException si une erreur survient lors de l'insertion
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

        // 1. Créer ou récupérer l'adresse
        Adresse adresse = societe.getAdresse();
        if (adresse == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "createSociete",
                    null,
                    "L'adresse de la société ne peut pas être null"
            );
        }

        if (adresse.getId() == null) {
            try {
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

        // 2. Insérer la société
        String query = "INSERT INTO societe(raison_sociale" +
                ", adresse_id, " +
                "telephone, " +
                "email, " +
                "commentaires) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = dbConnexion.getConnection()
                .prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, societe.getRaisonSociale());
            preparedStatement.setInt(2, adresse.getId());
            preparedStatement.setString(3, societe.getTelephone());
            preparedStatement.setString(4, societe.getEmail());
            preparedStatement.setString(5, societe.getCommentaires());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("L'insertion de la société a échoué, aucune ligne affectée");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Integer societeId = generatedKeys.getInt(1);
                    societe.setId(societeId);
//                    LOGGER.log(Level.INFO, "Société créée avec l'ID {0}", societeId);
                    return societeId;
                } else {
                    throw new SQLException("L'insertion a échoué, aucun ID généré");
                }
            }
        } catch (SQLException sqlEx) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la société", sqlEx);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(sqlEx),
                    "createSociete",
                    null,
                    "Erreur lors de la création de la société : " + SQLExceptionAnalyzer.analyze(sqlEx),
                    sqlEx
            );
        }
    }

    /**
     * Met à jour une société (partie commune) dans la base de données.
     *
     * @param societe la société à mettre à jour
     * @return true si la mise à jour a réussi
     * @throws DAOException si une erreur survient
     */
    protected boolean saveSociete(Societe societe) throws DAOException {
        if (societe == null || societe.getId() == null || societe.getId() <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "saveSociete",
                    societe != null ? societe.getId() : null,
                    "La société doit avoir un ID valide"
            );
        }

        // Mettre à jour l'adresse si elle a un ID
        Adresse adresse = societe.getAdresse();
        if (adresse != null && adresse.getId() != null) {
            try {
                adresseDAO.save(adresse);
            } catch (DAOException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'adresse", ex);
                throw new DAOException(
                        DAOException.ErrorCode.UPDATE_ERROR,
                        "saveSociete",
                        societe.getId(),
                        "Erreur lors de la mise à jour de l'adresse : " + ex.getMessage(),
                        ex
                );
            }
        }

        // Metre à jour la société
        String query = "UPDATE societe " +
                "SET raison_sociale = ?, " +
                "adresse_id = ?, " +
                "telephone = ?, " +
                "email = ?, " +
                "commentaires = ? " +
                "WHERE id_societe = ?";

        try (PreparedStatement preparedStatement = dbConnexion.getConnection()
                .prepareStatement(query)) {
            preparedStatement.setString(1, societe.getRaisonSociale());
            preparedStatement.setInt(2, adresse.getId());
            preparedStatement.setString(3, societe.getTelephone());
            preparedStatement.setString(4, societe.getEmail());
            preparedStatement.setString(5, societe.getCommentaires());
            preparedStatement.setInt(6, societe.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
//                LOGGER.log(Level.INFO, "Société mise à jour avec l'ID {0}", societe.getId());
                return true;
            } else {
//                LOGGER.log(Level.WARNING, "Aucune société trouvée avec l'ID {0}", societe.getId());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la société", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "saveSociete",
                    societe.getId(),
                    "Erreur lors de la mise à jour : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        }
    }

    protected boolean deleteSociete(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteSociete",
                    id,
                    "L'ID doit être valide"
            );
        }

        Connection connection = dbConnexion.getConnection();

        try {
            connection.setAutoCommit(false);

            String query = "DELETE FROM societe " +
                    "WHERE id_societe = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, id);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit();
//                    LOGGER.log(Level.INFO, "Société supprimée avec l'ID {0}", id);
                    return true;
                } else {
                    connection.rollback();
//                    LOGGER.log(Level.WARNING, "Aucune société trouve avec l'ID {0}", id);
                    return false;
                }
            }
        } catch (SQLException sqlEx) {
            try {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Erreur lors de la suppession, rollback effectué", sqlEx);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(sqlEx),
                    "deleteSociete",
                    id,
                    "Erreur lors de la suppression : " + SQLExceptionAnalyzer.analyze(sqlEx),
                    sqlEx
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException sqlEx) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", sqlEx);
            }
        }
    }


}
