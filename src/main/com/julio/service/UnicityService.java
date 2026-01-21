package main.com.julio.service;

import main.com.julio.dao.ClientDAO;
import main.com.julio.dao.ProspectDAO;
import main.com.julio.exception.DAOException;
import main.com.julio.model.Client;
import main.com.julio.model.Prospect;

/**
 * Service de vérification de l'unicité des données métier.
 * <p>
 * Version optimisée utilisant des requêtes ciblées dans les DAO
 * plutôt que des parcours complets via findAll().
 * </p>
 */
public class UnicityService {

    private final ClientDAO clientDAO;
    private final ProspectDAO prospectDAO;

    public UnicityService(ClientDAO clientDAO, ProspectDAO prospectDAO) {
        this.clientDAO = clientDAO;
        this.prospectDAO = prospectDAO;
    }

    /**
     * Vérifie si une raison sociale existe déjà dans le système
     * (clients + prospects), en excluant éventuellement une entité.
     *
     * @param raisonSociale la raison sociale à vérifier
     * @param idExclure     ID à exclure (0 ou null si création)
     * @return true si doublon trouvé, false sinon
     * @throws DAOException en cas d'erreur d'accès aux données
     */
    public boolean isRaisonSocialDuplique(String raisonSociale, Integer idExclure)
            throws DAOException {

        Integer idIgnore = (idExclure == null) ? 0 : idExclure;

        // 1) Vérifier côté prospects
        Prospect prospect = prospectDAO.findByRaisonSociale(raisonSociale);
        if (prospect != null && !prospect.getId().equals(idIgnore)) {
            return true;
        }

        // 2) Vérifier côté clients
        Client client = clientDAO.findByRaisonSociale(raisonSociale);
        if (client != null && !client.getId().equals(idIgnore)) {
            return true;
        }

        return false;
    }
}
