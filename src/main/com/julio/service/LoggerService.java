package main.com.julio.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.*;

/**
 * Utilitaire pour configurer et gérer les logs de l'application de manière centralisée.
 * <p>
 * Charge la configuration depuis logging.properties au démarrage.
 * Fournit un accès simplifié aux loggers par classe.
 * </p>
 *
 * @author Julio FERMIN
 * @version 3.0
 * @since 22/01/2026
 */
public class LoggerService {
    private static final String CONFIG_FILE = "/logging.properties";
    private static boolean isConfigured = false;

    /**
     * Bloc statique d'initialisation.
     * Charge automatiquement la configuration au premier accès à la classe.
     */
    static {
        configure();
    }

    /**
     * Configure le système de logging depuis logging.properties.
     * <p>
     * Cette méthode est thread-safe et ne s'exécute qu'une seule fois.
     * </p>
     */
    private static synchronized void configure() {
        if (isConfigured) {
            return;
        }

        try {
            // Charger configuration depuis classpath
            InputStream configStream = LoggerService.class.getResourceAsStream(CONFIG_FILE);

            if (configStream == null) {
                System.err.println("ATTENTION : Fichier " + CONFIG_FILE + " introuvable dans le classpath");
                System.err.println("Configuration logs par défaut utilisée");
                isConfigured = true;
                return;
            }

            // Appliquer la configuration
            LogManager.getLogManager().readConfiguration(configStream);
            configStream.close();

            // Créer le dossier logs s'il n'existe pas
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));

            isConfigured = true;

            // Log confirmation (seulement si niveau INFO activé)
            Logger rootLogger = Logger.getLogger("");
            rootLogger.log(Level.CONFIG, "Système de logging configuré depuis " + CONFIG_FILE);

        } catch (IOException e) {
            System.err.println("ERREUR : Impossible de charger la configuration logs");
            e.printStackTrace();
            isConfigured = true; // Pour éviter de réessayer en boucle
        }
    }

    /**
     * Obtient un logger configuré pour une classe donnée.
     * <p>
     * Le logger hérite de la configuration définie dans logging.properties.
     * </p>
     *
     * @param clazz la classe pour laquelle obtenir un logger
     * @return le logger configuré
     */
    public static Logger getLogger(Class<?> clazz) {
        // configure() est déjà appelé dans le bloc static
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Formatter personnalisé pour les logs.
     * <p>
     * Format : {@code yyyy-MM-dd HH:mm:ss.SSS LEVEL [ClassName.methodName] - Message}
     * </p>
     */
    public static class CustomFormatter extends Formatter {

        private static final String LINE_SEPARATOR = System.lineSeparator();

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            // Date et heure avec millisecondes
            sb.append(java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            sb.append(" ");

            // Niveau (aligné sur 7 caractères)
            sb.append(String.format("%-7s", record.getLevel().getName()));
            sb.append(" ");

            // Classe et méthode (nom court)
            if (record.getSourceClassName() != null) {
                String className = record.getSourceClassName();
                // Ne garder que le nom de la classe (pas le package complet)
                int lastDot = className.lastIndexOf('.');
                if (lastDot > 0) {
                    className = className.substring(lastDot + 1);
                }
                sb.append("[").append(className);

                if (record.getSourceMethodName() != null) {
                    sb.append(".").append(record.getSourceMethodName());
                }
                sb.append("]");
            }
            sb.append(" - ");

            // Message
            sb.append(formatMessage(record));
            sb.append(LINE_SEPARATOR);

            // Exception si présente (stack trace complète)
            if (record.getThrown() != null) {
                try {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    // Ignorer les erreurs de formatage exception
                }
            }

            return sb.toString();
        }
    }
}
