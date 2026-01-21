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

/**
 * Vue d'affichage des contrats d'un client spécifique.
 *
 * @author Julio FERMIN
 * @version 1.1
 * @since 21/01/2026
 */
public class ListeContratsView extends JFrame {

    private final ClientViewModel clientVM;
    private final ProspectViewModel prospectVM;
    private final ContratViewModel contratVM;

    private final Client client;  // ✅ Objet client complet
    private final String origin;

    private JTable table;
    private DefaultTableModel tableModel;

    /**
     * Constructeur initialisant la vue des contrats.
     *
     * @param clientVM ViewModel des clients
     * @param prospectVM ViewModel des prospects
     * @param contratVM ViewModel des contrats
     * @param client le client dont on affiche les contrats
     * @param origin vue d'origine ("listeview", "formulaireview", "accueil")
     */
    public ListeContratsView(ClientViewModel clientVM,
                             ProspectViewModel prospectVM,
                             ContratViewModel contratVM,
                             Client client,
                             String origin) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;
        this.contratVM = contratVM;
        this.client = client;  // ✅ Stockage objet client
        this.origin = origin;

        initComponents();
        chargerDonnees();
    }

    /**
     * Initialise l'interface graphique.
     */
    private void initComponents() {
        setTitle("Contrats de " + client.getRaisonSociale());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // En-tête
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titre = new JLabel("Contrats de " + client.getRaisonSociale());
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titre);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnCreer = new JButton("Créer Contrat");
        btnCreer.setPreferredSize(new Dimension(140, 35));
        btnCreer.addActionListener(e -> creerContrat());
        buttonPanel.add(btnCreer);

        JButton btnModifier = new JButton("Modifier");
        btnModifier.setPreferredSize(new Dimension(140, 35));
        btnModifier.addActionListener(e -> modifierContrat());
        buttonPanel.add(btnModifier);

        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setPreferredSize(new Dimension(140, 35));
        btnSupprimer.addActionListener(e -> supprimerContrat());
        buttonPanel.add(btnSupprimer);

        JButton btnRetour = new JButton("Retour");
        btnRetour.setPreferredSize(new Dimension(140, 35));
        btnRetour.addActionListener(e -> retour());
        buttonPanel.add(btnRetour);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Charge les contrats du client dans la table.
     */
    private void chargerDonnees() {
        try {
            // ✅ CORRECTION : Passer l'objet client complet
            tableModel = contratVM.construireTableModel(client);
            table.setModel(tableModel);

        } catch (DAOException e) {
            String message = switch (e.getErrorCode()) {
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                case READ_ERROR ->
                        "Erreur lors de la lecture des contrats.";
                default ->
                        "Erreur lors du chargement des contrats : " + e.getMessage();
            };

            DisplayDialog.messageError("Erreur de Chargement", message);

            // Créer table vide en cas d'erreur
            creerTableVide();
            table.setModel(tableModel);
        }
    }

    /**
     * Crée une table vide en cas d'erreur de chargement.
     */
    private void creerTableVide() {
        String[] colonnes = {"ID", "Nom Contrat", "Montant (€)"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * Ouvre le formulaire de création de contrat.
     */
    private void creerContrat() {
        // TODO: Implémenter FormulaireContratView
        DisplayDialog.messageInfo("Info", "Fonctionnalité en cours de développement");
    }

    /**
     * Modifie le contrat sélectionné.
     */
    private void modifierContrat() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            DisplayDialog.messageWarning("Aucune sélection",
                    "Veuillez sélectionner un contrat à modifier");
            return;
        }

        int contratId = (int) table.getValueAt(selectedRow, 0);
        // TODO: Implémenter FormulaireContratView pour modification
        DisplayDialog.messageInfo("Info", "Modification contrat ID=" + contratId);
    }

    /**
     * Supprime le contrat sélectionné après confirmation.
     */
    private void supprimerContrat() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            DisplayDialog.messageWarning("Aucune sélection",
                    "Veuillez sélectionner un contrat à supprimer");
            return;
        }

        int contratId = (int) table.getValueAt(selectedRow, 0);
        String nomContrat = (String) table.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer le contrat :\n" + nomContrat + " ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = contratVM.supprimerContrat(contratId);

            if (success) {
                DisplayDialog.messageInfo("Succès", "Contrat supprimé avec succès");
                chargerDonnees();  // Rafraîchir la table
            } else {
                DisplayDialog.messageWarning("Attention", "Le contrat n'existe plus");
            }

        } catch (DAOException e) {
            String message = switch (e.getErrorCode()) {
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                case ENTITY_NOT_FOUND ->
                        "Le contrat n'existe plus dans la base de données.";
                default ->
                        "Erreur lors de la suppression : " + e.getMessage();
            };
            DisplayDialog.messageError("Erreur", message);
        }
    }

    /**
     * Retourne à la vue d'origine.
     */
    private void retour() {
        switch (origin) {
            case "accueil" -> {
                AccueilView accueil = new AccueilView(clientVM, prospectVM, contratVM);
                accueil.setVisible(true);
            }
            case "listeview" -> {
                ListeView liste = new ListeView(clientVM, prospectVM, contratVM, true);
                liste.setVisible(true);
            }
            case "formulaireview" -> {
                FormulaireView formulaire = new FormulaireView(
                        clientVM, prospectVM, contratVM,
                        true, client.getId(), "Modifier", "accueil"
                );
                formulaire.setVisible(true);
            }
        }
        this.dispose();
    }
}
