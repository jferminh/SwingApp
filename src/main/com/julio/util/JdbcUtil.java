package main.com.julio.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitaires JDBC pour la gestion des ressources.
 * <p>
 * Ferme en sécurité ResultSet et Statement, sans jamais fermer la connexion
 * qui est gérée par le Singleton DatabaseConnection.
 * </p>
 */
public final class JdbcUtil {

    private static final Logger LOGGER = Logger.getLogger(JdbcUtil.class.getName());

    private JdbcUtil() {
        // Util class, pas d'instanciation
    }

    /**
     * Ferme les ressources JDBC de manière sécurisée.
     * <p><b>IMPORTANT :</b> la connexion n'est jamais fermée ici.</p>
     *
     * @param rs   le ResultSet à fermer (peut être null)
     * @param stmt le Statement/PreparedStatement à fermer (peut être null)
     * @param connection connexion ignorée (ne sera JAMAIS fermée)
     */
    public static void closeResources(ResultSet rs, Statement stmt, Connection connection) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture du ResultSet", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture du Statement", e);
            }
        }

        // ✅ CRUCIAL : Réactiver autoCommit pour les prochaines opérations
        if (connection != null) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.setAutoCommit(true);

                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur réactivation autoCommit", e);
            }
        }
        // NE PAS fermer la connexion : elle est gérée par DatabaseConnection (Singleton)
    }
}
