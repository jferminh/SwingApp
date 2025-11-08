package main.com.julio.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingService {
    private static final String LOG_FILE = "logs/application.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            writer.println("[" + timestamp + "] " + message);
            } catch (IOException e) {
                System.err.println("Erreur d'écriture dans le log: " + e.getMessage());
        }
    }

    public static void logError(String message, Exception e) {
        log("ERREUR: " + message + " - " + e.getMessage());
    }
}
