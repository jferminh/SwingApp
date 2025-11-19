package main.com.julio.viewmodel;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

public class ContratViewModel {
    private final ContratRepository contratRepo;
    private final ClientRepository clientRepo;

    public ContratViewModel(ContratRepository contratRepo, ClientRepository clientRepo) {
        this.contratRepo = contratRepo;
        this.clientRepo = clientRepo;
    }

    public void creerContrat(int clientId, String nomContrat, double montant) throws ValidationException {
        try {
            Client client = clientRepo.findById(clientId);
            if (client == null) {
                throw new ValidationException("Client introuvable");
            }

            Contrat contrat = new Contrat(clientId, nomContrat, montant);

            contratRepo.add(contrat);

            client.ajouterContrat(contrat);

        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    public void modifierContrat(int id, String nomContrat, double montant) throws ValidationException {
        try {
            Contrat contrat = contratRepo.findById(id);
            if (contrat == null) {
                throw new ValidationException("Contrat introuvable");
            }

            contrat.setNomContrat(nomContrat);
            contrat.setMontant(montant);

            contratRepo.update(contrat);
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    public boolean supprimerContrat(int id) {
        try {
            Contrat contrat = contratRepo.findById(id);
            if (contrat == null) {
                return false;
            }

            Client client = clientRepo.findById(contrat.getClientId());
            if (client != null) {
                client.supprimerContrat(contrat);
            }

            return contratRepo.delete(id);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }

    public List<Contrat> getContratsParClient(int clientId) {
        return contratRepo.findByClientId(clientId);
    }

    public DefaultTableModel construireTableModel(int clientId) {
        String[] colonnes = {"ID", "Nom du Contrat", "Montant (€)"};
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {

                return false;
            }
        };

        List<Contrat> contratList = getContratsParClient(clientId);
        for (Contrat contrat : contratList) {
            Object[] row = {
                    contrat.getId(),
                    contrat.getNomContrat(),
                    String.format("%.2f", contrat.getMontant())
            };
            model.addRow(row);
        }
        return model;
    }
}
