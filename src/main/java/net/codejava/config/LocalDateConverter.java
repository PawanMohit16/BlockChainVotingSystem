package net.codejava.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class LocalDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    @Override
    public LocalDate convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        source = source.trim();
        
        // Try each formatter
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(source, formatter);
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        
        // If none work, throw an exception
        throw new IllegalArgumentException("Unable to parse date: " + source + 
            ". Supported formats: yyyy-MM-dd, dd/MM/yyyy, MM/dd/yyyy, yyyy/MM/dd");
    }
}
