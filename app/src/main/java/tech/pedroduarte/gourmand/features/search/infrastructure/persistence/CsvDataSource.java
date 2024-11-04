package tech.pedroduarte.gourmand.features.search.infrastructure.persistence;

import tech.pedroduarte.gourmand.common.exception.DataLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvDataSource {
    private static final String RESTAURANTS_FILENAME = "restaurants.csv";
    private static final String CUISINES_FILENAME = "cuisines.csv";

    private final Path dataDirectory;
    private final boolean useResources;

    private CsvDataSource(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.useResources = (dataDirectory == null);
        validate();
    }

    public static CsvDataSource fromResources() {
        return new CsvDataSource(null);
    }

    public static CsvDataSource fromDirectory(Path directory) {
        return new CsvDataSource(directory);
    }

    public InputStream getRestaurantsStream() {
        return getInputStream(RESTAURANTS_FILENAME);
    }

    public InputStream getCuisinesStream() {
        return getInputStream(CUISINES_FILENAME);
    }

    private InputStream getInputStream(String filename) {
        if (useResources) {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("data/" + filename);
            if (is == null) {
                throw new DataLoadException("Resource not found: " + filename);
            }
            return is;
        } else {
            Path filePath = dataDirectory.resolve(filename);
            if (!Files.exists(filePath)) {
                throw new DataLoadException("File not found: " + filePath);
            }
            try {
                return Files.newInputStream(filePath);
            } catch (IOException e) {
                throw new DataLoadException("Failed to open file: " + filePath, e);
            }
        }
    }

    public void validate() {
        // Try opening both files to ensure they exist
        try (InputStream restaurants = getRestaurantsStream();
             InputStream cuisines = getCuisinesStream()) {
            // Just checking if we can open them
        } catch (IOException e) {
            throw new DataLoadException("Failed to load data files", e);
        }
    }
}
