package main.com.julio.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Utilitaire pour configurer et gérer les logs de l'application de manière centralisée.
 * Configure le format des logs, les handlers et les niveaux de log.
 *
 * @author Julio FERMIN
 * @version 2.0
 * @since 15/01/2026
 */
public class LoggerUtil {
    private static final String LOG_FILE_PATTERN = "logs/ecf_dao_%u.log";
    private static final int MAX_LOG_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final int MAX_LOG_FILES = 10;
    private static boolean isConfigured = false;

    /**
     * Configure le logger racine avec un FileHandler et un ConsoleHandler.
     */
    public static synchronized void configure() {
        if (isConfigured) {
            return;
        }

        try {
            // Créer le dossier logs s'il n'existe pas
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs"));

            // Récupérer le logger racine
            Logger rootLogger = Logger.getLogger("");

            // Supprimer les handlers par défault
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Créer un formatter personnalisé
            Formatter customFormatter = new CustomFormatter();

            // Handler pour fichier avec rotation
            FileHandler fileHandler = new FileHandler(
                    LOG_FILE_PATTERN,
                    MAX_LOG_FILE_SIZE,
                    MAX_LOG_FILES,
                    true
            );
            fileHandler.setLevel(Level.WARNING);
            fileHandler.setFormatter(customFormatter);
            rootLogger.addHandler(fileHandler);

            // Handler pour console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(customFormatter);
            rootLogger.addHandler(consoleHandler);

            // Niveau global
            rootLogger.setLevel(Level.ALL);

            isConfigured = true;
        } catch (IOException ex) {
            System.err.println("Erreur lors de la configuration des logs : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Obtient un logger configuré pour une classe donnée.
     */
    public static Logger getLogger(Class<?> clazz) {
        configure();
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Formatter personnalisé pour les logs.
     */
    private static class CustomFormatter extends Formatter {

        private static final String LINE_SEPARATOR = System.lineSeparator();

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            // Date et heure
            sb.append(java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            sb.append(" ");

            // Niveau
            sb.append(String.format("%-7s", record.getLevel().getName()));
            sb.append(" ");

            // Classe et méthode
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

            // Exception si présente
            if (record.getThrown() != null) {
                try {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    // Ignorer
                }
            }

            return sb.toString();
        }
    }
}
