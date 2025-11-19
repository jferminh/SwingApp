package main.com.julio.service;

import main.com.julio.model.Client;
import main.com.julio.model.Prospect;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ProspectRepository;

public class UnicityService {
    private final ClientRepository clientRepo;
    private final ProspectRepository prospectRepo;

    public UnicityService(ClientRepository clientRepo, ProspectRepository prospectRepo) {
        this.clientRepo = clientRepo;
        this.prospectRepo = prospectRepo;
    }

    public boolean isRaisonSocialeUnique(String raisonSociale, int idExcluire) {
        for (Prospect prospect : prospectRepo.findAll()) {
            if (prospect.getId() != idExcluire && prospect.getRaisonSociale().equalsIgnoreCase(raisonSociale)) {
                return true;
            }
        }
        for (Client client : clientRepo.findAll()) {
            if (client.getId() != idExcluire && client.getRaisonSociale().equalsIgnoreCase(raisonSociale)) {
                return true;

            }
        }

        return false;
    }
}
