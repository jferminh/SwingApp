package main.com.julio.app_init;

import main.com.julio.exception.ValidationException;
import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;
import main.com.julio.repository.ProspectRepository;
import main.com.julio.service.LoggingService;
import main.com.julio.service.UnicityService;
import main.com.julio.view.AccueilView;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * Point d'entrée et configuration de démarrage de l'application Swing (MVVM).
 * <p>
 * Initialise le logging, le Look and Feel, les repositories, services et ViewModels,
 * puis lance la vue d'accueil sur l'Event Dispatch Thread (EDT).
 * </p>
 *
 */
public class MainApp {

    /**
     * Lance l'application desktop.
     * @throws ValidationException échec d'initialisation des données de démo
     * @throws IOException échec d'initialisation du fichier de logs
     */
    void main() throws ValidationException, IOException {
        // Logging fichier + formatter custom
        LoggingService.intFichierLog();
        LOGGER.log(Level.INFO, "Démarrage de la application");

        // Look & Feel natif (fallback + log SEVERE en cas d'échec)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        // Repositories en mémoire (clients préchargés avec contrats)
        ContratRepository contratRepo = new ContratRepository();
        ClientRepository clientRepo = new ClientRepository(contratRepo);
        ProspectRepository prospectRepo = new ProspectRepository();

        // Services transverses
        UnicityService unicityService = new UnicityService(clientRepo, prospectRepo);

        // ViewModels (injection par constructeur)
        ClientViewModel clientVM = new ClientViewModel(clientRepo, contratRepo, unicityService);
        ProspectViewModel prospectVM = new ProspectViewModel(prospectRepo, unicityService);
        ContratViewModel contratVM = new ContratViewModel(contratRepo, clientRepo);

        // Lancer l'UI sur l'Event Dispatch Thread (bonne pratique Swing)
        SwingUtilities.invokeLater(() -> {
            AccueilView accueil = new AccueilView(clientVM, prospectVM, contratVM);
            accueil.setVisible(true);
        });
    }
}
