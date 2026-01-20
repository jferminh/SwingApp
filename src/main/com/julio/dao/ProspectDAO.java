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

/**
 * Classe DAO pour la gestion des prospects en base de données.
 * Implémente le pattern Data Access Object (DAO) pour l'entité Prospect.
 * <p>
 * Structure de la base de données :
 * Table prospect : (id, id_societe FK, date_prospection, interesse)
 * Table societe : (id, raison_sociale, adresse_id FK, telephone, email, commentaires, type_societe)
 * Table adresse : (id, numero_rue, nom_rue, code_postal, ville)
 * <p>
 * Cette classe gère les opérations CRUD sur les prospects et leurs relations
 * avec les sociétés et adresses, en utilisant des transactions pour garantir
 * l'intégrité des données.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 20/01/2026
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
        String sql = "SELECT p.id_prospect, p.id_societe, p.date_prospection, p.interesse, " +
                "s.raison_sociale, s.adresse_id, s.telephone, s.email, s.commentaires, " +
                "a.numero_rue, a.nom_rue, a.code_postal, a.ville " +
                "FROM prospect p " +
                "INNER JOIN societe s ON p.id_societe = s.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse " +
                "ORDER BY s.raison_sociale";

        try (Statement stmt = dbConnexion.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Prospect prospect = mapResultSetToProspect(rs);
                prospects.add(prospect);
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
        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping dans findAll()", e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findAll",
                    null,
                    "Erreur de validation des données du prospect : " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Récupère un prospect par son identifiant.
     *
     * @param id l'identifiant du prospect (id dans table prospect, pas id_societe)
     * @return le prospect correspondant ou null si non trouvé
     * @throws DAOException si une erreur survient lors de la requête
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

        String sql = "SELECT p.id_prospect, p.id_societe, p.date_prospection, p.interesse, " +
                "s.raison_sociale, s.adresse_id, s.telephone, s.email, s.commentaires, " +
                "a.numero_rue, a.nom_rue, a.code_postal, a.ville " +
                "FROM prospect p " +
                "INNER JOIN societe s ON p.id_societe = s.id_societe " +
                "INNER JOIN adresse a ON s.adresse_id = a.id_adresse " +
                "WHERE p.id_prospect = ? ";

        try (PreparedStatement pstmt = dbConnexion.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Prospect prospect = mapResultSetToProspect(rs);
                    return prospect;
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
                    "Erreur lors de la recherche du prospect : " + SQLExceptionAnalyzer.analyze(e),
                    e
            );
        } catch (ValidationException e) {
            LOGGER.log(Level.SEVERE, "Erreur de validation lors du mapping dans findById, ID=" + id, e);
            throw new DAOException(
                    DAOException.ErrorCode.READ_ERROR,
                    "findById",
                    id,
                    "Erreur de validation des données du prospect : " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Insère un nouveau prospect dans la base de données avec transaction.
     * <p>
     * Processus :
     * 1. Démarre une transaction
     * 2. Insère la partie société (table societe) via SocieteDAO
     * 3. Insère la partie prospect (table prospect) avec id_societe comme FK
     * 4. Commit de la transaction
     *
     * @param prospect le prospect à insérer
     * @return le prospect avec son ID généré
     * @throws DAOException si une erreur survient lors de l'insertion
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

        Connection conn = dbConnexion.getConnection();

        try {
            conn.setAutoCommit(false);

            // 1. Insérer la partie société (via classe mère)
            Integer societeId = createSociete(prospect);

            // 2. Insérer la partie prospect avec id_societe comme FK
            String sql = "INSERT INTO prospect (id_societe, date_prospection, interesse) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, societeId);
                pstmt.setDate(2, Date.valueOf(prospect.getDateProspection()));
                pstmt.setInt(3, prospect.getInteresse().toInt());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("L'insertion du prospect a échoué, aucune ligne affectée");
                }

                // Récupérer l'ID généré pour le prospect
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer prospectId = generatedKeys.getInt(1);
                        prospect.setId(prospectId);  // ID de la table prospect

                        conn.commit();
                        LOGGER.log(Level.INFO,
                                "Prospect créé avec succès : ID prospect={0}, ID société={1}, Raison sociale={2}, Date={3}, Intéressé={4}",
                                new Object[]{prospectId, societeId, prospect.getRaisonSociale(),
                                        prospect.getDateProspection(), prospect.getInteresse()});
                    } else {
                        throw new SQLException("L'insertion a échoué, aucun ID généré");
                    }
                }

                return prospect;
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de création", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
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
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur DAO", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
        }
    }

    /**
     * Met à jour un prospect existant dans la base de données avec transaction.
     * <p>
     * Met à jour :
     * Les informations de la société (raison sociale, téléphone, email, etc.)
     * Les informations spécifiques du prospect (date prospection, intéressé)
     * L'adresse associée
     *
     * @param prospect le prospect à mettre à jour (doit avoir un ID valide)
     * @return true si la mise à jour a réussi, false sinon
     * @throws DAOException si une erreur survient lors de la mise à jour
     */
    public boolean save(Prospect prospect) throws DAOException {
        if (prospect == null || prospect.getId() == null || prospect.getId() <= 0) {
            LOGGER.log(Level.WARNING, "Tentative de save avec un prospect invalide : {0}", prospect);
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "save",
                    prospect != null ? prospect.getId() : null,
                    "Le prospect doit avoir un ID valide pour être mis à jour"
            );
        }

        Connection conn = dbConnexion.getConnection();
        boolean originalAutoCommit = true;

        try {
            originalAutoCommit = conn.getAutoCommit();

            if (originalAutoCommit) {
                conn.setAutoCommit(false);
            }
            // Récupérer id_societe depuis la table prospect
            Integer societeId = null;
            String getSocieteIdSQL = "SELECT id_societe FROM prospect WHERE id_prospect = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(getSocieteIdSQL)) {
                pstmt.setInt(1, prospect.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        societeId = rs.getInt("id_societe");
                    } else {
                        if (originalAutoCommit) {
                            conn.rollback();
                        }
                        LOGGER.log(Level.WARNING, "Aucun prospect trouvé avec l'ID {0}", prospect.getId());
                        return false;
                    }
                }
            }

            // Metre à jour l'adresse
            if (prospect.getAdresse() != null && prospect.getAdresse().getId() != null) {
                adresseDAO.save(prospect.getAdresse(), conn);
            }

            // Mettre à jour la partie société
            saveSociete(prospect, societeId, conn);

            // Mettre à jour la partie prospect
            String sql = "UPDATE prospect SET date_prospection = ?, interesse = ? WHERE id_prospect = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, Date.valueOf(prospect.getDateProspection()));
                pstmt.setInt(2, prospect.getInteresse().toInt());
                pstmt.setInt(3, prospect.getId());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                        LOGGER.log(Level.INFO,
                                "Prospect mis à jour avec succès : ID prospect={0}, ID société={1}, Date={2}, Intéressé={3}",
                                new Object[]{prospect.getId(), societeId, prospect.getDateProspection(), prospect.getInteresse()});
                    }
                    return true;
                } else {
                    if (!conn.getAutoCommit()) {
                        conn.rollback();
                    }
                    LOGGER.log(Level.WARNING, "Aucun prospect trouvé avec l'ID {0} pour la mise à jour",
                            prospect.getId());
                    return false;
                }
            }

        } catch (SQLException e) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de mise à jour", e);
                }
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
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
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur DAO", e);
                }
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }
            throw e;
        } finally {
            try {
                if (originalAutoCommit && !conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
        }
    }

    /**
     * Supprime un prospect de la base de données avec transaction.
     * <p>
     * Processus de suppression en respectant les FK :
     * 1. Récupère id_societe et adresse_id
     * 2. Supprime l'enregistrement prospect (table prospect)
     * 3. Supprime l'enregistrement société (table societe) via SocieteDAO
     * 4. Vérifie si l'adresse est référencée par d'autres sociétés
     * 5. Si l'adresse n'est plus référencée, la supprime via AdresseDAO
     * <p>
     * Note : Si la suppression échoue à n'importe quelle étape,
     * toute la transaction est annulée (rollback).
     *
     * @param id l'identifiant du prospect (id dans la table prospect)
     * @return true si la suppression a réussi, false sinon
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

        LOGGER.log(Level.INFO, "Début de la suppression du prospect ID={0}", id);

        Connection conn = dbConnexion.getConnection();
        Integer societeId = null;
        Integer adresseId = null;

        try {
            conn.setAutoCommit(false);

            // 1. RÉCUPÉRER id_societe ET adresse_id
            String getIdsSQL = "SELECT p.id_societe, s.adresse_id " +
                    "FROM prospect p " +
                    "INNER JOIN societe s ON p.id_societe = s.id_societe " +
                    "WHERE p.id_prospect = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(getIdsSQL)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        societeId = rs.getInt("id_societe");
                        adresseId = rs.getInt("adresse_id");
                    } else {
                        conn.rollback();
                        LOGGER.log(Level.WARNING, "Aucun prospect trouvé avec l'ID {0}", id);
                        return false;
                    }
                }
            }

            // 2. SUPPRIMER L'ENREGISTREMENT PROSPECT
            String deleteProspectSQL = "DELETE FROM prospect WHERE id_prospect = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteProspectSQL)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                LOGGER.log(Level.FINE, "Enregistrement prospect supprimé : ID={0}", id);
            }

            // 3. SUPPRIMER L'ENREGISTREMENT SOCIÉTÉ (via SocieteDAO)
            try {
                deleteSocieteInTransaction(societeId);
                LOGGER.log(Level.FINE, "Enregistrement société supprimé via SocieteDAO : ID={0}", societeId);
            } catch (DAOException e) {
                // Propager l'exception pour déclencher le rollback
                throw e;
            }

            // 4. VÉRIFIER SI L'ADRESSE EST RÉFÉRENCÉE PAR D'AUTRES SOCIÉTÉS
            boolean adresseEstReferenciee = false;
            if (adresseId != null) {
                String checkAdresseSQL = "SELECT COUNT(*) AS nb FROM societe WHERE adresse_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checkAdresseSQL)) {
                    pstmt.setInt(1, adresseId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int nbReferences = rs.getInt("nb");
                            adresseEstReferenciee = (nbReferences > 0);
                            LOGGER.log(Level.FINE,
                                    "Adresse ID={0} : {1} référence(s) trouvée(s)",
                                    new Object[]{adresseId, nbReferences});
                        }
                    }
                }
            }

            // 5. SUPPRIMER L'ADRESSE SI ELLE N'EST PLUS RÉFÉRENCÉE (via AdresseDAO)
            if (adresseId != null && !adresseEstReferenciee) {
                try {
                    adresseDAO.deleteAdresseInTransaction(adresseId);
                    LOGGER.log(Level.FINE, "Adresse supprimée via AdresseDAO : ID={0}", adresseId);
                } catch (DAOException e) {
                    // Si l'adresse ne peut pas être supprimée (FK), on continue quand même
                    // car la suppression du prospect a réussi
                    LOGGER.log(Level.WARNING,
                            "Impossible de supprimer l'adresse ID={0} : {1}",
                            new Object[]{adresseId, e.getMessage()});
                }
            } else if (adresseId != null) {
                LOGGER.log(Level.INFO,
                        "Adresse conservée car référencée par d'autres sociétés : ID={0}", adresseId);
            }

            conn.commit();
            LOGGER.log(Level.INFO,
                    "Prospect supprimé avec succès : ID prospect={0}, ID société={1}, Adresse {2}",
                    new Object[]{id, societeId,
                            adresseEstReferenciee ? "conservée (ID=" + adresseId + ")" : "supprimée (ID=" + adresseId + ")"});

            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur de suppression", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }

            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la suppression du prospect ID=" + id, e);

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
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Rollback effectué suite à l'erreur DAO", e);
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", rollbackEx);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la réactivation de l'autoCommit", e);
            }
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
            Integer prospectId = rs.getInt("p.id_prospect");
            Integer societeId = rs.getInt("p.id_societe");
            String raisonSociale = rs.getString("s.raison_sociale");
            String telephone = rs.getString("s.telephone");
            String email = rs.getString("s.email");
            String commentaires = rs.getString("s.commentaires");

            // Adresse
            Integer adresseId = rs.getInt("s.adresse_id");
            String numeroRue = rs.getString("a.numero_rue");
            String nomRue = rs.getString("a.nom_rue");
            String codePostal = rs.getString("a.code_postal");
            String ville = rs.getString("a.ville");

            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);
            adresse.setId(adresseId);

            // Prospect spécifique
            java.sql.Date sqlDate = rs.getDate("p.date_prospection");
            LocalDate dateProspection = sqlDate != null ? sqlDate.toLocalDate() : null;

            int interesseInt = rs.getInt("p.interesse");
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

            LOGGER.log(Level.FINE,
                    "Prospect mappé : ID={0}, Raison sociale={1}, Intéressé={2}",
                    new Object[]{prospectId, raisonSociale, interesse});

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
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE,
                    "Valeur 'interesse' invalide dans la BDD : " + rs.getInt("p.interesse"), e);
            throw new DAOException(
                    DAOException.ErrorCode.INVALID_PARAMETER,
                    "mapResultSetToProspect",
                    null,
                    e.getMessage(),
                    e
            );
        }
    }
}
