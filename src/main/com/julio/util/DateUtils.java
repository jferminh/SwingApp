package main.com.julio.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Classe utilitaire pour la gestion et le formatage des dates dans l'application.
 * <p>
 * Cette classe fournit un formateur de dates standardisé et des méthodes de parsing
 * pour garantir la cohérence du format des dates à travers toute l'application.
 * Le format utilisé est le format français standard "dd/MM/yyyy" avec validation stricte.
 * </p>
 *
 * <p><b>Caractéristiques du formateur :</b></p>
 * <ul>
 *   <li><b>Format</b> : "dd/MM/uuuu" (jour/mois/année)</li>
 *   <li><b>Validation stricte</b> : {@link ResolverStyle#STRICT} pour éviter les dates invalides
 *       comme le 31/02/2025 ou le 30/02/2024</li>
 *   <li><b>Pattern 'uuuu'</b> : utilisation de 'uuuu' au lieu de 'yyyy' pour une meilleure
 *       gestion des années avec le mode strict</li>
 * </ul>
 *
 * @author Julio FERMIN
 * @version 1.0
 * @since 19/11/2025
 * @see LocalDate
 * @see DateTimeFormatter
 * @see ResolverStyle
 */
public class DateUtils {

    /**
     * Formateur de dates standardisé pour toute l'application.
     * <p>
     * Ce formateur utilise le format français "dd/MM/uuuu" (jour/mois/année sur 4 chiffres)
     * avec une validation stricte ({@link ResolverStyle#STRICT}) qui rejette les dates
     * invalides comme le 31 février ou le 30 février.
     * </p>
     *
     * <p><b>Différence 'uuuu' vs 'yyyy' :</b></p>
     * <ul>
     *   <li>'uuuu' : année du calendrier (proleptic year), recommandé avec STRICT</li>
     *   <li>'yyyy' : année de l'ère (year-of-era), peut causer des problèmes avec STRICT</li>
     * </ul>
     *
     * @see ResolverStyle#STRICT
     */
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Parse une chaîne de caractères en objet {@link LocalDate}.
     * <p>
     * Cette méthode utilise le formateur {@link #FORMATTER} pour convertir
     * une date textuelle au format "dd/MM/yyyy" en objet LocalDate.
     * La validation stricte garantit que seules les dates réellement valides
     * sont acceptées.
     * </p>
     *
     * @param dateString la chaîne représentant la date au format "dd/MM/yyyy"
     *                   (ne doit pas être null)
     * @return un objet LocalDate correspondant à la date parsée
     * @throws java.time.format.DateTimeParseException si la chaîne ne respecte pas
     *         le format attendu ou si la date est invalide (ex: 31/02/2025)
     * @throws NullPointerException si dateString est null
     * @see #FORMATTER
     */
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, FORMATTER);
    }
}
