package main.com.julio;

import main.com.julio.dao.*;
import main.com.julio.exception.DAOException;
import main.com.julio.exception.ValidationException;
import main.com.julio.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Classe de test pour vérifier les méthodes CRUD de ClientDAO depuis l'application.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 */
public class TestDAOSwing {

    private static final Scanner scanner = new Scanner(System.in);
    private static ClientDAO clientDAO;
    private static AdresseDAO adresseDAO;
//    private static ContratDAO contratDAO;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║   Test CRUD ClientDAO - Application Swing         ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        try {
            // Initialiser les DAO
            clientDAO = new ClientDAO();
            adresseDAO = new AdresseDAO();
//            contratDAO = new ContratDAO();

            boolean continuer = true;
            while (continuer) {
                afficherMenu();
                int choix = lireChoix();

                switch (choix) {
                    case 1:
                        testFindAll();
                        break;
                    case 2:
                        testFindById();
                        break;
                    case 3:
                        testCreate();
                        break;
                    case 4:
                        testSave();
                        break;
                    case 5:
                        testDelete();
                        break;
                    case 6:
                        testFindContratsClient();
                        break;
                    case 7:
                        testComplet();
                        break;
                    case 0:
                        continuer = false;
                        System.out.println("\n👋 Au revoir !");
                        break;
                    default:
                        System.out.println("❌ Choix invalide. Veuillez réessayer.\n");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        } finally {
            scanner.close();
        }
    }

    private static void afficherMenu() {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│           MENU PRINCIPAL                │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ 1. findAll()    - Afficher tous         │");
        System.out.println("│ 2. findById()   - Rechercher par ID     │");
        System.out.println("│ 3. create()     - Créer un client       │");
        System.out.println("│ 4. save()       - Modifier un client    │");
        System.out.println("│ 5. delete()     - Supprimer un client   │");
        System.out.println("│ 6. Contrats     - Voir contrats client  │");
        System.out.println("│ 7. Test complet - Toutes les opérations │");
        System.out.println("│ 0. Quitter                              │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.print("Votre choix : ");
    }

    private static int lireChoix() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 1 : FIND ALL
    // ═══════════════════════════════════════════════════════════
    private static void testFindAll() throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  TEST 1 : findAll() - Afficher tous les clients   ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        try {
            List<Client> clients = clientDAO.findAll();

            if (clients.isEmpty()) {
                System.out.println("⚠️  Aucun client trouvé dans la base de données.\n");
            } else {
                System.out.println("✅ Nombre de clients : " + clients.size() + "\n");

                for (Client client : clients) {
                    afficherClient(client);
                    System.out.println("─────────────────────────────────────────────────────");
                }
            }

        } catch (DAOException e) {
            System.err.println("\n❌ ERREUR : " + e.getMessage());
            System.err.println("   Code d'erreur : " + e.getErrorCode());
            System.err.println("   Opération : " + e.getOperation());

            // Log détaillé (déjà fait par le DAO, mais on peut ajouter du contexte)
            if (e.getCause() != null) {
                System.err.println("   Cause : " + e.getCause().getMessage());
            }
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 2 : FIND BY ID
    // ═══════════════════════════════════════════════════════════
    private static void testFindById() throws SQLException, DAOException {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  TEST 2 : findById() - Rechercher par ID          ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        System.out.print("Entrez l'ID du client à rechercher : ");
        int id = lireChoix();

        Client client = clientDAO.findById(id);

        if (client == null) {
            System.out.println("❌ Aucun client trouvé avec l'ID " + id + "\n");
        } else {
            System.out.println("✅ Client trouvé !\n");
            afficherClient(client);
        }

        pause();
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 3 : CREATE
    // ═══════════════════════════════════════════════════════════
    private static void testCreate() throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  TEST 3 : create() - Créer un nouveau client      ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        try {
            // Créer une adresse
            System.out.println("📍 Création de l'adresse du client :");
            System.out.print("Numéro de rue : ");
            String numeroRue = scanner.nextLine();

            System.out.print("Nom de rue : ");
            String nomRue = scanner.nextLine();

            System.out.print("Code postal (5 chiffres) : ");
            String codePostal = scanner.nextLine();

            System.out.print("Ville : ");
            String ville = scanner.nextLine();

            Adresse adresse = new Adresse(numeroRue, nomRue, codePostal, ville);

            // Créer le client
            System.out.println("\n🏢 Informations du client :");
            System.out.print("Raison sociale : ");
            String raisonSociale = scanner.nextLine();

            System.out.print("Téléphone (format: 0123456789) : ");
            String telephone = scanner.nextLine();

            System.out.print("Email : ");
            String email = scanner.nextLine();

            System.out.print("Commentaires (optionnel) : ");
            String commentaires = scanner.nextLine();

            System.out.print("Chiffre d'affaires (>=200) : ");
            long chiffreAffaires = Long.parseLong(scanner.nextLine());

            System.out.print("Nombre d'employés (>=1) : ");
            int nbEmployes = Integer.parseInt(scanner.nextLine());

            Client client = new Client(
                    raisonSociale,
                    adresse,
                    telephone,
                    email,
                    commentaires.isEmpty() ? null : commentaires,
                    chiffreAffaires,
                    nbEmployes
            );

            // Insérer en base
            Client clientCree = clientDAO.create(client);

            System.out.println("\n✅ Client créé avec succès !");
            System.out.println("ID généré : " + clientCree.getId());
            afficherClient(clientCree);

        } catch (ValidationException e) {
            System.out.println("❌ Erreur de validation : " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur de saisie : format numérique invalide");
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        pause();
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 4 : SAVE (UPDATE)
    // ═══════════════════════════════════════════════════════════
    private static void testSave() throws SQLException, DAOException {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  TEST 4 : save() - Modifier un client             ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        System.out.print("Entrez l'ID du client à modifier : ");
        int id = lireChoix();

        Client client = clientDAO.findById(id);

        if (client == null) {
            System.out.println("❌ Aucun client trouvé avec l'ID " + id + "\n");
            pause();
            return;
        }

        System.out.println("\n📋 Client actuel :");
        afficherClient(client);

        try {
            System.out.println("\n🔧 Modification (appuyez sur Entrée pour conserver la valeur actuelle)");

            System.out.print("Nouvelle raison sociale [" + client.getRaisonSociale() + "] : ");
            String raisonSociale = scanner.nextLine();
            if (!raisonSociale.isEmpty()) {
                client.setRaisonSociale(raisonSociale);
            }

            System.out.print("Nouveau téléphone [" + client.getTelephone() + "] : ");
            String telephone = scanner.nextLine();
            if (!telephone.isEmpty()) {
                client.setTelephone(telephone);
            }

            System.out.print("Nouvel email [" + client.getEmail() + "] : ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) {
                client.setEmail(email);
            }

            System.out.print("Nouveau chiffre d'affaires [" + client.getChiffreAffaires() + "] : ");
            String ca = scanner.nextLine();
            if (!ca.isEmpty()) {
                client.setChiffreAffaires(Long.parseLong(ca));
            }

            System.out.print("Nouveau nombre d'employés [" + client.getNbEmployes() + "] : ");
            String nb = scanner.nextLine();
            if (!nb.isEmpty()) {
                client.setNbEmployes(Integer.parseInt(nb));
            }

            // Mettre à jour en base
            boolean success = clientDAO.save(client);

            if (success) {
                System.out.println("\n✅ Client mis à jour avec succès !");
                afficherClient(client);
            } else {
                System.out.println("\n❌ Échec de la mise à jour");
            }

        } catch (ValidationException e) {
            System.out.println("❌ Erreur de validation : " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("❌ Erreur de saisie : format numérique invalide");
        }

        pause();
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 5 : DELETE
    // ═══════════════════════════════════════════════════════════
    private static void testDelete() throws SQLException, DAOException {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  TEST 5 : delete() - Supprimer un client          ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        System.out.print("Entrez l'ID du client à supprimer : ");
        int id = lireChoix();

        Client client = clientDAO.findById(id);

        if (client == null) {
            System.out.println("❌ Aucun client trouvé avec l'ID " + id + "\n");
            pause();
            return;
        }

        System.out.println("\n📋 Client à supprimer :");
        afficherClient(client);

        // Vérifier les contrats
//        List<Contrat> contrats = contratDAO.findByIdClient(id);
//        if (!contrats.isEmpty()) {
//            System.out.println("\n⚠️  ATTENTION : Ce client possède " + contrats.size() + " contrat(s)");
//            System.out.println("La suppression échouera en raison de la contrainte ON DELETE RESTRICT.\n");
//        }

        System.out.print("\n⚠️  Êtes-vous sûr de vouloir supprimer ce client ? (oui/non) : ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("oui")) {
            boolean success = clientDAO.delete(id);

            if (success) {
                System.out.println("✅ Client supprimé avec succès !");
            } else {
                System.out.println("❌ Échec de la suppression");
            }
        } else {
            System.out.println("Suppression annulée.");
        }

        pause();
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 6 : FIND CONTRATS BY CLIENT
    // ═══════════════════════════════════════════════════════════
    private static void testFindContratsClient() throws SQLException, DAOException {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  TEST 6 : Afficher les contrats d'un client       ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        System.out.print("Entrez l'ID du client : ");
        int clientId = lireChoix();

        Client client = clientDAO.findById(clientId);

        if (client == null) {
            System.out.println("❌ Aucun client trouvé avec l'ID " + clientId + "\n");
            pause();
            return;
        }

        System.out.println("\n📋 Client : " + client.getRaisonSociale());

//        List<Contrat> contrats = contratDAO.findByIdClient(clientId);

//        if (contrats.isEmpty()) {
//            System.out.println("⚠️  Aucun contrat trouvé pour ce client.\n");
//        } else {
//            System.out.println("✅ Nombre de contrats : " + contrats.size() + "\n");
//
//            double totalMontant = 0;
//            for (Contrat contrat : contrats) {
//                System.out.println("  📄 Contrat ID: " + contrat.getId());
//                System.out.println("     Nom: " + contrat.getNomContrat());
//                System.out.println("     Montant: " + contrat.getMontant() + " €");
//                System.out.println();
//                totalMontant += contrat.getMontant();
//            }
//
//            System.out.println("💰 Montant total des contrats : " + totalMontant + " €");
//        }

        pause();
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 7 : TEST COMPLET (CREATE → READ → UPDATE → DELETE)
    // ═══════════════════════════════════════════════════════════
    private static void testComplet() throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  TEST 7 : Test CRUD complet automatique           ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        try {
            // 1. CREATE
            System.out.println("1️⃣  Création d'un client de test...");
            Adresse adresse = new Adresse("99", "Rue du Test", "75000", "TestVille");
            Client client = new Client(
                    "Société Test CRUD",
                    adresse,
                    "0199999999",
                    "test@crud.fr",
                    "Client de test automatique",
                    100000,
                    10
            );

            client = clientDAO.create(client);
            System.out.println("   ✅ Client créé avec l'ID : " + client.getId());

            // 2. READ
            System.out.println("\n2️⃣  Lecture du client créé...");
            Client clientLu = clientDAO.findById(client.getId());
            if (clientLu != null) {
                System.out.println("   ✅ Client trouvé : " + clientLu.getRaisonSociale());
            }

            // 3. UPDATE
            System.out.println("\n3️⃣  Modification du client...");
            clientLu.setChiffreAffaires(200000);
            clientLu.setNbEmployes(20);
            boolean updated = clientDAO.save(clientLu);
            if (updated) {
                System.out.println("   ✅ Client mis à jour (CA: 200000, Employés: 20)");
            }

            // 4. Vérification de la modification
            System.out.println("\n4️⃣  Vérification de la modification...");
            Client clientModifie = clientDAO.findById(client.getId());
            if (clientModifie.getChiffreAffaires() == 200000) {
                System.out.println("   ✅ Modification confirmée");
            }

            // 5. DELETE
            System.out.println("\n5️⃣  Suppression du client de test...");
            boolean deleted = clientDAO.delete(client.getId());
            if (deleted) {
                System.out.println("   ✅ Client supprimé");
            }

            // 6. Vérification de la suppression
            System.out.println("\n6️⃣  Vérification de la suppression...");
            Client clientSupprime = clientDAO.findById(client.getId());
            if (clientSupprime == null) {
                System.out.println("   ✅ Suppression confirmée");
            }

            System.out.println("\n🎉 Test CRUD complet réussi !\n");

        } catch (ValidationException e) {
            System.out.println("❌ Erreur de validation : " + e.getMessage());
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        pause();
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    private static void afficherClient(Client client) {
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│ ID: " + client.getId());
        System.out.println("│ Raison sociale: " + client.getRaisonSociale());
        System.out.println("│ Chiffre d'affaires: " + client.getChiffreAffaires() + " €");
        System.out.println("│ Nombre d'employés: " + client.getNbEmployes());
        System.out.println("│ Téléphone: " + client.getTelephone());
        System.out.println("│ Email: " + client.getEmail());
        System.out.println("│ Commentaires: " + (client.getCommentaires() != null ? client.getCommentaires() : "Aucun"));
        System.out.println("│ Adresse: " + client.getAdresse());
        System.out.println("└─────────────────────────────────────────────────────┘\n");
    }

    private static void pause() {
        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }
}
