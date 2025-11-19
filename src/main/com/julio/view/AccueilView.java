package main.com.julio.view;

import main.com.julio.model.Client;
import main.com.julio.model.Prospect;
import main.com.julio.util.DisplayDialog;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

public class AccueilView extends JFrame {
    private ClientViewModel clientVM;
    private ProspectViewModel prospectVM;

    private JLabel titre;
    private JRadioButton rbClients;
    private JRadioButton rbProspects;

    private JButton btnVoirContrats;

    private JPanel selectPanel;
    private JComboBox<Object> comboSelect;

    private String currentAction = null;
    private final String origin = "accueil";

    public AccueilView(ClientViewModel clientVM, ProspectViewModel prospectVM) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;

        initComponents();
    }

    private void initComponents() {
        setTitle("Gestion Clients-Prospects - Accueil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        titre = new JLabel("Gestion des entités : Clients");
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titre, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        rbClients = new JRadioButton("Clients");
        rbProspects = new JRadioButton("Prospects");
        rbClients.setSelected(true);
        ButtonGroup group = new ButtonGroup();
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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnCreer = new JButton("Créer");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        btnVoirContrats = new JButton("Voir Contrats");
        JButton btnAfficher = new JButton("Afficher");
        JButton btnQuitter = new JButton("Quitter");

        Dimension btnSize = new Dimension(140, 36);
        for (JButton b : new JButton[]{btnCreer, btnModifier, btnSupprimer, btnVoirContrats, btnAfficher}) {
            b.setPreferredSize(btnSize);
            actions.add(b);
        }

        gbc.gridy = 2;
        centerPanel.add(actions, gbc);

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

        JPanel bottonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottonPanel.add(btnQuitter);
        mainPanel.add(bottonPanel, BorderLayout.SOUTH);

        setSelectPanelVisible(false);

        // Listeners
        rbClients.addActionListener(e -> {
            titre.setText("Gestion des entités : Clients");
            updateVoirContratsEnabled();
        });
        rbProspects.addActionListener(e -> {
            titre.setText("Gestion des entités : Prospects");
            updateVoirContratsEnabled();
        });
        updateVoirContratsEnabled();

        btnCreer.addActionListener(e -> onCreer());
        btnModifier.addActionListener(e -> {
            actions.setVisible(false);
            radios.setVisible(false);
            labelSelection.setVisible(false);
            titre.setText(rbClients.isSelected() ?
                    "Gestion des entités : Clients | Action : Modifier" :
                    "Gestion des entités : Prospects | Action : Modifier");
            prepareSelection("modifier");
        });
        btnSupprimer.addActionListener(e -> {
            actions.setVisible(false);
            radios.setVisible(false);
            labelSelection.setVisible(false);
            titre.setText(rbClients.isSelected() ?
                    "Gestion des entités : Clients | Action : Supprimer" :
                    "Gestion des entités : Prospects | Action : Supprimer");
            prepareSelection("supprimer");
        });
        btnVoirContrats.addActionListener(e -> {
            actions.setVisible(false);
            radios.setVisible(false);
            labelSelection.setVisible(false);
            titre.setText("Gestion des entités : Clients | Action : Voir Contrats");
            prepareSelection("voirContrats");
        });
        btnAfficher.addActionListener(e -> {
            if (isClientSelected()) ouvrirGestionClients();
            else ouvrirGestionProspects();
        });
        btnQuitter.addActionListener(e -> {
            LOGGER.log(Level.INFO, "Application terminée (Quitter)");
            System.exit(0);
        });

        btnValider.addActionListener(e -> onValiderSelection());
        btnAnnuler.addActionListener(e -> {
            actions.setVisible(true);
            radios.setVisible(true);
            labelSelection.setVisible(true);
            cancelSelection();
        });

        setContentPane(mainPanel);

    }

    private void updateVoirContratsEnabled() {
        btnVoirContrats.setEnabled(rbClients.isSelected());

    }

    private void setSelectPanelVisible(boolean visible) {
        selectPanel.setVisible(visible);
        selectPanel.revalidate();
        selectPanel.repaint();
    }

    private boolean isClientSelected() {
        return rbClients.isSelected();
    }

    private void onCreer() {
        boolean clients = isClientSelected();
        Integer id = null;
        FormulaireView form = new FormulaireView(clientVM, prospectVM, clients, id, "Créer", origin);
        form.setVisible(true);
        this.dispose();
    }

    private void prepareSelection(String action) {
        this.currentAction = action;
        comboSelect.removeAllItems();

        if (isClientSelected()) {
            List<Client> clients = clientVM.getTousLesClients();
            if (clients.isEmpty()) {
                DisplayDialog.messageInfo(
                        "Info",
                        "Aucun client disponible"
                );
                this.currentAction = null;
                setSelectPanelVisible(false);
                return;
            }
            for (Client c : clients) {
                comboSelect.addItem(c);
            }
        } else {
            List<Prospect> prospects = prospectVM.getTousLesProspects();
            if (prospects.isEmpty()) {
                DisplayDialog.messageInfo(
                        "Info",
                        "Aucun prospect disponible"
                );
                this.currentAction = null;
                setSelectPanelVisible(false);
                return;
            }
            for (Prospect p : prospects) {
                comboSelect.addItem(p);
            }

        }

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

    private void cancelSelection() {
        titre.setText(rbClients.isSelected() ?
                "Gestion des entités : Clients" :
                "Gestion des entités : Prospects");
        this.currentAction = null;
        setSelectPanelVisible(false);
    }

    private void onValiderSelection() {
        if (currentAction == null) {
            setSelectPanelVisible(false);
            return;
        }
        Object selected = comboSelect.getSelectedItem();
        if (selected == null) {
            DisplayDialog.messageInfo(
                    "Info",
                    "Veuillez sélectionner un élément"
            );

            return;
        }
        switch (currentAction) {
            case "modifier" -> handleModifier(selected);
            case "supprimer" -> handleSupprimer(selected);
            case "voirContrats" -> handleVoirContrats(selected);
            default -> {
            }
        }

        cancelSelection();
    }

    private void handleModifier(Object selected) {
        if (selected instanceof Client c) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, true, c.getId(), "Modifier", origin);
            form.setVisible(true);
            this.dispose();
        } else if (selected instanceof Prospect p) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, false, p.getId(), "Modifier", origin);
            form.setVisible(true);
            this.dispose();
        }
    }

    private void handleSupprimer(Object selected) {
        if (selected instanceof Client c) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, true, c.getId(), "Supprimer", origin);
            form.setVisible(true);
            this.dispose();
        } else if (selected instanceof Prospect p) {
            FormulaireView form = new FormulaireView(clientVM, prospectVM, false, p.getId(), "Supprimer", origin);
            form.setVisible(true);
            this.dispose();
        }
    }

    private void handleVoirContrats(Object selected) {
        if (!(selected instanceof Client c)) {
            DisplayDialog.messageInfo(
                    "Info",
                    "La visualisation des contrats n'est disponible que pour les clients."
            );
            return;
        }
        ListeContratsView contratsView = new ListeContratsView(clientVM, prospectVM, c, origin);
        contratsView.setVisible(true);
        this.dispose();
    }

    private void ouvrirGestionClients() {
        ListeView listeClients = new ListeView(clientVM, prospectVM, true);
        listeClients.setVisible(true);
        this.dispose();
    }

    private void ouvrirGestionProspects() {
        ListeView listeProspects = new ListeView(clientVM, prospectVM, false);
        listeProspects.setVisible(true);
        this.dispose();
    }


}
