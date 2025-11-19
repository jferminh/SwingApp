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

import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;


void main() throws ValidationException, IOException {
    // Initialisation de Logs
    LoggingService.intFichierLog();
    LOGGER.log(Level.INFO, "Démarrage de la application");

    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        e.printStackTrace();
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }


    // Initialisation des repositories
    ContratRepository contratRepo = new ContratRepository();
    ClientRepository clientRepo = new ClientRepository(contratRepo);
    ProspectRepository prospectRepo = new ProspectRepository();

    // Initialisation des services
    UnicityService unicityService = new UnicityService(clientRepo, prospectRepo);

    // Initialisation des ViewModels
    ClientViewModel clientVM = new ClientViewModel(clientRepo, contratRepo, unicityService);
    ProspectViewModel prospectVM = new ProspectViewModel(prospectRepo, unicityService);
    ContratViewModel contratVM = new ContratViewModel(contratRepo, clientRepo);

    SwingUtilities.invokeLater(() -> {
        AccueilView accueil = new AccueilView(clientVM, prospectVM, contratVM);
        accueil.setVisible(true);
    });
}
