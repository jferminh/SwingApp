package main.com.julio;

import main.com.julio.exception.DAOException;
import main.com.julio.service.LoggerService;
import main.com.julio.util.DisplayDialog;
import main.com.julio.view.AccueilView;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Point d'entrée principal de l'application de gestion clients-prospects.
 * Initialise les ViewModels et affiche la vue d'accueil.
 *
 * @author Julio FERMIN
 * @version 2.1
 * @since 21/01/2026
 */
public class Main {
    private static final Logger LOGGER = LoggerService.getLogger(Main.class);

    /**
     * Méthode principale de l'application.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        LOGGER.info("=== Démarrage de l'application Gestion Clients-Prospects ===");
        // ✅ Configuration Look & Feel système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Impossible de définir le Look & Feel système", e);
        }

        // ✅ Lancer l'application sur l'Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // ========== Initialisation des ViewModels ==========
                LOGGER.log(Level.INFO, "Démarrage de l'application...");

                ClientViewModel clientVM = new ClientViewModel();
                ProspectViewModel prospectVM = new ProspectViewModel();
                ContratViewModel contratVM = new ContratViewModel();

                LOGGER.log(Level.INFO, "ViewModels initialisés avec succès");

                // ========== Affichage de la vue d'accueil ==========
                AccueilView accueilView = new AccueilView(clientVM, prospectVM, contratVM);
                accueilView.setVisible(true);

                LOGGER.log(Level.INFO, "Application démarrée avec succès");

            } catch (DAOException e) {
                // ✅ Gestion erreur fatale : impossible d'initialiser les ViewModels
                LOGGER.log(Level.SEVERE, "Erreur fatale lors de l'initialisation de l'application", e);

                String message = switch (e.getErrorCode()) {
                    case CONNECTION_ERROR ->
                            "Impossible de se connecter à la base de données.\n\n" +
                                    "Vérifiez que :\n" +
                                    "• Le serveur MySQL est démarré\n" +
                                    "• Les paramètres de connexion sont corrects\n" +
                                    "• La base de données existe\n\n" +
                                    "Détails : " + e.getMessage();
                    case INVALID_PARAMETER ->
                            "Erreur de configuration de l'application.\n\n" +
                                    "Détails : " + e.getMessage();
                    default ->
                            "Erreur lors du démarrage de l'application.\n\n" +
                                    "Détails : " + e.getMessage();
                };

                // ✅ Afficher message d'erreur à l'utilisateur
                DisplayDialog.messageError("Erreur Fatale", message);

                // ✅ Terminer l'application
                LOGGER.log(Level.SEVERE, "Application terminée suite à une erreur fatale");
                System.exit(1);

            } catch (Exception e) {
                // ✅ Gestion erreur inattendue
                LOGGER.log(Level.SEVERE, "Erreur inattendue lors du démarrage", e);

                DisplayDialog.messageError(
                        "Erreur Inattendue",
                        "Une erreur inattendue s'est produite :\n\n" + e.getMessage()
                );

                System.exit(1);
            }
        });
    }
}
