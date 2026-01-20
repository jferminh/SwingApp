package main.com.julio.viewmodel;

import main.com.julio.dao.AdresseDAO;
import main.com.julio.dao.ProspectDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.exception.NotFoundException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.repository.ProspectRepository;
import main.com.julio.service.LoggerService;
import main.com.julio.service.UnicityService;

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * ViewModel gérant la logique de présentation pour les prospects.
 * <p>
 * Sert d'intermédiaire entre les vues et le repository, orchestrant
 * les validations métier et la préparation des données pour l'affichage.
 * Implémente le pattern MVVM.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public class ProspectViewModel {
    private static final Logger LOGGER = LoggerService.getLogger(ProspectViewModel.class);
    private final ProspectDAO prospectDAO;
    private final AdresseDAO adresseDAO;

    public ProspectViewModel() throws DAOException {
        this.prospectDAO = new ProspectDAO();
        this.adresseDAO = new AdresseDAO();
        LOGGER.info("ProspectViewModel initialisé avec succès");
    }

    /**
     * Crée un nouveau prospect avec validation de l'unicité.
     *
     * @param raisonSociale raison sociale du prospect
     * @param numeroRue numéro de rue
     * @param nomRue nom de rue
     * @param codePostal code postal (5 chiffres)
     * @param ville ville
     * @param telephone téléphone (format validé)
     * @param email email (format validé)
     * @param commentaires commentaires optionnels
     * @param dateProspection date de prospection (obligatoire)
     * @param interesse niveau d'intérêt (OUI/NON)
     * @throws ValidationException si validation échoue ou raison sociale existe
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
                              Interesse interesse
    ) {
        try {
            // 1. Créer l'adresse
            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);
            adresse = adresseDAO.create(adresse);

            // 2. Créer le prospect
            Prospect prospect = new Prospect(
                    raisonSociale,
                    adresse,
                    telephone,
                    email,
                    commentaires,
                    dateProspection,
                    interesse
            );

            prospect = prospectDAO.create(prospect);

            LOGGER.log(Level.INFO, "Prospect créé avec succès : ID={0}", prospect.getId());

            return prospect;

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation", e);
            throw new IllegalArgumentException("Données invalides : " + e.getMessage(), e);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur DAO", e);
            throw new RuntimeException("Erreur lors de la création : " + e.getMessage(), e);
        }
    }

    /**
     * Modifie un prospect existant avec validation de l'unicité.
     *
     * @param id identifiant du prospect à modifier
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
     * @throws ValidationException si validation échoue ou raison sociale dupliquée
     * @throws NotFoundException si prospect inexistant
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
                                 Interesse interesse) throws NotFoundException {
        try {
            Prospect prospect = prospectDAO.findById(id);

            if (prospect == null) {
                throw new IllegalArgumentException("Prospect introuvable avec l'ID " + id);
            }

            // Mise à jour
            prospect.setRaisonSociale(raisonSociale);
            prospect.setTelephone(telephone);
            prospect.setEmail(email);
            prospect.setCommentaires(commentaires);
            prospect.setDateProspection(dateProspection);
            prospect.setInteresse(interesse);

            // Mise à jour adresse
            Adresse adresse = prospect.getAdresse();
            adresse.setNumeroRue(numeroRue);
            adresse.setNomRue(nomRue);
            adresse.setCodePostal(codePostal);
            adresse.setVille(ville);

            adresseDAO.save(adresse);

            return prospectDAO.save(prospect);
        } catch (ValidationException e) {
            throw new IllegalArgumentException("Données invalides : " + e.getMessage(), e);
        } catch (DAOException e) {
            throw new RuntimeException("Erreur lors de la modification : " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un prospect.
     *
     * @param id identifiant du prospect à supprimer
     * @return true si suppression réussie, false sinon
     */
    public boolean supprimerProspect(Integer id) {
        try {
            return prospectDAO.delete(id);
        } catch (DAOException e) {
            throw new RuntimeException("Erreur lors de la suppression : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère un prospect par son identifiant.
     *
     * @param id identifiant du prospect
     * @return le prospect trouvé ou null si inexistant
     */
    public Prospect getProspectById(Integer id) {
        try {
            return prospectDAO.findById(id);
        } catch (DAOException e) {
            throw new RuntimeException("Erreur lors de la récupération : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère tous les prospects triés par raison sociale.
     *
     * @return liste de tous les prospects
     */
    public List<Prospect> getTousLesProspects() {
        try {
            return prospectDAO.findAll();
        } catch (DAOException e) {
            throw new RuntimeException("Erreur lors de la récupération : " + e.getMessage(), e);
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
     */
    public DefaultTableModel construireTableModel() {
        String[] colonnes = {"ID", "Raison Sociale", "Adresse", "Téléphone",
                "Email", "Date Prospection", "Intéressé"};

        // Modèle non-éditable via override isCellEditable
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Toutes cellules en lecture seule
            }
        };

        // Remplissage avec données prospects
        List<Prospect> prospects = getTousLesProspects();
        for (Prospect prospect : prospects) {
            Object[] row = {
                    prospect.getId(),
                    prospect.getRaisonSociale(),
                    prospect.getAdresse().toString(),  // Formatage adresse
                    prospect.getTelephone(),
                    prospect.getEmail(),
                    prospect.getDateProspectionFormatee(),  // Date formatée dd/MM/yyyy
                    prospect.getInteresse().getLibelle()  // "Oui" ou "Non"
            };
            model.addRow(row);
        }

        return model;
    }

    public Prospect[] getProspectsForComboBox() {
        List<Prospect> prospects = getTousLesProspects();
        return prospects.toArray(new Prospect[0]);
    }
}
