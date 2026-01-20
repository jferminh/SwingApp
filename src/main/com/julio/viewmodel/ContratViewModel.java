package main.com.julio.viewmodel;

import main.com.julio.dao.ClientDAO;
import main.com.julio.dao.ContratDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;
import main.com.julio.service.LoggerService;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * ViewModel pour la gestion des contrats.
 * Fait le lien entre la Vue et les DAO (pattern MVVM).
 * <p>
 * Cette classe orchestre les opérations CRUD sur les contrats en utilisant
 * ContratDAO et ClientDAO, avec une gestion complète des exceptions pour
 * fournir des messages clairs à l'utilisateur.
 * <p>
 * Responsabilités :
 * - Validation de l'existence du client avant création de contrat
 * - Orchestration des opérations DAO
 * - Transformation des exceptions en messages utilisateur compréhensibles
 * - Préparation des données pour l'affichage
 * - Logging détaillé des opérations
 * <p>
 * Gestion des exceptions :
 * ValidationException : données invalides (montant négatif, nom vide, etc.)
 * DAOException.FOREIGN_KEY_VIOLATION : client inexistant
 * DAOException.NOT_FOUND : contrat inexistant
 * DAOException.CONNECTION_ERROR : problème de connexion BDD
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 20/01/2026
 */
public class ContratViewModel {
    private static final Logger LOGGER = LoggerService.getLogger(ContratViewModel.class);

    private final ContratDAO contratDAO;
    private final ClientDAO clientDAO;

    /**
     * Constructeur avec injection des DAO.
     *
     * @throws DAOException si l'initialisation des DAO échoue
     */
    public ContratViewModel() throws DAOException {
        this.contratDAO = new ContratDAO();
        this.clientDAO = new ClientDAO();
        LOGGER.info("ContratViewModel initialisé avec succès");
    }

    /**
     * Crée un nouveau contrat associé à un client.
     * <p>
     * Processus de validation :
     * 1. Vérifie que le client existe dans la base de données
     * 2. Valide les données du contrat (nom, montant)
     * 3. Crée le contrat via ContratDAO
     * 4. Ajoute le contrat à la liste du client en mémoire
     *
     * @param clientId identifiant du client (doit exister)
     * @param nomContrat nom du contrat (obligatoire)
     * @param montant montant du contrat en euros (> 0)
     * @return le contrat créé avec son ID généré
     * @throws IllegalArgumentException si le client n'existe pas ou si les données sont invalides
     * @throws RuntimeException si une erreur DAO survient
     */
    public Contrat creerContrat(Integer clientId, String nomContrat, double montant) {
        try {
            // 1. Vérifier que le client existe
            Client client = clientDAO.findById(clientId);

            if (client == null) {
                LOGGER.log(Level.WARNING,
                        "Tentative de création de contrat pour client inexistant : ID={0}", clientId);
                throw new IllegalArgumentException(
                        "Client introuvable avec l'ID " + clientId + ". " +
                                "Veuillez sélectionner un client existant."
                );
            }

            // 2. Créer le contrat (la validation se fait dans le constructeur)
            Contrat contrat = new Contrat(clientId, nomContrat, montant);

            // 3. Persister le contrat
            contrat = contratDAO.create(contrat);

            // 4. Ajouter le contrat au client en mémoire (optionnel, pour cohérence)
            client.ajouterContrat(contrat);

            return contrat;

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING,
                    "Erreur de validation lors de la création du contrat", e);

            // Message utilisateur spécifique selon le type de validation
            String messageUtilisateur = analyserValidationException(e, "création");
            throw new IllegalArgumentException(messageUtilisateur, e);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO lors de la création du contrat pour client ID=" + clientId, e);

            // Analyser le code d'erreur pour un message approprié
            String messageUtilisateur = analyserDAOException(e, "création");
            throw new RuntimeException(messageUtilisateur, e);
        }
    }

    /**
     * Modifie un contrat existant.
     * <p>
     * Note : Le client_id ne peut pas être modifié. Si vous devez changer
     * le client, supprimez le contrat et créez-en un nouveau.
     *
     * @param id identifiant du contrat
     * @param nomContrat nouveau nom du contrat
     * @param montant nouveau montant
     * @return true si la modification a réussi
     * @throws IllegalArgumentException si le contrat n'existe pas ou si les données sont invalides
     * @throws RuntimeException si une erreur DAO survient
     */
    public boolean modifierContrat(Integer id, String nomContrat, double montant) {
        try {
            // 1. Récupérer le contrat existant
            Contrat contrat = contratDAO.findById(id);

            if (contrat == null) {
                LOGGER.log(Level.WARNING,
                        "Contrat ID={0} introuvable pour modification", id);
                throw new IllegalArgumentException(
                        "Contrat introuvable avec l'ID " + id
                );
            }

            // 2. Mettre à jour les données (validation dans les setters)
            contrat.setNomContrat(nomContrat);
            contrat.setMontant(montant);

            // 3. Sauvegarder les modifications
            boolean success = contratDAO.save(contrat);

            if (success) {
                LOGGER.log(Level.INFO,
                        "Contrat modifié avec succès : ID={0}, Nom={1}, Montant={2} €",
                        new Object[]{id, nomContrat, montant});
            } else {
                LOGGER.log(Level.WARNING,
                        "La modification du contrat ID={0} n'a affecté aucune ligne", id);
            }

            return success;

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING,
                    "Erreur de validation lors de la modification du contrat ID=" + id, e);

            String messageUtilisateur = analyserValidationException(e, "modification");
            throw new IllegalArgumentException(messageUtilisateur, e);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO lors de la modification du contrat ID=" + id, e);

            String messageUtilisateur = analyserDAOException(e, "modification");
            throw new RuntimeException(messageUtilisateur, e);
        }
    }

    /**
     * Supprime un contrat.
     * <p>
     * Cette opération est irréversible. Le client associé n'est pas affecté.
     *
     * @param id identifiant du contrat à supprimer
     * @return true si la suppression a réussi
     * @throws IllegalArgumentException si le contrat n'existe pas
     * @throws RuntimeException si une erreur DAO survient
     */
    public boolean supprimerContrat(Integer id) {
        LOGGER.log(Level.INFO, "Tentative de suppression du contrat ID={0}", id);

        try {
            // Optionnel : Récupérer le contrat pour retirer du client en mémoire
            Contrat contrat = contratDAO.findById(id);

            if (contrat != null) {
                // Retirer du client en mémoire (si chargé)
                try {
                    Client client = clientDAO.findById(contrat.getClientId());
                    if (client != null) {
                        client.supprimerContrat(contrat);
                        LOGGER.log(Level.FINE,
                                "Contrat retiré de la liste du client ID={0}",
                                contrat.getClientId());
                    }
                } catch (DAOException e) {
                    LOGGER.log(Level.FINE,
                            "Impossible de charger le client pour retirer le contrat de sa liste", e);
                    // Pas critique, on continue la suppression
                }
            }

            // Supprimer le contrat de la base de données
            boolean success = contratDAO.delete(id);

            if (success) {
                LOGGER.log(Level.INFO, "Contrat supprimé avec succès : ID={0}", id);
            } else {
                LOGGER.log(Level.WARNING,
                        "Aucun contrat trouvé avec l'ID {0} pour suppression", id);
            }

            return success;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO lors de la suppression du contrat ID=" + id, e);

            String messageUtilisateur = analyserDAOException(e, "suppression");
            throw new RuntimeException(messageUtilisateur, e);
        }
    }

    /**
     * Récupère un contrat par son ID.
     *
     * @param id identifiant du contrat
     * @return le contrat ou null si non trouvé
     * @throws RuntimeException si une erreur DAO survient
     */
    public Contrat getContratById(Integer id) {
        LOGGER.log(Level.FINE, "Récupération du contrat ID={0}", id);

        try {
            Contrat contrat = contratDAO.findById(id);

            if (contrat != null) {
                LOGGER.log(Level.FINE,
                        "Contrat trouvé : ID={0}, Nom={1}, Montant={2}",
                        new Object[]{id, contrat.getNomContrat(), contrat.getMontant()});
            } else {
                LOGGER.log(Level.FINE, "Aucun contrat trouvé avec l'ID {0}", id);
            }

            return contrat;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur lors de la récupération du contrat ID=" + id, e);
            throw new RuntimeException(
                    "Erreur lors de la récupération du contrat : " + e.getMessage(), e
            );
        }
    }

    /**
     * Récupère tous les contrats d'un client spécifique.
     *
     * @param clientId identifiant du client
     * @return liste des contrats du client (peut être vide)
     * @throws IllegalArgumentException si clientId est invalide
     * @throws RuntimeException si une erreur DAO survient
     */
    public List<Contrat> getContratsParClient(Integer clientId) {
        if (clientId == null || clientId <= 0) {
            LOGGER.log(Level.WARNING,
                    "Tentative de récupération des contrats avec client ID invalide : {0}",
                    clientId);
            throw new IllegalArgumentException(
                    "L'ID du client doit être un entier positif non null"
            );
        }

        LOGGER.log(Level.FINE, "Récupération des contrats pour client ID={0}", clientId);

        try {
            List<Contrat> contrats = contratDAO.findByIdClient(clientId);

            LOGGER.log(Level.INFO,
                    "{0} contrat(s) récupéré(s) pour le client ID={1}",
                    new Object[]{contrats.size(), clientId});

            return contrats;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur lors de la récupération des contrats du client ID=" + clientId, e);

            String messageUtilisateur = switch (e.getErrorCode()) {
                case INVALID_PARAMETER ->
                        "Paramètre invalide : " + e.getMessage();
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                default ->
                        "Erreur lors de la récupération des contrats : " + e.getMessage();
            };

            throw new RuntimeException(messageUtilisateur, e);
        }
    }

    /**
     * Récupère tous les contrats de la base de données.
     *
     * @return liste de tous les contrats (peut être vide)
     * @throws RuntimeException si une erreur DAO survient
     */
    public List<Contrat> getTousLesContrats() {
        LOGGER.fine("Récupération de tous les contrats");

        try {
            List<Contrat> contrats = contratDAO.findAll();

            LOGGER.log(Level.INFO, "{0} contrat(s) récupéré(s)", contrats.size());

            return contrats;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de tous les contrats", e);
            throw new RuntimeException(
                    "Erreur lors de la récupération de la liste des contrats : " + e.getMessage(), e
            );
        }
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

    // ========== MÉTHODES PRIVÉES D'ANALYSE DES EXCEPTIONS ==========

    /**
     * Analyse une ValidationException et retourne un message utilisateur approprié.
     *
     * @param e l'exception de validation
     * @param operation le type d'opération (création, modification, suppression)
     * @return un message utilisateur compréhensible
     */
    private String analyserValidationException(ValidationException e, String operation) {
        String message = e.getMessage().toLowerCase();

        // Analyser le message pour identifier le champ en erreur
        if (message.contains("nom") && message.contains("obligatoire")) {
            return "Le nom du contrat est obligatoire. Veuillez saisir un nom.";
        } else if (message.contains("montant") && message.contains("positif")) {
            return "Le montant doit être un nombre positif supérieur à zéro.";
        } else if (message.contains("client") && message.contains("obligatoire")) {
            return "Le client est obligatoire. Veuillez sélectionner un client.";
        } else {
            // Message générique si non identifié
            return "Erreur de validation lors de la " + operation + " : " + e.getMessage();
        }
    }

    /**
     * Analyse une DAOException et retourne un message utilisateur approprié.
     *
     * @param e l'exception DAO
     * @param operation le type d'opération (création, modification, suppression)
     * @return un message utilisateur compréhensible
     */
    private String analyserDAOException(DAOException e, String operation) {
        return switch (e.getErrorCode()) {
            case FOREIGN_KEY_VIOLATION -> {
                // Analyser si c'est une violation de client_id
                String msg = e.getMessage();
                if (msg != null && msg.toLowerCase().contains("client")) {
                    yield "Le client sélectionné n'existe plus dans la base de données. " +
                            "Veuillez actualiser la liste des clients.";
                }
                yield "Erreur de contrainte de base de données lors de la " + operation + ".";
            }
            case ENTITY_NOT_FOUND ->
                    "Contrat introuvable. Il a peut-être été supprimé.";
            case UNIQUE_CONSTRAINT_VIOLATION ->
                    "Un contrat avec ces caractéristiques existe déjà.";
            case CONNECTION_ERROR ->
                    "Impossible de se connecter à la base de données. " +
                            "Vérifiez votre connexion réseau.";
            case INVALID_PARAMETER ->
                    "Paramètre invalide : " + e.getMessage();
            case READ_ERROR ->
                    "Erreur lors de la lecture des données du contrat.";
            case UPDATE_ERROR ->
                    "Erreur lors de l'écriture des données du contrat.";
            case DELETE_ERROR ->
                    "Erreur lors de la suppression du contrat.";
            case TRANSACTION_ERROR ->
                    "Erreur de transaction. L'opération a été annulée.";
            default ->
                    "Erreur inattendue lors de la " + operation + " du contrat : " + e.getMessage();
        };
    }
}
