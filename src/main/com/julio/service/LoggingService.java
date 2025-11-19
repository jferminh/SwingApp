package main.com.julio.service;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class LoggingService {
    private static final String LOG_FILE = "logs/application.log";
    public static final Logger LOGGER = Logger.getLogger(LoggingService.class.getName());

    public static void intFichierLog() throws IOException {
        FileHandler fh = new FileHandler(LOG_FILE, true);
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(fh);
        fh.setFormatter(new FormatterLog());
    }

}
