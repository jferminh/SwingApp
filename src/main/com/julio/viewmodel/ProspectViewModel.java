package main.com.julio.viewmodel;

import main.com.julio.exception.NotFoundException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.repository.ProspectRepository;
import main.com.julio.service.UnicityService;

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;

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

    // Repository - Accès données
    private final ProspectRepository prospectRepo;

    // Services métier
    private final UnicityService unicityService;

    /**
     * Constructeur initialisant le ViewModel avec ses dépendances.
     *
     * @param prospectRepo repository des prospects
     * @param unicityService service de vérification d'unicité
     */
    public ProspectViewModel(ProspectRepository prospectRepo, UnicityService unicityService) {
        this.prospectRepo = prospectRepo;
        this.unicityService = unicityService;
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
    public void creerProspect(String raisonSociale,
                              String numeroRue,
                              String nomRue,
                              String codePostal,
                              String ville,
                              String telephone,
                              String email,
                              String commentaires,
                              LocalDate dateProspection,
                              Interesse interesse
    ) throws ValidationException {
        try {
            // Vérification unicité raison sociale (-1 = nouvelle entité)
            if (unicityService.isRaisonSocialDuplique(raisonSociale, -1)) {
                throw new ValidationException("Cette raison sociale existe déjà");
            }

            // Construction objets avec validation intégrée
            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);
            Prospect prospect = new Prospect(
                    raisonSociale,
                    adresse,
                    telephone,
                    email,
                    commentaires,
                    dateProspection,
                    interesse
            );

            prospectRepo.add(prospect);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw e;  // Propagation pour affichage dans la vue
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
    public void modifierProspect(int id,
                                 String raisonSociale,
                                 String numeroRue,
                                 String nomRue,
                                 String codePostal,
                                 String ville,
                                 String telephone,
                                 String email,
                                 String commentaires,
                                 LocalDate dateProspection,
                                 Interesse interesse) throws ValidationException, NotFoundException {
        try {
            // Vérification unicité (exclure l'entité en cours de modification)
            if (unicityService.isRaisonSocialDuplique(raisonSociale, id)) {
                throw new ValidationException("Cette raison sociale existe déjà");
            }

            Prospect prospect = prospectRepo.findById(id);
            if (prospect == null) {
                throw new NotFoundException("Prospect introuvable");
            }

            // Modification adresse (objet imbriqué)
            prospect.getAdresse().setNumeroRue(numeroRue);
            prospect.getAdresse().setNomRue(nomRue);
            prospect.getAdresse().setCodePostal(codePostal);
            prospect.getAdresse().setVille(ville);

            // Modification attributs prospect
            prospect.setRaisonSociale(raisonSociale);
            prospect.setTelephone(telephone);
            prospect.setEmail(email);
            prospect.setCommentaires(commentaires);
            prospect.setDateProspection(dateProspection);
            prospect.setInteresse(interesse);

            prospectRepo.update(prospect);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Supprime un prospect.
     *
     * @param id identifiant du prospect à supprimer
     * @return true si suppression réussie, false sinon
     */
    public boolean supprimerProspect(int id) {
        try {
            return prospectRepo.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Récupère un prospect par son identifiant.
     *
     * @param id identifiant du prospect
     * @return le prospect trouvé ou null si inexistant
     */
    public Prospect getProspectById(int id) {
        return prospectRepo.findById(id);
    }

    /**
     * Récupère tous les prospects triés par raison sociale.
     *
     * @return liste de tous les prospects
     */
    public List<Prospect> getTousLesProspects() {
        return prospectRepo.findAll();
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
}
