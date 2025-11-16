import main.com.julio.repository.ClientRepository;
import main.com.julio.repository.ContratRepository;
import main.com.julio.repository.ProspectRepository;
import main.com.julio.service.UnicityService;
import main.com.julio.view.AccueilView;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;


void main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        e.printStackTrace();
    }

    // Initialisation des repositories
    ClientRepository clientRepo = new ClientRepository();
    ProspectRepository prospectRepo = new ProspectRepository();
    ContratRepository contratRepo = new ContratRepository();

    // Initialisation des services
    UnicityService unicityService = new UnicityService(clientRepo, prospectRepo);

    // Initialisation des ViewModels
    ClientViewModel clientVM = new ClientViewModel(clientRepo, contratRepo, unicityService);
    ProspectViewModel prospectVM = new ProspectViewModel(prospectRepo, unicityService);

    SwingUtilities.invokeLater(() -> {
        AccueilView accueil = new AccueilView(clientVM, prospectVM);
        accueil.setVisible(true);
    });
}
