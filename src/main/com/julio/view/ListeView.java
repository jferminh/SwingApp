package main.com.julio.view;

import main.com.julio.model.Client;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ListeView extends JFrame {
    private final ClientViewModel clientVM;
    private final ProspectViewModel prospectVM;
    private final boolean isClient;

    private JTable table;
    private DefaultTableModel tableModel;

    ListeView(ClientViewModel clientVM, ProspectViewModel prospectVM, boolean isClient) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;
        this.isClient = isClient;

        initComponents();
        chargerDonnees();
    }

    private void initComponents() {
        String type = isClient ? "Client" : "Prospect";
        setTitle("Gestion " + type);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // En-tête
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titre = new JLabel("Liste des " + type);
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titre);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        tableModel = isClient ? clientVM.construireTableModel() : prospectVM.construireTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons
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
        btnQuitter.addActionListener(e -> System.exit(0));
        buttonPanel.add(btnQuitter);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

    }

    private void chargerDonnees() {
        tableModel = isClient ? clientVM.construireTableModel() : prospectVM.construireTableModel();
        table.setModel(tableModel);
    }

    private void ouvrirFormulaire(Integer id, String action) {
        FormulaireView form = new FormulaireView(clientVM, prospectVM, isClient, id, action, "listeview");
        form.setVisible(true);
        this.dispose();
        form.addWindowListener(new  WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                chargerDonnees();
            }
        });
    }

    private void modifierSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez sélectionner une ligne",
                    "Aucune séléction", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int id = (int) table.getValueAt(selectedRow, 0);
        ouvrirFormulaire(id, "Modifier");
    }

    private void supprimerSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez sélectionner une ligne",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        int id = (int) table.getValueAt(selectedRow, 0);
        ouvrirFormulaire(id, "Supprimer");

    }

    private void voirContrats() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez sélectionner un client",
                    "Aucune client", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int clientId = (int) table.getValueAt(selectedRow, 0);
        Client client = clientVM.getClientById(clientId);

        ListeContratsView contratsView = new ListeContratsView(clientVM, prospectVM, client, "listeview");
        contratsView.setVisible(true);
        this.dispose();
    }

    private void retourAccueil() {
        AccueilView accueil = new AccueilView(clientVM, prospectVM);
        accueil.setVisible(true);
        this.dispose();
    }
}
