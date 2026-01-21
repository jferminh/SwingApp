package main.com.julio.view;

import main.com.julio.exception.DAOException;
import main.com.julio.model.Client;
import main.com.julio.util.DisplayDialog;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * Vue d'affichage en liste (tableau) des clients ou prospects.
 * Permet la sélection et navigation vers les opérations CRUD.
 *
 * @author Julio FERMIN
 * @version 1.1
 * @since 21/01/2026
 */
public class ListeView extends JFrame {

    // ViewModels - Pattern MVVM
    private final ClientViewModel clientVM;
    private final ProspectViewModel prospectVM;
    private final ContratViewModel contratVM;

    // Contexte de la liste
    private final boolean isClient;  // true = liste clients, false = liste prospects

    // Composants UI
    private JTable table;
    private DefaultTableModel tableModel;

    /**
     * Constructeur initialisant la vue de liste.
     *
     * @param clientVM ViewModel des clients
     * @param prospectVM ViewModel des prospects
     * @param contratVM ViewModel des contrats
     * @param isClient true pour liste clients, false pour liste prospects
     */
    ListeView(ClientViewModel clientVM, ProspectViewModel prospectVM, ContratViewModel contratVM, boolean isClient) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;
        this.contratVM = contratVM;
        this.isClient = isClient;

        initComponents();
        chargerDonnees();
    }

    /**
     * Initialise l'interface graphique avec table et boutons d'action.
     */
    private void initComponents() {
        String type = isClient ? "Client" : "Prospect";
        setTitle("Gestion " + type);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // En-tête avec titre dynamique
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titre = new JLabel("Liste des " + type + "s");
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titre);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ✅ Initialiser table avec modèle vide (sera remplie dans chargerDonnees)
        creerTableVide();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // Une ligne à la fois
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons: CRUD + Navigation
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnCreer = new JButton("Créer");
        btnCreer.setPreferredSize(new Dimension(120, 35));
        btnCreer.addActionListener(e -> ouvrirFormulaire(null, "Créer"));
        buttonPanel.add(btnCreer);

        JButton btnModifier = new JButton("Modifier");
        btnModifier.setPreferredSize(new Dimension(120, 35));
        btnModifier.addActionListener(e -> modifierSelection());
        buttonPanel.add(btnModifier);

        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setPreferredSize(new Dimension(120, 35));
        btnSupprimer.addActionListener(e -> supprimerSelection());
        buttonPanel.add(btnSupprimer);

        // Bouton "Voir Contrats" uniquement pour clients
        if (isClient) {
            JButton btnContrats = new JButton("Voir Contrats");
            btnContrats.setPreferredSize(new Dimension(120, 35));
            btnContrats.addActionListener(e -> voirContrats());
            buttonPanel.add(btnContrats);
        }

        JButton btnAccueil = new JButton("Accueil");
        btnAccueil.setPreferredSize(new Dimension(120, 35));
        btnAccueil.addActionListener(e -> retourAccueil());
        buttonPanel.add(btnAccueil);

        JButton btnQuitter = new JButton("Quitter");
        btnQuitter.setPreferredSize(new Dimension(120, 35));
        btnQuitter.addActionListener(e -> {
            LOGGER.log(Level.INFO, "Application terminée (Quitter)");
            System.exit(0);
        });
        buttonPanel.add(btnQuitter);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Crée une table vide avec les colonnes appropriées.
     * Utilisé en cas d'erreur de chargement ou à l'initialisation.
     */
    private void creerTableVide() {
        String[] colonnes;

        if (isClient) {
            colonnes = new String[]{"ID", "Raison Sociale", "Adresse", "Téléphone",
                    "Email", "CA (€)", "Nb Employés", "Nb Contrats"};
        } else {
            colonnes = new String[]{"ID", "Raison Sociale", "Adresse", "Téléphone",
                    "Email", "Date Prospection", "Intéressé"};
        }

        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Table en lecture seule
            }
        };
    }

    /**
     * Recharge les données de la table depuis le ViewModel.
     * Gère les erreurs en affichant un message et en créant une table vide.
     */
    private void chargerDonnees() {
        try {
            // ✅ Appel avec gestion d'exception
            tableModel = isClient
                    ? clientVM.construireTableModel()
                    : prospectVM.construireTableModel();

            table.setModel(tableModel);

        } catch (DAOException e) {
            // ✅ Message d'erreur adapté selon le type d'erreur
            String type = isClient ? "clients" : "prospects";
            String message = switch (e.getErrorCode()) {
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.\n" +
                                "Vérifiez que le serveur MySQL est démarré.";
                case READ_ERROR ->
                        "Erreur lors de la lecture des " + type + ".";
                default ->
                        "Erreur lors du chargement des " + type + " :\n" + e.getMessage();
            };

            DisplayDialog.messageError("Erreur de Chargement", message);

            // ✅ Créer une table vide pour éviter NullPointerException
            creerTableVide();
            table.setModel(tableModel);
        }
    }

    /**
     * Ouvre le formulaire pour création ou modification/suppression.
     *
     * @param id ID de l'entité (null pour création)
     * @param action action à effectuer ("Créer", "Modifier", "Supprimer")
     */
    private void ouvrirFormulaire(Integer id, String action) {
        FormulaireView form = new FormulaireView(clientVM, prospectVM, contratVM, isClient, id, action, "listeview");
        form.setVisible(true);
        this.dispose();

        // WindowListener pour recharger données au retour
        form.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                chargerDonnees();
            }
        });
    }

    /**
     * Modifie l'entité sélectionnée dans la table.
     * Vérifie qu'une ligne est sélectionnée avant d'ouvrir le formulaire.
     */
    private void modifierSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            DisplayDialog.messageWarning("Aucune sélection",
                    "Veuillez sélectionner une ligne");
            return;
        }

        // ID situé en colonne 0
        int id = (int) table.getValueAt(selectedRow, 0);
        ouvrirFormulaire(id, "Modifier");
    }

    /**
     * Supprime l'entité sélectionnée dans la table.
     * Vérifie qu'une ligne est sélectionnée avant d'ouvrir le formulaire de confirmation.
     */
    private void supprimerSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            DisplayDialog.messageWarning("Aucune sélection",
                    "Veuillez sélectionner une ligne");
            return;
        }

        int id = (int) table.getValueAt(selectedRow, 0);
        ouvrirFormulaire(id, "Supprimer");
    }

    /**
     * Affiche les contrats du client sélectionné.
     * Uniquement disponible pour la liste des clients.
     */
    private void voirContrats() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            DisplayDialog.messageWarning("Aucune sélection",
                    "Veuillez sélectionner un client");
            return;
        }

        try {
            // ✅ Récupérer le client complet depuis le ViewModel avec gestion d'exception
            int clientId = (int) table.getValueAt(selectedRow, 0);
            Client client = clientVM.getClientById(clientId);

            if (client == null) {
                DisplayDialog.messageWarning("Client introuvable",
                        "Le client sélectionné n'existe plus.");
                chargerDonnees();  // Rafraîchir la liste
                return;
            }

            ListeContratsView contratsView = new ListeContratsView(clientVM, prospectVM, contratVM, client, "listeview");
            contratsView.setVisible(true);
            this.dispose();

        } catch (DAOException e) {
            // ✅ Gestion erreur récupération client
            String message = switch (e.getErrorCode()) {
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                case READ_ERROR ->
                        "Erreur lors de la récupération du client.";
                default ->
                        "Erreur : " + e.getMessage();
            };

            DisplayDialog.messageError("Erreur", message);
        }
    }

    /**
     * Retourne à la vue d'accueil.
     */
    private void retourAccueil() {
        AccueilView accueil = new AccueilView(clientVM, prospectVM, contratVM);
        accueil.setVisible(true);
        this.dispose();
    }
}
