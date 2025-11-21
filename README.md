
### Aperçu

Application desktop Java Swing pour gérer Clients, Prospects et Contrats, en architecture MVVM avec validations, unicité de raison sociale et logging. L’interface d’accueil permet la sélection Clients/Prospects, les actions CRUD et la visualisation des contrats depuis une même fenêtre.

### Fonctionnalités

- Gestion des entités Client, Prospect, Contrat (Créer, Modifier, Supprimer, Afficher).
- Accueil unifié : sélection Clients/Prospects via JRadioButton, barre d’actions (Créer, Modifier, Supprimer, Voir Contrats, Quitter) et panneau inline de sélection avec JComboBox + Valider/Annuler.
- Liste triée par raison sociale (Comparator null-safe, insensible à la casse), avec désambigüisation par ID.
- Préchargement des contrats de démonstration pour des clients spécifiés.
- Suppression en cascade : supprimer un client supprime d’abord ses contrats puis le client.
- Validations (regex pour téléphone, email, code postal) et règles métier (CA >= 200; nb employés > 0).
- Unicité inter-entités : raison sociale unique parmi Clients et Prospects, insensible à la casse, avec exclusion par ID lors d’une modification.
- Tests JUnit5 paramétrés et structurés en @Nested, couvrant validation, modèles et services d’unicité.

### Architecture

- Pattern MVVM : View (Swing), ViewModel (logique de présentation), Model (entités), Services (validation, unicité, logging), Repositories (données en mémoire).
- Packages recommandés :
    - model/ (Adresse, Societe, Client, Prospect, Contrat, Interesse)
    - service/ (ValidationService, UnicityService, LoggingService)
    - repository/ (ClientRepository, ProspectRepository, ContratRepository)
    - viewmodel/ (ClientViewModel, ProspectViewModel, ContratViewModel)
    - view/ (AccueilView, ListeView, FormulaireView, ListeContratsView)
    - util/ (DateUtils, RegexPatterns, DisplayDialog)

### AccueilView unifiée

- JRadioButtons pour choisir “Clients” ou “Prospects”.
- Barre d’actions : Créer, Modifier, Supprimer, Voir Contrats, Quitter ; Voir Contrats activé uniquement pour Clients.
- Panel inline qui s’affiche pour Modifier/Supprimer/Voir Contrats : JComboBox trié par raison sociale + boutons “Valider” et “Annuler”.
- Pas de boîtes de dialogue externes pour la sélection : tout se passe dans AccueilView.

### Repository Client avec Comparator

- Tri par défaut: raison sociale ascendante, insensible à la casse, null-safe.
- findAll() retourne une copie triée, idéale pour les vues et les JComboBox.

### Suppression en cascade des contrats

- delete(clientId) supprime d’abord tous les contrats du client via ContratRepository.delete(contratId), nettoie la liste interne des contrats dans Client, puis supprime le Client.
- Bonnes pratiques : validation d’entrée, itération sur copie et retour boolean.

### Préchargement de contrats

- Méthodes de semis :
    - Dans ClientRepository avec injection de ContratRepository et méthode privée precargarContratos.
    - Centralisée dans Main, après création des repositories, en appelant une fonction utilitaire.
    - Via ContratViewModel pour réutiliser la logique métier et le logging.

### Service d’unicité

- UnicityService vérifie l’existence d’une raison sociale dans Clients et Prospects, insensible à la casse, et exclut l’ID courant lors d’une modification.

### Logs de démarrage/fin

- “Application démarrée” après l’initialisation des repositories/services/viewmodels et avant l’affichage de la fenêtre principale.
- “Application terminée” dans:
    - Bouton Quitter.
    - WindowListener (windowClosing/windowClosed) de la fenêtre principale.

### Prérequis

- Java 17+ (ou version compatible avec votre environnement).
- JUnit 5 pour les tests (junit-jupiter-api, junit-jupiter-params, junit-jupiter-engine).

### Construction et exécution

- Compilation:
    - javac -d bin -sourcepath src src/Main.java
- Exécution :
    - java -cp bin Main
- Tests JUnit5 (console launcher):
    - java -cp bin:junit-platform-console-standalone.jar org.junit.platform.console.ConsoleLauncher --scan-classpath

### Tests unitaires

- ValidationServiceTest : tests paramétrés pour code postal, téléphone, email, null/empty.
- ClientTest : création, règles métier, gestion de contrats.
- ProspectTest : dates, enum Interesse, format jj/MM/aaaa.
- UnicityServiceTest : détection de doublons inter-entités, exclusion par ID, insensibilité à la casse.

### Bonnes pratiques appliquées

- MVVM : la View ne contient pas de logique métier.
- Validations centralisées (regex et règles métier).
- Unicité inter-entités avant création/modification.
- Repositories en mémoire isolant l’accès aux données, tri et copies défensives.
- Suppression en cascade atomique en mémoire.
- Tests paramétrés, @Nested, assertAll, messages explicites et cas limites.
- Logging structuré pour cycle de vie de l’application.

### Personnalisation rapide

- Activer/désactiver la précharge : appeler ou non la méthode de semis dans Main.
- Modifier l’ordre de tri par défaut: ajuster le Comparator BY_RAISON_SOCIALE.
- Étendre AccueilView : ajouter champ de filtrage au-dessus du JComboBox si grand volume d’entités.
- Remplacer les repositories en mémoire par une persistance sans impacter View/ViewModel.

### Licence et auteur

- Projet pédagogique ECF POO – AFPA. Auteur: Julio FERMIN. Étudiant Conception et Développement d’Applications.
