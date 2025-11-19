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

public class ProspectViewModel {
    private final ProspectRepository prospectRepo;
    private final UnicityService unicityService;

    public ProspectViewModel(ProspectRepository prospectRepo, UnicityService unicityService) {
        this.prospectRepo = prospectRepo;
        this.unicityService = unicityService;
    }

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
            if (unicityService.isRaisonSocialeUnique(raisonSociale, -1)) {
                throw new ValidationException("Cette raison sociale existe dèjà");
            }

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
            throw e;
        }
    }

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
            if (unicityService.isRaisonSocialeUnique(raisonSociale, id)) {
                throw new ValidationException("Cette raison sociale existe dèjà");
            }

            Prospect prospect = prospectRepo.findById(id);
            if (prospect == null) {
                throw new NotFoundException("Prospect introuvable");
            }

            prospect.getAdresse().setNumeroRue(numeroRue);
            prospect.getAdresse().setNomRue(nomRue);
            prospect.getAdresse().setCodePostal(codePostal);
            prospect.getAdresse().setVille(ville);

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

    public boolean supprimerProspect(int id) {
        try {

            return prospectRepo.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    public Prospect getProspectById(int id) {
        return prospectRepo.findById(id);
    }

    public List<Prospect> getTousLesProspects() {
        return prospectRepo.findAll();
    }

    public DefaultTableModel construireTableModel() {
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
}
