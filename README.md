# 📊 ECF - Application de Gestion Clients/Prospects

> Application desktop Java Swing pour gérer Clients, Prospects et Contrats avec persistance MySQL, architecture DAO et pattern Singleton.

[![Java](https://img.shields.io/badge/Java-25+-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![JUnit](https://img.shields.io/badge/JUnit-5.8.1-green.svg)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-AFPA_ECF-red.svg)](LICENSE)

---

## 📋 Aperçu

Application desktop Java Swing pour gérer **Clients**, **Prospects** et **Contrats** avec persistance **MySQL**, architecture **DAO/Singleton**, validations métier strictes et logging structuré. Interface MVVM avec navigation fluide entre entités et opérations CRUD complètes.

---

## 🏗️ Architecture

### Pattern DAO/Singleton
- **Model** : Entités métier (`Client`, `Prospect`, `Contrat`, `Adresse`, `Societe`, `Interesse`)
- **DAO** : Accès données MySQL (`ClientDAO`, `ProspectDAO`, `ContratDAO`, `AdresseDAO`, `SocieteDAO`)
- **Database** : `DatabaseConnection` (Singleton thread-safe)
- **Service** : Logique métier (`ValidationService`, `UnicityService`, `LoggerService`)
- **ViewModel** : Présentation (`ClientViewModel`, `ProspectViewModel`, `ContratViewModel`)
- **View** : Interface Swing (`AccueilView`, `ListeView`, `FormulaireView`, `ListeContratsView`)
- **Util** : Utilitaires (`SQLExceptionAnalyzer`, `JdbcUtil`, `DisplayDialog`)

### Base de Données (5 tables)
- **adresse** 
- **societe** (raison_sociale UNIQUE)
- **client** 
- **prospect** 
- **contrat** (ON DELETE RESTRICT)

**Relations :**
- `societe.adresse_id` → `adresse.id_adresse`
- `client.id_societe` → `societe.id_societe` (UNIQUE)
- `prospect.id_societe` → `societe.id_societe` (UNIQUE)
- `contrat.client_id` → `client.id_client`

---

## ✨ Principales Caractéristiques

### Gestion Entités
- CRUD complet (Créer, Lire, Modifier, Supprimer) pour Clients, Prospects, Contrats
- Navigation inter-entités (Client → Contrats)

### Validations Métier
- **Email** : Format RFC 5322 (regex)
- **Téléphone** : 10 chiffres (France)
- **Code postal** : 5 chiffres
- **Chiffre affaires** : ≥ 200 €
- **Nombre employés** : ≥ 1

### Unicité Raison Sociale
- Vérification inter-tables (`client` + `prospect`)
- Sensible à la casse
- Exclusion ID lors de modifications

### Persistence MySQL
- Transactions ACID (rollback automatique sur erreur)
- PreparedStatements (sécurité SQL Injection)
- Connection pooling (Singleton)
- Gestion ressources JDBC (try-with-resources + `JdbcUtil`)

### Gestion Erreurs
- `DAOException` typée (9 codes d'erreur)
- `SQLExceptionAnalyzer` (analyse SQLState)
- Logging multi-niveaux (SEVERE/INFO/FINE)
- Messages utilisateur contextuels

---

### 🚀 Prérequis

- **Java** : 25 (avec JDBC)
- **MySQL** : 8.0+

### 🧪 Tests Unitaires
### Couverture
- ClientTest : 25+ tests (création, validation, setters, @ParameterizedTest)

- ProspectTest : 25+ tests (dates, Interesse enum, formatage)

- UnicityServiceTest : 20 tests (mocking DAO, détection doublons)

- ValidationServiceTest : Tests paramétrés (email, téléphone, code postal)

### Dépendances :

- JUnit 5.10.1 (junit-jupiter-api, junit-jupiter-params)

### 🔧 Configuration de la base de données
### 📋 Prérequis

- MySQL 8.0+ installé et démarré
- Base de données `ecf_dao` créée (voir script `ecf_dao_struct.sql`)
- Driver JDBC MySQL ajouté au projet

---

### ⚙️ Installation

### Étape 1 : Copier le fichier d'exemple


Ou dans Windows :
sur le dossier resources, copy database.properties.example vers database.properties. Ne pas oublier faire click droit sur le dossier resources  "Mark Directory As -> Resource Root"  

### Étape 2 : Configurer vos paramètres
Ouvrir database.properties et modifier :
db.password=VOTRE_MOT_DE_PASSE_MYSQL

Paramètres disponibles :

| Paramètre   | Description             | Valeur par défaut                   |
| ----------- | ----------------------- | ----------------------------------- |
| db.url      | URL JDBC de connexion   | jdbc:mysql://localhost:3306/ecf_dao?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC |
| db.username | Nom d'utilisateur MySQL | root                                |
| db.password | Mot de passe MySQL      | À configurer                        |
| db.driver   | Classe du driver JDBC   | com.mysql.cj.jdbc.Driver            |

🔒 Sécurité

⚠️ IMPORTANT : Le fichier database.properties contient votre mot de passe et ne doit jamais être commité sur Git.

### Gestion Exceptions
9 codes d'erreur DAOException.ErrorCode :

- CONNECTION_ERROR - Connexion BDD échouée

- CREATE_ERROR - Insertion échouée

- READ_ERROR - Lecture échouée

- UPDATE_ERROR - Modification échouée

- DELETE_ERROR - Suppression échouée

- FOREIGN_KEY_VIOLATION - Contrainte FK

- UNIQUE_CONSTRAINT_VIOLATION - Doublon raison sociale

- NOT_NULL_VIOLATION - Champ obligatoire manquant

- INVALID_PARAMETER - Paramètre invalide

### 📖 Utilisation
### Interface d'Accueil
- Sélection Clients ou Prospects (radio buttons)

- Actions : Créer, Modifier, Supprimer, Voir Contrats, Quitter

- Navigation fluide entre entités

### 🎓 Bonnes Pratiques Implémentées
- ✅ Architecture DAO - Séparation logique/données
- ✅ Singleton Thread-Safe - Une connexion MySQL partagée
- ✅ Transactions ACID - Rollback automatique sur erreur
- ✅ PreparedStatements - Protection SQL Injection
- ✅ Validations centralisées - ValidationService réutilisable
- ✅ Exceptions typées - DAOException avec codes d'erreur
- ✅ Logging structuré - 3 niveaux (SEVERE/WARNING/INFO)
- ✅ Tests paramétrés - @ParameterizedTest JUnit 5

### 📚 Documentation
- Javadoc : docs/api/index.html 

- MCD : docs/conception/MCD_ECF_DAO.loo (application Looping)

- Scripts SQL : docs/conception/bdd/

- Dictionnaire données : docs/conception/bdd/dictionnaire_donnees.md

## 🐛 Résolution Problèmes Courants
### Erreur connexion MySQL
DAOException: CONNECTION_ERROR - Communications link failure
### Solution :

- Vérifier MySQL démarré : sudo systemctl status mysql

- Tester connexion : mysql -u votre_user -p votre_password

- Vérifier URL/USER/PASSWORD dans database.properties

## 👨‍💻 Auteur & Licence
### Projet pédagogique ECF DAO – AFPA

- Auteur : Julio FERMIN

- Formation : Concepteur Développeur d'Applications (CDA)

- Date : Janvier 2026

- Version : 2.0
