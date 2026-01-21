package main.com.julio.viewmodel;

import main.com.julio.dao.ProspectDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.service.LoggerService;

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel pour la gestion des prospects.
 * Fait le lien entre la Vue et les DAO (pattern MVVM).
 * <p>
 * Cette classe orchestre les opérations CRUD sur les prospects en utilisant
 * ProspectDAO et en gérant les exceptions de manière appropriée pour la couche présentation.
 * <p>
 * Responsabilités :
 * 1. Validation des données d'entrée de l'utilisateur
 * 2. Orchestration des opérations DAO
 * 3. Transformation des exceptions DAO en exceptions métier
 * 4. Préparation des données pour l'affichage (TableModel)
 * 5. Logging des erreurs critiques uniquement
 *
 * @author Julio FERMIN
 * @version 2.1
 * @since 21/01/2026
 */
public class ProspectViewModel {
    private static final Logger LOGGER = LoggerService.getLogger(ProspectViewModel.class);

    private final ProspectDAO prospectDAO;

    /**
     * Constructeur avec injection du DAO.
     *
     * @throws DAOException si l'initialisation du DAO échoue
     */
    public ProspectViewModel() throws DAOException {
        this.prospectDAO = new ProspectDAO();
    }

    /**
     * Crée un nouveau prospect avec son adresse.
     * <p>
     * <strong>IMPORTANT :</strong> L'adresse est créée dans la même transaction
     * que le prospect via ProspectDAO.create(). Si la création échoue, aucune donnée
     * n'est persistée (rollback complet).
     * </p>
     *
     * @param raisonSociale raison sociale du prospect
     * @param numeroRue numéro de rue
     * @param nomRue nom de rue
     * @param codePostal code postal (5 chiffres)
     * @param ville ville
     * @param telephone téléphone (format validé)
     * @param email email (format validé)
     * @param commentaires commentaires optionnels
     * @param dateProspection date de prospection
     * @param interesse niveau d'intérêt (enum Interesse)
     * @return le prospect créé avec son ID généré
     * @throws ValidationException si les données ne respectent pas les contraintes métier
     * @throws DAOException si une erreur survient lors de la persistance
     */
    public Prospect creerProspect(String raisonSociale,
                                  String numeroRue,
                                  String nomRue,
                                  String codePostal,
                                  String ville,
                                  String telephone,
                                  String email,
                                  String commentaires,
                                  LocalDate dateProspection,
                                  Interesse interesse) throws ValidationException, DAOException {

        try {
            // Créer l'entité Prospect avec Adresse
            Adresse adresse = new main.com.julio.model.Adresse(
                    numeroRue,
                    nomRue,
                    codePostal,
                    ville
            );

            Prospect prospect = new Prospect(
                    raisonSociale,
                    adresse,
                    telephone,
                    email,
                    commentaires,
                    dateProspection,
                    interesse
            );

            // ProspectDAO.create() gère TOUTE la transaction
            prospect = prospectDAO.create(prospect);

            return prospect;

        } catch (ValidationException e) {
            throw e;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO création prospect : {0}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Modifie un prospect existant.
     * <p>
     * <strong>IMPORTANT :</strong> La modification de l'adresse est incluse
     * dans la transaction de ProspectDAO.save(). Rollback complet en cas d'erreur.
     * </p>
     *
     * @param id identifiant du prospect
     * @param raisonSociale nouvelle raison sociale
     * @param numeroRue nouveau numéro de rue
     * @param nomRue nouveau nom de rue
     * @param codePostal nouveau code postal
     * @param ville nouvelle ville
     * @param telephone nouveau téléphone
     * @param email nouvel email
     * @param commentaires nouveaux commentaires
     * @param dateProspection nouvelle date de prospection
     * @param interesse nouveau niveau d'intérêt
     * @return true si la modification a réussi, false si le prospect n'existe pas
     * @throws ValidationException si les données ne respectent pas les contraintes métier
     * @throws DAOException si une erreur survient lors de la persistance
     */
    public boolean modifierProspect(Integer id,
                                    String raisonSociale,
                                    String numeroRue,
                                    String nomRue,
                                    String codePostal,
                                    String ville,
                                    String telephone,
                                    String email,
                                    String commentaires,
                                    LocalDate dateProspection,
                                    Interesse interesse) throws ValidationException, DAOException {

        try {
            // Récupérer le prospect existant
            Prospect prospect = prospectDAO.findById(id);

            if (prospect == null) {
                return false;
            }

            // Mettre à jour les données
            prospect.setRaisonSociale(raisonSociale);
            prospect.setTelephone(telephone);
            prospect.setEmail(email);
            prospect.setCommentaires(commentaires);
            prospect.setDateProspection(dateProspection);
            prospect.setInteresse(interesse);

            // Mettre à jour l'adresse
            main.com.julio.model.Adresse adresse = prospect.getAdresse();
            adresse.setNumeroRue(numeroRue);
            adresse.setNomRue(nomRue);
            adresse.setCodePostal(codePostal);
            adresse.setVille(ville);

            // Persister les modifications
            return prospectDAO.save(prospect);

        } catch (ValidationException e) {
            throw e;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO modification prospect ID={0} : {1}",
                    new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Supprime un prospect.
     * <p>
     * <strong>IMPORTANT :</strong> La suppression est gérée en transaction
     * par ProspectDAO.delete(). L'adresse et la société associées sont supprimées
     * dans la même transaction.
     * </p>
     *
     * @param id identifiant du prospect à supprimer
     * @return true si la suppression a réussi, false si le prospect n'existe pas
     * @throws DAOException si une erreur survient lors de la suppression
     */
    public boolean supprimerProspect(Integer id) throws DAOException {
        try {
            return prospectDAO.delete(id);

        } catch (DAOException e) {
            throw e;
        }
    }

    /**
     * Récupère un prospect par son ID.
     *
     * @param id identifiant du prospect
     * @return le prospect ou null si non trouvé
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public Prospect getProspectById(Integer id) throws DAOException {
        try {
            return prospectDAO.findById(id);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur récupération prospect ID={0} : {1}",
                    new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Récupère tous les prospects triés par raison sociale.
     *
     * @return liste de tous les prospects (peut être vide)
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public List<Prospect> getTousLesProspects() throws DAOException {
        try {
            return prospectDAO.findAll();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur récupération prospects : {0}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Construit un modèle de table Swing pour affichage des prospects.
     * <p>
     * Crée un DefaultTableModel non-éditable avec colonnes :
     * ID, Raison Sociale, Adresse, Téléphone, Email, Date Prospection, Intéressé
     * </p>
     *
     * @return modèle de table prêt pour JTable
     * @throws DAOException si une erreur survient lors de la récupération des prospects
     */
    public DefaultTableModel construireTableModel() throws DAOException {
        String[] colonnes = {"ID", "Raison Sociale", "Adresse", "Téléphone",
                "Email", "Date Prospection", "Intéressé"};

        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Prospect> prospects = getTousLesProspects();

        for (Prospect prospect : prospects) {
            Object[] row = {
                    prospect.getId(),
                    prospect.getRaisonSociale(),
                    prospect.getAdresse().toString(),
                    prospect.getTelephone(),
                    prospect.getEmail(),
                    prospect.getDateProspectionFormatee(),
                    prospect.getInteresse().getLibelle()
            };
            model.addRow(row);
        }

        return model;
    }

    /**
     * Retourne un tableau de prospects pour utilisation dans un JComboBox.
     * <p>
     * Les prospects sont triés par raison sociale.
     * La méthode toString() de Prospect retourne "Raison Sociale (Prospect)".
     * </p>
     *
     * @return un tableau de prospects (peut être vide)
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public Prospect[] getProspectsForComboBox() throws DAOException {
        List<Prospect> prospects = getTousLesProspects();
        return prospects.toArray(new Prospect[0]);
    }
}
