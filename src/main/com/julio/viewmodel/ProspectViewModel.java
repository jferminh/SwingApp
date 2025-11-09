package main.com.julio.viewmodel;

import main.com.julio.exception.NotFoundException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.repository.ProspectRepository;
import main.com.julio.service.LoggingService;
import main.com.julio.service.UnicityService;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class ProspectViewModel {
    private ProspectRepository prospectRepo;
    private UnicityService unicityService;

    public ProspectViewModel(ProspectRepository prospectRepo, UnicityService unicityService) {
        this.prospectRepo = prospectRepo;
        this.unicityService = unicityService;
    }

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
    ){
        try {
            if (unicityService.isRaisonSocialeUnique(raisonSociale, -1)) {
                throw new ValidationException("raisonSociale", "Cette raison sociale existe dèjà");
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

            LoggingService.log("Prospect créé avec succès: " + raisonSociale);
            return prospect;
        } catch (Exception e) {
            LoggingService.logError("Erreur création prospect", e);
            throw e;
        }
    }

    public boolean modifierProspect(int id,
                                    String raisonSociale,
                                    String numeroRue,
                                    String nomRue,
                                    String codePostal,
                                    String ville,
                                    String telephone,
                                    String email,
                                    String commentaires,
                                    LocalDate dateProspection,
                                    Interesse interesse
    ){
        try {
            if (unicityService.isRaisonSocialeUnique(raisonSociale, id)) {
                throw new ValidationException("raisonSociale", "Cette raison sociale existe dèjà");
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

            boolean success = prospectRepo.update(prospect);

            if (success) {
                LoggingService.log("Prospect modifié avec succès: ID= " + id);
            }
            return success;
        } catch (Exception e) {
            LoggingService.logError("Erreur modification prospect", e);
            throw e;
        }
    }

    public boolean supprimerProspect(int id){
        try {
            boolean success = prospectRepo.delete(id);
            if (success) {
                LoggingService.log("Prospect supprimé avec succès: ID= " + id);
            }
            return success;
        } catch (Exception e) {
            LoggingService.logError("Erreur suppression prospect", e);
            return false;
        }
    }

    public Prospect getProspectById(int id){
        return prospectRepo.findById(id);
    }

    public List<Prospect> getTousLesProspects() {
        return prospectRepo.findAll();
    }
}
