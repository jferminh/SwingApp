package main.com.julio.view;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.util.DisplayDialog;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Vue d'affichage et de gestion des contrats d'un client spécifique.
 * Permet les opérations CRUD sur les contrats associés à un client.
 *
 * @author Julio FERMIN
 * @version 1.2
 * @since 22/01/2026
 */
public class ListeContratsView extends JFrame {

    private final ClientViewModel clientVM;
    private final ProspectViewModel prospectVM;
    private final ContratViewModel contratVM;

    private final Client client;  // Client propriétaire des contrats
    private final String origin;  // Vue d'origine pour navigation retour

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
        this.client = client;
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

        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === EN-TÊTE ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titre = new JLabel("Contrats de " + client.getRaisonSociale());
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titre);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // === TABLE ===
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // === BOUTONS ===
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
            // ✅ Utiliser la méthode construireTableModel(Client)
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
     * Affiche un dialogue pour créer un nouveau contrat.
     * Valide les données et rafraîchit la table en cas de succès.
     */
    private void creerContrat() {
        JTextField txtNom = new JTextField(20);
        JTextField txtMontant = new JTextField(20);

        // Panel de saisie avec GridLayout
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nom du contrat :"));
        panel.add(txtNom);
        panel.add(new JLabel("Montant (€) :"));
        panel.add(txtMontant);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Créer un contrat", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;  // Annulation
        }

        try {
            String nom = txtNom.getText().trim();
            double montant = Double.parseDouble(txtMontant.getText().trim());

            // ✅ Appel ViewModel avec gestion exceptions
            contratVM.creerContrat(client.getId(), nom, montant);

            DisplayDialog.messageInfo("Succès", "Contrat créé avec succès!");
            chargerDonnees();  // Rafraîchir table

        } catch (NumberFormatException e) {
            // ✅ Erreur de format du montant
            DisplayDialog.messageWarning("Format Invalide",
                    "Le montant doit être un nombre valide.\nExemple : 1500.50");

        } catch (ValidationException e) {
            // ✅ Erreur de validation métier
            DisplayDialog.messageWarning("Validation", e.getMessage());

        } catch (DAOException e) {
            // ✅ Erreur DAO (connexion, FK, etc.)
            String message = switch (e.getErrorCode()) {
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                case FOREIGN_KEY_VIOLATION ->
                        "Le client n'existe plus dans la base de données.";
                default ->
                        "Erreur lors de la création : " + e.getMessage();
            };
            DisplayDialog.messageError("Erreur", message);
        }
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
        String nomActuel = (String) table.getValueAt(selectedRow, 1);
        String montantActuel = (String) table.getValueAt(selectedRow, 2);

        // Extraire le montant numérique (enlever " €" et espaces)
        String montantStr = montantActuel.replace(" €", "").replace(",", ".").trim();

        // Pré-remplir les champs
        JTextField txtNom = new JTextField(nomActuel, 20);
        JTextField txtMontant = new JTextField(montantStr, 20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nom du contrat :"));
        panel.add(txtNom);
        panel.add(new JLabel("Montant (€) :"));
        panel.add(txtMontant);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Modifier le contrat", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;  // Annulation
        }

        try {
            String nom = txtNom.getText().trim();
            double montant = Double.parseDouble(txtMontant.getText().trim());

            // ✅ Appel ViewModel
            boolean success = contratVM.modifierContrat(contratId, nom, montant);

            if (success) {
                DisplayDialog.messageInfo("Succès", "Contrat modifié avec succès!");
                chargerDonnees();  // Rafraîchir table
            } else {
                DisplayDialog.messageWarning("Attention", "Le contrat n'existe plus");
            }

        } catch (NumberFormatException e) {
            DisplayDialog.messageWarning("Format Invalide",
                    "Le montant doit être un nombre valide.\nExemple : 1500.50");

        } catch (ValidationException e) {
            DisplayDialog.messageWarning("Validation", e.getMessage());

        } catch (DAOException e) {
            String message = switch (e.getErrorCode()) {
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                case ENTITY_NOT_FOUND ->
                        "Le contrat n'existe plus dans la base de données.";
                default ->
                        "Erreur lors de la modification : " + e.getMessage();
            };
            DisplayDialog.messageError("Erreur", message);
        }
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
