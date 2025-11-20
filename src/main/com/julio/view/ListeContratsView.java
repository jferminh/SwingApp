package main.com.julio.view;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.util.DisplayDialog;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * Vue d'affichage et de gestion des contrats d'un client spécifique.
 * Permet les opérations CRUD sur les contrats associés à un client.
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 */
public class ListeContratsView extends JFrame {

    // ViewModels - Pattern MVVM
    private final ClientViewModel clientVM;
    private final ContratViewModel contratVM;
    private final ProspectViewModel prospectVM;

    // Données contextuelles
    private final Client client;  // Client dont on affiche les contrats
    private final String origin;  // Vue d'origine pour navigation retour

    // Composants UI
    private JTable table;
    private DefaultTableModel tableModelContrats;

    /**
     * Constructeur initialisant la vue avec le client et ses contrats.
     *
     * @param clientVM ViewModel des clients
     * @param prospectVM ViewModel des prospects
     * @param contratVM ViewModel des contrats
     * @param client client dont on affiche les contrats
     * @param origin identifiant de la vue d'origine ("accueil", "listeview", etc.)
     */
    public ListeContratsView(ClientViewModel clientVM, ProspectViewModel prospectVM,
                             ContratViewModel contratVM, Client client, String origin) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;
        this.contratVM = contratVM;
        this.client = client;
        this.origin = origin;

        initialiserInterface();
        chargerDonnees();
    }

    /**
     * Initialise l'interface graphique avec table et boutons d'action.
     */
    private void initialiserInterface() {
        setTitle("Contrats de " + client.getRaisonSociale());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // En-tête: info client (raison sociale, ID, adresse)
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel titre = new JLabel("Contrats de " + client.getRaisonSociale());
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titre);

        JLabel infos = new JLabel("Client ID: " + client.getId() + " | " +
                client.getAdresse().toString());
        infos.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(infos);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table des contrats avec modèle chargé depuis ViewModel
        tableModelContrats = contratVM.construireTableModel(client.getId());
        table = new JTable(tableModelContrats);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // Une seule ligne à la fois
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons: CRUD + Navigation
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnCreer = new JButton("Créer Contrat");
        btnCreer.setPreferredSize(new Dimension(120, 35));
        btnCreer.addActionListener(e -> creerContrat());
        buttonPanel.add(btnCreer);

        JButton btnModifier = new JButton("Modifier");
        btnModifier.setPreferredSize(new Dimension(120, 35));
        btnModifier.addActionListener(e -> modifierContrat());
        buttonPanel.add(btnModifier);

        JButton btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setPreferredSize(new Dimension(120, 35));
        btnSupprimer.addActionListener(e -> supprimerContrat());
        buttonPanel.add(btnSupprimer);

        JButton btnFermer = new JButton("Retourner");
        btnFermer.setPreferredSize(new Dimension(120, 35));
        btnFermer.addActionListener(e -> retour());
        buttonPanel.add(btnFermer);

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
     * Recharge les données de la table depuis le ViewModel.
     */
    private void chargerDonnees() {
        tableModelContrats = contratVM.construireTableModel(client.getId());
        table.setModel(tableModelContrats);
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
                "Créer un contrat", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nom = txtNom.getText().trim();
                double montant = Double.parseDouble(txtMontant.getText().trim());

                contratVM.creerContrat(client.getId(), nom, montant);
                JOptionPane.showMessageDialog(this, "Contrat créé avec succès!",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerDonnees();  // Rafraîchir table

            } catch (ValidationException ve) {
                DisplayDialog.messageError("Erreur d'entrée", ve.getMessage());
            } catch (NumberFormatException nfe) {
                DisplayDialog.messageError("Erreur d'entrée",
                        "Erreur de format numérique. Vérifiez vos saisies.");
            } catch (Exception e) {
                DisplayDialog.messageError("Erreur", e.getMessage());
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Affiche un dialogue pour modifier le contrat sélectionné.
     * Pré-remplit les champs avec les valeurs actuelles.
     */
    private void modifierContrat() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un contrat",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Récupération des données de la ligne sélectionnée
        int contratId = (int) table.getValueAt(selectedRow, 0);
        String nomActuel = (String) table.getValueAt(selectedRow, 1);
        String montantActuel = (String) table.getValueAt(selectedRow, 2);

        // Pré-remplissage des champs
        JTextField txtNom = new JTextField(nomActuel, 20);
        JTextField txtMontant = new JTextField(montantActuel, 20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nom du contrat :"));
        panel.add(txtNom);
        panel.add(new JLabel("Montant (€) :"));
        panel.add(txtMontant);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Modifier le contrat", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nom = txtNom.getText().trim();
                // Remplacer virgule par point pour parsing décimal
                double montant = Double.parseDouble(
                        txtMontant.getText().trim().replace(",", ".")
                );

                contratVM.modifierContrat(contratId, nom, montant);
                DisplayDialog.messageInfo("Succès", "Contrat modifié avec succès!");
                chargerDonnees();

            } catch (ValidationException ve) {
                DisplayDialog.messageError("Erreur d'entrée", ve.getMessage());
            } catch (NumberFormatException nfe) {
                DisplayDialog.messageError("Erreur d'entrée",
                        "Veuillez saisir des chiffres.");
            } catch (Exception e) {
                DisplayDialog.messageError("Erreur", e.getMessage());
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Supprime le contrat sélectionné après confirmation.
     */
    private void supprimerContrat() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un contrat",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int contratId = (int) table.getValueAt(selectedRow, 0);
        String nom = (String) table.getValueAt(selectedRow, 1);

        // Dialogue de confirmation
        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer le contrat : " + nom + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = contratVM.supprimerContrat(contratId);

            if (success) {
                DisplayDialog.messageInfo("Succès", "Contrat supprimé avec succès!");
                chargerDonnees();
            } else {
                DisplayDialog.messageError("Erreur", "Erreur lors de la suppression!");
            }
        }
    }

    /**
     * Retourne à la vue d'origine selon le paramètre origin.
     * Gère la navigation contextuelle (accueil, listeview, formulaire).
     */
    private void retour() {
        // Switch sur l'origine pour navigation contextuelle
        switch (origin) {
            case "accueil" -> {
                AccueilView accueilView = new AccueilView(clientVM, prospectVM, contratVM);
                accueilView.setVisible(true);
                this.dispose();
            }
            case "listeview" -> {
                ListeView listeView = new ListeView(clientVM, prospectVM, contratVM, true);
                listeView.setVisible(true);
                this.dispose();
            }
            default -> {
                // Retour au formulaire de modification du client
                FormulaireView formulaireView = new FormulaireView(clientVM, prospectVM, contratVM,
                        true, client.getId(), "Modifier", "accueil");
                formulaireView.setVisible(true);
                this.dispose();
            }
        }
    }
}
