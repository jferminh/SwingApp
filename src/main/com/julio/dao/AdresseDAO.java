package main.com.julio.dao;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * Classe DAO pour la gestion des adresses en base de données.
 * Implémente le pattern Data Access Object (DAO) pour l'entité Adresse.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 14/01/2026
 */
public class AdresseDAO {
    private final DatabaseConnexion dbConnexion;

    /**
     * Constructeur qui récupère l'instance de DatabaseConnexion.
     */
    public AdresseDAO() throws SQLException {
        this.dbConnexion = DatabaseConnexion.getInstance();
    }

    /**
     * Récupère toutes les adresses de la base de données.
     *
     * @return une liste de toutes les adresses
     * @throws SQLException si une erreur survient lors de la requête
     */
    public List<Adresse> findAll() throws SQLException {
        List<Adresse> adresses = new ArrayList<>();
        String query = "SELECT * FROM Adresse";

        try (Statement stmt = dbConnexion.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Adresse adresse = mapResultSetToAdresse(rs);
                adresses.add(adresse);
            }

            LOGGER.log(Level.INFO, "Recupération de {0} adresses", adresses.size());
        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de toutes les adresses", e);
            throw e;
        } catch (ValidationException e){
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping");
            throw new SQLException("Erreur de validation lors des données", e);
        }
        return adresses;
    }

    /**
     * Récupère une adresse par son identifiant.
     *
     * @param id l'identifiant de l'adresse
     * @return l'adresse correspondante ou null si non trouvée
     * @throws SQLException si une erreur survient lors de la requête
     */
    public Adresse findById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            LOGGER.log(Level.WARNING, "Tentative de recherche avec un ID invalide : {0}", id);
            return null;
        }

        String query = "SELECT * FROM Adresse WHERE id_adresse = ?";
        try (PreparedStatement pstmt = dbConnexion.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Adresse adresse = mapResultSetToAdresse(rs);
                    return adresse;
                } else {
                    LOGGER.log(Level.INFO, "Aucune adresse trouvée avec l'ID {0}" + id);
                    return null;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de l'adresse avec l'ID {0}" + id, e);
            throw e;
        } catch (ValidationException e){
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping", e);
            throw new SQLException("Erreur de validation des données", e);
        }
    }

    /**
     * Insère une nouvelle adresse dans la base de données.
     * L'ID est généré automatiquement et affecté à l'objet.
     *
     * @param adresse l'adresse à insérer
     * @return l'adresse avec son ID généré
     * @throws SQLException si une erreur survient lors de l'insertion
     */
    public Adresse create(Adresse adresse) throws SQLException {
        if (adresse == null) {
            throw new IllegalArgumentException("L'adresse ne peut pas être null");
        }

        String query = "INSERT INTO adresse (numero_rue, nom_rue, code_postal, ville) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConnexion.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
                    LOGGER.log(Level.INFO, "Adresse créée avec l'ID {0}", adresse.getId());

                } else {
                    throw new SQLException("L'insertion a échoué, aucun ID généré");
                }
            }
            return adresse;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de l'adresse", e);
            throw e;
        }
    }

    /**
     * Met à jour une adresse existante dans la base de données.
     *
     * @param adresse l'adresse à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException si une erreur survient lors de la mise à jour
     */
    public boolean save(Adresse adresse) throws SQLException {
        if (adresse == null || adresse.getId() == null || adresse.getId() <= 0) {
            throw new IllegalArgumentException("L'adresse doit avoir un ID valide pour être mise à jour");
        }

        String query = "UPDATE adresse SET numero_rue = ?, nom_rue = ?, code_postal = ?, ville = ? WHERE id_adresse = ?";

        try (PreparedStatement pstmt = dbConnexion.getConnection().prepareStatement(query)) {
            pstmt.setString(1, adresse.getNumeroRue());
            pstmt.setString(2, adresse.getNomRue());
            pstmt.setString(3, adresse.getCodePostal());
            pstmt.setString(4, adresse.getVille());
            pstmt.setInt(5, adresse.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Adresse mise à jour avec l'ID {0}", adresse.getId());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Aucune adresse trouvée avec l'ID {0} pour la mise à jour", adresse.getId());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'adresse avec ID " + adresse.getId(), e);
            throw e;
        }
    }

    /**
     * Supprime une adresse de la base de données.
     * Utilise une transaction pour garantir la cohérence.
     *
     * @param id l'identifiant de l'adresse à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws SQLException si une erreur survient lors de la suppression
     */
    public boolean delete(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("L'ID doit être valide pour supprimer une adresse");
        }

        Connection conn = dbConnexion.getConnection();
        String query = "DELETE FROM adresse WHERE id_adresse = ?";

        try {
            // Démarrer la transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, id);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    LOGGER.log(Level.INFO, "Adresse supprimée avec l'ID {0}", id);
                    return true;
                } else {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Aucune adresse trouvée avec l'ID {0} pour la suppression", id);
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
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la connexion", e);
            }
        }
    }

    /**
     * Méthode utilitaire pour mapper un ResultSet vers un objet Adresse.
     *
     * @param rs le ResultSet contenant les données
     * @return l'objet Adresse créé
     * @throws SQLException si une erreur survient lors de la lecture du ResultSet
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
