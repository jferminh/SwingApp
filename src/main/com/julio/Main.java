package main.com.julio;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Adresse;
import main.com.julio.model.Client;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.service.LoggerService;
import main.com.julio.view.AccueilView;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Point d'entrée principal de l'application de gestion Clients-Prospects.
 * <p>
 * Cette classe initialise :
 * Le Look & Feel de l'application
 * Les ViewModels (ClientViewModel, ProspectViewModel, ContratViewModel)
 * Les données de démonstration (optionnel)
 * L'interface graphique principale (AccueilView)
 * <p>
 * Architecture MVVM :
 * - Main → ViewModel → DAO → Base de données
 * - Main → View → ViewModel
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 20/01/2026
 */
public class Main {

    private static final Logger LOGGER = LoggerService.getLogger(Main.class);

    // Flag pour activer/désactiver le chargement des données de démonstration
    private static final boolean CHARGER_DONNEES_DEMO = false;

    /**
     * Point d'entrée de l'application.
     *
     * @param args arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        LOGGER.info("=== Démarrage de l'application Gestion Clients-Prospects ===");

        // 1. Configurer le Look & Feel
        configurerLookAndFeel();

        // 2. Initialiser les ViewModels
        ClientViewModel clientVM = null;
        ProspectViewModel prospectVM = null;
        ContratViewModel contratVM = null;

        try {
            LOGGER.info("Initialisation des ViewModels...");

            clientVM = new ClientViewModel();
            prospectVM = new ProspectViewModel();
            contratVM = new ContratViewModel();

            LOGGER.info("ViewModels initialisés avec succès");

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Erreur fatale lors de l'initialisation des DAO", e);
            afficherErreurFatale(
                    "Impossible de démarrer l'application",
                    "Erreur de connexion à la base de données :\n" + e.getMessage() +
                            "\n\nVérifiez :\n" +
                            "- Que MySQL est démarré\n" +
                            "- Que la base de données 'reverso' existe\n" +
                            "- Que les identifiants de connexion sont corrects"
            );
            System.exit(1);
        }

        // 3. Charger les données de démonstration (optionnel)
        if (CHARGER_DONNEES_DEMO) {
            chargerDonneesDemo(clientVM, prospectVM, contratVM);
        }

        // 4. Lancer l'interface graphique sur l'EDT (Event Dispatch Thread)
        ClientViewModel finalClientVM = clientVM;
        ProspectViewModel finalProspectVM = prospectVM;
        ContratViewModel finalContratVM = contratVM;

        SwingUtilities.invokeLater(() -> {
            try {
                LOGGER.info("Lancement de l'interface graphique...");

                AccueilView accueil = new AccueilView(
                        finalClientVM,
                        finalProspectVM,
                        finalContratVM
                );

                accueil.setVisible(true);

                LOGGER.info("Application démarrée avec succès");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du lancement de l'interface", e);
                afficherErreurFatale(
                        "Erreur d'interface",
                        "Impossible de démarrer l'interface graphique :\n" + e.getMessage()
                );
                System.exit(1);
            }
        });
    }

    /**
     * Configure le Look & Feel de l'application.
     * <p>
     * Tente d'utiliser le Look & Feel natif du système d'exploitation.
     * En cas d'échec, utilise le Look & Feel par défaut de Java (Metal).
     */
    private static void configurerLookAndFeel() {
        try {
            LOGGER.fine("Configuration du Look & Feel...");

            // Utiliser le Look & Feel du système (Windows, macOS, Linux)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            LOGGER.fine("Look & Feel configuré : " + UIManager.getLookAndFeel().getName());

        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Impossible de configurer le Look & Feel natif, utilisation du Look & Feel par défaut",
                    e);

            // Continuer avec le Look & Feel par défaut (Metal)
        }
    }

    /**
     * Charge des données de démonstration dans la base de données.
     * <p>
     * Cette méthode crée :
     * - 3 clients avec leurs adresses
     * - 2 prospects avec leurs adresses
     * - 4 contrats associés aux clients.
     * <p>
     * Les données sont créées via les ViewModels pour respecter toutes
     * les validations et la logique métier.
     *
     * @param clientVM ViewModel des clients
     * @param prospectVM ViewModel des prospects
     * @param contratVM ViewModel des contrats
     */
    private static void chargerDonneesDemo(
            ClientViewModel clientVM,
            ProspectViewModel prospectVM,
            ContratViewModel contratVM) {

        LOGGER.info("Chargement des données de démonstration...");

        try {
            // ========== CRÉATION DES CLIENTS ==========

            // Client 1 : TechCorp Solutions
            Client client1 = clientVM.creerClient(
                    "TechCorp Solutions",
                    "15",
                    "Avenue des Champs-Élysées",
                    "75008",
                    "Paris",
                    "01 42 56 78 90",
                    "contact@techcorp.fr",
                    "Client premium depuis 2020",
                    150000,
                    25
            );
            LOGGER.info("Client créé : " + client1.getRaisonSociale());

            // Client 2 : Digital Innovators
            Client client2 = clientVM.creerClient(
                    "Digital Innovators",
                    "33",
                    "Rue de la République",
                    "69002",
                    "Lyon",
                    "04 78 12 34 56",
                    "info@digital-innovators.fr",
                    "Spécialisé en transformation digitale",
                    250000,
                    50
            );
            LOGGER.info("Client créé : " + client2.getRaisonSociale());

            // Client 3 : WebServices Pro
            Client client3 = clientVM.creerClient(
                    "WebServices Pro",
                    "7",
                    "Boulevard Saint-Germain",
                    "75005",
                    "Paris",
                    "01 43 26 85 92",
                    "contact@webservices-pro.com",
                    "Développement web et mobile",
                    95000,
                    15
            );
            LOGGER.info("Client créé : " + client3.getRaisonSociale());

            // ========== CRÉATION DES PROSPECTS ==========

            // Prospect 1 : FutureTech SARL
            Prospect prospect1 = prospectVM.creerProspect(
                    "FutureTech SARL",
                    "42",
                    "Rue Victor Hugo",
                    "13001",
                    "Marseille",
                    "04 91 55 66 77",
                    "contact@futuretech.fr",
                    "Intéressé par nos solutions cloud",
                    LocalDate.now().minusDays(15),
                    Interesse.OUI
            );
            LOGGER.info("Prospect créé : " + prospect1.getRaisonSociale());

            // Prospect 2 : InnoSystems
            Prospect prospect2 = prospectVM.creerProspect(
                    "InnoSystems",
                    "88",
                    "Avenue Jean Jaurès",
                    "33000",
                    "Bordeaux",
                    "05 56 78 90 12",
                    "info@innosystems.com",
                    "Premier contact suite à salon professionnel",
                    LocalDate.now().minusDays(5),
                    Interesse.NON
            );
            LOGGER.info("Prospect créé : " + prospect2.getRaisonSociale());

            // ========== CRÉATION DES CONTRATS ==========

            // Contrats pour TechCorp Solutions (client1)
            contratVM.creerContrat(
                    client1.getId(),
                    "Développement application mobile",
                    45000.00
            );
            LOGGER.info("Contrat créé pour : " + client1.getRaisonSociale());

            contratVM.creerContrat(
                    client1.getId(),
                    "Maintenance annuelle",
                    12000.00
            );
            LOGGER.info("Contrat créé pour : " + client1.getRaisonSociale());

            // Contrat pour Digital Innovators (client2)
            contratVM.creerContrat(
                    client2.getId(),
                    "Refonte site web e-commerce",
                    85000.00
            );
            LOGGER.info("Contrat créé pour : " + client2.getRaisonSociale());

            // Contrat pour WebServices Pro (client3)
            contratVM.creerContrat(
                    client3.getId(),
                    "Audit de sécurité",
                    15000.00
            );
            LOGGER.info("Contrat créé pour : " + client3.getRaisonSociale());

            LOGGER.info("Données de démonstration chargées avec succès : " +
                    "3 clients, 2 prospects, 4 contrats");

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING,
                    "Erreur de validation lors du chargement des données de démonstration", e);
            LOGGER.warning("Les données de démonstration n'ont pas pu être chargées complètement");

        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING,
                    "Erreur lors du chargement des données de démonstration", e);
            LOGGER.warning("Les données de démonstration n'ont pas pu être chargées complètement");
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur fatale et arrête l'application.
     *
     * @param titre titre de la boîte de dialogue
     * @param message message d'erreur détaillé
     */
    private static void afficherErreurFatale(String titre, String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                titre,
                JOptionPane.ERROR_MESSAGE
        );
    }
}

