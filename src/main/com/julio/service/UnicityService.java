package main.com.julio.service;

import main.com.julio.dao.ClientDAO;
import main.com.julio.dao.ProspectDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.model.Client;
import main.com.julio.model.Prospect;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service de vérification de l'unicité des données métier.
 * <p>
 * Ce service vérifie l'unicité de la raison sociale à travers
 * les tables Client et Prospect pour éviter les doublons dans le système.
 * </p>
 * <p>
 * <strong>Utilisation :</strong>
 * - Avant création : exclure ID null ou 0
 * - Avant modification : exclure l'ID de l'entité en cours de modification
 * </p>
 *
 * @author Julio FERMIN
 * @version 2.1
 * @since 22/01/2026
 */
public class UnicityService {
    private static final Logger LOGGER = LoggerService.getLogger(UnicityService.class);

    private final ClientDAO clientDAO;
    private final ProspectDAO prospectDAO;

    /**
     * Constructeur avec injection des DAO.
     *
     * @param clientDAO DAO des clients
     * @param prospectDAO DAO des prospects
     * @throws IllegalArgumentException si un des DAO est null
     */
    public UnicityService(ClientDAO clientDAO, ProspectDAO prospectDAO) {
        if (clientDAO == null || prospectDAO == null) {
            throw new IllegalArgumentException("Les DAO ne peuvent pas être null");
        }
        this.clientDAO = clientDAO;
        this.prospectDAO = prospectDAO;
    }

    /**
     * Vérifie si une raison sociale existe déjà dans le système (clients + prospects).
     * <p>
     * Cette méthode vérifie l'unicité en excluant éventuellement une entité spécifique
     * (utile lors de la modification pour exclure l'entité en cours d'édition).
     * </p>
     * <p>
     * <strong>Exemples d'utilisation :</strong>
     * </p>
     * <pre>
     * // Création (pas d'ID à exclure)
     * if (unicityService.isRaisonSocialeDupliquee("ACME Corp", null)) {
     *     throw new ValidationException("Cette raison sociale existe déjà");
     * }
     *
     * // Modification (exclure l'ID de l'entité en cours)
     * if (unicityService.isRaisonSocialeDupliquee("ACME Corp", clientId)) {
     *     throw new ValidationException("Cette raison sociale est déjà utilisée");
     * }
     * </pre>
     *
     * @param raisonSociale la raison sociale à vérifier (sensible à la casse)
     * @param idExclure ID de l'entité à exclure de la vérification (null ou 0 pour création)
     * @return true si un doublon existe, false si la raison sociale est unique
     * @throws DAOException si une erreur survient lors de l'accès aux données
     * @throws IllegalArgumentException si raisonSociale est null ou vide
     */
    public boolean isRaisonSocialeDupliquee(String raisonSociale, Integer idExclure)
            throws DAOException {

        // Validation paramètre
        if (raisonSociale == null || raisonSociale.trim().isEmpty()) {
            throw new IllegalArgumentException("La raison sociale ne peut pas être null ou vide");
        }

        // Normaliser l'ID à exclure
        Integer idIgnore = (idExclure == null || idExclure <= 0) ? 0 : idExclure;

        try {
            // ========== 1. Vérifier côté PROSPECTS ==========
            Prospect prospect = prospectDAO.findByRaisonSociale(raisonSociale);
            if (prospect != null && !prospect.getId().equals(idIgnore)) {
                return true;  // Doublon trouvé dans prospects
            }

            // ========== 2. Vérifier côté CLIENTS ==========
            Client client = clientDAO.findByRaisonSociale(raisonSociale);
            if (client != null && !client.getId().equals(idIgnore)) {
                return true;  // Doublon trouvé dans clients
            }

            // Aucun doublon trouvé
            return false;

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur lors de la vérification unicité raison sociale ''{0}''",
                    raisonSociale);
            throw e;  // Propager l'exception
        }
    }

    /**
     * Vérifie si une raison sociale existe déjà (version simplifiée pour création).
     * <p>
     * Équivalent à {@code isRaisonSocialeDupliquee(raisonSociale, null)}.
     * </p>
     *
     * @param raisonSociale la raison sociale à vérifier
     * @return true si un doublon existe, false sinon
     * @throws DAOException si une erreur survient lors de l'accès aux données
     */
    public boolean isRaisonSocialeDupliquee(String raisonSociale) throws DAOException {
        return isRaisonSocialeDupliquee(raisonSociale, null);
    }
}
