package main.com.julio.service;

import main.com.julio.model.Client;
import main.com.julio.model.Prospect;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ProspectRepository;

/**
 * Service de vérification de l'unicité des données métier.
 * <p>
 * Cette classe fournit des méthodes pour garantir l'unicité des raisons sociales
 * des sociétés (clients et prospects). Elle interroge les repositories pour
 * détecter les doublons potentiels avant la création ou la modification d'entités.
 * </p>
 *
 * <p><b>Règles métier appliquées :</b></p>
 * <ul>
 *   <li>Une raison sociale doit être unique à travers tous les clients ET prospects</li>
 *   <li>La comparaison est insensible à la casse (majuscules/minuscules)</li>
 *   <li>Lors d'une modification, l'entité en cours d'édition est exclue de la vérification</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see ClientRepository
 * @see ProspectRepository
 */
public class UnicityService {

    /** Repository des clients pour vérifier l'unicité parmi les clients */
    private final ClientRepository clientRepo;

    /** Repository des prospects pour vérifier l'unicité parmi les prospects */
    private final ProspectRepository prospectRepo;

    /**
     * Constructeur initialisant le service avec les repositories nécessaires.
     * <p>
     * Les repositories sont injectés via le constructeur pour faciliter
     * les tests unitaires (injection de dépendances).
     * </p>
     *
     * @param clientRepo repository des clients
     * @param prospectRepo repository des prospects
     */
    public UnicityService(ClientRepository clientRepo, ProspectRepository prospectRepo) {
        this.clientRepo = clientRepo;
        this.prospectRepo = prospectRepo;
    }

    /**
     * Vérifie si une raison sociale existe déjà dans le système.
     * <p>
     * Cette méthode parcourt tous les prospects et clients pour détecter
     * si la raison sociale fournie est déjà utilisée. La vérification est
     * insensible à la casse grâce à {@link String#equalsIgnoreCase(String)}.
     * </p>
     *
     * <p><b>Algorithme de vérification :</b></p>
     * <ol>
     *   <li>Parcourt tous les prospects en excluant celui dont l'ID correspond à {@code idExcluire}</li>
     *   <li>Si une correspondance est trouvée, retourne immédiatement true (doublon détecté)</li>
     *   <li>Parcourt tous les clients en excluant celui dont l'ID correspond à {@code idExcluire}</li>
     *   <li>Si une correspondance est trouvée, retourne immédiatement true (doublon détecté)</li>
     *   <li>Si aucune correspondance n'est trouvée, retourne false (raison sociale unique)</li>
     * </ol>
     *
     * @param raisonSociale la raison sociale à vérifier (ne devrait pas être null ou vide)
     * @param idExcluire l'identifiant de l'entité à exclure de la vérification
     *                   (0 pour une nouvelle entité, ID existant pour une modification)
     * @return true si la raison sociale existe déjà (doublon détecté), false si elle est unique
     */
    public boolean isRaisonSocialDuplique(String raisonSociale, int idExcluire) {
        // Vérification dans les prospects
        for (Prospect prospect : prospectRepo.findAll()) {
            if (prospect.getId() != idExcluire && prospect.getRaisonSociale().equalsIgnoreCase(raisonSociale)) {
                return true; // Doublon trouvé dans les prospects
            }
        }

        // Vérification dans les clients
        for (Client client : clientRepo.findAll()) {
            if (client.getId() != idExcluire && client.getRaisonSociale().equalsIgnoreCase(raisonSociale)) {
                return true; // Doublon trouvé dans les clients
            }
        }

        return false; // Aucun doublon : raison sociale unique
    }
}
