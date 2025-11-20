package main.com.julio.service;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Service centralisé de gestion des logs de l'application.
 * <p>
 * Cette classe fournit un {@link Logger} unique et configuré pour toute l'application,
 * avec écriture automatique dans un fichier de log. Elle utilise le formateur personnalisé
 * {@link FormatterLog} pour structurer les entrées de log selon les besoins métier.
 * </p>
 *
 * <p><b>Caractéristiques :</b></p>
 * <ul>
 *   <li>Logger statique accessible depuis toute l'application</li>
 *   <li>Écriture dans le fichier {@code logs/application.log}</li>
 *   <li>Mode append (true) : les logs sont ajoutés sans écraser les précédents</li>
 *   <li>Format personnalisé via {@link FormatterLog}</li>
 *   <li>Désactivation des handlers parents pour éviter les doublons</li>
 * </ul>
 *
 * <p><b>Utilisation dans l'application :</b></p>
 * <pre>
 * // Initialisation au démarrage de l'application
 * LoggingService.intFichierLog();
 *
 * // Utilisation dans n'importe quelle classe
 *
 * </pre>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see FormatterLog
 * @see Logger
 * @see FileHandler
 */
public class LoggingService {

    /** Chemin du fichier de log dans le répertoire logs/ */
    private static final String LOG_FILE = "logs/application.log";

    /**
     * Logger statique global utilisé par toute l'application.
     * <p>
     * Ce logger doit être initialisé via {@link #intFichierLog()} avant
     * utilisation pour garantir l'écriture dans le fichier de log avec
     * le format personnalisé.
     * </p>
     */
    public static final Logger LOGGER = Logger.getLogger(LoggingService.class.getName());

    /**
     * Initialise le système de logging avec écriture dans un fichier.
     * <p>
     * Cette méthode doit être appelée au démarrage de l'application, généralement
     * dans la méthode {@code main()} ou dans un bloc d'initialisation. Elle configure :
     * </p>
     * <ol>
     *   <li>Un {@link FileHandler} pointant vers {@code logs/application.log}</li>
     *   <li>Le mode append (true) pour conserver l'historique des logs</li>
     *   <li>Le formateur personnalisé {@link FormatterLog}</li>
     *   <li>La désactivation des handlers parents pour éviter les logs en double</li>
     * </ol>
     *
     * <p><b>Note importante :</b></p>
     * <ul>
     *   <li>Le répertoire {@code logs/} doit exister avant l'appel à cette méthode,
     *       sinon une {@link IOException} sera levée</li>
     *   <li>Le fichier {@code application.log} sera créé automatiquement s'il n'existe pas</li>
     *   <li>Les logs existants seront préservés grâce au mode append</li>
     * </ul>
     *
     * @throws IOException si le fichier de log ne peut pas être créé ou ouvert
     *                     (permissions insuffisantes, répertoire inexistant, etc.)
     */
    public static void intFichierLog() throws IOException {
        // Configuration du handler de fichier en mode append
        FileHandler fh = new FileHandler(LOG_FILE, true);

        // Désactivation des handlers parents pour éviter les doublons dans la console
        LOGGER.setUseParentHandlers(false);

        // Association du handler au logger
        LOGGER.addHandler(fh);

        // Application du format personnalisé
        fh.setFormatter(new FormatterLog());
    }
}
