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

/**
 * ViewModel gérant la logique de présentation pour les clients.
 * <p>
 * Sert d'intermédiaire entre les vues et les repositories, orchestrant
 * les validations métier et la préparation des données pour l'affichage.
 * Implémente le pattern MVVM.
 * </p>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public class ClientViewModel {

    // Repositories - Accès données
    public ClientRepository clientRepo;
    private final ContratRepository contratRepo;

    // Services métier
    private final UnicityService unicityService;

    /**
     * Constructeur initialisant le ViewModel avec ses dépendances.
     *
     * @param clientRepo repository des clients
     * @param contratRepo repository des contrats
     * @param unicityService service de vérification d'unicité
     */
    public ClientViewModel(ClientRepository clientRepo,
                           ContratRepository contratRepo,
                           UnicityService unicityService) {
        this.clientRepo = clientRepo;
        this.contratRepo = contratRepo;
        this.unicityService = unicityService;
    }

    /**
     * Crée un nouveau client avec validation de l'unicité.
     *
     * @param raisonSociale raison sociale du client
     * @param numeroRue numéro de rue
     * @param nomRue nom de rue
     * @param codePostal code postal (5 chiffres)
     * @param ville ville
     * @param telephone téléphone (format validé)
     * @param email email (format validé)
     * @param commentaires commentaires optionnels
     * @param chiffreAffaires chiffre d'affaires (>= 200)
     * @param nbEmployes nombre d'employés (>= 1)
     * @throws ValidationException si validation échoue ou raison sociale existe
     */
    public void creerClient(String raisonSociale,
                            String numeroRue,
                            String nomRue,
                            String codePostal,
                            String ville,
                            String telephone,
                            String email,
                            String commentaires,
                            long chiffreAffaires,
                            int nbEmployes) throws ValidationException {
        try {
            // Vérification unicité raison sociale (-1 = nouvelle entité)
            if (unicityService.isRaisonSocialDuplique(raisonSociale, -1)) {
                throw new ValidationException("Cette raison sociale existe déjà");
            }

            // Construction objets avec validation intégrée
            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);
            Client client = new Client(raisonSociale, adresse,
                    telephone, email, commentaires, chiffreAffaires, nbEmployes);

            clientRepo.add(client);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw e;  // Propagation pour affichage dans la vue
        }
    }

    /**
     * Modifie un client existant avec validation de l'unicité.
     *
     * @param id identifiant du client à modifier
     * @param raisonSociale nouvelle raison sociale
     * @param numeroRue nouveau numéro de rue
     * @param nomRue nouveau nom de rue
     * @param codePostal nouveau code postal
     * @param ville nouvelle ville
     * @param telephone nouveau téléphone
     * @param email nouvel email
     * @param commentaires nouveaux commentaires
     * @param chiffreAffaires nouveau chiffre d'affaires
     * @param nbEmployes nouveau nombre d'employés
     * @throws ValidationException si validation échoue ou raison sociale dupliquée
     * @throws NotFoundException si client inexistant
     */
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
            // Vérification unicité (exclure l'entité en cours de modification)
            if (unicityService.isRaisonSocialDuplique(raisonSociale, id)) {
                throw new ValidationException("Cette raison sociale existe déjà");
            }

            Client client = clientRepo.findById(id);
            if (client == null) {
                throw new NotFoundException("Client n'existe pas");
            }

            // Modification adresse (objet imbriqué)
            client.getAdresse().setNumeroRue(numeroRue);
            client.getAdresse().setNomRue(nomRue);
            client.getAdresse().setCodePostal(codePostal);
            client.getAdresse().setVille(ville);

            // Modification attributs client
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

    /**
     * Supprime un client et tous ses contrats associés.
     * <p>
     * Effectue une suppression en cascade : suppression des contrats
     * puis suppression du client.
     * </p>
     *
     * @param id identifiant du client à supprimer
     * @return true si suppression réussie, false sinon
     */
    public boolean supprimerClient(int id) {
        try {
            // Suppression en cascade des contrats
            contratRepo.findByClientId(id).forEach(c -> {
                contratRepo.delete(c.getId());
            });

            return clientRepo.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Récupère un client par son identifiant.
     *
     * @param id identifiant du client
     * @return le client trouvé ou null si inexistant
     */
    public Client getClientById(int id) {
        return clientRepo.findById(id);
    }

    /**
     * Récupère tous les clients triés par raison sociale.
     *
     * @return liste de tous les clients
     */
    public List<Client> getTousLesClients() {
        return clientRepo.findAll();
    }

    /**
     * Construit un modèle de table Swing pour affichage des clients.
     * <p>
     * Crée un DefaultTableModel non-éditable avec colonnes :
     * ID, Raison Sociale, Adresse, Téléphone, Email, CA (€), Nb Employés
     * </p>
     *
     * @return modèle de table prêt pour JTable
     */
    public DefaultTableModel construireTableModel() {
        String[] colonnes = {"ID", "Raison Sociale", "Adresse", "Téléphone",
                "Email", "CA (€)", "Nb Employés"};

        // Modèle non-éditable via override isCellEditable
        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Toutes cellules en lecture seule
            }
        };

        // Remplissage avec données clients
        List<Client> clients = getTousLesClients();
        for (Client client : clients) {
            Object[] row = {
                    client.getId(),
                    client.getRaisonSociale(),
                    client.getAdresse().toString(),  // Formatage adresse
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
