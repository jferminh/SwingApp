package main.com.julio.view;

import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.util.DateUtils;
import main.com.julio.util.DisplayDialog;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ContratViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.logging.Level;

import static main.com.julio.service.LoggingService.LOGGER;

/**
 * Formulaire universel pour les opérations CRUD sur clients et prospects.
 * Adapte dynamiquement l'interface selon le type d'entité et l'action.
 *
 * @author Julio FERMIN
 * @version 1.1
 * @since 21/01/2026
 */
public class FormulaireView extends JFrame {

    // ViewModels - Pattern MVVM
    private final ClientViewModel clientVM;
    private final ProspectViewModel prospectVM;
    private final ContratViewModel contratVM;

    // Contexte du formulaire
    private final boolean isClient;  // true = Client, false = Prospect
    private final Integer entityId;  // null = création, non-null = modification/suppression
    private final String action;     // "Créer", "Modifier", "Supprimer"
    private final String origin;     // Vue d'origine pour navigation retour

    // Champs communs (Client + Prospect)
    private JTextField txtId;
    private JTextField txtRaisonSociale;
    private JTextField txtNumeroRue;
    private JTextField txtNomRue;
    private JTextField txtCodePostal;
    private JTextField txtVille;
    private JTextField txtTelephone;
    private JTextField txtEmail;
    private JTextArea txtCommentaires;

    // Champs spécifiques Client
    private JTextField txtChiffreAffaires;
    private JTextField txtNbEmployes;

    // Champs spécifiques Prospect
    private JTextField txtDateProspection;
    private JComboBox<Interesse> cmbInteresse;

    /**
     * Constructeur initialisant le formulaire selon le contexte.
     *
     * @param clientVM ViewModel des clients
     * @param prospectVM ViewModel des prospects
     * @param contratVM ViewModel des contrats
     * @param isClient true pour client, false pour prospect
     * @param entityId ID de l'entité (null pour création)
     * @param action action à effectuer ("Créer", "Modifier", "Supprimer")
     * @param origin vue d'origine ("accueil", "listeview")
     */
    public FormulaireView(ClientViewModel clientVM, ProspectViewModel prospectVM, ContratViewModel contratVM,
                          boolean isClient, Integer entityId, String action, String origin) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;
        this.contratVM = contratVM;
        this.isClient = isClient;
        this.entityId = entityId;
        this.action = action;
        this.origin = origin;

        initialiserInterface();

        // ✅ Charger données existantes si modification/suppression
        if (entityId != null) {
            chargerDonnees();
        }
    }

    /**
     * Initialise l'interface adaptée selon le type d'entité et l'action.
     * Construit dynamiquement les champs spécifiques.
     */
    private void initialiserInterface() {
        String type = isClient ? "Client" : "Prospect";
        setTitle(action + " un " + type);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal avec GridBagLayout pour positionnement flexible
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        // Titre dynamique
        JLabel titre = new JLabel(action + " un " + type);
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        mainPanel.add(titre, gbc);

        gbc.gridwidth = 1;

        // === CHAMPS COMMUNS (Client et Prospect) ===

        ajouterChamp(mainPanel, gbc, row++, "ID :",
                txtId = new JTextField(30));

        ajouterChamp(mainPanel, gbc, row++, "Raison Sociale *:",
                txtRaisonSociale = new JTextField(30));

        ajouterChamp(mainPanel, gbc, row++, "Numéro de rue *:",
                txtNumeroRue = new JTextField(30));

        ajouterChamp(mainPanel, gbc, row++, "Nom de rue *:",
                txtNomRue = new JTextField(30));

        ajouterChamp(mainPanel, gbc, row++, "Code postal *:",
                txtCodePostal = new JTextField(30));

        ajouterChamp(mainPanel, gbc, row++, "Ville *:",
                txtVille = new JTextField(30));

        ajouterChamp(mainPanel, gbc, row++, "Téléphone *:",
                txtTelephone = new JTextField(30));

        ajouterChamp(mainPanel, gbc, row++, "Email *:",
                txtEmail = new JTextField(30));

        // Commentaires avec JTextArea scrollable
        gbc.gridx = 0;
        gbc.gridy = row++;
        mainPanel.add(new JLabel("Commentaires :"), gbc);

        txtCommentaires = new JTextArea(3, 30);
        txtCommentaires.setLineWrap(true);
        txtCommentaires.setWrapStyleWord(true);
        JScrollPane scrollCommentaires = new JScrollPane(txtCommentaires);
        gbc.gridx = 1;
        mainPanel.add(scrollCommentaires, gbc);

        txtId.setEnabled(false);  // ID non éditable

        // === CHAMPS SPÉCIFIQUES selon type d'entité ===

        if (isClient) {
            // Champs Client uniquement
            ajouterChamp(mainPanel, gbc, row++, "Chiffre d'affaires (€) *:",
                    txtChiffreAffaires = new JTextField(30));

            ajouterChamp(mainPanel, gbc, row++, "Nombre d'employés *:",
                    txtNbEmployes = new JTextField(30));
        } else {
            // Champs Prospect uniquement
            // Date avec indication de format
            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            txtDateProspection = new JTextField(15);
            datePanel.add(txtDateProspection);
            JLabel lblFormatDate = new JLabel("(jj/MM/aaaa)");
            lblFormatDate.setFont(new Font("Arial", Font.ITALIC, 11));
            datePanel.add(lblFormatDate);

            gbc.gridx = 0;
            gbc.gridy = row++;
            mainPanel.add(new JLabel("Date de prospection *:"), gbc);
            gbc.gridx = 1;
            mainPanel.add(datePanel, gbc);

            // ComboBox Intéressé avec valeurs enum
            cmbInteresse = new JComboBox<>(Interesse.values());
            ajouterChamp(mainPanel, gbc, row++, "Intéressé *:", cmbInteresse);
        }

        // === BOUTONS selon action ===

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnSauvegarder = new JButton();

        if (action.equals("Supprimer")) {
            // Mode suppression: tous champs disabled
            desactiverTousLesChamps();

            btnSauvegarder.setText("Supprimer");
            btnSauvegarder.setPreferredSize(new Dimension(100, 28));
            btnSauvegarder.addActionListener(e -> supprimer());

        } else {
            // Mode création/modification
            btnSauvegarder.setText("Sauvegarder");
            btnSauvegarder.setPreferredSize(new Dimension(100, 28));
            btnSauvegarder.addActionListener(e -> sauvegarder());
        }
        buttonPanel.add(btnSauvegarder);

        // Bouton "Voir Contrats" uniquement pour modification de client
        if (isClient && action.equals("Modifier")) {
            JButton btnVoirContrats = new JButton("Voir Contrats");
            btnVoirContrats.setPreferredSize(new Dimension(110, 28));
            btnVoirContrats.addActionListener(e -> voirContrats());
            buttonPanel.add(btnVoirContrats);
        }

        JButton btnAnnuler = new JButton("Retourner");
        btnAnnuler.setPreferredSize(new Dimension(100, 28));
        btnAnnuler.addActionListener(e -> retour());
        buttonPanel.add(btnAnnuler);

        JButton btnQuitter = new JButton("Quitter");
        btnQuitter.setPreferredSize(new Dimension(100, 28));
        btnQuitter.addActionListener(e -> {
            LOGGER.log(Level.INFO, "Application terminée (Quitter)");
            System.exit(0);
        });
        buttonPanel.add(btnQuitter);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        // Scroll pane global pour gérer formulaires longs
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);
    }

    /**
     * Désactive tous les champs du formulaire (mode suppression).
     */
    private void desactiverTousLesChamps() {
        txtRaisonSociale.setEnabled(false);
        txtNumeroRue.setEnabled(false);
        txtNomRue.setEnabled(false);
        txtCodePostal.setEnabled(false);
        txtVille.setEnabled(false);
        txtTelephone.setEnabled(false);
        txtEmail.setEnabled(false);
        txtCommentaires.setEnabled(false);
        if (isClient) {
            txtChiffreAffaires.setEnabled(false);
            txtNbEmployes.setEnabled(false);
        } else {
            txtDateProspection.setEnabled(false);
            cmbInteresse.setEnabled(false);
        }
    }

    /**
     * Ouvre la vue des contrats du client en cours de modification.
     */
    private void voirContrats() {
        try {
            // ✅ Gestion exception pour récupération client
            Client client = clientVM.getClientById(entityId);

            if (client == null) {
                DisplayDialog.messageWarning("Client introuvable",
                        "Le client n'existe plus dans la base de données.");
                retour();
                return;
            }

            ListeContratsView contratsView = new ListeContratsView(
                    clientVM, prospectVM, contratVM, client, "formulaireview"
            );
            contratsView.setVisible(true);
            this.dispose();

        } catch (DAOException e) {
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
     * Méthode utilitaire pour ajouter un champ label + composant.
     *
     * @param panel panel parent
     * @param gbc contraintes GridBag
     * @param row numéro de ligne
     * @param label texte du label
     * @param component composant de saisie
     */
    private void ajouterChamp(JPanel panel, GridBagConstraints gbc, int row,
                              String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    /**
     * Charge les données de l'entité dans les champs du formulaire.
     * Appelé en mode modification/suppression.
     */
    private void chargerDonnees() {
        try {
            // ✅ Gestion exception pour chargement données
            if (isClient) {
                Client client = clientVM.getClientById(entityId);

                if (client == null) {
                    DisplayDialog.messageError("Client introuvable",
                            "Le client n'existe plus dans la base de données.");
                    retour();
                    return;
                }

                // Remplir champs communs
                txtId.setText(String.valueOf(client.getId()));
                txtRaisonSociale.setText(client.getRaisonSociale());
                txtNumeroRue.setText(client.getAdresse().getNumeroRue());
                txtNomRue.setText(client.getAdresse().getNomRue());
                txtCodePostal.setText(client.getAdresse().getCodePostal());
                txtVille.setText(client.getAdresse().getVille());
                txtTelephone.setText(client.getTelephone());
                txtEmail.setText(client.getEmail());
                txtCommentaires.setText(client.getCommentaires());
                // Remplir champs spécifiques Client
                txtChiffreAffaires.setText(String.valueOf(client.getChiffreAffaires()));
                txtNbEmployes.setText(String.valueOf(client.getNbEmployes()));

            } else {
                Prospect prospect = prospectVM.getProspectById(entityId);

                if (prospect == null) {
                    DisplayDialog.messageError("Prospect introuvable",
                            "Le prospect n'existe plus dans la base de données.");
                    retour();
                    return;
                }

                // Remplir champs communs
                txtId.setText(String.valueOf(prospect.getId()));
                txtRaisonSociale.setText(prospect.getRaisonSociale());
                txtNumeroRue.setText(prospect.getAdresse().getNumeroRue());
                txtNomRue.setText(prospect.getAdresse().getNomRue());
                txtCodePostal.setText(prospect.getAdresse().getCodePostal());
                txtVille.setText(prospect.getAdresse().getVille());
                txtTelephone.setText(prospect.getTelephone());
                txtEmail.setText(prospect.getEmail());
                txtCommentaires.setText(prospect.getCommentaires());
                // Remplir champs spécifiques Prospect
                txtDateProspection.setText(prospect.getDateProspectionFormatee());
                cmbInteresse.setSelectedItem(prospect.getInteresse());
            }

        } catch (DAOException e) {
            String type = isClient ? "client" : "prospect";
            String message = switch (e.getErrorCode()) {
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                case READ_ERROR ->
                        "Erreur lors de la lecture des données du " + type + ".";
                default ->
                        "Erreur lors du chargement du " + type + " : " + e.getMessage();
            };
            DisplayDialog.messageError("Erreur de Chargement", message);
            retour();
        }
    }

    /**
     * Supprime l'entité après confirmation utilisateur.
     */
    private void supprimer() {
        String raisonSociale = txtRaisonSociale.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer : " + raisonSociale + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;  // Annulation
        }

        try {
            // ✅ Gestion exception pour suppression
            boolean success = isClient ?
                    clientVM.supprimerClient(entityId) :
                    prospectVM.supprimerProspect(entityId);

            if (success) {
                DisplayDialog.messageInfo("Succès", "Suppression réussie");
                retour();
            } else {
                DisplayDialog.messageWarning("Attention",
                        "L'entité n'existe plus dans la base de données.");
                retour();
            }

        } catch (DAOException e) {
            String type = isClient ? "client" : "prospect";
            String message = switch (e.getErrorCode()) {
                case FOREIGN_KEY_VIOLATION ->
                        "Impossible de supprimer : ce " + type + " possède des contrats associés.\n" +
                                "Veuillez d'abord supprimer ses contrats.";
                case ENTITY_NOT_FOUND ->
                        "L'entité n'existe plus dans la base de données.";
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                default ->
                        "Erreur lors de la suppression : " + e.getMessage();
            };
            DisplayDialog.messageError("Erreur de Suppression", message);
        }
    }

    /**
     * Sauvegarde l'entité (création ou modification).
     * Valide et parse les données avant appel au ViewModel.
     */
    private void sauvegarder() {
        try {
            // Récupération champs communs
            String raisonSociale = txtRaisonSociale.getText().trim();
            String numeroRue = txtNumeroRue.getText().trim();
            String nomRue = txtNomRue.getText().trim();
            String codePostal = txtCodePostal.getText().trim();
            String ville = txtVille.getText().trim();
            String telephone = txtTelephone.getText().trim();
            String email = txtEmail.getText().trim();
            String commentaires = txtCommentaires.getText().trim();

            if (isClient) {
                // Parsing champs spécifiques Client
                long chiffreAffaires = Long.parseLong(txtChiffreAffaires.getText().trim());
                int nbEmployes = Integer.parseInt(txtNbEmployes.getText().trim());

                if (entityId == null) {
                    // Mode création
                    clientVM.creerClient(raisonSociale, numeroRue, nomRue, codePostal,
                            ville, telephone, email, commentaires,
                            chiffreAffaires, nbEmployes);
                    DisplayDialog.messageInfo("Succès", "Client créé avec succès");
                } else {
                    // Mode modification
                    boolean success = clientVM.modifierClient(entityId, raisonSociale, numeroRue, nomRue,
                            codePostal, ville, telephone, email, commentaires,
                            chiffreAffaires, nbEmployes);

                    if (success) {
                        DisplayDialog.messageInfo("Succès", "Client modifié avec succès");
                    } else {
                        DisplayDialog.messageWarning("Attention", "Aucune modification effectuée");
                    }
                }
            } else {
                // Parsing champs spécifiques Prospect
                LocalDate dateProspection = DateUtils.parseDate(txtDateProspection.getText().trim());
                Interesse interesse = (Interesse) cmbInteresse.getSelectedItem();

                if (entityId == null) {
                    // Mode création
                    prospectVM.creerProspect(raisonSociale, numeroRue, nomRue, codePostal,
                            ville, telephone, email, commentaires,
                            dateProspection, interesse);
                    DisplayDialog.messageInfo("Succès", "Prospect créé avec succès!");
                } else {
                    // Mode modification
                    boolean success = prospectVM.modifierProspect(entityId, raisonSociale, numeroRue, nomRue,
                            codePostal, ville, telephone, email, commentaires,
                            dateProspection, interesse);

                    if (success) {
                        DisplayDialog.messageInfo("Succès", "Prospect modifié avec succès!");
                    } else {
                        DisplayDialog.messageWarning("Attention", "Aucune modification effectuée");
                    }
                }
            }

            retour();

        } catch (ValidationException e) {
            // ✅ Erreur de validation métier
            DisplayDialog.messageWarning("Erreur de Validation", e.getMessage());

        } catch (NumberFormatException e) {
            // ✅ Erreur format numérique
            DisplayDialog.messageError("Erreur de Format",
                    "Erreur de format numérique.\n" +
                            "Vérifiez le chiffre d'affaires et le nombre d'employés.");

        } catch (DateTimeException e) {
            // ✅ Erreur format date
            DisplayDialog.messageError("Erreur de Date",
                    "La date doit être au format jj/MM/aaaa\n" +
                            "Exemple : 21/01/2026");

        } catch (DAOException e) {
            // ✅ Erreur DAO (connexion, contraintes, etc.)
            String operation = (entityId == null) ? "création" : "modification";
            String message = switch (e.getErrorCode()) {
                case UNIQUE_CONSTRAINT_VIOLATION ->
                        "Cette raison sociale existe déjà dans la base de données.";
                case FOREIGN_KEY_VIOLATION ->
                        "Erreur de référence dans la base de données.";
                case CONNECTION_ERROR ->
                        "Impossible de se connecter à la base de données.";
                case ENTITY_NOT_FOUND ->
                        "L'entité n'existe plus dans la base de données.";
                default ->
                        "Erreur lors de la " + operation + " : " + e.getMessage();
            };
            DisplayDialog.messageError("Erreur", message);

        } catch (Exception e) {
            // ✅ Erreur inattendue
            DisplayDialog.messageError("Erreur Inattendue",
                    "Une erreur inattendue s'est produite :\n" + e.getMessage());
        }
    }

    /**
     * Retourne à la vue d'origine selon le contexte.
     */
    private void retour() {
        if (origin.equals("accueil")) {
            AccueilView accueilView = new AccueilView(clientVM, prospectVM, contratVM);
            accueilView.setVisible(true);
            this.dispose();
        } else {
            ListeView listeView = new ListeView(clientVM, prospectVM, contratVM, isClient);
            listeView.setVisible(true);
            this.dispose();
        }
    }
}
