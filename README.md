
### Aperçu

Application desktop Java Swing pour gérer Clients, Prospects et Contrats, en architecture MVVM avec validations, unicité de raison sociale et logging. L’interface d’accueil permet la sélection Clients/Prospects, les actions CRUD et la visualisation des contrats depuis une même fenêtre.[1]

### Fonctionnalités

- Gestion des entités Client, Prospect, Contrat (Créer, Modifier, Supprimer, Afficher).[2]
- Accueil unifié: sélection Clients/Prospects via JRadioButton, barre d’actions (Créer, Modifier, Supprimer, Voir Contrats, Quitter) et panneau inline de sélection avec JComboBox + Valider/Annuler.[1]
- Liste triée par raison sociale (Comparator null-safe, insensible à la casse), avec désambigüisation par ID.[1]
- Préchargement des contrats de démonstration pour des clients spécifiés.[1]
- Suppression en cascade: supprimer un client supprime d’abord ses contrats puis le client.[1]
- Validations (regex pour téléphone, email, code postal) et règles métier (CA >= 200; nb employés > 0).[3]
- Unicité inter-entités: raison sociale unique parmi Clients et Prospects, insensible à la casse, avec exclusion par ID lors d’une modification.[3]
- Tests JUnit5 paramétrés et structurés en @Nested, couvrant validation, modèles et services d’unicité.[1]

### Architecture

- Pattern MVVM: View (Swing), ViewModel (logique de présentation), Model (entités), Services (validation, unicité, logging), Repositories (données en mémoire).[1]
- Packages recommandés:
    - model/ (Adresse, Societe, Client, Prospect, Contrat, Interesse)[3]
    - service/ (ValidationService, UnicityService, LoggingService)[1]
    - repository/ (ClientRepository, ProspectRepository, ContratRepository)[1]
    - viewmodel/ (ClientViewModel, ProspectViewModel, ContratViewModel)[1]
    - view/ (AccueilView, ListeView, FormulaireView, ListeContratsView)[1]
    - util/ (DateUtils, RegexPatterns)[1]

### AccueilView unifiée

- JRadioButtons pour choisir “Clients” ou “Prospects”.[1]
- Barre d’actions: Créer, Modifier, Supprimer, Voir Contrats, Quitter; Voir Contrats activé uniquement pour Clients.[1]
- Panel inline qui s’affiche pour Modifier/Supprimer/Voir Contrats: JComboBox trié par raison sociale + boutons “Valider” et “Annuler”.[1]
- Pas de boîtes de dialogue externes pour la sélection: tout se passe dans AccueilView.[1]

### Repository Client avec Comparator

- Tri par défaut: raison sociale ascendante, insensible à la casse, null-safe, avec fallback de stabilité via thenComparing ID.[1]
- Helpers d’ordonnancement: par ville, par chiffre d’affaires desc.[1]
- findAll() retourne une copie triée, idéale pour les vues et les JComboBox.[1]

### Suppression en cascade des contrats

- delete(clientId) supprime d’abord tous les contrats du client via ContratRepository.delete(contratId), nettoie la liste interne des contrats dans Client, puis supprime le Client.[1]
- Bonnes pratiques: validation d’entrée, vérification d’existence, itération sur copie, logs détaillés et retour boolean.[1]

### Préchargement de contrats

- Méthodes de semis:
    - Dans ClientRepository avec injection de ContratRepository et méthode privée precargarContratos.[1]
    - Centralisée dans Main, après création des repositories, en appelant une fonction utilitaire.[1]
    - Via ContratViewModel pour réutiliser la logique métier et le logging.[1]

### Service d’unicité

- UnicityService vérifie l’existence d’une raison sociale dans Clients et Prospects, insensible à la casse, et exclut l’ID courant lors d’une modification.[3]
- Test unitaire fourni couvrant: doublons côté prospects et clients, insensibilité à la casse, exclusion par ID, et cas unique.[1]

### Logs de démarrage/fin

- “Application démarrée” après l’initialisation des repositories/services/viewmodels et avant l’affichage de la fenêtre principale.[1]
- “Application terminée” dans:
    - Bouton Quitter.[1]
    - WindowListener (windowClosing/windowClosed) de la fenêtre principale.[1]
    - Shutdown hook pour garantir un log à la fermeture système.[1]

### Prérequis

- Java 17+ (ou version compatible avec votre environnement).[3]
- JUnit 5 pour les tests (junit-jupiter-api, junit-jupiter-params, junit-jupiter-engine).[1]

### Construction et exécution

- Compilation:
    - javac -d bin -sourcepath src src/Main.java[1]
- Exécution:
    - java -cp bin Main[1]
- Tests JUnit5 (console launcher):
    - java -cp bin:junit-platform-console-standalone.jar org.junit.platform.console.ConsoleLauncher --scan-classpath[1]

### Tests unitaires

- ValidationServiceTest: tests paramétrés pour code postal, téléphone, email, null/empty.[1]
- ClientTest: création, IDs auto, règles métier, gestion de contrats, toString/getType.[1]
- ProspectTest: dates, enum Interesse, format jj/MM/aaaa, toString/getType.[1]
- UnicityServiceTest: détection de doublons inter-entités, exclusion par ID, insensibilité à la casse.[1]

### Bonnes pratiques appliquées

- MVVM strict: la View ne contient pas de logique métier.[1]
- Validations centralisées (regex et règles métier).[3]
- Unicité inter-entités avant création/modification.[3]
- Repositories en mémoire isolant l’accès aux données, tri et copies défensives.[1]
- Suppression en cascade atomique en mémoire et traçable.[1]
- Tests paramétrés, @Nested, assertAll, messages explicites et cas limites.[1]
- Logging structuré pour opérations CRUD et cycle de vie de l’application.[1]

### Personnalisation rapide

- Activer/désactiver la précharge: appeler ou non la méthode de semis dans Main.[1]
- Modifier l’ordre de tri par défaut: ajuster le Comparator BY_RAISON_SOCIALE.[1]
- Étendre AccueilView: ajouter champ de filtrage au-dessus du JComboBox si grand volume d’entités.[1]
- Remplacer les repositories en mémoire par une persistance (JDBC/JPA) sans impacter View/ViewModel.[1]

### Licence et auteur

- Projet pédagogique ECF POO – AFPA. Auteur: Étudiant Conception et Développement d’Applications (adaptable).[3]


[1](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_35ed81a0-e030-4460-83c9-1ae0caaa04e3/528a9794-6199-4067-9a77-1e4a7b9231f7/analyse-la-source-adjointe-et-ASTb997VSTy6GZpQW6p6lQ.md)
[2](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_35ed81a0-e030-4460-83c9-1ae0caaa04e3/020c6fe0-cec1-4e2b-b617-493807bab068/cree-la-liste-de-fonctionnalit-l9p1wdnYQbOGc7XlgkcEww.md)
[3](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_35ed81a0-e030-4460-83c9-1ae0caaa04e3/1cdbc404-5aec-463c-a1a1-c87bbcc1aa66/ECF-Programmation-Orientee-Objet.pdf)