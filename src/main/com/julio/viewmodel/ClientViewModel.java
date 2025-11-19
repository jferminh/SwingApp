package main.com.julio.viewmodel;

import main.com.julio.exception.NotFoundException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;
import main.com.julio.service.UnicityService;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

public class ClientViewModel {
    public ClientRepository clientRepo;
    private final ContratRepository contratRepo;
    private final UnicityService unicityService;

    public ClientViewModel(ClientRepository clientRepo,
                           ContratRepository contratRepo,
                           UnicityService unicityService) {
        this.clientRepo = clientRepo;
        this.contratRepo = contratRepo;
        this.unicityService = unicityService;
    }

    public void creerClient(String raisonSociale,
                            String numeroRue,
                            String nomRue,
                            String codePostal,
                            String ville,
                            String telephone,
                            String email,
                            String commentaires,
                            long chiffreAffaires,
                            int nbEmployes) throws ValidationException, NotFoundException {
        try {
            if (unicityService.isRaisonSocialeUnique(raisonSociale, -1)) {
                throw new ValidationException("Cette raison sociale existe déjà");
            }

            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);

            Client client = new Client(raisonSociale, adresse,
                    telephone, email, commentaires, chiffreAffaires, nbEmployes);

            clientRepo.add(client);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    public void modifierClient(int id,
                               String raisonSociale,
                               String numeroRue,
                               String nomRue,
                               String codePostal,
                               String ville,
                               String telephone,
                               String email,
                               String commentaires,
                               long chiffreAffaires,
                               int nbEmployes) throws ValidationException, NotFoundException {
        try {
            if (unicityService.isRaisonSocialeUnique(raisonSociale, id)) {
                throw new ValidationException("Cette raison sociale existe dèjà");
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

            clientRepo.update(client);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    public boolean supprimerClient(int id) {
        try {
            contratRepo.findByClientId(id).forEach(c -> {
                contratRepo.delete(c.getId());
            });

            return clientRepo.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }

    public Client getClientById(int id) {
        return clientRepo.findById(id);
    }

    public List<Client> getTousLesClients() {
        return clientRepo.findAll();
    }

    public DefaultTableModel construireTableModel() {
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

}
