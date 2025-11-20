package main.com.julio.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formateur personnalisé pour les logs de l'application.
 * <p>
 * Cette classe étend {@link Formatter} pour définir un format de log structuré
 * et lisible, adapté aux besoins de débogage et de traçabilité de l'application.
 * Chaque entrée de log contient la date/heure, le niveau de gravité, le message,
 * la classe source et la méthode source.
 * </p>
 *
 * <p><b>Format de sortie :</b></p>
 * <pre>
 * dd/MM/yyyy HH:mm:ss Level : [LEVEL] / Message : [MESSAGE] / Classe :[CLASSE] / Methode :[METHODE]
 * </pre>
 *
 * <p><b>Exemple concret :</b></p>
 * <pre>
 * 20/11/2025 09:15:32 Level : INFO / Message : Client créé avec succès / Classe :main.com.julio.service.ClientService / Methode :createClient
 * </pre>
 *
 * <p><b>Utilisation :</b></p>
 * <pre>
 * FileHandler fileHandler = new FileHandler("application.log");
 * fileHandler.setFormatter(new FormatterLog());
 * logger.addHandler(fileHandler);
 * </pre>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see Formatter
 * @see LogRecord
 * @see java.util.logging.Logger
 */
public class FormatterLog extends Formatter {

    /**
     * Formate un enregistrement de log selon le format personnalisé de l'application.
     * <p>
     * Construit une chaîne de caractères structurée contenant :
     * <ol>
     *   <li><b>Date/Heure</b> : horodatage au format "dd/MM/yyyy HH:mm:ss"</li>
     *   <li><b>Niveau</b> : gravité du log (INFO, WARNING, SEVERE, etc.)</li>
     *   <li><b>Message</b> : contenu descriptif du log</li>
     *   <li><b>Classe</b> : nom complet de la classe source de l'événement</li>
     *   <li><b>Méthode</b> : nom de la méthode source de l'événement</li>
     * </ol>
     * Chaque entrée se termine par un retour à la ligne pour faciliter la lecture
     * dans les fichiers de log.
     * </p>
     *
     * @param record l'enregistrement de log à formater, fourni par le système de logging Java
     * @return une chaîne de caractères formatée représentant l'entrée de log complète
     */
    @Override
    public String format(LogRecord record) {
        // Format de date français avec heure complète
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Construction du message de log structuré
        StringBuilder result = new StringBuilder();
        result.append(dateFormat.format(new Date()));
        result.append(" Level : ");
        result.append(record.getLevel());
        result.append(" / Message : ");
        result.append(record.getMessage());
        result.append(" / Classe :");
        result.append(record.getSourceClassName());
        result.append(" / Methode :");
        result.append(record.getSourceMethodName());
        result.append("\n");

        return result.toString();
    }
}
