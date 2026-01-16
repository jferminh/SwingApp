# Configuration de la base de données

## 📋 Prérequis

- MySQL 8.0+ installé et démarré
- Base de données `ecf_dao` créée (voir script `create_database.sql`)
- Driver JDBC MySQL ajouté au projet

---

## ⚙️ Installation

### Étape 1 : Copier le fichier d'exemple


Ou dans Windows :
copy database.properties.example database.properties

### Étape 2 : Configurer vos paramètres
Ouvrir database.properties et modifier :
db.password=VOTRE_MOT_DE_PASSE_MYSQL

Paramètres disponibles :

| Paramètre   | Description             | Valeur par défaut                   |
| ----------- | ----------------------- | ----------------------------------- |
| db.url      | URL JDBC de connexion   | jdbc:mysql://localhost:3306/ecf_dao |
| db.username | Nom d'utilisateur MySQL | root                                |
| db.password | Mot de passe MySQL      | À configurer                        |
| db.driver   | Classe du driver JDBC   | com.mysql.cj.jdbc.Driver            |

🔒 Sécurité

⚠️ IMPORTANT : Le fichier database.properties contient votre mot de passe et ne doit jamais être commité sur Git.

✅ Vérification
Pour tester la connexion, exécuter :

main.com.julio.TestDatabaseConnection

Résultat attendu :

✅ Instance DatabaseConnection créée avec succès

✅ Connexion à la base de données active et valide

✅ Pattern Singleton respecté : même instance
