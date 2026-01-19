package main.com.julio.dao;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.util.SQLExceptionAnalyzer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe DAO pour la gestion des adresses en base de données.
 * Implémente le pattern Data Access Object (DAO) pour l'entité Adresse.
 * <p>
 * Les adresses sont partagées entre clients et prospects.
 * La suppression d'une adresse n'est possible que si elle n'est référencée
 * par aucune société (client ou prospect).
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 */
public class AdresseDAO {
    private static final Logger LOGGER = Logger.getLogger(AdresseDAO.class.getName());
    private final DatabaseConnexion dbConnexion;

    /**
     * Constructeur qui récupère l'instance de DatabaseConnexion.
     */
    public AdresseDAO() throws DAOException {
        try {
            this.dbConnexion = DatabaseConnexion.getInstance();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Echec de l'initialisation de AdresseDAO", ex);
            throw new DAOException(
                    DAOException.ErrorCode.CONNECTION_ERROR,
                    "init",
                    null,
                    "Impossible d'initialiser AdresseDAO : " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Récupère toutes les adresses de la base de données.
     *
     * @return une liste de toutes les adresses
     * @throws DAOException si une erreur survient lors de la requête
     */
    public List<Adresse> findAll() throws DAOException {
        List<Adresse> adresses = new ArrayList<>();
        String query = "SELECT id, " +
                "numero_rue, " +
                "nom_rue, " +
                "code_postal, " +
                "ville " +
                "FROM adresse";

        try (Statement stmt = dbConnexion.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Adresse adresse = mapResultSetToAdresse(rs);
                adresses.add(adresse);
            }
            return adresses;

//            LOGGER.log(Level.INFO, "Recupération de {0} adresses", adresses.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de toutes les adresses", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findAll",
                    null,
                    "Erreur lors de la récupération de toutes les adresses" + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping");
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findAll",
                    null,
                    "Erre de validation des données : " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Récupère une adresse par son identifiant.
     *
     * @param id l'identifiant de l'adresse
     * @return l'adresse correspondante ou null si non trouvée
     * @throws DAOException si une erreur survient lors de la requête
     */
    public Adresse findById(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "findById",
                    id,
                    "L'ID doit être un entier positif non null"
            );
        }

        String query = "SELECT id_adresse, " +
                "numero_rue, " +
                "nom_rue, " +
                "code_postal, " +
                "ville " +
                "FROM adresse " +
                "WHERE id = ?";
        try (PreparedStatement pstmt = dbConnexion.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Adresse adresse = mapResultSetToAdresse(rs);
                    return adresse;
                } else {
//                    LOGGER.log(Level.INFO, "Aucune adresse trouvée avec l'ID {0}" + id);
                    return null;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de l'adresse avec l'ID {0}" + id, e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findById",
                    id,
                    "Erreur lors de la recherche de l'adresse : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping", e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findById",
                    id,
                    "Erreur de validation des données : " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Insère une nouvelle adresse dans la base de données.
     * L'ID est généré automatiquement et affecté à l'objet.
     *
     * @param adresse l'adresse à insérer
     * @return l'adresse avec son ID généré
     * @throws DAOException si une erreur survient lors de l'insertion
     */
    public Adresse create(Adresse adresse) throws DAOException {
        if (adresse == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "create",
                    null,
                    "L'adresse ne peut pas être null"
            );
        }

        String query = "INSERT INTO adresse (" +
                "numero_rue, " +
                "nom_rue, " +
                "code_postal, " +
                "ville) " +
                "VALUES (?, ?, ?, ?)";

        Connection connection = dbConnexion.getConnection();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, adresse.getNumeroRue());
                pstmt.setString(2, adresse.getNomRue());
                pstmt.setString(3, adresse.getCodePostal());
                pstmt.setString(4, adresse.getVille());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("L'insertion de l'adresse a échoué, aucune ligne affectée");
                }

                // Récupérer l'ID généré
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        adresse.setId(generatedKeys.getInt(1));
                        connection.commit();

//                    LOGGER.log(Level.INFO, "Adresse créée avec l'ID {0}", adresse.getId());

                    } else {
                        throw new SQLException("L'insertion a échoué, aucun ID généré");
                    }
                }
                return adresse;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de création", e);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }

            LOGGER.log(Level.SEVERE, "Erreur lors de la création de l'adresse", e);

            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "create",
                    adresse.getId(),
                    "Erreur lors de la création de l'adresse : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivatio de l'autoCommit", ex);
            }
        }
    }

    /**
     * Met à jour une adresse existante dans la base de données.
     *
     * @param adresse l'adresse à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi, false sinon
     * @throws DAOException si une erreur survient lors de la mise à jour
     */
    public boolean save(Adresse adresse) throws DAOException {
        if (adresse == null || adresse.getId() == null || adresse.getId() <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    adresse != null ? adresse.getId() : null,
                    "L'adresse doit avoir un ID valide pour être mise à jour"
            );
        }

        String query = "UPDATE adresse " +
                "SET numero_rue = ?, " +
                "nom_rue = ?, " +
                "code_postal = ?, " +
                "ville = ? " +
                "WHERE id_adresse = ?";
        Connection connection = dbConnexion.getConnection();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = dbConnexion.getConnection().prepareStatement(query)) {
                pstmt.setString(1, adresse.getNumeroRue());
                pstmt.setString(2, adresse.getNomRue());
                pstmt.setString(3, adresse.getCodePostal());
                pstmt.setString(4, adresse.getVille());
                pstmt.setInt(5, adresse.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
//                LOGGER.log(Level.INFO, "Adresse mise à jour avec l'ID {0}", adresse.getId());
                    return true;
                } else {
                    connection.rollback();
//                LOGGER.log(Level.WARNING, "Aucune adresse trouvée avec l'ID {0} pour la mise à jour", adresse.getId());
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de mise à jour", e);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'adresse avec ID " + adresse.getId(), e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "save",
                    adresse.getId(),
                    "Erreur lors de la mise à jour de l'adresse : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", ex);
            }
        }
    }


    /**
     * Supprime une adresse de la base de données avec transaction.
     * <p>
     * ATTENTION : La suppression échouera si l'adresse est référencée par
     * une ou plusieurs sociétés (clients ou prospects) en raison des
     * contraintes de clé étrangère. Une exception FOREIGN_KEY_VIOLATION
     * sera levée dans ce cas.
     *
     * @param id l'identifiant de l'adresse à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws DAOException si une erreur survient lors de la suppression
     */
    public boolean delete(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "delete",
                    id,
                    "L'ID doit être un entier positif non null pour supprimer une adresse"
            );
        }

        Connection conn = dbConnexion.getConnection();

        try {
            // Démarrer la transaction
            conn.setAutoCommit(false);

            // Supprimer l'adresse
            String query2 = "DELETE FROM adresse WHERE id_adresse = ?";
            try (PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
                pstmt2.setInt(1, id);

                int rowsAffected = pstmt2.executeUpdate();

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
                LOGGER.log(Level.SEVERE, "Erreur lors de la suppression, rollback effectué", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression de l'adresse ID= " + id, e);

            // Analyse spécifique pour les violations de clés étrangères
            if (SQLExceptionAnalyzer.isForeignKeyViolation(e)){
                String constraintName = SQLExceptionAnalyzer.extractConstraintName(e);
                throw new DAOException(
                        DAOException.ErrorCode.FOREIGN_KEY_VIOLATION,
                        "delete",
                        id,
                        "Impossible de supprimer l'adresse : elle est référencée par une ou plusieurs sociétés" +
                                (constraintName == null ? "(contrainte: " + constraintName + ")" : ""),
                        e
                );
            }
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "delete",
                    id,
                    "Erreur lors de la suppression de l'adresse : " + SQLExceptionAnalyzer.analyze(e),
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
     * Méthode utilitaire pour mapper un ResultSet vers un objet Adresse.
     *
     * @param rs le ResultSet contenant les données
     * @return l'objet Adresse créé
     * @throws SQLException        si une erreur survient lors de la lecture du ResultSet
     * @throws ValidationException si les données ne respectent pas les règles métier
     */
    private Adresse mapResultSetToAdresse(ResultSet rs) throws SQLException, ValidationException {
        Adresse adresse = new Adresse(
                rs.getString("numero_rue"),
                rs.getString("nom_rue"),
                rs.getString("code_postal"),
                rs.getString("ville")
        );
        adresse.setId(rs.getInt("id_adresse"));
        return adresse;
    }
}
