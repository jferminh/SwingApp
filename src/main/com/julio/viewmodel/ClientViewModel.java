package main.com.julio.viewmodel;

import main.com.julio.exception.NotFoundException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;
import main.com.julio.service.LoggingService;
import main.com.julio.service.UnicityService;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ClientViewModel {
    private ClientRepository clientRepo;
    private ContratRepository contratRepo;
    private UnicityService unicityService;

    public ClientViewModel(ClientRepository clientRepo,
                           ContratRepository contratRepo,
                           UnicityService unicityService) {
        this.clientRepo = clientRepo;
        this.contratRepo = contratRepo;
        this.unicityService = unicityService;
    }

    public Client creerClient(String raisonSociale,
                              String numeroRue,
                              String nomRue,
                              String codePostal,
                              String ville,
                              String telephone,
                              String email,
                              String commentaires,
                              long chiffreAffaires,
                              int nbEmployes){
        try {
            if (!unicityService.isRaisonSocialeUnique(raisonSociale, -1)) {
                throw new ValidationException("raisonSociale","Cette raison sociale existe déjà");
            }

            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);

            Client client = new Client(raisonSociale, adresse,
                    telephone, email, commentaires, chiffreAffaires, nbEmployes);

            clientRepo.add(client);
            LoggingService.log("Client créé avec succès: " + raisonSociale);
            return client;
        } catch (Exception e) {
            LoggingService.logError("Erreur création client", e);
            throw e;
        }
    }

    public boolean modifierClient(int id,
                          String raisonSociale,
                          String numeroRue,
                          String nomRue,
                          String codePostal,
                          String ville,
                          String telephone,
                          String email,
                          String commentaires,
                          long chiffreAffaires,
                          int nbEmployes){
        try {
            if (!unicityService.isRaisonSocialeUnique(raisonSociale, id)) {
                throw new ValidationException("raisonSociale","Cette raison sociale existe dèjà");
            }
            Client client = clientRepo.findById(id);
            if (client == null) {
                throw new NotFoundException("Client n'existe pas");
            }

            client.getAdresse().setNumeroRue(numeroRue);
            client.getAdresse().setNomRue(nomRue);
            client.getAdresse().setCodePostal(codePostal);
            client.getAdresse().setVille(ville);

            client.setRaisonSociale(raisonSociale);
            client.setTelephone(telephone);
            client.setEmail(email);
            client.setCommentaires(commentaires);
            client.setChiffreAffaires(chiffreAffaires);
            client.setNbEmployes(nbEmployes);

            boolean success = clientRepo.update(client);
            if (success) {
                LoggingService.log("Client modifié avec succès: ID= " + id);
            }
            return success;
        } catch (Exception e) {
            LoggingService.logError("Erreur modification client", e);
            throw e;
        }
    }

    public boolean supprimerClient(int id){
        try {
            contratRepo.findByClientId(id).forEach(c -> {
                contratRepo.delete(c.getId());
            });

            boolean success = clientRepo.delete(id);
            if (success) {
                LoggingService.log("Client supprimé avec succès: ID= " + id);
            }
            return success;
        } catch (Exception e) {
            LoggingService.logError("Erreur suppression client", e);
            return  false;
        }
    }

    public Client getClientById(int id){
        return clientRepo.findById(id);
    }

    public List<Client> getTousLesClients(){
        return clientRepo.findAll();
    }

    public DefaultTableModel construireTableModel(){
        String[] colonnes = {"ID", "Raison Sociale", "Adresse", "Téléphone",
                "Email", "CA (€)", "Nb Employés"};
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Client> clients = getTousLesClients();
        for (Client client : clients) {
            Object[] row = {
                    client.getId(),
                    client.getRaisonSociale(),
                    client.getAdresse().toString(),
                    client.getTelephone(),
                    client.getEmail(),
                    client.getChiffreAffaires(),
                    client.getNbEmployes()
            };
            model.addRow(row);
        }
        return model;
    }

    public Client[] getClientsForComboBox() {
        List<Client> clients = getTousLesClients();
        return clients.toArray(new Client[0]);
    }
}
