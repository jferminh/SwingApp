package main.com.julio.util;

import main.com.julio.exception.DAOException;

import java.sql.SQLException;

/**
 * Classe utilitaire pour analyser et catégoriser les SQLException.
 * Fournit des méthodes statiques pour convertir les codes d'erreur SQL
 * en messages compréhensibles et en codes d'erreur métier.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 19/01/2026
 */
public final class SQLExceptionAnalyzer {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe contient uniquement des méthodes statiques.
     */
    private SQLExceptionAnalyzer() {
        throw new AssertionError("Cette classe utilitaire ne doit pas être instanciée");
    }

    /**
     * Analyse une SQLException pour fournir un message d'erreur détaillé et compréhensible.
     *
     * Cette méthode examine le SQLState de l'exception et retourne un message
     * adapté selon le type d'erreur rencontré.
     *
     * @param e l'exception SQL à analyser
     * @return un message d'erreur détaillé et compréhensible
     */
    public static String analyze(SQLException e) {
        if (e == null) {
            return "Erreur SQL inconnue (exception null)";
        }

        String sqlState = e.getSQLState();

        if (sqlState != null) {
            // Codes SQL standard (SQLSTATE)
            if (sqlState.startsWith("23")) {
                // Violation de contrainte d'intégrité
                if (sqlState.equals("23000")) {
                    return "Violation de contrainte d'intégrité (vérifiez les clés étrangères)";
                } else if (sqlState.equals("23505")) {
                    return "Violation de contrainte d'unicité (valeur déjà existante)";
                } else if (sqlState.equals("23502")) {
                    return "Violation de contrainte NOT NULL (valeur obligatoire manquante)";
                } else if (sqlState.equals("23503")) {
                    return "Violation de contrainte de clé étrangère (référence inexistante)";
                } else if (sqlState.equals("23514")) {
                    return "Violation de contrainte CHECK (valeur hors limites autorisées)";
                }
                return "Violation de contrainte d'intégrité (code: " + sqlState + ")";

            } else if (sqlState.startsWith("42")) {
                // Erreur de syntaxe ou objet non trouvé
                if (sqlState.equals("42000")) {
                    return "Erreur de syntaxe SQL";
                } else if (sqlState.equals("42S02")) {
                    return "Table ou vue inexistante";
                } else if (sqlState.equals("42S22")) {
                    return "Colonne inexistante";
                }
                return "Erreur de syntaxe SQL ou objet non trouvé (code: " + sqlState + ")";

            } else if (sqlState.startsWith("08")) {
                // Problème de connexion
                if (sqlState.equals("08001")) {
                    return "Impossible d'établir la connexion à la base de données";
                } else if (sqlState.equals("08003")) {
                    return "Connexion inexistante (déjà fermée)";
                } else if (sqlState.equals("08006")) {
                    return "Échec de la connexion (connexion perdue)";
                }
                return "Problème de connexion à la base de données (code: " + sqlState + ")";

            } else if (sqlState.startsWith("40")) {
                // Problème de transaction
                if (sqlState.equals("40001")) {
                    return "Échec de la transaction (deadlock détecté)";
                }
                return "Problème de transaction (code: " + sqlState + ")";

            } else if (sqlState.startsWith("22")) {
                // Erreur de données
                if (sqlState.equals("22001")) {
                    return "Données trop longues pour la colonne";
                } else if (sqlState.equals("22003")) {
                    return "Valeur numérique hors limites";
                } else if (sqlState.equals("22007")) {
                    return "Format de date/heure invalide";
                }
                return "Erreur de format ou de type de données (code: " + sqlState + ")";
            }
        }

        // Si pas de SQLState ou code non reconnu, retourner le message original
        String message = e.getMessage();
        return message != null ? message : "Erreur SQL sans message détaillé";
    }

    /**
     * Catégorise une SQLException en ErrorCode métier.
     *
     * Cette méthode examine le SQLState de l'exception et retourne le code
     * d'erreur métier le plus approprié pour faciliter le traitement côté application.
     *
     * @param e l'exception SQL à catégoriser
     * @return le code d'erreur métier correspondant
     */
    public static DAOException.ErrorCode categorize(SQLException e) {
        if (e == null) {
            return DAOException.ErrorCode.GENERAL_ERROR;
        }

        String sqlState = e.getSQLState();

        if (sqlState != null) {
            // Codes SQL standard
            if (sqlState.startsWith("23")) {
                // Contraintes d'intégrité
                if (sqlState.equals("23503") || sqlState.contains("foreign")) {
                    return DAOException.ErrorCode.FOREIGN_KEY_VIOLATION;
                } else if (sqlState.equals("23505") || sqlState.contains("unique")) {
                    return DAOException.ErrorCode.UNIQUE_CONSTRAINT_VIOLATION;
                } else if (sqlState.equals("23502") || sqlState.contains("null")) {
                    return DAOException.ErrorCode.NOT_NULL_VIOLATION;
                } else if (sqlState.equals("23514") || sqlState.contains("check")) {
                    return DAOException.ErrorCode.CHECK_CONSTRAINT_VIOLATION;
                }
                // Par défaut pour les erreurs 23xxx
                return DAOException.ErrorCode.CHECK_CONSTRAINT_VIOLATION;

            } else if (sqlState.startsWith("08")) {
                // Problèmes de connexion
                return DAOException.ErrorCode.CONNECTION_ERROR;

            } else if (sqlState.startsWith("40")) {
                // Problèmes de transaction
                return DAOException.ErrorCode.TRANSACTION_ERROR;
            }
        }

        // Par défaut
        return DAOException.ErrorCode.GENERAL_ERROR;
    }

    /**
     * Vérifie si une SQLException est due à une violation de clé étrangère.
     *
     * @param e l'exception SQL à vérifier
     * @return true si c'est une violation de clé étrangère
     */
    public static boolean isForeignKeyViolation(SQLException e) {
        if (e == null) {
            return false;
        }

        String sqlState = e.getSQLState();
        if (sqlState != null && sqlState.equals("23503")) {
            return true;
        }

        String message = e.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("foreign key") ||
                    lowerMessage.contains("constraint") ||
                    lowerMessage.contains("référence");
        }

        return false;
    }

    /**
     * Vérifie si une SQLException est due à une violation de contrainte d'unicité.
     *
     * @param e l'exception SQL à vérifier
     * @return true si c'est une violation d'unicité
     */
    public static boolean isUniqueConstraintViolation(SQLException e) {
        if (e == null) {
            return false;
        }

        String sqlState = e.getSQLState();
        if (sqlState != null && sqlState.equals("23505")) {
            return true;
        }

        String message = e.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("unique") ||
                    lowerMessage.contains("duplicate");
        }

        return false;
    }

    /**
     * Extrait le nom de la contrainte violée depuis le message d'erreur SQL.
     *
     * @param e l'exception SQL contenant la violation
     * @return le nom de la contrainte ou null si non trouvé
     */
    public static String extractConstraintName(SQLException e) {
        if (e == null || e.getMessage() == null) {
            return null;
        }

        String message = e.getMessage();

        // Patterns courants pour MySQL, PostgreSQL, H2
        String[] patterns = {
                "constraint `(.+?)`",      // MySQL
                "constraint \"(.+?)\"",    // PostgreSQL
                "constraint '(.+?)'",      // Variante
                "constraint \\[(.+?)\\]",  // H2
                "CONSTRAINT_(.+?)_",       // Variante H2
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(message);
            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }
}
