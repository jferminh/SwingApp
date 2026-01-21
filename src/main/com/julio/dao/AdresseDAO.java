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

import static main.com.julio.util.JdbcUtil.closeResources;

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

        String sql = "INSERT INTO adresse (numero_rue, nom_rue, code_postal, ville) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            // ✅ Récupérer la connexion (peut être en transaction)
            Connection connection = dbConnexion.getConnection();

            pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, adresse.getNumeroRue());
            pstmt.setString(2, adresse.getNomRue());
            pstmt.setString(3, adresse.getCodePostal());
            pstmt.setString(4, adresse.getVille());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("L'insertion de l'adresse a échoué, aucune ligne affectée");
            }

            generatedKeys = pstmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                Integer adresseId = generatedKeys.getInt(1);
                adresse.setId(adresseId);

                return adresse;
            } else {
                throw new SQLException("L'insertion a échoué, aucun ID généré");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de create adresse", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "create",
                    null,
                    "Erreur création adresse : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            closeResources(generatedKeys, pstmt, null);
        }
    }


    /**
     * Met à jour une adresse existante dans la base de données.
     *
     * @param adresse l'adresse à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     * @throws DAOException si une erreur survient
     */
    public boolean save(Adresse adresse) throws DAOException {
        Connection conn = dbConnexion.getConnection();
        return save(adresse, conn);
    }

    /**
     * Met à jour une adresse existante dans la base de données.
     *
     * <p><strong>IMPORTANT :</strong> Cette méthode reçoit une connexion externe
     * et participe à une transaction parent. Elle NE DOIT PAS gérer commit/rollback.</p>
     *
     * @param adresse l'adresse à mettre à jour (doit avoir un ID valide)
     * @param connection la connexion à utiliser (en transaction)
     * @return true si la mise à jour a réussi, false si l'adresse n'existe pas
     * @throws DAOException si une erreur survient lors de la mise à jour
     */
    public boolean save(Adresse adresse, Connection connection) throws DAOException {
        if (adresse == null || adresse.getId() == null || adresse.getId() <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    adresse != null ? adresse.getId() : null,
                    "L'adresse doit avoir un ID valide pour être mise à jour"
            );
        }

        if (connection == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    adresse.getId(),
                    "La connexion ne peut pas être null"
            );
        }

        String sql = "UPDATE adresse " +
                "SET numero_rue = ?, nom_rue = ?, code_postal = ?, ville = ? " +
                "WHERE id_adresse = ?";

        PreparedStatement pstmt = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources
            // La connexion est gérée par l'appelant (transaction parent)
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, adresse.getNumeroRue());
            pstmt.setString(2, adresse.getNomRue());
            pstmt.setString(3, adresse.getCodePostal());
            pstmt.setString(4, adresse.getVille());
            pstmt.setInt(5, adresse.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.log(Level.FINE,
                        "Adresse mise à jour : ID={0}, {1} {2}, {3} {4}",
                        new Object[]{adresse.getId(), adresse.getNumeroRue(), adresse.getNomRue(),
                                adresse.getCodePostal(), adresse.getVille()});
                return true;
            } else {
                LOGGER.log(Level.WARNING,
                        "Aucune adresse trouvée avec l'ID {0} pour la mise à jour",
                        adresse.getId());
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur SQL lors de la mise à jour de l'adresse ID=" + adresse.getId(), e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "save",
                    adresse.getId(),
                    "Erreur lors de la mise à jour de l'adresse : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT le PreparedStatement
            // NE PAS :
            // - Fermer la connexion (gérée par l'appelant)
            // - Faire commit/rollback (géré par l'appelant)
            // - Modifier autoCommit (géré par l'appelant)

            closeResources(null, pstmt, null);
        }
    }

    /**
     * Supprime une adresse dans le contexte d'une transaction parent.
     * <p>
     * <strong>IMPORTANT :</strong> Cette méthode participe à une transaction
     * gérée par l'appelant (ClientDAO ou ProspectDAO). Elle NE DOIT PAS gérer
     * commit/rollback ni fermer la connexion.
     * </p>
     *
     * @param connection la connexion en transaction (non null)
     * @param adresseId l'ID de l'adresse à supprimer
     * @throws DAOException si une erreur survient lors de la suppression
     */
    protected void deleteAdresse(Connection connection, Integer adresseId)
            throws DAOException {

        if (connection == null) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteAdresse",
                    adresseId,
                    "La connexion ne peut pas être null"
            );
        }

        if (adresseId == null || adresseId <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "deleteAdresse",
                    adresseId,
                    "L'ID adresse doit être un entier positif non null"
            );
        }

        String sql = "DELETE FROM adresse WHERE id_adresse = ?";
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, adresseId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected <= 0) {
                throw new DAOException(
                        DAOException.ErrorCode.ENTITY_NOT_FOUND,
                        "deleteAdresse",
                        adresseId,
                        "Aucune adresse trouvée avec l'ID " + adresseId
                );
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression de l'adresse ID=" + adresseId, e);

            if (SQLExceptionAnalyzer.isForeignKeyViolation(e)) {
                String constraintName = SQLExceptionAnalyzer.extractConstraintName(e);
                throw new DAOException(
                        DAOException.ErrorCode.FOREIGN_KEY_VIOLATION,
                        "deleteAdresse",
                        adresseId,
                        "Impossible de supprimer l'adresse : elle est référencée par d'autres entités" +
                                (constraintName != null ? " (contrainte: " + constraintName + ")" : ""),
                        e
                );
            }

            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "deleteAdresse",
                    adresseId,
                    "Erreur lors de la suppression de l'adresse : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT le PreparedStatement
            closeResources(null, pstmt, null);
            // ❌ NE PAS fermer connection (Singleton)
            // ❌ NE PAS gérer transaction (responsabilité de l'appelant)
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
