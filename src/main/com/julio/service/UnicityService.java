package main.com.julio.service;

import main.com.julio.model.Client;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ProspectRepository;

public class UnicityService {
    private ClientRepository clientRepo;
    private ProspectRepository prospectRepo;

    public UnicityService(ClientRepository clientRepo, ProspectRepository prospectRepo) {
        this.clientRepo = clientRepo;
        this.prospectRepo = prospectRepo;
    }

    public boolean isRaisonSocialeUnique(String raisonSociale, int idExcluire) {
        for (Client client : clientRepo.findAll()) {
            if (client.getId() != idExcluire && client.getRaisonSociale().equalsIgnoreCase(raisonSociale)) {
                return false;

            }
        }
        return true;
    }
}
