package main.com.julio.view;

import main.com.julio.model.Client;
import main.com.julio.model.Prospect;
import main.com.julio.util.DisplayDialog;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * Vue principale (écran d'accueil) de l'application de gestion clients-prospects.
 * Permet de sélectionner le type d'entité (Client/Prospect) et d'effectuer les opérations CRUD.
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public class AccueilView extends JFrame {

    // ViewModels - Pattern MVVM pour séparer logique métier et présentation
    private ClientViewModel clientVM;
    private ProspectViewModel prospectVM;
    private ContratViewModel contratVM;

    // Composants UI principaux
    private JLabel titre;
    private JRadioButton rbClients;
    private JRadioButton rbProspects;
    private JButton btnVoirContrats;
    private JPanel selectPanel;
    private JComboBox<Object> comboSelect;

    // Gestion de l'état de l'interface
    private String currentAction = null;  // Action en cours: "modifier", "supprimer", "voirContrats"
    private final String origin = "accueil";  // Identifiant pour la navigation

    /**
     * Constructeur initialisant la vue d'accueil avec les ViewModels.
     *
     * @param clientVM ViewModel des clients
     * @param prospectVM ViewModel des prospects
     * @param contratVM ViewModel des contrats
     */
    public AccueilView(ClientViewModel clientVM, ProspectViewModel prospectVM, ContratViewModel contratVM) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;
        this.contratVM = contratVM;

        initComponents();
    }

    /**
     * Initialise les composants graphiques de la vue.
     * Configure la fenêtre, les panneaux, les boutons et les listeners.
     */
    private void initComponents() {
        // Configuration fenêtre principale
        setTitle("Gestion Clients-Prospects - Accueil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);  // Centrer sur l'écran
        setResizable(false);

        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Titre dynamique (change selon Client/Prospect)
        titre = new JLabel("Gestion des entités : Clients");
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titre, BorderLayout.NORTH);

        // Panel central avec GridBagLayout pour positionnement flexible
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Radio buttons pour sélection Client/Prospect
        rbClients = new JRadioButton("Clients");
        rbProspects = new JRadioButton("Prospects");
        rbClients.setSelected(true);  // Clients par défaut
        ButtonGroup group = new ButtonGroup();  // Exclusion mutuelle
        group.add(rbClients);
        group.add(rbProspects);

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        radios.add(rbClients);
        radios.add(rbProspects);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel labelSelection = new JLabel("Sélectionnez le type d'entité : ");
        centerPanel.add(labelSelection, gbc);
        gbc.gridy = 1;
        centerPanel.add(radios, gbc);

        // Panel des actions CRUD
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnCreer = new JButton("Créer");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        btnVoirContrats = new JButton("Voir Contrats");
        JButton btnAfficher = new JButton("Afficher");
        JButton btnQuitter = new JButton("Quitter");

        // Uniformisation taille des boutons
        Dimension btnSize = new Dimension(140, 36);
        for (JButton b : new JButton[]{btnCreer, btnModifier, btnSupprimer, btnVoirContrats, btnAfficher}) {
            b.setPreferredSize(btnSize);
            actions.add(b);
        }

        gbc.gridy = 2;
        centerPanel.add(actions, gbc);

        // Panel de sélection d'entité (masqué par défaut)
        selectPanel = new JPanel(new GridBagLayout());
        selectPanel.setBorder(BorderFactory.createTitledBorder("Sélectionnez une société :"));

        GridBagConstraints gs = new GridBagConstraints();
        gs.insets = new Insets(8, 8, 8, 8);
        gs.anchor = GridBagConstraints.WEST;

        comboSelect = new JComboBox<>();
        comboSelect.setPreferredSize(new Dimension(420, 28));
        gs.gridy = 0;
        gs.weightx = 0;
        gs.fill = GridBagConstraints.HORIZONTAL;
        selectPanel.add(comboSelect, gs);

        // Boutons Valider/Annuler pour la sélection
        JPanel buttonSelect = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnValider = new JButton("Valider");
        JButton btnAnnuler = new JButton("Annuler");
        buttonSelect.add(btnValider);
        buttonSelect.add(btnAnnuler);

        gs.gridx = 0;
        gs.gridy = 1;
        gs.gridwidth = 2;
        gs.weightx = 0;
        gs.fill = GridBagConstraints.NONE;
        selectPanel.add(buttonSelect, gs);

        gbc.gridy = 3;
        centerPanel.add(selectPanel, gbc);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panel inférieur avec bouton Quitter
        JPanel bottonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottonPanel.add(btnQuitter);
        mainPanel.add(bottonPanel, BorderLayout.SOUTH);

        setSelectPanelVisible(false);  // Masquer au démarrage

        // === LISTENERS ===

        // Changement de type d'entité (Client/Prospect)
        rbClients.addActionListener(e -> {
            titre.setText("Gestion des entités : Clients");
            updateVoirContratsEnabled();  // Activer "Voir Contrats" pour clients
        });
        rbProspects.addActionListener(e -> {
            titre.setText("Gestion des entités : Prospects");
            updateVoirContratsEnabled();  // Désactiver "Voir Contrats" pour prospects
        });
        updateVoirContratsEnabled();

        // Action Créer: ouvre formulaire vierge
        btnCreer.addActionListener(e -> onCreer());

        // Action Modifier: affiche sélection puis ouvre formulaire pré-rempli
        btnModifier.addActionListener(e -> {
            // Masquer les contrôles principaux
            actions.setVisible(false);
            radios.setVisible(false);
            labelSelection.setVisible(false);
            // Mettre à jour le titre
            titre.setText(rbClients.isSelected() ?
                    "Gestion des entités : Clients | Action : Modifier" :
                    "Gestion des entités : Prospects | Action : Modifier");
            prepareSelection("modifier");
        });

        // Action Supprimer: affiche sélection puis ouvre confirmation
        btnSupprimer.addActionListener(e -> {
            actions.setVisible(false);
            radios.setVisible(false);
            labelSelection.setVisible(false);
            titre.setText(rbClients.isSelected() ?
                    "Gestion des entités : Clients | Action : Supprimer" :
                    "Gestion des entités : Prospects | Action : Supprimer");
            prepareSelection("supprimer");
        });

        // Action Voir Contrats: uniquement pour clients
        btnVoirContrats.addActionListener(e -> {
            actions.setVisible(false);
            radios.setVisible(false);
            labelSelection.setVisible(false);
            titre.setText("Gestion des entités : Clients | Action : Voir Contrats");
            prepareSelection("voirContrats");
        });

        // Action Afficher: ouvre vue liste complète
        btnAfficher.addActionListener(e -> {
            if (isClientSelected()) ouvrirGestionClients();
            else ouvrirGestionProspects();
        });

        // Action Quitter: log et fermeture application
        btnQuitter.addActionListener(e -> {
            LOGGER.log(Level.INFO, "Application terminée (Quitter)");
            System.exit(0);
        });

        // Validation/Annulation de la sélection
        btnValider.addActionListener(e -> onValiderSelection());
        btnAnnuler.addActionListener(e -> {
            // Réafficher les contrôles principaux
            actions.setVisible(true);
            radios.setVisible(true);
            labelSelection.setVisible(true);
            cancelSelection();
        });

        setContentPane(mainPanel);
    }

    /**
     * Active/désactive le bouton "Voir Contrats" selon le type d'entité sélectionné.
     * Les contrats ne sont disponibles que pour les clients.
     */
    private void updateVoirContratsEnabled() {
        btnVoirContrats.setEnabled(rbClients.isSelected());
    }

    /**
     * Affiche ou masque le panel de sélection d'entité.
     *
     * @param visible true pour afficher, false pour masquer
     */
    private void setSelectPanelVisible(boolean visible) {
        selectPanel.setVisible(visible);
        selectPanel.revalidate();  // Recalculer layout
        selectPanel.repaint();     // Redessiner
    }

    /**
     * Vérifie si le type "Client" est sélectionné.
     *
     * @return true si clients sélectionné, false si prospects
     */
    private boolean isClientSelected() {
        return rbClients.isSelected();
    }

    /**
     * Gère l'action de création d'une nouvelle entité.
     * Ouvre le formulaire de création et ferme la vue actuelle.
     */
    private void onCreer() {
        boolean clients = isClientSelected();
        Integer id = null;  // null = mode création
        FormulaireView form = new FormulaireView(clientVM, prospectVM, contratVM, clients, id, "Créer", origin);
        form.setVisible(true);
        this.dispose();  // Fermer vue actuelle
    }

    /**
     * Prépare la liste de sélection pour une action donnée.
     * Charge les entités disponibles dans la ComboBox.
     *
     * @param action l'action à effectuer (modifier/supprimer/voirContrats)
     */
    private void prepareSelection(String action) {
        this.currentAction = action;
        comboSelect.removeAllItems();  // Vider la combo

        if (isClientSelected()) {
            // Charger les clients
            List<Client> clients = clientVM.getTousLesClients();
            if (clients.isEmpty()) {
                DisplayDialog.messageInfo("Info", "Aucun client disponible");
                this.currentAction = null;
                setSelectPanelVisible(false);
                return;
            }
            for (Client c : clients) {
                comboSelect.addItem(c);
            }
        } else {
            // Charger les prospects
            List<Prospect> prospects = prospectVM.getTousLesProspects();
            if (prospects.isEmpty()) {
                DisplayDialog.messageInfo("Info", "Aucun prospect disponible");
                this.currentAction = null;
                setSelectPanelVisible(false);
                return;
            }
            for (Prospect p : prospects) {
                comboSelect.addItem(p);
            }
        }

        // Renderer personnalisé pour afficher: "Raison Sociale (ID x)"
        comboSelect.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                if (value instanceof Client cli) {
                    setText(cli.getRaisonSociale() + " (ID " + cli.getId() + ")");
                } else if (value instanceof Prospect pro) {
                    setText(pro.getRaisonSociale() + " (ID " + pro.getId() + ")");
                }
                return c;
            }
        });
        setSelectPanelVisible(true);
    }

    /**
     * Annule l'action en cours et masque le panel de sélection.
     */
    private void cancelSelection() {
        titre.setText(rbClients.isSelected() ?
                "Gestion des entités : Clients" :
                "Gestion des entités : Prospects");
        this.currentAction = null;
        setSelectPanelVisible(false);
    }

    /**
     * Valide la sélection et exécute l'action correspondante.
     */
    private void onValiderSelection() {
        if (currentAction == null) {
            setSelectPanelVisible(false);
            return;
        }
        Object selected = comboSelect.getSelectedItem();
        if (selected == null) {
            DisplayDialog.messageInfo("Info", "Veuillez sélectionner un élément");
            return;
        }

        // Dispatcher vers le handler approprié selon l'action
        switch (currentAction) {
            case "modifier" -> handleModifier(selected);
            case "supprimer" -> handleSupprimer(selected);
            case "voirContrats" -> handleVoirContrats(selected);
            default -> {}
        }
        cancelSelection();
    }

    /**
     * Gère la modification de l'entité sélectionnée.
     *
     * @param selected l'entité à modifier (Client ou Prospect)
     */
    private void handleModifier(Object selected) {
        if (selected instanceof Client c) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, contratVM,
                    true, c.getId(), "Modifier", origin);
            form.setVisible(true);
            this.dispose();
        } else if (selected instanceof Prospect p) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, contratVM,
                    false, p.getId(),"Modifier", origin);
            form.setVisible(true);
            this.dispose();
        }
    }

    /**
     * Gère la suppression de l'entité sélectionnée.
     *
     * @param selected l'entité à supprimer (Client ou Prospect)
     */
    private void handleSupprimer(Object selected) {
        if (selected instanceof Client c) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, contratVM,
                    true, c.getId(), "Supprimer", origin);
            form.setVisible(true);
            this.dispose();
        } else if (selected instanceof Prospect p) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, contratVM,
                    false, p.getId(), "Supprimer", origin);
            form.setVisible(true);
            this.dispose();
        }
    }

    /**
     * Gère l'affichage des contrats du client sélectionné.
     *
     * @param selected le client dont on veut voir les contrats
     */
    private void handleVoirContrats(Object selected) {
        // Vérification type: contrats uniquement pour clients
        if (!(selected instanceof Client c)) {
            DisplayDialog.messageInfo(
                    "Info",
                    "La visualisation des contrats n'est disponible que pour les clients.");
            return;
        }
        ListeContratsView contratsView = new ListeContratsView(clientVM, prospectVM, contratVM, c, origin);
        contratsView.setVisible(true);
        this.dispose();
    }

    /**
     * Ouvre la vue de liste des clients.
     */
    private void ouvrirGestionClients() {
        ListeView listeClients = new ListeView(clientVM, prospectVM, contratVM, true);
        listeClients.setVisible(true);
        this.dispose();
    }

    /**
     * Ouvre la vue de liste des prospects.
     */
    private void ouvrirGestionProspects() {
        ListeView listeProspects = new ListeView(clientVM, prospectVM, contratVM, false);
        listeProspects.setVisible(true);
        this.dispose();
    }
}
