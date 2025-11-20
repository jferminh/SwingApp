package main.com.julio.exception;

/**
 * Exception levée lorsqu'une entité recherchée n'est pas trouvée dans le système.
 * <p>
 * Cette exception non vérifiée (hérite de {@link RuntimeException}) est utilisée
 * pour signaler qu'une ressource demandée (client, prospect, contrat, etc.)
 * n'existe pas dans la collection en mémoire.
 * </p>
 * <p>
 * En tant que RuntimeException, elle n'a pas besoin d'être déclarée explicitement
 * dans les signatures de méthodes avec throws, ce qui simplifie le code pour
 * les cas d'erreur exceptionnels.
 * </p>
 *
 * <p><b>Exemples d'utilisation :</b></p>
 * <pre>
 * // Recherche d'un client inexistant
 * throw new NotFoundException("Client avec l'ID 42 introuvable");
 *
 * // Recherche d'un contrat inexistant
 * throw new NotFoundException("Aucun contrat trouvé pour ce client");
 * </pre>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see RuntimeException
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructeur créant une exception avec un message descriptif.
     * <p>
     * Le message devrait clairement identifier le type de ressource
     * non trouvée et éventuellement l'identifiant recherché pour
     * faciliter le débogage et l'information à l'utilisateur.
     * </p>
     *
     * @param message message décrivant la ressource non trouvée
     */
    public NotFoundException(String message) {
        super(message);
    }
}
