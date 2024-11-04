package tech.pedroduarte.gourmand.common.utils;

import tech.pedroduarte.gourmand.common.exception.DataLoadException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CsvReader {

    private static final Character SEPARATOR = ',';

    public <T> List<T> readCsv(InputStream inputStream,
                               boolean skipHeader,
                               Function<String[], T> mapper) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            List<String> lines;
            try {
                lines = reader.lines().collect(Collectors.toList());
            } catch (UncheckedIOException e) {
                throw new DataLoadException("Failed to read CSV data", e.getCause());
            }

            if (lines.isEmpty()) {
                throw new DataLoadException("CSV data is empty");
            }

            try {
                return lines.stream()
                        .skip(skipHeader ? 1 : 0)
                        .map(this::parseLine)
                        .filter(Objects::nonNull)
                        .map(mapper)
                        .collect(Collectors.toList());
            } catch (UncheckedIOException e) {
                throw new DataLoadException("Failed to process CSV data", e.getCause());
            }

        } catch (IOException e) {
            throw new DataLoadException("Failed to read CSV data", e);
        }
    }


    private String[] parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == SEPARATOR && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
        }

        values.add(currentValue.toString().trim());

        return values.stream()
                .map(this::cleanValue)
                .toArray(String[]::new);
    }

    private String cleanValue(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

}