package main.com.julio.dao;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.service.LoggerService;
import main.com.julio.util.SQLExceptionAnalyzer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.com.julio.util.JdbcUtil.closeResources;

/**
 * DAO pour la gestion de la persistance des prospects.
 * <p>
 * Gère les opérations CRUD sur la table {@code prospect} et les tables
 * associées ({@code societe}, {@code adresse}) via transactions ACID.
 * </p>
 *
 * <h2>Opérations</h2>
 * <ul>
 *   <li>{@link #create(Prospect)} - Crée un prospect</li>
 *   <li>{@link #findById(Integer)} - Recherche par ID</li>
 *   <li>{@link #findAll()} - Liste tous les prospects</li>
 *   <li>{@link #findByRaisonSociale(String)} - Recherche par raison sociale</li>
 *   <li>{@link #save(Prospect)} - Modifie un prospect</li>
 *   <li>{@link #delete(Integer)} - Supprime un prospect</li>
 * </ul>
 *
 * <h2>Contraintes</h2>
 * <ul>
 *   <li><b>Unique</b> : raison_sociale</li>
 *   <li><b>Enum</b> : interesse (OUI=1, NON=0) - mapping automatique</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 * @see Prospect
 * @see Interesse
 * @see DAOException
 */
public class ProspectDAO extends SocieteDAO {

    private static final Logger LOGGER = LoggerService.getLogger(ProspectDAO.class);
    private final AdresseDAO adresseDAO;

    /**
     * Constructeur qui récupère l'instance de DatabaseConnection.
     *
     * @throws DAOException si la connexion à la base de données échoue
     */
    public ProspectDAO() throws DAOException {
        super();
        this.adresseDAO = new AdresseDAO();
    }

    /**
     * Récupère tous les prospects de la base de données avec leurs adresses.
     * <p>
     * Effectue une jointure entre les tables prospect, societe et adresse
     * pour récupérer toutes les informations en une seule requête.
     * Les prospects sont triés par raison sociale.
     *
     * @return une liste de tous les prospects
     * @throws DAOException si une erreur survient lors de la requête
     */
    public List<Prospect> findAll() throws DAOException {
        List<Prospect> prospects = new ArrayList<>();

        String sql = "SELECT " +
                "    p.id_prospect, p.id_societe, p.date_prospection, p.interesse, " +
                "    s.raison_sociale, a.id_adresse, s.telephone, s.email, s.commentaires, " +
                "    a.numero_rue, a.nom_rue, a.code_postal, a.ville " +
                "FROM prospect p " +
                "INNER JOIN societe s ON p.id_societe = s.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse " +
                "ORDER BY s.raison_sociale ASC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Connection conn = dbConnexion.getConnection();
            pstmt = conn.prepareStatement(sql); // ✅ PreparedStatement au lieu de Statement
            rs = pstmt.executeQuery();

            while (rs.next()) {
                try {
                    Prospect prospect = mapResultSetToProspect(rs);
                    prospects.add(prospect);

                } catch (ValidationException e) {
                    Integer prospectId = rs.getInt("id_prospect");
                }
            }

            return prospects;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de findAll()", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findAll",
                    null,
                    "Erreur lors de la récupération de tous les prospects : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            closeResources(rs, pstmt, null);
        }
    }

    /**
     * Recherche un prospect par son identifiant.
     *
     * @param id l'identifiant du prospect à rechercher
     * @return le prospect trouvé, ou null si aucun prospect ne correspond
     * @throws DAOException si une erreur survient lors de la recherche
     */
    public Prospect findById(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "findById",
                    id,
                    "L'ID doit être un entier positif non null"
            );
        }

        String sql = "SELECT " +
                "    p.id_prospect, p.id_societe, p.date_prospection, p.interesse, " +
                "    s.raison_sociale, a.id_adresse, s.telephone, s.email, s.commentaires, " +
                "    a.numero_rue, a.nom_rue, a.code_postal, a.ville " +
                "FROM prospect p " +
                "INNER JOIN societe s ON p.id_societe = s.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse " +
                "WHERE p.id_prospect = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // ✅ CORRECTION : Ne pas utiliser try-with-resources sur la connexion
            Connection conn = dbConnexion.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                try {
                    Prospect prospect = mapResultSetToProspect(rs);

                    return prospect;

                } catch (ValidationException e) {
                    LOGGER.log(Level.SEVERE,
                            "Erreur de validation lors du mapping du prospect ID={0}", id);
                    throw new DAOException(
                            DAOException.ErrorCode.INVALID_PARAMETER,
                            "findById",
                            id,
                            "Données invalides pour le prospect : " + e.getMessage(),
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
                    "Erreur lors de la recherche du prospect : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            // ✅ IMPORTANT : Fermer SEULEMENT ResultSet et PreparedStatement
            // NE PAS FERMER la connexion (gérée par Singleton)
            closeResources(rs, pstmt, null);
        }
    }

    /**
     * Crée un nouveau prospect dans la base de données.
     *
     * <p>Cette méthode effectue une transaction qui :</p>
     * <ul>
     *   <li>Insère d'abord la société (via createSociete)</li>
     *   <li>Puis insère le prospect avec l'ID société généré</li>
     * </ul>
     *
     * @param prospect le prospect à créer (ne doit pas être null)
     * @return le prospect créé avec son ID généré
     * @throws DAOException si une erreur survient lors de la création
     */
    public Prospect create(Prospect prospect) throws DAOException {
        if (prospect == null) {
            LOGGER.severe("Tentative de create avec un prospect null");
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "create",
                    null,
                    "Le prospect ne peut pas être null"
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
            Integer societeId = createSociete(prospect);

            // ========== ÉTAPE 2 : Insérer la partie prospect ==========
            String sql = "INSERT INTO prospect (id_societe, date_prospection, interesse) " +
                    "VALUES (?, ?, ?)";

            pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, societeId);
            pstmt.setDate(2, Date.valueOf(prospect.getDateProspection()));
            pstmt.setInt(3, prospect.getInteresse().toInt());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("L'insertion du prospect a échoué, aucune ligne affectée");
            }

            // ========== ÉTAPE 3 : Récupérer l'ID généré ==========
            generatedKeys = pstmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                Integer prospectId = generatedKeys.getInt(1);
                prospect.setId(prospectId);  // ID de la table prospect

                // ✅ COMMIT : Transaction réussie
                connection.commit();

                LOGGER.log(Level.INFO,
                        "Prospect créé avec succès : ID prospect={0}, ID société={1}, " +
                                "Raison sociale={2}, Date={3}, Intéressé={4}",
                        new Object[]{prospectId, societeId, prospect.getRaisonSociale(),
                                prospect.getDateProspection(), prospect.getInteresse()});

                return prospect;

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

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la création du prospect", e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "create",
                    prospect.getId(),
                    "Erreur lors de la création du prospect : " + SQLExceptionAnalyzer.analyze(e),
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
            // ✅ IMPORTANT : Fermer les ressources et réactiver autoCommit
            // NE PAS FERMER la connexion (Singleton)

            closeResources(generatedKeys, pstmt, connection);

            // ❌ NE PAS FERMER connection (gérée par Singleton)
        }
    }

    /**
     * Met à jour un prospect existant dans la base de données.
     *
     * <p>Cette méthode effectue une transaction qui met à jour :</p>
     * <ul>
     *   <li>L'adresse (si elle existe et a un ID)</li>
     *   <li>La société</li>
     *   <li>Le prospect</li>
     * </ul>
     *
     * @param prospect le prospect à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi, false si le prospect n'existe pas
     * @throws DAOException si une erreur survient lors de la mise à jour
     */
    public boolean save(Prospect prospect) throws DAOException {
        if (prospect == null || prospect.getId() == null || prospect.getId() <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    prospect != null ? prospect.getId() : null,
                    "Le prospect doit avoir un ID valide pour être mis à jour"
            );
        }

        Connection connection = null;
        PreparedStatement pstmtGetSociete = null;
        PreparedStatement pstmtUpdateProspect = null;
        ResultSet rs = null;

        try {
            // ✅ CORRECTION : Récupérer la connexion sans try-with-resources
            connection = dbConnexion.getConnection();
            connection.setAutoCommit(false);

            // ========== ÉTAPE 1 : Récupérer id_societe ==========
            Integer societeId = null;
            String getSocieteIdSQL = "SELECT id_societe FROM prospect WHERE id_prospect = ?";

            pstmtGetSociete = connection.prepareStatement(getSocieteIdSQL);
            pstmtGetSociete.setInt(1, prospect.getId());
            rs = pstmtGetSociete.executeQuery();

            if (rs.next()) {
                societeId = rs.getInt("id_societe");
            } else {
                connection.rollback();
                return false;
            }

            // Fermer rs et pstmt
            rs.close();
            rs = null;
            pstmtGetSociete.close();
            pstmtGetSociete = null;

            // ========== ÉTAPE 2 : Mettre à jour l'adresse ==========
            if (prospect.getAdresse() != null && prospect.getAdresse().getId() != null) {
                adresseDAO.save(prospect.getAdresse(), connection);
            }

            // ========== ÉTAPE 3 : Mettre à jour la société ==========
            saveSociete(prospect, societeId, connection);

            // ========== ÉTAPE 4 : Mettre à jour le prospect ==========
            String sql = "UPDATE prospect " +
                    "SET date_prospection = ?, interesse = ? " +
                    "WHERE id_prospect = ?";

            pstmtUpdateProspect = connection.prepareStatement(sql);
            pstmtUpdateProspect.setDate(1, Date.valueOf(prospect.getDateProspection()));
            pstmtUpdateProspect.setInt(2, prospect.getInteresse().toInt());
            pstmtUpdateProspect.setInt(3, prospect.getId());

            int rowsAffected = pstmtUpdateProspect.executeUpdate();

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

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la mise à jour du prospect ID=" + prospect.getId(), e);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "save",
                    prospect.getId(),
                    "Erreur lors de la mise à jour du prospect : " + SQLExceptionAnalyzer.analyze(e),
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
            if (pstmtGetSociete != null && pstmtUpdateProspect == null) {
                closeResources(rs, pstmtGetSociete, connection);
            } else if (pstmtUpdateProspect != null) {
                closeResources(null, pstmtUpdateProspect, connection);
            } else {
                closeResources(rs, null, connection);
            }

            // ❌ NE PAS FERMER connection (Singleton)
        }
    }

    /**
     * Supprime un prospect de la base de données.
     *
     * <p>Cette méthode effectue une transaction qui :</p>
     * <ol>
     *   <li>Vérifie que le prospect existe</li>
     *   <li>Supprime le prospect</li>
     *   <li>Supprime la société associée</li>
     *   <li>Supprime l'adresse si elle n'est plus référencée</li>
     * </ol>
     *
     * @param id l'ID du prospect à supprimer
     * @return true si la suppression a réussi, false si le prospect n'existe pas
     * @throws DAOException si une erreur survient lors de la suppression
     */
    public boolean delete(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "delete",
                    id,
                    "L'ID doit être un entier positif non null pour supprimer un prospect"
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

            // ========== ÉTAPE 1 : Récupérer id_societe et adresse_id ==========
            String getIdsSQL = "SELECT p.id_societe, s.adresse_id " +
                    "FROM prospect p " +
                    "INNER JOIN societe s ON p.id_societe = s.id_societe " +
                    "WHERE p.id_prospect = ?";

            pstmt = connection.prepareStatement(getIdsSQL);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                societeId = rs.getInt("id_societe");
                adresseId = rs.getInt("adresse_id");
            } else {
                connection.rollback();
                return false;
            }

            // Fermer rs et pstmt
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;

            // ========== ÉTAPE 2 : Supprimer le prospect ==========
            String deleteProspectSQL = "DELETE FROM prospect WHERE id_prospect = ?";
            pstmt = connection.prepareStatement(deleteProspectSQL);
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Aucune ligne supprimée dans la table prospect pour ID=" + id);
            }

            pstmt.close();
            pstmt = null;

            // ========== ÉTAPE 3 : Supprimer la société ==========
            this.deleteSociete(connection, societeId);
            LOGGER.log(Level.FINE, "Société supprimée : ID={0}", societeId);

            // ========== ÉTAPE 4 : Vérifier si l'adresse est référencée ==========
            boolean adresseEstReferenciee = false;

            if (adresseId != null) {
                String checkAdresseSQL = "SELECT COUNT(*) AS nb FROM societe WHERE adresse_id = ?";
                pstmt = connection.prepareStatement(checkAdresseSQL);
                pstmt.setInt(1, adresseId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    int nbReferences = rs.getInt("nb");
                    adresseEstReferenciee = (nbReferences > 0);
                    LOGGER.log(Level.FINE,
                            "Adresse ID={0} : {1} référence(s) trouvée(s)",
                            new Object[]{adresseId, nbReferences});
                }

                rs.close();
                rs = null;
                pstmt.close();
                pstmt = null;
            }

            // ========== ÉTAPE 5 : Supprimer l'adresse si elle n'est plus référencée ==========
            if (adresseId != null && !adresseEstReferenciee) {
                try {
                    adresseDAO.deleteAdresse(connection, adresseId);
                    LOGGER.log(Level.FINE, "Adresse supprimée : ID={0}", adresseId);
                } catch (DAOException e) {
                    // Si l'adresse ne peut pas être supprimée, on log mais on continue
                    LOGGER.log(Level.WARNING,
                            "Impossible de supprimer l'adresse ID={0} : {1}",
                            new Object[]{adresseId, e.getMessage()});
                }
            } else if (adresseId != null) {
                LOGGER.log(Level.INFO,
                        "Adresse conservée car référencée par d'autres sociétés : ID={0}", adresseId);
            }

            // ========== COMMIT ==========
            connection.commit();

            LOGGER.log(Level.INFO,
                    "Prospect supprimé avec succès : ID prospect={0}, ID société={1}, Adresse {2}",
                    new Object[]{id, societeId,
                            adresseEstReferenciee ? "conservée (ID=" + adresseId + ")" : "supprimée (ID=" + adresseId + ")"});

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

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression du prospect ID=" + id, e);

            // Vérifier si c'est une violation de clé étrangère
            if (SQLExceptionAnalyzer.isForeignKeyViolation(e)) {
                String constraintName = SQLExceptionAnalyzer.extractConstraintName(e);
                throw new DAOException(
                        DAOException.ErrorCode.FOREIGN_KEY_VIOLATION,
                        "delete",
                        id,
                        "Impossible de supprimer le prospect : il est référencé par d'autres entités" +
                                (constraintName != null ? " (contrainte: " + constraintName + ")" : ""),
                        e
                );
            }

            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "delete",
                    id,
                    "Erreur lors de la suppression du prospect : " + SQLExceptionAnalyzer.analyze(e),
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

            // ❌ NE PAS FERMER connection (Singleton)
        }
    }

    /**
     * Recherche un prospect par sa raison sociale (exact match, sensible à la casse).
     *
     * @param raisonSociale la raison sociale à rechercher
     * @return le prospect trouvé ou null si non trouvé
     * @throws DAOException si une erreur survient lors de la recherche
     */
    public Prospect findByRaisonSociale(String raisonSociale) throws DAOException {
        if (raisonSociale == null || raisonSociale.trim().isEmpty()) {
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "findByRaisonSociale",
                    null,
                    "La raison sociale ne peut pas être null ou vide"
            );
        }

        String sql = "SELECT p.id_prospect, s.raison_sociale, " +
                "a.id_adresse, a.numero_rue, a.nom_rue, a.code_postal, a.ville, " +
                "s.telephone, s.email, s.commentaires, " +
                "p.date_prospection, p.interesse " +
                "FROM prospect p " +
                "INNER JOIN societe s ON p.id_societe = s.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse " +
                "WHERE s.raison_sociale = ?";

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = dbConnexion.getConnection();
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, raisonSociale);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                try {
                    return mapResultSetToProspect(rs);

                } catch (ValidationException e) {
                    LOGGER.log(Level.SEVERE,
                            "Erreur validation données prospect avec raison sociale ''{0}''",
                            raisonSociale);
                    throw new DAOException(
                            DAOException.ErrorCode.INVALID_PARAMETER,
                            "findByRaisonSociale",
                            null,
                            "Données invalides pour le prospect : " + e.getMessage(),
                            e
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur SQL lors de findByRaisonSociale avec ''{0}''",
                    raisonSociale);
            throw new DAOException(
                    SQLExceptionAnalyzer.categorize(e),
                    "findByRaisonSociale",
                    null,
                    "Erreur lors de la recherche par raison sociale : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } finally {
            closeResources(rs, pstmt, connection);
        }
    }

    /**
     * Méthode utilitaire privée pour mapper un ResultSet vers un objet Prospect.
     * <p>
     * Cette méthode reconstruit un objet Prospect complet à partir des données
     * d'une requête SQL joignant les tables prospect, societe et adresse.
     *
     * @param rs le ResultSet contenant les données du prospect
     * @return un objet Prospect reconstitué
     * @throws SQLException si une erreur survient lors de la lecture du ResultSet
     * @throws DAOException si les données ne respectent pas les contraintes métier
     */
    private Prospect mapResultSetToProspect(ResultSet rs) throws SQLException, DAOException, ValidationException {
        try {
            Integer prospectId = rs.getInt("id_prospect");
//            Integer societeId = rs.getInt("p.id_societe");
            String raisonSociale = rs.getString("raison_sociale");
            String telephone = rs.getString("telephone");
            String email = rs.getString("email");
            String commentaires = rs.getString("commentaires");

            // Adresse
            Integer adresseId = rs.getInt("id_adresse");
            String numeroRue = rs.getString("numero_rue");
            String nomRue = rs.getString("nom_rue");
            String codePostal = rs.getString("code_postal");
            String ville = rs.getString("ville");

            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);
            adresse.setId(adresseId);

            // Prospect spécifique
            java.sql.Date sqlDate = rs.getDate("date_prospection");
            LocalDate dateProspection = sqlDate != null ? sqlDate.toLocalDate() : null;

            int interesseInt = rs.getInt("interesse");
            Interesse interesse = Interesse.fromInt(interesseInt);

            // Créer le prospect
            Prospect prospect = new Prospect(
                    raisonSociale,
                    adresse,
                    telephone,
                    email,
                    commentaires,
                    dateProspection,
                    interesse
            );

            prospect.setId(prospectId);

            return prospect;

        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping du prospect", e);
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "mapResultSetToProspect",
                    null,
                    "Données invalides lors du mapping du prospect : " + e.getMessage(),
                    e
            );
        }
    }
}
