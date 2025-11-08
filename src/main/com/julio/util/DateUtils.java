package main.com.julio.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class DateUtils {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static LocalDate parseDate(String dateString){
        return LocalDate.parse(dateString, FORMATTER);
    }

    public static String formatDate(LocalDate date){
        return date.format(FORMATTER);
    }
}
