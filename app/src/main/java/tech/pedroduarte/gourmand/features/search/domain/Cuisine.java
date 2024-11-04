package tech.pedroduarte.gourmand.features.search.domain;

import java.util.regex.Pattern;

public class Cuisine {

    private final String name;
    private static final Pattern VALID_CUISINE_PATTERN = Pattern.compile("^[A-Za-z\\s-]+$");

    public Cuisine(String name) {
        validateCuisine(name);
        this.name = toTitleCase(name);
    }

    private void validateCuisine(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cuisine name cannot be empty");
        }
        if (!VALID_CUISINE_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Cuisine name must contain only letters, spaces, and hyphens"
            );
        }
    }

    private String toTitleCase(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cuisine)) return false;
        Cuisine cuisine = (Cuisine) o;
        return name.equals(cuisine.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
