
# DICTIONNAIRE DE DONNÉES - Projet ECF DAO
# Gestion Clients/Prospects avec MySQL

## TABLE: adresse
| Nom           | Type         | Longueur | Contrainte      | Description                               |
|---------------|--------------|----------|-----------------|-------------------------------------------|
| id            | INT          | -        | PK, AUTO_INC    | Identifiant unique de l'adresse           |
| numero_rue    | VARCHAR      | 10       | NOT NULL        | Numéro de la rue (ex: "12", "5 bis")     |
| nom_rue       | VARCHAR      | 100      | NOT NULL        | Nom de la voie                            |
| code_postal   | CHAR         | 5        | NOT NULL        | Code postal français (5 chiffres)         |
| ville         | VARCHAR      | 50       | NOT NULL        | Nom de la ville                           |

## TABLE: societe
| Nom             | Type         | Longueur | Contrainte      | Description                               |
|-----------------|--------------|----------|-----------------|-------------------------------------------|
| id              | INT          | -        | PK, AUTO_INC    | Identifiant unique de la société          |
| raison_sociale  | VARCHAR      | 100      | NOT NULL        | Nom de la société                         |
| adresse_id      | INT          | -        | FK, NOT NULL    | Référence vers la table adresse           |
| telephone       | VARCHAR      | 15       | NOT NULL        | Numéro de téléphone validé                |
| email           | VARCHAR      | 100      | NOT NULL        | Adresse email validée                     |
| commentaires    | TEXT         | -        | NULL            | Notes additionnelles                      |
| type_societe    | ENUM         | -        | NOT NULL        | 'CLIENT' ou 'PROSPECT'                    |

## TABLE: client
| Nom               | Type         | Longueur | Contrainte      | Description                               |
|-------------------|--------------|----------|-----------------|-------------------------------------------|
| id                | INT          | -        | PK, FK          | Référence vers societe.id                 |
| chiffre_affaires  | BIGINT       | -        | NOT NULL, >=200 | Chiffre d'affaires en euros               |
| nb_employes       | INT          | -        | NOT NULL, >=1   | Nombre d'employés                         |

## TABLE: prospect
| Nom               | Type         | Longueur | Contrainte      | Description                               |
|-------------------|--------------|----------|-----------------|-------------------------------------------|
| id                | INT          | -        | PK, FK          | Référence vers societe.id                 |
| date_prospection  | DATE         | -        | NOT NULL        | Date de première prospection              |
| interesse         | BOOLEAN      | -        | NOT NULL        | Niveau d'intérêt (true=Oui, false=Non)    |

## TABLE: contrat
| Nom           | Type         | Longueur | Contrainte      | Description                               |
|---------------|--------------|----------|-----------------|-------------------------------------------|
| id            | INT          | -        | PK, AUTO_INC    | Identifiant unique du contrat             |
| client_id     | INT          | -        | FK, NOT NULL    | Référence vers client.id                  |
| nom_contrat   | VARCHAR      | 100      | NOT NULL        | Nom ou désignation du contrat             |
| montant       | DECIMAL      | 10,2     | NOT NULL, >0    | Montant financier en euros                |

## RELATIONS
- adresse (1) ---- (N) societe : Une adresse peut être liée à plusieurs sociétés
- societe (1) ---- (0,1) client : Héritage - Une société peut être un client
- societe (1) ---- (0,1) prospect : Héritage - Une société peut être un prospect
- client (1) ---- (N) contrat : Un client peut avoir plusieurs contrats
