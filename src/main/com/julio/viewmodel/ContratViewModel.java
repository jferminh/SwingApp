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

/**
 * ViewModel gérant la logique de présentation pour les contrats.
 * <p>
 * Orchestre les opérations CRUD sur les contrats et maintient la cohérence
 * bidirectionnelle avec les clients. Implémente le pattern MVVM.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public class ContratViewModel {

    // Repositories - Accès données
    private final ContratRepository contratRepo;
    private final ClientRepository clientRepo;  // Nécessaire pour lien bidirectionnel

    /**
     * Constructeur initialisant le ViewModel avec ses repositories.
     *
     * @param contratRepo repository des contrats
     * @param clientRepo repository des clients (pour association bidirectionnelle)
     */
    public ContratViewModel(ContratRepository contratRepo, ClientRepository clientRepo) {
        this.contratRepo = contratRepo;
        this.clientRepo = clientRepo;
    }

    /**
     * Crée un nouveau contrat et l'associe au client.
     * <p>
     * Maintient la cohérence bidirectionnelle en ajoutant le contrat
     * dans le repository ET dans la liste du client.
     * </p>
     *
     * @param clientId identifiant du client propriétaire
     * @param nomContrat nom du contrat
     * @param montant montant en euros (> 0)
     * @throws ValidationException si client inexistant ou validation échoue
     */
    public void creerContrat(int clientId, String nomContrat, double montant) throws ValidationException {
        try {
            // Vérification existence client
            Client client = clientRepo.findById(clientId);
            if (client == null) {
                throw new ValidationException("Client introuvable");
            }

            // Création contrat avec validation intégrée
            Contrat contrat = new Contrat(clientId, nomContrat, montant);

            // Ajout dans repository
            contratRepo.add(contrat);

            // Synchronisation bidirectionnelle: ajout dans liste client
            client.ajouterContrat(contrat);

        } catch (ValidationException ve) {
            throw ve;  // Propagation directe pour affichage vue
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Modifie un contrat existant.
     *
     * @param id identifiant du contrat à modifier
     * @param nomContrat nouveau nom du contrat
     * @param montant nouveau montant en euros
     * @throws ValidationException si contrat inexistant ou validation échoue
     */
    public void modifierContrat(int id, String nomContrat, double montant) throws ValidationException {
        try {
            Contrat contrat = contratRepo.findById(id);
            if (contrat == null) {
                throw new ValidationException("Contrat introuvable");
            }

            // Modification via setters avec validation
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

    /**
     * Supprime un contrat et met à jour la liste du client associé.
     * <p>
     * Maintient la cohérence bidirectionnelle en supprimant le contrat
     * du repository ET de la liste du client.
     * </p>
     *
     * @param id identifiant du contrat à supprimer
     * @return true si suppression réussie, false sinon
     */
    public boolean supprimerContrat(int id) {
        try {
            Contrat contrat = contratRepo.findById(id);
            if (contrat == null) {
                return false;
            }

            // Synchronisation bidirectionnelle: retrait de la liste client
            Client client = clientRepo.findById(contrat.getClientId());
            if (client != null) {
                client.supprimerContrat(contrat);
            }

            // Suppression du repository
            return contratRepo.delete(id);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Récupère tous les contrats d'un client spécifique.
     *
     * @param clientId identifiant du client
     * @return liste des contrats du client
     */
    public List<Contrat> getContratsParClient(int clientId) {
        return contratRepo.findByClientId(clientId);
    }

    /**
     * Construit un modèle de table Swing pour affichage des contrats d'un client.
     * <p>
     * Crée un DefaultTableModel non-éditable avec colonnes :
     * ID, Nom du Contrat, Montant (€) formaté avec 2 décimales
     * </p>
     *
     * @param clientId identifiant du client dont afficher les contrats
     * @return modèle de table prêt pour JTable
     */
    public DefaultTableModel construireTableModel(int clientId) {
        String[] colonnes = {"ID", "Nom du Contrat", "Montant (€)"};

        // Modèle non-éditable
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Lecture seule
            }
        };

        // Remplissage avec contrats filtrés par client
        List<Contrat> contratList = getContratsParClient(clientId);
        for (Contrat contrat : contratList) {
            Object[] row = {
                    contrat.getId(),
                    contrat.getNomContrat(),
                    String.format("%.2f", contrat.getMontant())  // Formatage 2 décimales
            };
            model.addRow(row);
        }
        return model;
    }
}
