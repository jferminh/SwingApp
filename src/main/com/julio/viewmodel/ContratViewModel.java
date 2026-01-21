package main.com.julio.viewmodel;

import main.com.julio.dao.ContratDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.model.Contrat;
import main.com.julio.service.LoggerService;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel pour la gestion des contrats.
 * Fait le lien entre la Vue et les DAO (pattern MVVM).
 * <p>
 * Cette classe orchestre les opérations CRUD sur les contrats en utilisant
 * ContratDAO et en gérant les exceptions de manière appropriée pour la couche présentation.
 * <p>
 * Responsabilités :
 * 1. Validation des données d'entrée de l'utilisateur
 * 2. Orchestration des opérations DAO
 * 3. Transformation des exceptions DAO en exceptions métier
 * 4. Préparation des données pour l'affichage (TableModel)
 * 5. Logging des erreurs critiques uniquement
 *
 * @author Julio FERMIN
 * @version 2.1
 * @since 21/01/2026
 */
public class ContratViewModel {
    private static final Logger LOGGER = LoggerService.getLogger(ContratViewModel.class);

    private final ContratDAO contratDAO;

    /**
     * Constructeur avec injection du DAO.
     *
     * @throws DAOException si l'initialisation du DAO échoue
     */
    public ContratViewModel() throws DAOException {
        this.contratDAO = new ContratDAO();
    }

    /**
     * Crée un nouveau contrat pour un client.
     * <p>
     * Le client doit exister dans la base de données, sinon une exception
     * FOREIGN_KEY_VIOLATION sera levée.
     * </p>
     *
     * @param clientId identifiant du client
     * @param nomContrat nom du contrat
     * @param montant montant du contrat
     * @return le contrat créé avec son ID généré
     * @throws ValidationException si les données ne respectent pas les contraintes métier
     * @throws DAOException si une erreur survient lors de la persistance
     */
    public Contrat creerContrat(Integer clientId,
                                String nomContrat,
                                double montant) throws ValidationException, DAOException {

        try {
            Contrat contrat = new Contrat(clientId, nomContrat, montant);
            contrat = contratDAO.create(contrat);

            return contrat;

        } catch (ValidationException e) {
            throw e;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO création contrat : {0}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Modifie un contrat existant.
     * <p>
     * <strong>Note :</strong> Le client_id ne peut pas être modifié.
     * Pour réaffecter un contrat à un autre client, il faut le supprimer et le recréer.
     * </p>
     *
     * @param id identifiant du contrat
     * @param nomContrat nouveau nom du contrat
     * @param montant nouveau montant
     * @return true si la modification a réussi, false si le contrat n'existe pas
     * @throws ValidationException si les données ne respectent pas les contraintes métier
     * @throws DAOException si une erreur survient lors de la persistance
     */
    public boolean modifierContrat(Integer id,
                                   String nomContrat,
                                   double montant) throws ValidationException, DAOException {

        try {
            // Récupérer le contrat existant
            Contrat contrat = contratDAO.findById(id);

            if (contrat == null) {
                return false;
            }

            // Mettre à jour les données
            contrat.setNomContrat(nomContrat);
            contrat.setMontant(montant);

            // Persister les modifications
            return contratDAO.save(contrat);

        } catch (ValidationException e) {
            throw e;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur DAO modification contrat ID={0} : {1}",
                    new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Supprime un contrat.
     *
     * @param id identifiant du contrat à supprimer
     * @return true si la suppression a réussi, false si le contrat n'existe pas
     * @throws DAOException si une erreur survient lors de la suppression
     */
    public boolean supprimerContrat(Integer id) throws DAOException {
        try {
            return contratDAO.delete(id);

        } catch (DAOException e) {
            throw e;
        }
    }

    /**
     * Récupère un contrat par son ID.
     *
     * @param id identifiant du contrat
     * @return le contrat ou null si non trouvé
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public Contrat getContratById(Integer id) throws DAOException {
        try {
            return contratDAO.findById(id);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur récupération contrat ID={0} : {1}",
                    new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Récupère tous les contrats d'un client spécifique.
     *
     * @param clientId identifiant du client
     * @return liste des contrats du client (peut être vide)
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public List<Contrat> getContratsParClient(Integer clientId) throws DAOException {
        try {
            return contratDAO.findByIdClient(clientId);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur récupération contrats client ID={0} : {1}",
                    new Object[]{clientId, e.getMessage()});
            throw e;
        }
    }

    /**
     * Récupère tous les contrats.
     *
     * @return liste de tous les contrats (peut être vide)
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public List<Contrat> getTousLesContrats() throws DAOException {
        try {
            return contratDAO.findAll();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur récupération contrats : {0}",
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Construit un modèle de table Swing pour affichage des contrats d'un client.
     * <p>
     * Crée un DefaultTableModel non-éditable avec colonnes :
     * ID, Nom Contrat, Montant (€)
     * </p>
     *
     * @param client le client dont on veut afficher les contrats
     * @return modèle de table prêt pour JTable
     * @throws DAOException si une erreur survient lors de la récupération des contrats
     */
    public DefaultTableModel construireTableModel(Client client) throws DAOException {
        String[] colonnes = {"ID", "Nom Contrat", "Montant (€)"};

        DefaultTableModel model = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Contrat> contrats = getContratsParClient(client.getId());

        for (Contrat contrat : contrats) {
            Object[] row = {
                    contrat.getId(),
                    contrat.getNomContrat(),
                    String.format("%,.2f €", contrat.getMontant())
            };
            model.addRow(row);
        }

        return model;
    }

    /**
     * Calcule le montant total des contrats d'un client.
     *
     * @param clientId identifiant du client
     * @return le montant total
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public double getMontantTotalContrats(Integer clientId) throws DAOException {
        try {
            List<Contrat> contrats = getContratsParClient(clientId);
            return contrats.stream()
                    .mapToDouble(Contrat::getMontant)
                    .sum();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur calcul montant total contrats client ID={0}",
                    clientId);
            throw e;
        }
    }

    /**
     * Récupère le nombre de contrats d'un client.
     *
     * @param clientId identifiant du client
     * @return le nombre de contrats
     * @throws DAOException si une erreur survient lors de la récupération
     */
    public int getNombreContrats(Integer clientId) throws DAOException {
        try {
            return getContratsParClient(clientId).size();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur comptage contrats client ID={0}",
                    clientId);
            throw e;
        }
    }
}
