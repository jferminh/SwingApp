package main.com.julio.view;

import main.com.julio.exception.ValidationException;
import main.com.julio.model.Client;
import main.com.julio.model.Interesse;
import main.com.julio.model.Prospect;
import main.com.julio.util.DateUtils;
import main.com.julio.util.DisplayDialog;
import main.com.julio.viewmodel.ClientViewModel;
import main.com.julio.viewmodel.ProspectViewModel;

import javax.swing.*;
import javax.swing.text.html.ListView;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;

public class FormulaireView extends JFrame {
    private final ClientViewModel clientVM;
    private final ProspectViewModel prospectVM;
    private final boolean isClient;
    private final Integer entityId;
    private final String action;
    private final String origin;


    // Champs communs
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

    public FormulaireView(ClientViewModel clientVM, ProspectViewModel prospectVM,
                          boolean isClient, Integer entityId, String action, String origin) {
        this.clientVM = clientVM;
        this.prospectVM = prospectVM;
        this.isClient = isClient;
        this.entityId = entityId;
        this.action = action;
        this.origin = origin;

        initialiserInterface();

        if (entityId != null) {
            chargerDonnees();
        }
    }

   private void initialiserInterface() {
        String type = isClient ? "Client" : "Prospect";
        setTitle(action + " un " + type);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal avec scroll
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        // Titre
        JLabel titre = new JLabel(action + " un " + type);
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        mainPanel.add(titre, gbc);

        gbc.gridwidth = 1;

        // === CHAMPS COMMUNS ===

        // Idéntifiant

        ajouterChamp(mainPanel, gbc, row++, "ID :",
                txtId = new JTextField(30));

        // Raison sociale
        ajouterChamp(mainPanel, gbc, row++, "Raison Sociale *:",
                txtRaisonSociale = new JTextField(30));

        // Numéro de rue
        ajouterChamp(mainPanel, gbc, row++, "Numéro de rue *:",
                txtNumeroRue = new JTextField(30));

        // Nom de rue
        ajouterChamp(mainPanel, gbc, row++, "Nom de rue *:",
                txtNomRue = new JTextField(30));

        // Code postal
        ajouterChamp(mainPanel, gbc, row++, "Code postal *:",
                txtCodePostal = new JTextField(30));

        // Ville
        ajouterChamp(mainPanel, gbc, row++, "Ville *:",
                txtVille = new JTextField(30));

        // Téléphone
        ajouterChamp(mainPanel, gbc, row++, "Téléphone *:",
                txtTelephone = new JTextField(30));

        // Email
        ajouterChamp(mainPanel, gbc, row++, "Email *:",
                txtEmail = new JTextField(30));

        // Commentaires
        gbc.gridx = 0;
        gbc.gridy = row++;
        mainPanel.add(new JLabel("Commentaires :"), gbc);

        txtCommentaires = new JTextArea(3, 30);
        txtCommentaires.setLineWrap(true);
        txtCommentaires.setWrapStyleWord(true);
        JScrollPane scrollCommentaires = new JScrollPane(txtCommentaires);
        gbc.gridx = 1;
        mainPanel.add(scrollCommentaires, gbc);

        txtId.setEnabled(false);

        // === CHAMPS SPÉCIFIQUES ===

        if (isClient) {
            // Chiffre d'affaires
            ajouterChamp(mainPanel, gbc, row++, "Chiffre d'affaires (€) *:",
                    txtChiffreAffaires = new JTextField(30));

            // Nombre d'employés
            ajouterChamp(mainPanel, gbc, row++, "Nombre d'employés *:",
                    txtNbEmployes = new JTextField(30));
        } else {
            // Date de prospection
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

            // Intéressé
            cmbInteresse = new JComboBox<>(Interesse.values());
            ajouterChamp(mainPanel, gbc, row++, "Intéressé *:", cmbInteresse);
        }

        // === BOUTONS ===

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnSauvegarder = new JButton();

        if (action.equals("Supprimer")) {
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
            btnSauvegarder.setText("Supprimer");
            btnSauvegarder.setPreferredSize(new Dimension(100, 28));
            btnSauvegarder.addActionListener(e -> supprimer());

        } else {
            btnSauvegarder.setText("Sauvegarder");
            btnSauvegarder.setPreferredSize(new Dimension(100, 28));
            btnSauvegarder.addActionListener(e -> {
                try {
                    sauvegarder();
                } catch (ValidationException ve) {
                    DisplayDialog.messageError("Erreur d'entrée", ve.getMessage());

                } catch (NumberFormatException nfe) {
                    DisplayDialog.messageError(
                            "Erreur d'entrée",
                            "Erreur de format numérique. Vérifiez vos saisies."
                    );

                } catch (DateTimeException dte) {
                    DisplayDialog.messageError(
                            "Erreur d'entrée",
                            "La date n'a pas le format jj/MM/aaaa"
                    );
                } catch (Exception ex) {
                    DisplayDialog.messageError("Erreur", ex.getMessage());

                }
            });
        }
        buttonPanel.add(btnSauvegarder);
        if (isClient && action.equals("Modifier")) {
            JButton btnVoirContrats = new JButton("Voir Contrats");
            btnVoirContrats.setPreferredSize(new Dimension(110, 28));
            btnVoirContrats.addActionListener(e -> voirContrats());
            buttonPanel.add(btnVoirContrats);
        }
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setPreferredSize(new Dimension(100, 28));
        btnAnnuler.addActionListener(e -> retour());
        buttonPanel.add(btnAnnuler);

        JButton btnQuitter = new JButton("Quitter");
        btnQuitter.setPreferredSize(new Dimension(100, 28));
        btnQuitter.addActionListener(e -> System.exit(0));
        buttonPanel.add(btnQuitter);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        // Scroll pane pour le formulaire complet
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);
    }

    private void voirContrats() {
        Client client = clientVM.getClientById(entityId);
        ListeContratsView contratsView = new ListeContratsView(clientVM, prospectVM, client, "formulaireview");
        contratsView.setVisible(true);
        this.dispose();
    }


    private void ajouterChamp(JPanel panel, GridBagConstraints gbc, int row,
                              String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    private void chargerDonnees() {
        if (isClient) {
            Client client = clientVM.getClientById(entityId);
            if (client != null) {
                txtId.setText(String.valueOf(client.getId()));
                txtRaisonSociale.setText(client.getRaisonSociale());
                txtNumeroRue.setText(client.getAdresse().getNumeroRue());
                txtNomRue.setText(client.getAdresse().getNomRue());
                txtCodePostal.setText(client.getAdresse().getCodePostal());
                txtVille.setText(client.getAdresse().getVille());
                txtTelephone.setText(client.getTelephone());
                txtEmail.setText(client.getEmail());
                txtCommentaires.setText(client.getCommentaires());
                txtChiffreAffaires.setText(String.valueOf(client.getChiffreAffaires()));
                txtNbEmployes.setText(String.valueOf(client.getNbEmployes()));
            }
        } else {
            Prospect prospect = prospectVM.getProspectById(entityId);
            if (prospect != null) {
                txtId.setText(String.valueOf(prospect.getId()));
                txtRaisonSociale.setText(prospect.getRaisonSociale());
                txtNumeroRue.setText(prospect.getAdresse().getNumeroRue());
                txtNomRue.setText(prospect.getAdresse().getNomRue());
                txtCodePostal.setText(prospect.getAdresse().getCodePostal());
                txtVille.setText(prospect.getAdresse().getVille());
                txtTelephone.setText(prospect.getTelephone());
                txtEmail.setText(prospect.getEmail());
                txtCommentaires.setText(prospect.getCommentaires());
                txtDateProspection.setText(prospect.getDateProspectionFormatee());
                cmbInteresse.setSelectedItem(prospect.getInteresse());
            }
        }
    }

    private void supprimer() {
        String raisonSociale = txtRaisonSociale.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer : " + raisonSociale + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = isClient ? clientVM.supprimerClient(entityId) : prospectVM.supprimerProspect(entityId);
            if (success) {
                DisplayDialog.messageInfo(
                        "Succèss",
                        "Suppression réussie"
                );
                chargerDonnees();
            } else {
                DisplayDialog.messageError(
                        "Erreur",
                        "Erreur lors de la suppression"
                );

            }
        }
        retour();
    }

    private void sauvegarder() throws ValidationException {
        String raisonSociale = txtRaisonSociale.getText().trim();
        String numeroRue = txtNumeroRue.getText().trim();
        String nomRue = txtNomRue.getText().trim();
        String codePostal = txtCodePostal.getText().trim();
        String ville = txtVille.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String email = txtEmail.getText().trim();
        String commentaires = txtCommentaires.getText().trim();

        if (isClient) {
            // Valider et parser les champs spécifiques Client
            long chiffreAffaires = Long.parseLong(txtChiffreAffaires.getText().trim());
            int nbEmployes = Integer.parseInt(txtNbEmployes.getText().trim());

            if (entityId == null) {
                // Création
                clientVM.creerClient(raisonSociale, numeroRue, nomRue, codePostal,
                        ville, telephone, email, commentaires,
                        chiffreAffaires, nbEmployes);
                DisplayDialog.messageInfo(
                        "Succès",
                        "Client créé avec succès"
                );
            } else {
                // Modification
                clientVM.modifierClient(entityId, raisonSociale, numeroRue, nomRue,
                        codePostal, ville, telephone, email, commentaires,
                        chiffreAffaires, nbEmployes);
                DisplayDialog.messageInfo(
                        "Succès",
                        "Client modifié avec succès"
                );
            }
        } else {
            // Valider et parser les champs spécifiques Prospect
            LocalDate dateProspection = DateUtils.parseDate(txtDateProspection.getText().trim());
            Interesse interesse = (Interesse) cmbInteresse.getSelectedItem();

            if (entityId == null) {
                // Création
                prospectVM.creerProspect(raisonSociale, numeroRue, nomRue, codePostal,
                        ville, telephone, email, commentaires,
                        dateProspection, interesse);
                DisplayDialog.messageInfo(
                        "Succès",
                        "Prospect créé avec succès!"
                );

            } else {
                // Modification
                prospectVM.modifierProspect(entityId, raisonSociale, numeroRue, nomRue,
                        codePostal, ville, telephone, email, commentaires,
                        dateProspection, interesse);
                DisplayDialog.messageInfo(
                        "Succès",
                        "Prospect modifié avec succès!"
                );
            }
        }

        retour();

    }
    private void retour(){
        if (origin.equals("accueil")) {
            AccueilView accueilView = new AccueilView(clientVM, prospectVM);
            accueilView.setVisible(true);
            this.dispose();
        } else {
            ListeView listeView = new ListeView(clientVM, prospectVM, isClient);
            listeView.setVisible(true);
            this.dispose();
        }
    }
}

