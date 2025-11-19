package main.com.julio.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class FormatterLog extends Formatter {
    @Override
    public String format(LogRecord record) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
