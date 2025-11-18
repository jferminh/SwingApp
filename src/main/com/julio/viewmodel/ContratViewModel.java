package main.com.julio.viewmodel;

import main.com.julio.exception.NotFoundException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;
import main.com.julio.service.LoggingService;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ContratViewModel {
    private final ContratRepository contratRepo;
    private final ClientRepository clientRepo;

    public ContratViewModel(ContratRepository contratRepo, ClientRepository clientRepo) {
        this.contratRepo = contratRepo;
        this.clientRepo = clientRepo;
    }

    public Contrat creerContrat(int clientId, String nomContrat, double montant) throws ValidationException {
        try {
            Client client = clientRepo.findById(clientId);
            if (client == null) {
                throw new ValidationException("Client introuvable");
            }

            Contrat contrat = new Contrat(clientId, nomContrat, montant);

            contratRepo.add(contrat);

            client.ajouterContrat(contrat);

            return contrat;
        } catch (Exception e) {
            LoggingService.logError("Erreur création contrat", e);
            throw e;
        }
    }

    public boolean modifierContrat(int id, String nomContrat, double montant) throws ValidationException {
        try {
            Contrat contrat = contratRepo.findById(id);
            if (contrat == null) {
                throw new ValidationException("Contrat introuvable");
            }

            contrat.setNomContrat(nomContrat);
            contrat.setMontant(montant);

            return contratRepo.update(contrat);
        } catch (Exception e) {
            LoggingService.logError("Erreur modification contrat", e);
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

            boolean success = contratRepo.delete(id);

            if (success) {
                LoggingService.log("Contrat supprimé avec succès: ID= " + id);
            }
            return success;

        } catch (Exception e) {
            LoggingService.logError("Erreur suppression contrat", e);
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
